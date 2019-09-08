package com.atguigu.gulimall.pms.component;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.pms.annotation.GuliCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 切面的步骤
 * 0)、导入aop的starter
 * 1）、这是一个切面，同时放在容器中
 * 2）、申明通知方法和切入点表达
 */
@Slf4j
@Component
@Aspect
public class GuliCacheAspect {

    @Autowired
    StringRedisTemplate redisTemplate;

    ReentrantLock lock = new ReentrantLock();

    /**
     * 环绕通知
     *
     * @param point
     *
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.atguigu.gulimall.pms.annotation.GuliCache)")
    public Object around(ProceedingJoinPoint point) throws Throwable {


        Object result = null;
        try {
            Object[] args = point.getArgs();//获取目标方法的所有参数的值
            //1、拿到注解的值
            //方法名
            MethodSignature signature = (MethodSignature) point.getSignature();
            GuliCache guliCache = signature.getMethod().getAnnotation(GuliCache.class);
//            System.out.println(guliCache.prefix());
//            String name = point.getSignature().getName();
//            for (Method method : point.getThis().getClass().getMethods()) {
//                if(method.getName().equals(name)){
//                    GuliCache guliCache = AnnotatedElementUtils.findMergedAnnotation(method, GuliCache.class);
//                    System.out.println("注解的值..."+guliCache.prefix());
//                }
//            }
            if (guliCache == null) {
                //无需缓存
                return point.proceed(args);
            }

            //需要缓存机制
            String prefix = guliCache.prefix();
            if (args != null) {
                for (Object arg : args) {
                    prefix += arg.toString();
                }
            }
            //去查询这个方法之前有没有缓存到数据
            //Spring。redisTemplate在高并发就完蛋
            //jedis
//            String s = redisTemplate.opsForValue().get(prefix);
//            if (!StringUtils.isEmpty(s)) {
//                //如果拿到的数据不是null。缓存有数据。目标方法不用执行
//                Class type = signature.getReturnType();//获取返回值类型
//                result = JSON.parseObject(s, type);
//                System.out.println("缓存命中....");
//
//            } else {
//                //目标方法真正执行...
//                lock.lock();
//                System.out.println("缓存没命中....");
//                result = point.proceed(args);
//                redisTemplate.opsForValue().set(prefix, JSON.toJSONString(result));
//            }
            Object cache = getFromCache(prefix, signature);
            if (cache != null) {
                return cache;
            } else {
                lock.lock();
                log.info("缓存切面介入工作....返回通知");
                //双检查
                cache = getFromCache(prefix, signature);
                if (cache == null) {
                    System.out.println("缓存没命中....");
                    result = point.proceed(args);

                    long timeout = guliCache.timeout();
                    TimeUnit timeunit = guliCache.timeunit();
                    redisTemplate.opsForValue().set(prefix, JSON.toJSONString(result),timeout,timeunit);
                    return result;
                }else {
                    return cache;
                }
            }
        } catch (Exception e) {

        } finally {
            log.info("缓存切面介入工作....后置通知");
            if(lock.isLocked()){
                lock.unlock();
            }

        }
        return null;
    }

    private Object getFromCache(String prefix, Signature signature) {
        String s = redisTemplate.opsForValue().get(prefix);
        if (!StringUtils.isEmpty(s)) {
            //如果拿到的数据不是null。缓存有数据。目标方法不用执行
            Class type = ((MethodSignature) signature).getReturnType();//获取返回值类型
            return JSON.parseObject(s, type);
        }
        return null;
    }

    private void clearCurrentCache(String prefix){
        redisTemplate.delete(prefix);
    }

}
