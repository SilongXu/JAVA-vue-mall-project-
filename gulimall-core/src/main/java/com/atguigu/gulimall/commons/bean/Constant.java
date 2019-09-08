package com.atguigu.gulimall.commons.bean;

public class Constant {

    public static  final String ES_SPU_INDEX = "gulimall";
    public static  final String ES_SPU_TYPE = "spu";


    public static  final String CACHE_CATELOG = "cache:catelog:";
    public static  final String CACHE_SKU_INFO = "cache:skuinfo:";

    public static  final String ORDER_QUICK_COUNT_DOWN = "order:quick:";

    public static  final String LOGIN_USER_PREFIX = "login:user:";

    public static final Long LOGIN_USER_TIMEOUT_MINUTES = 30L;//30分钟

    public static  final String CART_PREFIX = "cart:user:";

    public static  final Long UNLOGIN_CART_TIMEOUT = 60*24*30L;//未登录状态购物车数据只有一个月的时长


    public static  final String TOKENS = "guli:tokens:";


    public static  final Long TOKENS_TIMEOUT = 30L; //以分钟为单位

    public static final String STOCK_LOCKED = "stock:lock:";//+商品id，标识某个商品的库存锁；

    public static final String ORDER_STOCK_LOCKED = "order:stock:lock:";






}
