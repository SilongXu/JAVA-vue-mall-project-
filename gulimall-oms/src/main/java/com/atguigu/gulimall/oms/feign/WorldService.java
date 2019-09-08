package com.atguigu.gulimall.oms.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;



@FeignClient(name = "gulimall-pms")
public interface WorldService {

    @GetMapping("/world")
    public String world();
}
