package com.atguigu.lock.test.config;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MyRedisConfig {

    @Bean
    public ShardedJedisPool shardedJedisPool(){
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(20);
        List<JedisShardInfo> shards = new ArrayList<>();

        //String host, int port
        JedisShardInfo redis1 = new JedisShardInfo("192.168.128.130",6379);
        JedisShardInfo redis2 = new JedisShardInfo("192.168.128.130",6380);
        JedisShardInfo redis3 = new JedisShardInfo("192.168.128.130",6381);
        shards.add(redis1);
        shards.add(redis2);
        shards.add(redis3);


        ShardedJedisPool jedisPool = new ShardedJedisPool(poolConfig,shards);
        return jedisPool;
    }
}
