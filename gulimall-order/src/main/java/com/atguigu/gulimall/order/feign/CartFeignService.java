package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.vo.CartVo;
import com.atguigu.gulimall.order.vo.cart.ClearCartSkuVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/cart/getItemsForOrder")
    public Resp<CartVo> getCartCheckItemsAndStatics();

    @GetMapping("/cart/clearCartSku")
    public Resp<Object> clearSkuIds(@RequestBody ClearCartSkuVo skuVo);


}
