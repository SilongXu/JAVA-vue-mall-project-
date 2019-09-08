package com.atguigu.gulimall.order.controller;


import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.constant.BizCode;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderItemMqTo;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class SecKillController {


    @Autowired
    RedissonClient redisson;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @GetMapping("/miaosha/pay")
    public String payKillOrder(HttpServletRequest request,String orderSn) throws InterruptedException {

        RCountDownLatch latch = redisson.getCountDownLatch(Constant.ORDER_QUICK_COUNT_DOWN+orderSn);

        latch.await();

        //下面给我查询；
        //查询到远程订单....
        //1、限流：验证码？直接将瞬时的大量流量均分；

        return "";
    }

    /**
     * 快速的腾出服务器的资源来处理其他请求；
     * @param skuId
     * @param request
     * @return
     */
    @GetMapping("/miaosha/{skuId}")
    public Resp<Object> kill(@PathVariable("skuId") Long skuId, HttpServletRequest request){
        Long userId = getCurrentUserId(request);
        if(userId!=null){
            RSemaphore semaphore = redisson.getSemaphore("sec:kill:"+skuId);
            //0.1s
            boolean b = semaphore.tryAcquire();
            if(b){
                //创建订单；
                String orderSn = IdWorker.getTimeId();

                OrderMqTo mqTo = new OrderMqTo();
                mqTo.setOrderSn(orderSn);
                mqTo.setMemberId(userId);
                OrderItemMqTo itemMqTo = new OrderItemMqTo();
                itemMqTo.setSkuId(skuId);
                itemMqTo.setOrderSn(orderSn);
                mqTo.setOrderItems(Arrays.asList(itemMqTo));
                //准备闭锁信息；
                RCountDownLatch latch = redisson.getCountDownLatch(Constant.ORDER_QUICK_COUNT_DOWN+orderSn);
                latch.trySetCount(1);

                rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange,RabbitMQConstant.order_quick_create_routing_key,mqTo);
                Resp<Object> ok = Resp.ok(null);
                ok.setCode(BizCode.KILL_SUCCESS.getCode());
                ok.setMsg(BizCode.KILL_SUCCESS.getMsg());
                ok.setData(orderSn);
                return ok;
            }else {
                Resp<Object> fail = Resp.fail(null);
                fail.setCode(BizCode.MORE_PEOPLE.getCode());
                fail.setMsg(BizCode.MORE_PEOPLE.getMsg());
                return fail;
            }
        }

        Resp<Object> fail = Resp.fail(null);
        fail.setCode(BizCode.NEED_LOGIN.getCode());
        fail.setMsg(BizCode.NEED_LOGIN.getMsg());
        return fail;
    }


    private Long getCurrentUserId(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        Map<String, Object> body = GuliJwtUtils.getJwtBody(authorization);
        long id = Long.parseLong(body.get("id").toString());
        return id;
    }
}
