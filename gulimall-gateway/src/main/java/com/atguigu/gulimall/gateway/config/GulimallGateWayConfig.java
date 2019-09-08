package com.atguigu.gulimall.gateway.config;

import com.atguigu.gulimall.gateway.filter.GuliAutheticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class GulimallGateWayConfig {

    /**
     * Gateway；
     *  Reactive；  Webflux；
     *
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){

        //跨域的配置
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);//允许带cookie的跨域


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",config);

        CorsWebFilter filter = new CorsWebFilter(source);

        return filter;
    }


    /**
     * 全局过滤器或者gatewayfilterfactory都是在容器中添加的时候有序的。
     *
     * 如果我们的过滤器顺序太低，导致上一个放行不过就来不到这里。
     * @return
     */
//



}
