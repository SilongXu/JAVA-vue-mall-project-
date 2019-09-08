package com.atguigu.lock.test.service;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RedisService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 数量+1
     */
    public void incr() {

//        while(true){


        //1、获取锁 setnx;  占锁和设置超时应该是原子的。
        //Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "1");
        String token = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
        if (lock) {
            //设置超时


            System.out.println("获取到锁....");
            //1.1、获取redis中原来的值是多少
            String num = redisTemplate.opsForValue().get("num");
            Integer i = Integer.parseInt(num);
            i++;
            //1.2、把新加的值放进去
            redisTemplate.opsForValue().set("num", i.toString());


            //2、删除锁;我们直接删锁是很危险的。如果业务超时，锁自动过期，就会导致别人获取到锁，
            //如果再来删锁，删的是别人的锁
            //删锁必须是原子的
            //解锁脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //if redis.call('get', 'lock') == token then return redis.call('del', 'lock') else return 0 end
            //RedisScript<T> script, List<K> keys, Object... args
            Object o = redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), token);
            System.out.println("删锁...." + o.toString());

            //自动续期；超级麻烦...
            //Redisson：分布式锁&分布式集合  Map
//            break;

        } else {
            try {
//                synchronized (this){
//                    System.out.println("没获取到，等待重试");
//                }
//                synchronized (this){
//                    System.out.println("没获取到，等待重试");
//                }
//                synchronized (this){
//                    System.out.println("没获取到，等待重试");
//                }

//                Thread.sleep(1000);
////                //等待重试
////                incr();

            } catch (Exception e) {

            }
        }
//        }


//        redisTemplate.opsForValue().increment("num");

    }

    public void incr2() throws InterruptedException {
        RLock lock = redissonClient.getLock("lock"); //只要锁名字一样就是同一把锁
        //lock.lock();
        //lock.lock(10,TimeUnit.SECONDS);
//        lock.tryLock()
        boolean b = lock.tryLock(100,10,TimeUnit.SECONDS);//尝试加锁，如果超出100秒就不要了，加上锁了就10s后自动删


        if (b) {
            System.out.println("redisson锁住了...");
            String num = redisTemplate.opsForValue().get("num");
            Integer i = Integer.parseInt(num);
            i++;
            //1.2、把新加的值放进去
            redisTemplate.opsForValue().set("num", i.toString());
        }

//        lock.unlock();
        System.out.println("redisson释放锁...");

    }

    public String read() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("data");

        RLock lock = readWriteLock.readLock();
        lock.lock();
        String  hello= redisTemplate.opsForValue().get("hello");
        lock.unlock();
        return hello;
    }

    /**
     * 并发写
     * @return
     */
    public String write() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("data");

        RLock writeLock = readWriteLock.writeLock();
        writeLock.lock();

        Thread.sleep(3000);
        redisTemplate.opsForValue().set("hello",UUID.randomUUID().toString());
        writeLock.unlock();
        return "ok";
    }

    public String lockdoor() throws InterruptedException {
        //1、获取到一个闭锁
        RCountDownLatch latch = redissonClient.getCountDownLatch("0222");
        latch.await();
        return "大门已锁...溜了....";
    }

    public void go() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("0222");
        latch.countDown();
        ReentrantLock lock = new ReentrantLock();

    }
}
