package com.atguigu.gulimall.wms.service;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.commons.to.order.OrderItemVo;
import com.atguigu.gulimall.commons.to.order.OrderVo;
import com.atguigu.gulimall.wms.dao.WareSkuDao;
import com.atguigu.gulimall.wms.feign.OrderFeignService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Slf4j
@Service
public class WareRabbitListener {

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareSkuDao wareSkuDao;

    @RabbitListener(queues = "order-payed-queue")
    public void orderPayed(Message message, Channel channel, OrderMqTo to) throws IOException {
        //真正减库存
        try {
            String orderSn = to.getOrderSn();
            Resp<OrderVo> orderInfo = orderFeignService.getOrderInfo(orderSn);

            List<OrderItemVo> orderItems = orderInfo.getData().getOrderItems();

            for (OrderItemVo orderItem : orderItems) {
                Long skuId = orderItem.getSkuId();
                Integer quantity = orderItem.getSkuQuantity();
                //少传了一个字段。。。。
                log.info("商品【"+skuId+"】锁定了【"+quantity+"】。。。正在减库存");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){

            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
