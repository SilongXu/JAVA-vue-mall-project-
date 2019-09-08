package com.atguigu.gulimall.order.feign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MyFeignConfig {


    /**
     * 解决远程调用确实授权请求头问题
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){


        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                System.out.println("拦截器的线程号..."+Thread.currentThread().getId());
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes == null){

                }else {
                    HttpServletRequest request = attributes.getRequest();
                    String authorization = request.getHeader("Authorization");
                    System.out.println("拦截器获取到的内容...."+authorization);
                    template.header("Authorization",authorization);
                }

            }
        };
    }
}
