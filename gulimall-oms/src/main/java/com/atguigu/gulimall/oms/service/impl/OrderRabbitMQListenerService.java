package com.atguigu.gulimall.oms.service.impl;


import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderItemMqTo;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.oms.dao.OrderDao;
import com.atguigu.gulimall.oms.dao.OrderItemDao;
import com.atguigu.gulimall.oms.entity.OrderEntity;
import com.atguigu.gulimall.oms.enume.OrderStatusEnume;
import com.atguigu.gulimall.oms.service.OrderService;
import com.rabbitmq.client.Channel;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OrderRabbitMQListenerService {


    @Autowired
    OrderService orderService;

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    RedissonClient redisson;

    @RabbitListener(queues = RabbitMQConstant.order_queue_qucik_create)
    public void killOrder(OrderMqTo orderMqTo, Channel channel, Message message) throws IOException {
        try {
            //刚才的秒杀服务快速生成了一个订单号；

            //后台按照基本信息，生成订单的全量信息保存到数据库
            Long memberId = orderMqTo.getMemberId();
            String orderSn = orderMqTo.getOrderSn();

            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setMemberId(memberId);
            orderEntity.setStatus(OrderStatusEnume.UNPAY.getCode());


            List<OrderItemMqTo> orderItems = orderMqTo.getOrderItems();
            for (OrderItemMqTo orderItem : orderItems) {
                Long skuId = orderItem.getSkuId();
                //去数据库查询商品的详细信息

            }

            //总一个价格....
            // orderEntity.setPayAmount();


            //数据库保存订单
//            orderDao.insert(orderEntity);
            //以后有人点击立即支付就可以去数据库获取到订单的数据

//            orderItemDao.insert()
            RCountDownLatch latch = redisson.getCountDownLatch(Constant.ORDER_QUICK_COUNT_DOWN+orderSn);
            latch.countDown();
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {

            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
