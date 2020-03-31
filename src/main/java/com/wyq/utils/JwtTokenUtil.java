package com.wyq.utils;


import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @program: hope-master
 * @description: JwtToken 工具
 * @author: heyede
 * @create: 2018-11-24
 **/
@Slf4j
public class JwtTokenUtil {
    /**
     * jwt 秘钥
     */
    private static String secret = "XX#$%()(#*!()!KL<><MQLMNQNQJQK sdfkjsdrow32234545fdf>?N<:{LWPW";

    public JwtTokenUtil() {
    }


    /**
     * 获取token中对象信息
     */
    public static<T> T getObjectFromToken(String token,Class<T> t) throws ExpiredJwtException {
      return JSONObject.parseObject(getClaimFromToken(token).getSubject(), t);
    }

    /**
     * 获取jwt发布时间
     */
    public static Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token).getIssuedAt();
    }

    /**
     * 获取jwt失效时间
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token).getExpiration();
    }

    /**
     * 获取jwt接收者
     */
    public static String getAudienceFromToken(String token) {
        return getClaimFromToken(token).getAudience();
    }

    /**
     * 获取私有的jwt claim
     */
    public static String getPrivateClaimFromToken(String token, String key) {
        return getClaimFromToken(token).get(key).toString();
    }

    /**
     * 获取jwt的payload部分
     */
    public static Claims getClaimFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 解析token是否正确,不正确会报异常<br>
     */
    public static Boolean parseToken(String token) throws JwtException {
        try {
            Map claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            if(claims == null){
                return false;
            }
            return true;
        } catch (JwtException e) {
            log.error("JwtTokenUtil parseToken --->",e);
            return false;
        }
    }

    /**
     * <pre>
     *  验证token是否失效
     *  true:过期   false:没过期
     * </pre>
     */
    public static Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成token
     */
    public static String generateToken(Object object, long expireTime) {
        Map<String, Object> claims = new HashMap<String, Object>();
        return doGenerateToken(claims, object,expireTime);
    }

    /**
     * 生成token
     */
    private static String doGenerateToken(Map<String, Object> claims, Object object,long expireTime) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expireTime * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(JSONObject.toJSONString(object))
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

}
