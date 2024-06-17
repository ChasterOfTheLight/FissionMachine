package com.devil.fission.machine.redis.antirepeat.aop;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储防重复调用链路中的上下文数据.
 *
 * @author devil
 * @date Created in 2024/6/17 上午10:27
 */
public class AntiRepeatContext {
    
    /**
     * 获取锁结果.
     */
    private static final ThreadLocal<Map<String, Boolean>> LOCK_RESULT = new ThreadLocal<>();
    
    /**
     * 当前调用层的key.
     */
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();
    
    /**
     * 调用层计数器.
     */
    private static final ThreadLocal<Integer> COUNTER = new ThreadLocal<>();
    
    /**
     * 当前是否持有锁.
     *
     * @return 是否持有
     */
    public static boolean isGetLock() {
        String key = CURRENT_KEY.get();
        if (key == null) {
            return false;
        }
        return isGetLock(key);
    }
    
    /**
     * 指定的key是否持有锁.
     *
     * @param key 指定的key
     * @return 是否持有
     */
    static boolean isGetLock(String key) {
        Map<String, Boolean> resMap = LOCK_RESULT.get();
        if (resMap == null) {
            return false;
        }
        return resMap.getOrDefault(key, false);
    }
    
    /**
     * 设置key获取锁的结果.
     *
     * @param key 指定的key
     * @param res 锁结果
     */
    static void setResult(String key, boolean res) {
        Map<String, Boolean> resMap = LOCK_RESULT.get();
        if (resMap == null) {
            resMap = new HashMap<>(16);
            LOCK_RESULT.set(resMap);
        }
        resMap.put(key, res);
    }
    
    /**
     * 获取当前调用层的key.
     *
     * @return key
     */
    static String getCurrentKey() {
        return CURRENT_KEY.get();
    }
    
    /**
     * 设置当前调用层的key.
     *
     * @param key 指定的key
     */
    static void setCurrentKey(String key) {
        CURRENT_KEY.set(key);
    }
    
    /**
     * 自增计数器.
     *
     * @return 当前数
     */
    static Integer incrCounter() {
        Integer count = COUNTER.get();
        if (count == null) {
            count = 0;
        } else {
            count++;
        }
        COUNTER.set(count);
        return count;
    }
    
    /**
     * 清理上下文.
     */
    static void clean() {
        LOCK_RESULT.remove();
        CURRENT_KEY.remove();
        COUNTER.remove();
    }
    
}
