package com.devil.fission.machine.common;

/**
 * 常量.
 *
 * @author devil
 * @date Created in 2022/12/6 9:28
 */
public interface Constants {
    
    /**
     * 点号.
     */
    String DOT = ".";
    
    /**
     * 冒号.
     */
    String COLON = ":";
    
    /**
     * 逗号.
     */
    String COMMA = ",";
    
    /**
     * 空格.
     */
    String BLANK = " ";
    
    /**
     * 星号.
     */
    String WILDCARD = "*";
    
    /**
     * 换行.
     */
    String CRLF = "\n";
    
    /**
     * 空字符串.
     */
    String EMPTY = "";
    
    /**
     * 斜杠.
     */
    String SLASH = "/";
    
    /**
     * UTF-8 字符集.
     */
    String UTF8 = "UTF-8";
    
    /**
     * GBK 字符集.
     */
    String GBK = "GBK";
    
    /**
     * http请求.
     */
    String HTTP = "http://";
    
    /**
     * https请求.
     */
    String HTTPS = "https://";
    
    /**
     * 非法请求.
     */
    String BAD_REQUEST = "非法请求";
    
    /**
     * 服务器异常.
     */
    String SERVER_ERROR = "服务器异常";
    
    /**
     * 请求头 Authorization.
     */
    String REQUEST_HEADER_AUTHORIZATION = "Authorization";
    
    /**
     * 请求头 Authorization的承载.
     */
    String REQUEST_HEADER_AUTHORIZATION_BEARER = "Bearer ";
    
    /**
     * JWT默认过期时间12(小时) TimeUnit.Second.
     */
    Long JWT_DEFAULT_TTL = 12 * 3600L;
    
    /**
     * 验证码过期时间60(秒) TimeUnit.Second.
     */
    Long VERIFY_CODE_TTL = 60L;
    
    /**
     * 前台用户token最长过期时间30(天) TimeUnit.Second.
     */
    Long FRONT_USER_TOKEN_TTL = 30 * 24 * 3600L;
    
    /**
     * 后台用户token最长过期时间6(小时) TimeUnit.Second.
     */
    Long BACKGROUND_USER_TOKEN_TTL = 6 * 3600L;
    
    /**
     * 游客token最长过期时间30(分钟) TimeUnit.Second.
     */
    Long VISITOR_TOKEN_TTL = 1800L;
    
    /**
     * 验证码前缀.
     */
    String VERIFY_CODE_PREFIX = "verifyCode:";
    
    /**
     * 服务标记 头.
     */
    String HEADER_SERVICE_MARK = "x-service-mark";
    
    /**
     * 请求来源 头.
     */
    String HEADER_REQUEST_SOURCE = "x-request-source";
    
    /**
     * 用户id(该id可代表不同平台的用户id).
     */
    String HEADER_REQUEST_USER_ID = "x-request-user-id";
    
    /**
     * 用户名.
     */
    String HEADER_REQUEST_USER_NAME = "x-request-user-name";
    
    /**
     * 用户平台头.
     */
    String HEADER_REQUEST_USER_PLATFORM = "x-request-user-platform";
    
    /**
     * 调用链id 头.
     */
    String HEADER_SW_ID = "x-sw-id";
    
    /**
     * 请求IP 头.
     */
    String HEADER_REQUEST_IP = "x-request-ip";
    
    /**
     * 一般分页查询最大行数.
     */
    int COMMON_MAX_PAGE_NUM = 100;
    
}