package com.atguigu.gulimall.order.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class GulimallConfig {

    @Bean
    public JedisPool jedisPool(){
        JedisPool pool = new JedisPool("192.168.128.130",6379);

        return pool;
    }

    @Bean
    @Primary
    public ThreadPoolExecutor executor(){
        /**
         * int corePoolSize,
         *                               int maximumPoolSize,
         *                               long keepAliveTime,
         *                               TimeUnit unit,
         *                               BlockingQueue<Runnable> workQueue,
         */
        return new ThreadPoolExecutor(10,
                1000,0L,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(1000000));
        //顶多。 1000+1000000
    }
}
