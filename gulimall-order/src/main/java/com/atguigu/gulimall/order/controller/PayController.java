package com.atguigu.gulimall.order.controller;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.pay.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
public class PayController {

    @Autowired
    OrderService orderService;

    @RequestMapping("/pay/alipay/success")
    public String paySuccess(PayAsyncVo vo){

        orderService.paySuccess(vo);

        return "ok";
    }
}
