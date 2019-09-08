package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.gulimall.pms.entity.SkuImagesEntity;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.pms.service.ItemService;
import com.atguigu.gulimall.pms.service.SkuImagesService;
import com.atguigu.gulimall.pms.service.SkuInfoService;
import com.atguigu.gulimall.pms.service.SpuInfoDescService;
import com.atguigu.gulimall.pms.vo.SkuItemDetailVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    @Qualifier("mainThreadPool")
    ThreadPoolExecutor mainThreadPool;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;


    @Override
    public SkuItemDetailVo getDetail(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemDetailVo detailVo = new SkuItemDetailVo();
        //1、当前sku的基本信息   2s
        //将一个异步任务交由线程池运行
        //CompletableFuture.runAsync(()->{},mainThreadPool);  run：无返回值
        // CompletableFuture.supplyAsync(()->{return 1;})  ;    supply：有返回值
        CompletableFuture<SkuInfoEntity> skuInfo = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity byId = skuInfoService.getById(skuId);
            return byId;
        }, mainThreadPool);
        CompletableFuture<Void> skuInfoFz = skuInfo.thenAcceptAsync((t) -> {
            BeanUtils.copyProperties(t, detailVo);
        },mainThreadPool);


//        future.get()

        //2、sku的所有图片  1s
        CompletableFuture<List<SkuImagesEntity>> images = CompletableFuture.supplyAsync(() -> {
            List<SkuImagesEntity> imgs = imagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            return imgs;
        },mainThreadPool);

        CompletableFuture<Void> imagesFz = images.thenAcceptAsync((imges) -> {
            List<String> strs = new ArrayList<>();
            imges.forEach((item) -> {
                strs.add(item.getImgUrl());
            });
            detailVo.setPics(strs);
        },mainThreadPool);


        //3、sku的所有促销信息   2s


        //4、sku的所有销售属性组合   2s


        //5、spu的所有基本属性  1s


        //6、详情介绍  1s
        CompletableFuture<Void> spuInfoFz = skuInfo.thenAcceptAsync((skuInfoEntity) -> {
            Long spuId = skuInfoEntity.getSpuId();
            SpuInfoDescEntity byId = spuInfoDescService.getById(spuId);
            detailVo.setDesc(byId);
        }, mainThreadPool);

        //正常敲代码9秒以后
        //1、异步，线程，线程池。
        //2、缓存；
        CompletableFuture<Void> future = CompletableFuture.allOf(skuInfo, images, skuInfoFz, imagesFz, spuInfoFz);
        future.get();
        //多线程；提升吞吐量；
        return detailVo;
    }
}
