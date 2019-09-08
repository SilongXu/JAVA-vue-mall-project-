package com.atguigu.gulimall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.wms.vo.LockStockVo;
import com.atguigu.gulimall.wms.vo.SkuLock;
import com.atguigu.gulimall.wms.vo.SkuLockVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.wms.dao.WareSkuDao;
import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    RedissonClient redisson;

    @Autowired
    Executor executor;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    PlatformTransactionManager tm;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }


    @Transactional
    @Override
    public LockStockVo lockAndCheckStock(List<SkuLockVo> skuIds) throws ExecutionException, InterruptedException {
        LockStockVo stockVo = new LockStockVo();


        AtomicReference<Boolean> flag = new AtomicReference<>(true);
        List<SkuLock> skuLocks = new ArrayList<>();
        CompletableFuture<Void>[] futures = new CompletableFuture[skuIds.size()];
        //ForkJoinPool

        //1、核心；
        //1）、分布式锁； stock:locked:1   stock:locked:100    stock:locked:110
        //2）、数据库乐观锁机制；update wms_ware_sku set stock_locked= stock_locked+5,version=version+1 where sku_id=1 and version=xx

        //锁的粒度: 粒度越细，并发越高；
        //redisson.getLock("locked:stock");
        String orderToken = "";
        if (skuIds != null && skuIds.size() > 0) {
            orderToken = skuIds.get(0).getOrderToken();
            int i = 0;
            for (SkuLockVo skuId : skuIds) {
                String finalOrderToken = orderToken;
                CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("锁库存开始..."+skuId);
                        SkuLock skuLock = lockSku(skuId);
                        System.out.println("锁库存结束..."+skuId);
                        skuLock.setOrderToken(finalOrderToken);
                        skuLocks.add(skuLock);
                        if(skuLock.getSuccess() == false){
                            flag.set(false);
                        };
                    }catch (Exception e){
                    }
                },executor);
                futures[i]=async;
                i++;
            }
        }

        /**
         * 一旦库存锁不住大家都要回滚；
         * 1）、异步情况下；事务在线程级别就隔离了
         * 2）、锁库存失败不是异常；而且每一个商品都要单独看是否自己能锁住库存；手动判断回滚条件
         * 3）、一个商品锁不住库存，就提示该商品库存不足重新；
         * 4）、不能用最终一致性；必须是强一致。
         *
         * 难点：
         *   多线程事务回滚；
         *
         */



//        CompletableFuture[] objects = (CompletableFuture[]) futures.toArray();
        CompletableFuture.allOf(futures).get();
        stockVo.setLocks(skuLocks);
        stockVo.setLocked(flag.get());

        if(flag.get()){
            //都锁住了....
            //将所有锁住的库存发给消息队列；40mins过期
//            rabbitTemplate.convertAndSend("skuStockCreateExchange","dead.skuStock",skuLocks);
            //保存一下当前订单都锁了哪些库存；日志；

            Map<String,Object> map = new HashMap<>();
            map.put("createDate",System.currentTimeMillis());
            map.put("skuLocks",skuLocks);

            String jsonString = JSON.toJSONString(map);
            redisTemplate.opsForValue().set(Constant.ORDER_STOCK_LOCKED+orderToken,jsonString);

            //远程订单调用出问题，也要解锁库存；
            OrderMqTo orderMqTo = new OrderMqTo();
            orderMqTo.setOrderSn(orderToken);
            rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange,RabbitMQConstant.order_create_event_routing_key,orderMqTo);

        }

        return stockVo;
    }


    @Transactional
    @Override
    public void unlockSkuStock(List<SkuLock> skuLocks, String orderSn) {

        for (SkuLock skuLock : skuLocks) {

            Long skuId = skuLock.getSkuId();
            Long wareId = skuLock.getWareId();
            Integer locked = skuLock.getLocked();
            wareSkuDao.unLockSku(skuLock);
            log.info("商品【"+skuId+"】-->数量【"+locked+"】，已经在【"+wareId+"】仓库释放");
        }

        redisTemplate.delete(Constant.ORDER_STOCK_LOCKED + orderSn);

    }


    /**
     * 锁库存
     *
     * @param skuId
     * @return
     */
    public SkuLock lockSku(SkuLockVo skuId) throws InterruptedException {
        SkuLock skuLock = new SkuLock();

        //1、检查总库存够不够；
        /**
         *     private Long skuId;
         *     private Integer num;
         */
//        Long count = wareSkuDao.checkStock(skuId);
        //问题。非公平的
        RLock lock = redisson.getFairLock(Constant.STOCK_LOCKED + skuId.getSkuId());
        boolean b = lock.tryLock(1, 1, TimeUnit.MINUTES);
        try{
            if(b){
                List<WareSkuEntity> wareSkuEntities = wareSkuDao.getAllWareCanLocked(skuId);
                if (wareSkuEntities != null && wareSkuEntities.size() > 0) {
                    //1、拿到第一个仓库锁库存
                    WareSkuEntity skuEntity = wareSkuEntities.get(0);
                    long i = wareSkuDao.lockSku(skuId, skuEntity.getWareId());
                    if (i > 0) {
                        skuLock.setSkuId(skuId.getSkuId());
                        skuLock.setLocked(skuId.getNum());
                        skuLock.setSuccess(true);
                        skuLock.setWareId(skuEntity.getWareId());
                    }

                } else {
                    skuLock.setSkuId(skuId.getSkuId());
                    skuLock.setLocked(0);
                    skuLock.setSuccess(false);
                }
            }else {
            }
        }finally {
            if(lock.isLocked()){
                lock.unlock();
            }
        }

        return skuLock;
    }

}