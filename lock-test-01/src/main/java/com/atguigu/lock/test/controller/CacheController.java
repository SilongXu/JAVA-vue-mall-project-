package com.atguigu.lock.test.controller;

import com.atguigu.lock.test.CacheUtils;
import com.atguigu.lock.test.bean.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class CacheController {



    @GetMapping("/get")
    public User getUser(@RequestParam("username") String username){
        User cache = CacheUtils.getFromCache(username);
        if(cache == null){
            return new User("dbuser","db@qq.com");
        }

        return cache;
    }

    @GetMapping("/insert")
    public String getUser(User user){
        //1、双写模式
        CacheUtils.saveToCache(user);
        //2、给数据库保存
        System.out.println("数据库保存了..."+user);
        return "ok";
    }

    @GetMapping("/update")
    public User updateUser(String username,String email){
        User user = new User(username, email);
        //1、双写模式
        CacheUtils.saveToCache(user);
        //2、更数据库
        System.out.println("。。。。。。");
        user.setEmail("hahahaahah");
        return user;
    }

    @GetMapping("/info")
    public String userInfo(String username){
        //1、查缓存
        User cache = CacheUtils.getFromCache(username);
        if(cache==null){
            cache = new User("dbuser","db@qq.com");
        }

        //一堆计算；缓存和数据库不一致，
        //进程内缓存；Map；缓存的数据一个引用；脏数据；缓存读时复制，写时复制；
        //分布式缓存。由于是使用序列化与反序列机制，所以是安全的
        cache.setEmail("zhangsan@qqqqqqqqq.com");
        //改数据库
        return "ok";
    }
}
