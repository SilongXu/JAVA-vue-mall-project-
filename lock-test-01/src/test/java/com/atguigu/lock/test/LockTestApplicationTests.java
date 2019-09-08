package com.atguigu.lock.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LockTestApplicationTests {


    Map<String,Object> map = new HashMap<>();

    @Autowired
    ShardedJedisPool shardedJedisPool;
    @Test
    public void contextLoads() {

        ShardedJedis resource = shardedJedisPool.getResource();

        for(int i=0;i<100;i++){
            resource.set(UUID.randomUUID().toString().substring(0,5),i+"");
        }
    }


}


