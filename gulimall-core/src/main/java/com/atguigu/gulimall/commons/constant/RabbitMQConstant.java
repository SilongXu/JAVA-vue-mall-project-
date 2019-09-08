package com.atguigu.gulimall.commons.constant;

public class RabbitMQConstant {

    public static final String order_exchange = "order-exchange";


    public static final String order_create_event_routing_key = "order.create";
    public static final String order_dead_event_routing_key = "order.dead";
    public static final String order_dead_release_routing_key = "order.release";
    public static final String order_pay_success_routing_key = "order.payed";
    public static final String order_quick_create_routing_key = "order.quick.create";


    public static final Long order_timeout = 1000*60*3L;//分钟为单位

    public static final String order_queue_dead = "order-dead-queue";//过期单队列的名字
    public static final String order_queue_release = "order-release-queue";//释放订单的队列
    public static final String order_queue_payed = "order-payed-queue";//释放订单的队列
    public static final String order_queue_qucik_create = "order-quick-create-queue";

    public static final String stock_queue_sub = "stock-sub-queue";//库存扣减队列

}
