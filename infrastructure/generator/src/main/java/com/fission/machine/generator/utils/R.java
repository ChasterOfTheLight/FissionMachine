package com.fission.machine.generator.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据.
 *
 * @author devil
 * @date Created in 2022/4/27 10:16
 */
public class R extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    public R() {
        put("code", 0);
    }
    
    /**
     * 返回错误.
     */
    public static R error() {
        return error(500, "未知异常，请联系管理员");
    }
    
    /**
     * 返回错误（自定义消息）.
     */
    public static R error(String msg) {
        return error(500, msg);
    }
    
    /**
     * 返回错误（自定义消息和code）.
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }
    
    /**
     * 返回正确（自定义消息）.
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }
    
    /**
     * 返回正确（自定义map）.
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }
    
    /**
     * 返回正确.
     */
    public static R ok() {
        return new R();
    }
    
    /**
     * 设置map.
     */
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
