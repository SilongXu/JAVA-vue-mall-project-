package com.atguigu.gulimall.ums.feign;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.to.order.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-oms")
public interface OrderFeignService {


    @GetMapping("/oms/order/bysn/{orderSn}")
    public Resp<OrderVo> getOrderInfo(@PathVariable("orderSn") String orderSn);

}
