package com.atguigu.gulimall.order.service.impl;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.order.enume.OrderStatusEnume;
import com.atguigu.gulimall.order.feign.OrderCreateFeignService;
import com.atguigu.gulimall.order.vo.order.OrderCloseVo;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class OrderRabbitListenerService {


    @Autowired
    OrderCreateFeignService orderCreateFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * RabbitMQ；
     * unack状态的消息，无论消费者掉线还是RabbitMQ停机，都会下次启动的时候重新变为ready状态发给其他消费者
     *
     * @param message
     * @param channel
     * @param orderMqTo
     *
     * 1）、nginx
     * 2）、网关
     * 3）、验证码
     * 4）、默认RabbitMQ会将所有内容全部发过去；
     *
     * 试想一下，如果，我们单个消费者1分钟最多处理60条消息，
     * 但是，生产者1分钟可能会发送300条消息，
     * 如果，我们一台消费者客户端，1分钟同时要接收到300条消息，已经超过我们最大的负载，
     * 这时，就可能导致，服务器资源被耗尽，消费者客户端卡死等情况。
     *
     *
     * RabbitMQ提供了一种qos（服务质量保证）功能，
     * 即在非自动确认消息的前提下，
     * 如果一定数目的消息（通过基于consume或者channel设置Qos的值）未被确认前，
     * 不进行消费新的消息。
     *
     *
     *
     * 通过 BasicQos 方法设置prefetchCount = 3。
     * 这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理3个Message。
     * 换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
     * @throws IOException
     */
    @RabbitListener(queues = RabbitMQConstant.order_queue_dead)
    public void closeOrder(Message message, Channel channel, OrderMqTo orderMqTo) throws IOException {
        //最终一致性
        try {
            Long id = orderMqTo.getId();
            if(id != null){
                log.info("订单【"+id+"】正常过期，正在准备关闭....");
                Resp<OrderEntityVo> info = orderCreateFeignService.info(id);
                OrderEntityVo data = info.getData();
                if (data.getStatus() == OrderStatusEnume.UNPAY.getCode()) {
                    //关闭订单；
                    log.info("订单【"+id+"】由于未支付原因，过期，已关闭....");
                    OrderCloseVo closeVo = new OrderCloseVo();
                    closeVo.setId(data.getId());
                    closeVo.setStatus(OrderStatusEnume.CLOSED.getCode());
                    orderCreateFeignService.closeOrder(closeVo);

                    //继续发送消息；订单关闭成功
                    rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange, RabbitMQConstant.order_dead_release_routing_key, orderMqTo);
                }
            }else {

                log.info("库存系统模拟了订单的创建。只发了一个订单号，只需要告诉库存系统去解锁库存");
                //继续发送消息；订单关闭成功
                rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange, RabbitMQConstant.order_dead_release_routing_key, orderMqTo);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("关闭订单操作失败..."+e.getMessage());
            //如果操作失败消息重新入队，消费者收到继续进行操作
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


    /**
     * 1、流量控制，开启RabbitMQ的流控；
     *
     * @param message
     * @param channel
     * @param orderMqTo
     * @throws IOException
     */
    @RabbitListener(queues = "order-quick-create-queue")
    public void testKasi(Message message, Channel channel, OrderMqTo orderMqTo) throws IOException {

        /**
         * 1次一个来
         * 1、当前消息执行成功就会消费下一个消息；
         *
         * 1、消息抵达
         * 每次来10条，10条处理完了再来10条
         */
        channel.basicQos(10,true);
        try {
            log.info("消息..."+orderMqTo);
            Thread.sleep(1000);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }



    }


}
