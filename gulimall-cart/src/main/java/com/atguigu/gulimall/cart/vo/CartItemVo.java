package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.annotation.WebServlet;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 购物项数据
 */
public class CartItemVo {



    @Setter  @Getter
    private Long skuId;//商品的id
    @Setter  @Getter
    private String skuTitle;//商品的标题
    @Setter  @Getter
    private String setmeal;//套餐

    @Setter  @Getter
    private String pics;//商品图片

    @Setter  @Getter
    private BigDecimal price;//单价
    @Setter  @Getter
    private Integer num;//数量

    private BigDecimal totalPrice;//商品总价

    @Setter  @Getter
    private boolean check = true;

    @Setter  @Getter
    private List<SkuFullReductionVo> reductions;//商品满减信息，包含打折满减

    @Setter  @Getter
    private List<SkuCouponVo> coupons;//优惠券

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(num+""));
    }

    @Setter  @Getter
    private BigDecimal firstPrice;//老价格（第一次加入购物车的价格）

    private BigDecimal subPrice;//差价

    //计算差价
    public BigDecimal getSubPrice() {
        BigDecimal decimal = firstPrice.subtract(price);
        double aDouble = Double.parseDouble(decimal.toString());

        Double abs = Math.abs(aDouble);

        BigDecimal bigDecimal = new BigDecimal(abs.toString());
        return bigDecimal;
    }

    @Getter @Setter
    private Date updateTime = new Date();
}

