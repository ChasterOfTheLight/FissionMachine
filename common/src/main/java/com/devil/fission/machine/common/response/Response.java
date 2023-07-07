package com.devil.fission.machine.common.response;

import java.io.Serializable;

/**
 * 公共响应实体.
 *
 * @author Devil
 * @date Created in 2022/12/5 16:30
 */
public class Response<T> implements Serializable {
    
    private static final long serialVersionUID = 7951051178640546261L;
    
    private int code;
    
    private String msg;
    
    private T data;
    
    public Response() {
    }
    
    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    /**
     * 返回成功消息.
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> Response<T> success(String msg, T data) {
        return new Response<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }
    
    /**
     * 返回成功消息.
     *
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> Response<T> success(T data) {
        return success(ResponseCode.SUCCESS.getMsg(), data);
    }
    
    /**
     * 返回成功消息.
     *
     * @return 成功消息
     */
    public static <T> Response<T> success() {
        return success(null);
    }
    
    /**
     * 返回错误消息.
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static <T> Response<T> error(String msg, T data) {
        return new Response<T>(ResponseCode.FAIL.getCode(), msg, data);
    }
    
    /**
     * 返回错误消息.
     *
     * @param data 数据对象
     * @return 错误消息
     */
    public static <T> Response<T> error(T data) {
        return error(ResponseCode.FAIL.getMsg(), data);
    }
    
    /**
     * 返回其他消息.
     *
     * @param code 响应码
     * @param msg  返回内容
     * @param data 数据对象
     * @return 其他消息
     */
    public static <T> Response<T> other(int code, String msg, T data) {
        return new Response<T>(code, msg, data);
    }

    /**
     * 返回其他消息.
     *
     * @param code 响应码
     * @param msg  返回内容
     * @return 其他消息
     */
    public static <T> Response<T> other(int code, String msg) {
        return new Response<T>(code, msg, null);
    }
    
    
    /**
     * 返回其他消息.
     *
     * @param responseCode 响应码
     * @param msg          返回内容
     * @param data         数据对象
     * @return 其他消息
     */
    public static <T> Response<T> other(ResponseCode responseCode, String msg, T data) {
        return new Response<T>(responseCode.getCode(), msg, data);
    }
    
    /**
     * 返回其他消息.
     *
     * @param responseCode 响应码
     * @param data         数据对象
     * @return 其他消息
     */
    public static <T> Response<T> other(ResponseCode responseCode, T data) {
        return other(responseCode, responseCode.getMsg(), data);
    }
    
    /**
     * 返回其他消息.
     *
     * @param responseCode 响应码
     * @return 其他消息
     */
    public static <T> Response<T> other(ResponseCode responseCode) {
        return other(responseCode, responseCode.getMsg(), null);
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
     * get the msg .
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * the msg to set.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    /**
     * get the data .
     */
    public T getData() {
        return data;
    }
    
    /**
     * the data to set.
     */
    public void setData(T data) {
        this.data = data;
    }
}
