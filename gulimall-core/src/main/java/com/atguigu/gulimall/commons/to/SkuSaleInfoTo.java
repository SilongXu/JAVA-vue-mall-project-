package com.atguigu.gulimall.commons.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuSaleInfoTo {

    //skuid
    private Long skuId;
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private Integer[] work;
    //上面是  积分设置的信息
    // 0000


    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;
    //上面是  阶梯价格的信息

    /**
     * "fullPrice": 0, //满多少
     * "reducePrice": 0, //减多少
     * "fullAddOther": 0, //满减是否可以叠加其他优惠
     */
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
}
