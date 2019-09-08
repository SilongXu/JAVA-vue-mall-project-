package com.atguigu.gulimall.pms.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GuliCache {

    String prefix() default "cache";

    long timeout() default 60L*30;


    TimeUnit timeunit() default TimeUnit.SECONDS;

}
