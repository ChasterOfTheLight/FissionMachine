package com.devil.fission.machine.auth.api;

/**
 * 用户验证常量.
 *
 * @author devil
 * @date Created in 2022/12/6 9:36
 */
public class AuthConstants {
    
    /**
     * token key字段.
     */
    public static final String AUTH_TOKEN_KEY = "authTokenKey";
    
    /**
     * token创建时间.
     */
    public static final String AUTH_TOKEN_CREATE_TIME = "authTokenCreateTime";
    
    /**
     * token过期秒数.
     */
    public static final String AUTH_TOKEN_EXPIRE_SECONDS = "authTokenExpireSeconds";
    
    /**
     * 用户ID字段.
     */
    public static final String AUTH_USER_ID = "authUserId";
    
    /**
     * 用户名字段.
     */
    public static final String AUTH_USER_NAME = "authUserName";
    
    /**
     * 用户平台.
     */
    public static final String AUTH_USER_PLATFORM = "authUserPlatform";
    
    /**
     * 服务名称.
     */
    public static final String SERVICE_REGISTER_NAME = "fission-machine-auth-service";
    
    /**
     * sign认证 accessKey 访问key.
     */
    public static final String AUTH_ACCESS_KEY = "x-auth-access-key";
    
    /**
     * sign认证 timestamp 时间戳.
     */
    public static final String AUTH_TIMESTAMP = "x-auth-timestamp";
    
    /**
     * sign认证 nonce 随机数.
     */
    public static final String AUTH_NONCE = "x-auth-nonce";
    
    /**
     * sign认证 sign.
     */
    public static final String AUTH_SIGN = "x-auth-sign";
    
}
