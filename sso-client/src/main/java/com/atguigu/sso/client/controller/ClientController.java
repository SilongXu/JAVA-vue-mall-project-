package com.atguigu.sso.client.controller;

import com.atguigu.sso.client.util.GuliJwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ClientController {

    @Value("${sso.server}")
    String ssoServer;

    @GetMapping("/say")
    public String hello(){
        return "hello";
    }


    @GetMapping("/see")
    public String meinv(@CookieValue(value = "atguigusso",required = false) String ssocookie,
                        @RequestParam(value = "atguigusso",required = false) String ssoparam,
                        HttpServletRequest request,
                        HttpServletResponse response){
        StringBuffer url = request.getRequestURL();

        try {
            if(!StringUtils.isEmpty(ssoparam)){
                //有参数，验参数
                GuliJwtUtils.checkJwt(ssoparam);
                response.addCookie(new Cookie("atguigusso",ssoparam));
                return "meinv";
            }
            if(!StringUtils.isEmpty(ssocookie)){
                GuliJwtUtils.checkJwt(ssocookie);
                return "meinv";
            }
            throw new NullPointerException();
        }catch (NullPointerException e){
            return "redirect:"+ssoServer+"?redirect_url="+url.toString();
        }catch (Exception e){
            //验证失败
            System.out.println("令牌校验非法....");
            request.setAttribute("url",ssoServer+"?redirect_url="+url.toString());
            return "error";
        }

    }



}
