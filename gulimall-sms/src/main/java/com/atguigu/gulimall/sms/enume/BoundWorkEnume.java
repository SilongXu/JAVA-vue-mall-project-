package com.atguigu.gulimall.sms.enume;

public enum  BoundWorkEnume {

    /**
*                 //优惠生效情况[1111（四个状态位，从右到左）;
*                 // 0 - 无优惠，成长积分是否赠送;
*                 // 1 - 无优惠，购物积分是否赠送;
*                 // 2 - 有优惠，成长积分是否赠送;
*                 // 3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
    //0000-1111
    // 0001
    ALLNO(0,"任何情况都无优惠"),
    ALLNOBUTGROWTHYES(1,"成长积分无论如何都送");
    //.........

    private Integer code;
    private String msg;

    BoundWorkEnume(Integer code,String msg){
        this.code =code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
