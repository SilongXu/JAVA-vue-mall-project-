package com.atguigu.sso.server;

import com.atguigu.sso.server.utils.JwtUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class SsoServerApplicationTests {

    private String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiemhhbmdzYW4iLCJlbWFpbCI6InpoYW5nc2FuQHFxLmNvbSIsInRva2VuIjoiMTIzNDU2In0.aoHsS-2hwcg5HmkfndkdI745C-9TYJM0gRrcjg0qyBE";
    @Test
    public void contextLoads() {
        Map<String,Object> loginUser = new HashMap<>();
        loginUser.put("name","zhangsan");
        loginUser.put("email","zhangsan@qq.com");
        //redisTemplate.opsForValue().set(token,loginUserINfoxxxxxx);
        Date date = new Date(System.currentTimeMillis() + 1000 * 60 * 2);

        loginUser.put("token","123456");
        String compact = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, JwtUtils.JWT_RULE)
                .setClaims(loginUser) //设置自定义的负载
                .setNotBefore(date)
                .compact();
        //
        System.out.println("生成的jwt："+compact);
    }


    /**
     * 验证和解析数据
     */
    @Test
    public void checkJwt(){
        //验jwt
        //Jwt jwt = Jwts.parser().setSigningKey(JwtUtils.JWT_RULE).parse(this.jwt);
        
        //解析出负载内容
        Jws<Claims> jws = Jwts.parser().setSigningKey(JwtUtils.JWT_RULE).parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJuYmYiOjE1NjU3Njk3NjAsIm5hbWUiOiJ6aGFuZ3NhbiIsImVtYWlsIjoiemhhbmdzYW5AcXEuY29tIiwidG9rZW4iOiIxMjM0NTYifQ.mqcwnK7lgQUz5T_FUr1YjBRX_w_tib2rkSAnZu6L_D0");

        Map<String,Object> body = jws.getBody();

        System.out.println("当时jwt里面的内容"+body);

        Jwt parse = Jwts.parser().setSigningKey(JwtUtils.JWT_RULE).parse("eyJhbGciOiJIUzI1NiJ9.eyJuYmYiOjE1NjU3Njk3NjAsIm5hbWUiOiJ6aGFuZ3NhbiIsImVtYWlsIjoiemhhbmdzYW5AcXEuY29tIiwidG9rZW4iOiIxMjM0NTYifQ.mqcwnK7lgQUz5T_FUr1YjBRX_w_tib2rkSAnZu6L_D0");


    }

}
