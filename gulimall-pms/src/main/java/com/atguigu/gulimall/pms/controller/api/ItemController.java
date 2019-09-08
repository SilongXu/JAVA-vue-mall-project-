package com.atguigu.gulimall.pms.controller.api;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.pms.service.ItemService;
import com.atguigu.gulimall.pms.vo.SkuItemDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class ItemController {


    @Autowired
    ItemService itemService;

    @GetMapping("/item/{skuId}.html")
    public Resp<SkuItemDetailVo> skuDetails(@PathVariable("skuId") Long skuId,
                                            HttpServletResponse response) throws ExecutionException, InterruptedException {
        //获取商品详情
       SkuItemDetailVo skuItemDetailVo =  itemService.getDetail(skuId);


       return Resp.ok(skuItemDetailVo);
    }

    @GetMapping("/test/cookie")
    public void hello(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("hello", "hahahhaaha");


        //cookie有默认作用路径，默认路径当前请求所在的路径
        cookie.setPath("/");  //放大路径

        //设置范围作用的时候，只能设置本域名和父域名
        //user.atguigu.com   api.atguigu.com  atguigu.com
//        cookie.setDomain("user.atguigu.com");//作用的域名，默认就是当前请求所在的域名  user.atguigu.com:7000/test/cookie
//        cookie.setDomain(".atguigu.com");

        //作用范围
        /**
         * 1、user.atguigu.com:7000/test/cookie
         *      1）、设置cookie；user.atguigu.com； .atguigu.com
         *      2)、访问 看域名当时设置的作用域；
         *            user.atguigu.com：
         *                  访问user.atguigu.com下的所有都会带上
         *                  访问api.atguigu.com下的所有不会带上
         *                  访问atguigu.com下的所有不会带上
         *            .atguigu.com：
         *                  访问atguigu.com会带上
         *                  访问user.atguigu.com会带上
         *                  访问api.atguigu.com会带上
         *
         *       访问的时候，只会带上自己域名下的和父域名下的所有东西；
         *
         *
         *       - 兄弟域名下的cookie目前这种不能拿到；
         *          单点登录：核心
         *              1）、news.atguigu.com：只要登录好了，就保存一个cookie:  user=1
         *              2)、cart.atguigu.com：还想登录，跑到服务，如果带上了cookie:  user=1，服务器就认为登录过了
         *
         *
         *      3）、同域下单点登录
         *
         *      三个服务器：
         *              login.atguigu.com/login?user=xxxxx（认证服务器）
         *              news.atguigu.com/kkkkdjakj（新闻客户端）
         *              cart.atguigu.com/dsajdjalj（购物车服务）
         *
         *
         *        news.atguigu.com --- 检查请求头中如果有user这个cookie，没有登录，去--->  login.atguigu.com --登录，服务命令保存cookie---->cookie(user=1)【.atguigu.com】
         *        cart.atguigu.com/xxx --- 检查请求头中如果有user这个cookie
         *        order.atguigu.com/xxxx  --- 检查请求头中如果有user这个cookie
         *
         *
         * */



        response.addCookie(cookie);


        response.getWriter().write("hello 6666");
    }


    @GetMapping("/test/haha")
    public void hello666(HttpServletResponse response) throws IOException {


        response.getWriter().write("hello 6666");
    }
}
