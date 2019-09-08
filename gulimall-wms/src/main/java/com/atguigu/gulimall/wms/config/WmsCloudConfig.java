package com.atguigu.gulimall.wms.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableFeignClients("com.atguigu.gulimall.wms.feign")
@EnableDiscoveryClient
public class WmsCloudConfig {

    @Bean
    public Executor executor(){
        ExecutorService service = Executors.newFixedThreadPool(10);
        return service;
    }
}
