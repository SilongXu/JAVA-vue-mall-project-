package com.atguigu.gulimall.pms.vo;


import com.atguigu.gulimall.pms.entity.SpuInfoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spu全量信息【spu基本信息，spu的基本属性，所有sku的基本信息，所有sku的促销信息】
 */

@Data
public class SpuAllSaveVo  extends SpuInfoEntity {

    //spu的详情图
    private String[] spuImages;

    //当前spu的所有基本属性名值对
    private List<BaseAttrVo> baseAttrs;

    //当前spu对应的所有sku信息
    private List<SkuVo> skus;

}




