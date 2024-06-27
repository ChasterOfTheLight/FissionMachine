package com.devil.fission.machine.example.service.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * {@link SysUserWebController } unit test.
 *
 * @author Devil
 * @date Created in 2023/9/5 13:33
 */
public class CommonTest {
    
    @Test
    public void enumTest() {
        ResponseCode[] values = ResponseCode.values();
        for (ResponseCode value : values) {
            System.out.println(value.toString());
        }
    }
    
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
    
    @Test
    public void sortTest() {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list = list.stream().filter(e -> e > 0).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Long l : list) {
            System.out.println(l);
        }
    }
    
    @Test
    public void jsonObjectMergeTest() {
        String jsonOne = "{\"id\":\"1\",\"name\":\"test\",\"age\":\"56\",\"address\":\"77778888\"}";
        String jsonTwo = "{\"id\":\"2\",\"name\":\"qwe\",\"age\":\"\",\"address\":\"\"}";
        JSONObject jsonObjectOne = JSONUtil.parseObj(jsonOne);
        JSONObject jsonObjectTwo = JSONUtil.parseObj(jsonTwo, JSONConfig.create().setIgnoreNullValue(true));
        for (String key : jsonObjectOne.keySet()) {
            if (jsonObjectOne.containsKey(key) && jsonObjectTwo.containsKey(key)) {
                if (jsonObjectTwo.get(key) == null || StringUtils.isEmpty(jsonObjectTwo.getStr(key))) {
                    jsonObjectTwo.set(key, jsonObjectOne.get(key));
                }
            }
        }
        System.out.println(JSONUtil.toJsonStr(jsonObjectOne));
        System.out.println(jsonObjectTwo.toString());
    }
    
    @Test
    public void dateBetweenTest() {
        Date now = new Date();
        Date before = DateUtil.offsetMinute(now, -30);
        System.out.println("now: " + DateUtil.formatDateTime(now));
        System.out.println("before: " + DateUtil.formatDateTime(before));
        System.out.println("delay: " + DateUtil.between(before, now, DateUnit.MINUTE, false));
    }
    
}