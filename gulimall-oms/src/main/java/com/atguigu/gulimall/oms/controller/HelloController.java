package com.atguigu.gulimall.oms.controller;

import com.atguigu.gulimall.oms.feign.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope   //可以从配置中心动态的获取配置
@RestController
public class HelloController {

    @Autowired
    WorldService worldService;

    @Value("${my.content}") //从配置文件中获取名为my.content的值
    private String content = "";

    @Value("${url:默认}")
    private String redisUrl;
    @Value("${datasource:默认}")
    private String dbUrl;

    /**
     * feign声明式调用
     * @return
     */
    @GetMapping("/hello")
    public String hello(){
        String msg = "";
        msg = worldService.world();
        return "hello "+ msg + "" +content+"==>redis:"+redisUrl+"==>db："+dbUrl;
    }
}
