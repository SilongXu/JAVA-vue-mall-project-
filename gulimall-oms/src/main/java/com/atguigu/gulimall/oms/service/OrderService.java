package com.atguigu.gulimall.oms.service;

import com.atguigu.gulimall.commons.to.order.OrderVo;
import com.atguigu.gulimall.oms.vo.CartVo;
import com.atguigu.gulimall.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.oms.entity.OrderEntity;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.QueryCondition;


/**
 * 订单
 *
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 20:33:14
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity createAndSaveOrder(CartVo vo, Long userId);


    OrderEntity createAndSaveOrder(OrderSubmitVo vo);

    OrderVo getOrderInfoByOrderSn(String orderSn);


    void payedOrder(OrderEntity order);

}

