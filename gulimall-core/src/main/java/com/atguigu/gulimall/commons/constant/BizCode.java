package com.atguigu.gulimall.commons.constant;


import lombok.Data;


public enum  BizCode {



    TOKEN_INVAILIED(40003,"令牌失效"),


    ORDER_NEED_REFRESH(41000,"订单数据有修改，请重新提交再试"),

    STOCK_NOT_ENOUGH(50001,"库存不足"),
    NEED_LOGIN(20000,"用户请先登录"),
    KILL_SUCCESS(20001,"秒杀成功，请及时付款"),
    MORE_PEOPLE(30002,"当前用户太多，请稍后再试"),

    SERVICE_UNAVAILABLE(10000,"远程服务故障");

    Integer code;String msg;
    BizCode(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
