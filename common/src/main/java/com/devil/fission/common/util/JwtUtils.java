package com.devil.fission.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.devil.fission.common.security.AuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * Jwt工具类.
 *
 * @author devil
 * @date Created in 2022/12/6 9:35
 */
public class JwtUtils {
    
    public static String secret = "DevilFissionMachine";
    
    /**
     * 根据数据生成令牌.
     *
     * @param claims    数据声明
     * @param ttlMillis 过期时间 毫秒
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, long ttlMillis) {
        JwtBuilder builder = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret);
        long nowMillis = System.currentTimeMillis();
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
    
    /**
     * 校验令牌并获取数据.
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    
    /**
     * 根据令牌获取token key(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getTokenKey(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_TOKEN_KEY).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据身份信息获取Token Key.
     *
     * @param claims 身份信息
     */
    public static String getTokenKey(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_TOKEN_KEY);
    }
    
    /**
     * 根据令牌获取用户ID(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_USER_ID).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据身份信息获取用户ID.
     *
     * @param claims 身份信息
     */
    public static String getUserId(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_USER_ID);
    }
    
    /**
     * 根据令牌获取用户名(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getUserName(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_USER_NAME).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据身份信息获取用户名.
     *
     * @param claims 身份信息
     */
    public static String getUserName(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_USER_NAME);
    }
    
    /**
     * 根据令牌获取平台信息(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getPlatform(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_USER_PLATFORM).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据身份信息获取平台信息.
     *
     * @param claims 身份信息
     */
    public static String getPlatform(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_USER_PLATFORM);
    }
    
    /**
     * 根据令牌获取租户id(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getTenantId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_USER_TENANT_ID).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据令牌获取租户id.
     *
     * @param claims 身份信息
     */
    public static String getTenantId(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_USER_TENANT_ID);
    }
    
    /**
     * 根据令牌获取过期秒数(通过base64获取).
     *
     * @param token 令牌
     */
    public static String getExpireSeconds(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(AuthConstants.AUTH_TOKEN_EXPIRE_SECONDS).asString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据身份信息获取过期秒数.
     *
     * @param claims 身份信息
     */
    public static String getExpireSeconds(Claims claims) {
        return getValue(claims, AuthConstants.AUTH_TOKEN_EXPIRE_SECONDS);
    }
    
    /**
     * 根据身份信息获取键值.
     *
     * @param claims 身份信息
     * @param key    键
     * @return 值
     */
    public static String getValue(Claims claims, String key) {
        Object value = claims.get(key);
        if (null == value) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
}
