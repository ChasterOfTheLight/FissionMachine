package com.devil.fission.machine.redis.antirepeat.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * redission防重复提交分布式锁.
 *
 * @author devil
 * @date Created in 2024/6/17 上午10:25
 */
public class RedissonAntiRepeatLock implements AntiRepeatLock {
    
    private final String prefix = "antirepeat:key:";
    
    private final RedissonClient redissonClient;
    
    public RedissonAntiRepeatLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    @Override
    public String renameKey(String key) {
        return String.format("antirepeat:key:%s", key);
    }
    
    @Override
    public boolean tryLock(String key, long waitTime, long expireTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(prefix + key);
        try {
            return lock.tryLock(waitTime, expireTime, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    @Override
    public void unLock(String key) {
        RLock lock = redissonClient.getLock(prefix + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
