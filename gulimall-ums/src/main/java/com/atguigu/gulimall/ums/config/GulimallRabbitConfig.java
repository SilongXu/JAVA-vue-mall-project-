package com.atguigu.gulimall.ums.config;

//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.Exchange;
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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





}
