package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.to.SkuInfoVo;
import com.atguigu.gulimall.pms.annotation.GuliCache;
import com.atguigu.gulimall.pms.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.pms.entity.SkuSaleAttrValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.pms.dao.SkuInfoDao;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuInfoDao skuInfoDao;

    @Autowired
    SkuSaleAttrValueDao saleAttrValueDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageVo(page);
    }

    @GuliCache(prefix = Constant.CACHE_SKU_INFO,timeout = 1L,timeunit = TimeUnit.DAYS)
    @Override
    public SkuInfoVo getSkuVo(Long skuId) {

        SkuInfoEntity entity = skuInfoDao.selectById(skuId);

        SkuInfoVo vo = new SkuInfoVo();
        vo.setSkuId(skuId);
        vo.setPrice(entity.getPrice());
        vo.setPics(entity.getSkuDefaultImg());
        //SELECT * FROM `pms_sku_sale_attr_value` WHERE sku_id=73
        List<SkuSaleAttrValueEntity> sku_id = saleAttrValueDao.selectList(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", skuId));
        String meal = "";
        for (SkuSaleAttrValueEntity sku : sku_id) {
            meal += "-"+sku.getAttrValue();

        }
        vo.setSetmeal(meal);//套餐
        vo.setSkuTitle(entity.getSkuTitle());
        return vo;
    }

}