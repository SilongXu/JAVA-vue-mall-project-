package com.atguigu.gulimall.order.config;

//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.Exchange;
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitTemplate:收发消息
 * AmqpAdmin:管理RabbitMQ的exchange，queue，binding....创建删除等
 * @EnableRabbit：开启rabbit的功能
 *
 * 1、给RabbitMQ利用javaAPi创建exchange，queue，binding....
 *      1）、使用AmqpAdmin.declareXX方法来创建(exchange，queue，binding....)
 *          amqpAdmin.declareExchange/Queue/Binding();
 *      2）、直接给容器中放exchange，queue，binding....，自动创建
 *
 *      3）、消息发送，RabbitTemplate；
 *
 *    新版的坑：我们必须当前功能与rabbitmq得建立连接，才能将容器中的组件创建过去
 * 2、如何监听消息队列里面的消息
 *      @EnableRabbit
 *      @RabbitListener(queues = "myqueue")
 *
 */
@EnableRabbit
@Configuration
public class GulimallRabbitConfig {

    /**
     * 容器中放入自定义的messageConverter消息发送与接收就会用它进行转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * SpringBoot会自动给RabbitMQ中创建这个交换机/队列/绑定关系
     *
     * 1）、去RabbitMQ里面看有没有当前名字的交换机/队列/绑定关系，如果没有就创建，有就不管了。
     * @return
     */
    @Bean(RabbitMQConstant.order_exchange)
    public Exchange myExchange(){
        /**
         * String name,
         * boolean durable, boolean autoDelete, Map<String, Object> arguments
         */
        TopicExchange topicExchange = new TopicExchange(RabbitMQConstant.order_exchange,true,false,null);
        return topicExchange;
    }

    //延迟队列
    @Bean("order-delay-queue")
    public Queue myqueue(){
        Map<String,Object> properties = new HashMap<>();
        properties.put("x-dead-letter-exchange",RabbitMQConstant.order_exchange); //信死了以后发给那个交换机，而不是丢弃
        properties.put("x-dead-letter-routing-key",RabbitMQConstant.order_dead_event_routing_key);
        properties.put("x-message-ttl",RabbitMQConstant.order_timeout);//ms为单位
        return new Queue("order-delay-queue",true,false,false,properties);
    }


    /**
     * 延迟队列和交换机绑定了
     * @return
     */
    @Bean(RabbitMQConstant.order_exchange+"_order-delay-queue_"+RabbitMQConstant.order_create_event_routing_key)
    public Binding orderCreateBinding(){

        return new Binding("order-delay-queue",
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_create_event_routing_key,null);
    }

//    @Bean(RabbitMQConstant.order_exchange+"_order-delay-queue_"+RabbitMQConstant.order_dead_event_routing_key)
//    public Binding orderDeadBinding(){
//
//        return new Binding("order-delay-queue",
//                Binding.DestinationType.QUEUE,
//                RabbitMQConstant.order_exchange,RabbitMQConstant.order_dead_event_routing_key,null);
//    }


    /**
     * 死单存放的队列；
     * 订单服务监听死单队列，如果订单到期还没有支付，取消订单（关单）；
     * @return
     */
    @Bean(RabbitMQConstant.order_queue_dead)
    public Queue deadQueue(){
        return new Queue(RabbitMQConstant.order_queue_dead,true,false,false,null);
    }

    @Bean(RabbitMQConstant.order_exchange+"_"+RabbitMQConstant.order_queue_dead+"_"+RabbitMQConstant.order_dead_event_routing_key)
    public Binding orderDeadBinding(){
        return new Binding(RabbitMQConstant.order_queue_dead,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_dead_event_routing_key,null);
    }



    @Bean(RabbitMQConstant.order_queue_release)
    public Queue closeOrderQueue(){
        return new Queue(RabbitMQConstant.order_queue_release,true,false,false,null);

    }

    @Bean(RabbitMQConstant.order_exchange+"_"+RabbitMQConstant.order_queue_release+"_"+RabbitMQConstant.order_dead_release_routing_key)
    public Binding orderReleasBinding(){
        return new Binding(RabbitMQConstant.order_queue_release,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_dead_release_routing_key,null);
    }

    //准备一个存放所有订单支付成功信息的队列
    @Bean(RabbitMQConstant.order_queue_payed)
    public Queue orderPayedSuccess(){
        return new Queue(RabbitMQConstant.order_queue_payed,true,false,false,null);
    }


    @Bean(RabbitMQConstant.order_exchange+"_"+RabbitMQConstant.order_queue_payed+"_"+RabbitMQConstant.order_pay_success_routing_key)
    public Binding orderPayedSuccessBinding(){
        return new Binding(RabbitMQConstant.order_queue_payed,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_pay_success_routing_key,null);
    }


    //库存扣减的队列
    @Bean(RabbitMQConstant.stock_queue_sub)
    public Queue stockSubQueue(){
        return new Queue(RabbitMQConstant.stock_queue_sub,true,false,false,null);
    }


    @Bean(RabbitMQConstant.order_exchange+"_"+RabbitMQConstant.stock_queue_sub+"_"+RabbitMQConstant.order_pay_success_routing_key)
    public Binding stockSubQueueBinding(){
        return new Binding(RabbitMQConstant.stock_queue_sub,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_pay_success_routing_key,null);
    }


    @Bean(RabbitMQConstant.order_queue_qucik_create)
    public Queue orderCreateQuick(){
        return new Queue(RabbitMQConstant.order_queue_qucik_create,true,false,false,null);
    }


    @Bean(RabbitMQConstant.order_exchange+"_"+RabbitMQConstant.order_queue_qucik_create+"_"+RabbitMQConstant.order_quick_create_routing_key)
    public Binding orderCreateQuickBinding(){
        return new Binding(RabbitMQConstant.order_queue_qucik_create,
                Binding.DestinationType.QUEUE,
                RabbitMQConstant.order_exchange,RabbitMQConstant.order_quick_create_routing_key,null);
    }
}
