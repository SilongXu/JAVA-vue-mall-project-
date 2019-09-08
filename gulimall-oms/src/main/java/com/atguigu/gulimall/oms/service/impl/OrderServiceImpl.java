package com.atguigu.gulimall.oms.service.impl;

import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderItemMqTo;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.commons.to.order.OrderItemVo;
import com.atguigu.gulimall.commons.to.order.OrderVo;
import com.atguigu.gulimall.oms.dao.OrderItemDao;
import com.atguigu.gulimall.oms.entity.OrderItemEntity;
import com.atguigu.gulimall.oms.enume.OrderStatusEnume;
import com.atguigu.gulimall.oms.vo.CartItemVo;
import com.atguigu.gulimall.oms.vo.CartVo;
import com.atguigu.gulimall.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.oms.dao.OrderDao;
import com.atguigu.gulimall.oms.entity.OrderEntity;
import com.atguigu.gulimall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderItemDao orderItemDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public OrderEntity createAndSaveOrder(CartVo vo, Long userId) {



        return null;
    }


    @Transactional
    @Override
    public OrderEntity createAndSaveOrder(OrderSubmitVo vo) {
        OrderMqTo mqTo = new OrderMqTo();

        CartVo cartVo = vo.getCartVo();
        List<CartItemVo> items = cartVo.getItems();

        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(vo,orderEntity);
        //订单号
        orderEntity.setOrderSn(vo.getOrderToken());

        orderEntity.setTotalAmount(cartVo.getTotalPrice());
        orderEntity.setPayAmount(cartVo.getCartPrice());
        orderEntity.setPromotionAmount(cartVo.getReductionPrice());
        orderEntity.setNote(vo.getRemark());
        orderEntity.setStatus(OrderStatusEnume.UNPAY.getCode());
        orderEntity.setMemberId(vo.getUserId());

        int insert = orderDao.insert(orderEntity);
        BeanUtils.copyProperties(orderEntity,mqTo);


        List<OrderItemMqTo> itemMqTos = new ArrayList<>();
        //订单项；
        items.forEach((itemVo)->{
            OrderItemEntity itemEntity = new OrderItemEntity();

            itemEntity.setOrderId(orderEntity.getId());
            itemEntity.setOrderSn(orderEntity.getOrderSn());

            //
            itemEntity.setSkuId(itemVo.getSkuId());
            itemEntity.setSkuName(itemVo.getSkuTitle());
            itemEntity.setSkuPrice(itemVo.getPrice());
            itemEntity.setSkuQuantity(itemVo.getNum());

            //远程查询积分的情况；
            itemEntity.setGiftGrowth(1000);
            itemEntity.setGiftIntegration(1000);

            orderItemDao.insert(itemEntity);

            OrderItemMqTo mq = new OrderItemMqTo();
            BeanUtils.copyProperties(itemEntity,mq);
            itemMqTos.add(mq);

        });

        mqTo.setOrderItems(itemMqTos);


        //订单创建成功了....
        rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange,RabbitMQConstant.order_create_event_routing_key,mqTo);
        return orderEntity;
    }

    @Override
    public OrderVo getOrderInfoByOrderSn(String orderSn) {

        OrderEntity orderEntity = orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        List<OrderItemEntity> order_sn = orderItemDao.selectList(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));

        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(orderEntity,orderVo);
        List<OrderItemVo> orderItemVos = new ArrayList<>();
        order_sn.forEach((itemEntity)->{
            OrderItemVo vo  = new OrderItemVo();
            BeanUtils.copyProperties(itemEntity,vo);

            orderItemVos.add(vo);
        });
        orderVo.setOrderItems(orderItemVos);

        return orderVo;
    }

    @Override
    public void payedOrder(OrderEntity order) {


        orderDao.updateOrderStatusByOrderSn(order);
    }

}