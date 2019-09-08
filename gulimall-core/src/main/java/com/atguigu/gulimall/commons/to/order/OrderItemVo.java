package com.atguigu.gulimall.commons.to.order;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVo {


    private Long id;

    private Long orderId;

    private String orderSn;
    private Long skuId;

    private String skuName;

    private BigDecimal skuPrice;

    private Integer skuQuantity;


    private Integer giftIntegration;

    private Integer giftGrowth;
}
