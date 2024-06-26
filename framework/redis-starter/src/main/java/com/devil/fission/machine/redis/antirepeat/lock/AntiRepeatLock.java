package com.devil.fission.machine.redis.antirepeat.lock;

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交锁.
 *
 * @author devil
 * @date Created in 2024/6/17 上午10:21
 */
public interface AntiRepeatLock {
    
    /**
     * 重命名key.
     *
     * @param key 锁的key
     * @return 锁名成
     */
    String renameKey(String key);
    
    /**
     * 尝试加锁.
     *
     * @param key        锁的key
     * @param waitTime   等待获取锁的时间
     * @param expireTime 持有锁的过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String key, long waitTime, long expireTime, TimeUnit timeUnit);
    
    /**
     * 解锁，如果解除不属于自己的锁或者已经超时的锁，会抛出异常.
     *
     * @param key 锁的key
     */
    void unLock(String key);
    
}
