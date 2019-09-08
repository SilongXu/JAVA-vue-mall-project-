package com.atguigu.sso.server.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GuliJwtUtils {

    private static String key = "DSJAKLDJALJ_ATGUIGU";

    /**
     *
     * @param payload  自定义的负载内容
     * @param claims   jwt默认支持的属性
     * @return
     */
    public static String buildJwt(Map<String,Object> payload, Claims claims){

        JwtBuilder builder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, GuliJwtUtils.key)
                .setClaims(payload);//设置自定义的负载

        if(claims!=null){
            if(claims.getId()!=null){
                builder.setId(claims.getId());
            }
            if(claims.getAudience()!=null){
                builder.setAudience(claims.getAudience());
            }
            if(claims.getExpiration()!=null){
                builder.setExpiration(claims.getExpiration());
            }
            if (claims.getNotBefore()!=null){
                builder.setNotBefore(claims.getNotBefore());
            }
            //xxxxx
        }

        String compact = builder.compact();
        return compact;
    }


    public static void checkJwt(String jwt){

        Jwts.parser().setSigningKey(key).parse(jwt);


    }

    public static Map<String,Object> getJwtBody(String jwt){
        return Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
    }
}
