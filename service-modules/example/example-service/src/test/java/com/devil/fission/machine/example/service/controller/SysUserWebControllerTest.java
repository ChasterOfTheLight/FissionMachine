package com.devil.fission.machine.example.service.controller;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * {@link SysUserWebController } unit test.
 *
 * @author Devil 
 * @date Created in 2023/9/5 13:33
 */
public class SysUserWebControllerTest {
    
    @Test
    public void caffeineTest() {
        // 初始化本地缓存
        Caffeine<String, String> caffeine = Caffeine.newBuilder().scheduler(Scheduler.systemScheduler()).expireAfter(new Expiry<String, String>() {
            @Override
            public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
                return TimeUnit.SECONDS.toMillis(2);
            }
            
            @Override
            public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                return TimeUnit.SECONDS.toMillis(2);
            }
            
            @Override
            public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
                return TimeUnit.SECONDS.toMillis(2);
            }
        });
        // 最大存储单元，超过会调用驱逐策略
        caffeine.maximumSize(1000);
        LoadingCache<String, String> cache = caffeine.build(new CacheLoader<String, String>() {
            @NotNull
            @Override
            public String load(@NonNull String key) throws Exception {
                return "999";
            }
        });
        
        String s = cache.get("1");
        System.out.println(s);
        Assert.assertEquals("999", s);
    }
  
}