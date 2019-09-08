package com.atguigu.gulimall.ums.service;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.commons.to.order.OrderItemVo;
import com.atguigu.gulimall.commons.to.order.OrderVo;
import com.atguigu.gulimall.ums.dao.MemberDao;
import com.atguigu.gulimall.ums.entity.MemberEntity;
import com.atguigu.gulimall.ums.feign.OrderFeignService;
import com.rabbitmq.client.Channel;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MemberRabbitListenerService {


    @Autowired
    MemberDao memberDao;

    @Autowired
    OrderFeignService orderFeignService;

    @RabbitListener(queues = "order-payed-queue")
    public void orderPayed(Message message, Channel channel, OrderMqTo to) throws IOException {

        try{

            String orderSn = to.getOrderSn();
            log.info("获取到已经支付的订单，正在积分处理..."+orderSn);

            Resp<OrderVo> orderInfo = orderFeignService.getOrderInfo(orderSn);

            OrderVo data = orderInfo.getData();
            Long memberId = data.getMemberId();
            List<OrderItemVo> orderItems = data.getOrderItems();

            Integer grow = 0;
            Integer inter = 0;
            for (OrderItemVo orderItem : orderItems) {
                grow += orderItem.getGiftGrowth();
                inter += orderItem.getGiftGrowth();
            }


            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(memberId);
            memberEntity.setGrowth(grow);
            memberEntity.setIntegration(inter);
            memberDao.incrScore(memberEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            log.info("订单积分无数据...");
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }


    }
}
