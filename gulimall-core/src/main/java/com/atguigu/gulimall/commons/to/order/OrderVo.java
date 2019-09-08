package com.atguigu.gulimall.commons.to.order;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderVo {


    private Long id;
    private Long memberId;

    private String orderSn;

    private Long couponId;

    private Date createTime;

    private String memberUsername;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private BigDecimal promotionAmount;

    private BigDecimal integrationAmount;

    private BigDecimal couponAmount;

    private BigDecimal discountAmount;
    private Integer payType;

    private Integer sourceType;
     private Integer status;

    private String deliveryCompany;

    private String deliverySn;

    private Integer autoConfirmDay;

    private Integer integration;

    private Integer growth;
    private Integer billType;

    private String billHeader;

    private String billContent;

    private List<OrderItemVo> orderItems;
}
