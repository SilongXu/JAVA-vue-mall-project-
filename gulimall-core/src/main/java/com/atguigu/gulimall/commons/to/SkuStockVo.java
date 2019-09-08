package com.atguigu.gulimall.commons.to;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SkuStockVo {
    private Long skuId;

    private Integer stock;
}
