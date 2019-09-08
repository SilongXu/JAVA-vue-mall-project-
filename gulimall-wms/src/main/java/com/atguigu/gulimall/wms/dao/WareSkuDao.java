package com.atguigu.gulimall.wms.dao;

import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.vo.SkuLock;
import com.atguigu.gulimall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author leifengyang
 * @email lfy@atguigu.com
 * @date 2019-08-01 20:39:51
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long checkStock(SkuLockVo skuId);


    /**
     * 查询所有能减掉这个商品的仓库
     * @param skuId
     * @return
     */
    List<WareSkuEntity> getAllWareCanLocked(SkuLockVo skuId);

    long lockSku(@Param("sku") SkuLockVo skuId, @Param("wareId") Long id);

    void unLockSku(SkuLock skuLock);


}
