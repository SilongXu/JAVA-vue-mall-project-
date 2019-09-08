package com.atguigu.gulimall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.wms.service.WareReleaseService;
import com.atguigu.gulimall.wms.service.WareSkuService;
import com.atguigu.gulimall.wms.vo.SkuLock;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WareReleaseServiceImpl implements WareReleaseService {



    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    WareSkuService wareSkuService;


    //监听队列里面过时的锁库存信息； 40min   30min

    @RabbitListener(queues = RabbitMQConstant.order_queue_release)
    public void release(Channel channel, Message message, OrderMqTo orderMqTo) throws IOException {


        try{
            Long id = orderMqTo.getId();
            String orderSn = orderMqTo.getOrderSn();

            //从redis中拿到之前这个订单每一个库存的锁定信息；进行库存的释放
            String json = redisTemplate.opsForValue().get(Constant.ORDER_STOCK_LOCKED + orderSn);
            if(!StringUtils.isEmpty(json)){
                log.info("订单【"+orderSn+"】已被关闭，正在解锁库存....");
                List<SkuLock> skuLocks = null;
                //1、将一个json字符串转为JSONObject
                JSONObject object = JSON.parseObject(json);

                Object locks = object.get("skuLocks");
                // TypeReference<T> type fastjson复杂对象使用new TypeReference<List<SkuLock>>() { }
                skuLocks = JSON.parseObject(JSON.toJSONString(locks), new TypeReference<List<SkuLock>>() {
                });

                //
                wareSkuService.unlockSkuStock(skuLocks,orderSn);

            }else {
                log.info("订单【"+orderSn+"】正在重复扣库存，可惜不行");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

        }catch (Exception e){
            log.error("解锁库存出现问题{}。。。队列重试",e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }



    }
}
