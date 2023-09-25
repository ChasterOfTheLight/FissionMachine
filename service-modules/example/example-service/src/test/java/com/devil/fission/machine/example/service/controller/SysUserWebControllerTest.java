package com.devil.fission.machine.example.service.controller;

import com.devil.fission.machine.common.response.ResponseCode;
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

import java.util.concurrent.TimeUnit;

/**
 * {@link SysUserWebController } unit test.
 *
 * @author Devil 
 * @date Created in 2023/9/5 13:33
 */
public class SysUserWebControllerTest {
    
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
    public void jsonTest() {
        String jsonOriginal = "{\"id\":\"35701322798346566\",\"accountNo\":\"dengshuyuan\",\"accountName\":\"邓舒元\",\"email\":\"dengshuyuan@tojoy.com\",\"pinyin\":\"\",\"sex\":\"0\",\"mobile\":\"15756339749\",\"status\":\"0\",\"modifyTime\":\"2023-09-22 14:33:16\",\"birth\":\"1998-09-23\",\"workNo\":\"TJ944510\",\"weChatId\":\"TJ944510\",\"newWorkNo\":\"TJ944510\",\"psn_status\":\"1\",\"pk_dept\":\"385393\",\"displayOrgName\":\"天九共享控股集团有限公司_平台集团_南部大区_成渝事业部_侯泽川总经理团队(成都)_彭泉生总监团队\",\"org_name\":\"彭泉生总监团队\",\"pk_psndoc\":\"492323\",\"userTypeId\":\"101\",\"userTypeName\":\"内部员工\",\"name\":\"\",\"end_probation_date\":\"2023-09-02\",\"is_probation\":\"否\",\"jg\":\"四川省广安市岳池县罗渡镇\n"
                + "杨家桥街\",\"report_leader\":\"彭泉生\",\"report_leader_workno\":\"TJ910618\",\"period_end_date\":\"2023-09-01\",\"second_grade\":\"\",\"legal_person_cw\":\"王四立\",\"legal_person_cw_addr\":\"成都市成华区府青路二段2号1栋1单元13楼1303、1304、1305号\",\"legal_person_cw_pk\":\"0001Q810000000000K6C\",\"psnjobs\":[{\"pk_psnjob\":\"35701323158337564\",\"pk_org\":\"0001A810000000000H9W\",\"org_name\":\"平台集团\",\"pk_psncl\":\"\",\"psncl_name\":\"\",\"pk_dept\":\"385393\",\"dept_name\":\"彭泉生总监团队\",\"all_org_name\":\"天九共享控股集团有限公司_平台集团_南部大区_成渝事业部_侯泽川总经理团队(成都)_彭泉生总监团队\",\"enddate\":\"2199-12-31\",\"pk_series\":\"\",\"series_name\":\"\",\"job_name\":\"经理级\",\"job_code\":\"4\",\"pk_job\":\"185975\",\"pk_post\":\"185975\",\"post_name\":\"联席经理\",\"ismainjob\":\"Y\",\"poststat\":\"Y\",\"endflag\":\"N\",\"trial_flag\":\"N\",\"begindate\":\"2023-09-22 12:36:36\"}]}";
        System.out.println(jsonOriginal);
        String json = jsonOriginal.replaceAll("[\\t\\n\\r]", "");
        System.out.println(json);
    }
  
}