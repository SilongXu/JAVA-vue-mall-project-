package com.atguigu.lock.test;

import com.atguigu.lock.test.bean.User;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

public class CacheUtils {

    private  static Map<String, User> map = new HashMap<>();

    public static User getFromCache(String username){

        //不安全； 读时复制；
        User user = map.get(username);
        User user1 = new User();
        BeanUtils.copyProperties(user,user1);
        return user1;
    }

    public static void saveToCache(User user){

        //写时复制
        User user1 = new User();
        BeanUtils.copyProperties(user,user1);
       map.put(user.getUsername(),user1);

    }

    public static void removeKey(String key){

        map.remove(key);
    }
}
