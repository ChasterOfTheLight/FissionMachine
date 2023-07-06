package com.devil.fission.common.response;

/**
 * 公共响应码.
 *
 * @author Devil
 * @date Created in 2022/12/13 9:44
 */
public enum ResponseCode {
    
    /**
     * 请求成功.
     */
    SUCCESS(200, "请求成功"),
    
    /**
     * 请求失败.
     */
    FAIL(500, "请求失败"),
    
    /**
     * 参数异常.
     */
    BAD_REQUEST(400, "参数异常"),
    
    /**
     * 未授权.
     */
    UN_AUTHORIZED(401, "未授权"),
    
    /**
     * 拒绝访问.
     */
    FORBIDDEN(403, "拒绝访问"),
    
    /**
     * 未找到.
     */
    NOT_FOUND(404, "未找到请求路径"),
    
    /**
     * 刷新token.
     */
    REFRESH_TOKEN(10001, "刷新token"),
    
    /**
     * 没有登录.
     */
    NOT_LOGIN(10002, "请登录");
    
    int code;
    
    String msg;
    
    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    /**
     * get the code .
     */
    public int getCode() {
        return code;
    }
    
    /**
     * the code to set.
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * get the desc .
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * the desc to set.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
