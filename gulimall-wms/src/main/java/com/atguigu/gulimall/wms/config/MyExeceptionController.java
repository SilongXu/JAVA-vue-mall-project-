package com.atguigu.gulimall.wms.config;


import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.exception.UsernameExistException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@ResponseBody
//@ControllerAdvice //集中处理异常

@RestControllerAdvice
public class MyExeceptionController {

    @ExceptionHandler({Throwable.class}) //要处理那个异常
    public Resp<Object> exceptionHandler01(Exception e){

        return Resp.fail(e.getMessage());
    }


    @ExceptionHandler({NullPointerException.class})
    public Resp<Object> exceptionHandler02(Exception e){

        return Resp.fail(e.getMessage());
    }

    @ExceptionHandler({UsernameExistException.class})
    public Resp<Object> exceptionHandler03(Exception e){

        return Resp.fail(e.getMessage());
    }

}
