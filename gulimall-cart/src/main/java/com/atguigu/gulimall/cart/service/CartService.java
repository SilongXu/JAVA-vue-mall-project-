package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartVo;
import com.atguigu.gulimall.cart.vo.ClearCartSkuVo;

import java.util.concurrent.ExecutionException;

public interface CartService {

    CartVo getCart(String userKey, String authorization) throws ExecutionException, InterruptedException;



    CartVo addToCart(Long skuId, Integer num, String userKey, String authorization) throws ExecutionException, InterruptedException;

    CartVo updateCart(Long skuId, Integer num, String userKey, String authorization);


    CartVo checkCart(Long[] skuId, Integer status, String userKey, String authorization);


    /**
     * 获取当前用户购物车里面选中的商品及其他信息等
     * @param id
     * @return
     */
    CartVo getCartForOrder(Long id);

    void clearSkuIds(ClearCartSkuVo skuVo);


}
