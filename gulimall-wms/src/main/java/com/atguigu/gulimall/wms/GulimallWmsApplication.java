package com.atguigu.gulimall.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 1、注册中心
 *      1）、导包
 *      2）、写配置
 *      3）、加注释开功能
 *      @EnableDiscoveryClient
 *
 * 2、配置中心
 *     @RefreshScope
 *
 */
//@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.atguigu.gulimall.wms.dao")
@RefreshScope
@SpringBootApplication
public class GulimallWmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWmsApplication.class, args);
    }

}
