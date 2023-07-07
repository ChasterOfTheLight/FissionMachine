package com.devil.fission.machine.common.support;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.devil.fission.machine.common.util.StringUtils;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文(key-value形式).
 *
 * @author devil
 * @date Created in 2022/12/8 16:35
 */
public class MachineContextHolder {
    
    /**
     * 使用TTL目的是为了获取父级ITL的信息.
     */
    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();
    
    private static final Gson GSON = new Gson();
    
    /**
     * 往本地线程变量map塞值.
     */
    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }
    
    /**
     * 获取线程变量map值（字符串）.
     */
    public static String getAsString(String key) {
        Map<String, Object> map = getLocalMap();
        Object value = map.getOrDefault(key, StringUtils.EMPTY);
        return value == null ? "" : value instanceof String ? (String) value : value.toString();
    }
    
    /**
     * 获取线程变量map值（对象）.
     */
    public static <T> T getAsObject(String key, Class<T> objectClass) {
        Map<String, Object> map = getLocalMap();
        Object object = map.get(key);
        if (object == null) {
            return null;
        }
        return GSON.fromJson(GSON.toJson(object), objectClass);
    }
    
    /**
     * 获取本地线程map.
     */
    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>(16);
            THREAD_LOCAL.set(map);
        }
        return map;
    }
    
    /**
     * 清空本地线程变量.
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
    
    /**
     * 是否支持请求头.
     */
    public static boolean supportHeader(String headerName) {
        return false;
    }
    
}
