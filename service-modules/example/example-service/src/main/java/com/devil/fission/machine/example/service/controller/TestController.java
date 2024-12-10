package com.devil.fission.machine.example.service.controller;

import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.common.util.FileUtils;
import com.devil.fission.machine.example.service.delay.ExampleDelayHandler;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.service.ISysUserService;
import com.devil.fission.machine.example.service.utils.NoGenUtils;
import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.local.LocalFileStorableObject;
import com.devil.fission.machine.object.storage.minio.MinioStorageServiceImpl;
import com.devil.fission.machine.redis.delay.RedissonDelayedUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * TestController.
 *
 * @author Devil
 * @date Created in 2024/6/21 下午3:22
 */
@Slf4j
@RequestMapping(value = "/test")
@RestController
public class TestController {
    
    private final NoGenUtils noGenUtils;
    
    private final ISysUserService sysUserService;
    
    private final MinioStorageServiceImpl minioStorageService;
    
    @Autowired
    @Lazy
    private RedissonDelayedUtil redissonDelayedUtil;
    
    /**
     * TestController.
     *
     * @param noGenUtils noGenUtils
     */
    public TestController(NoGenUtils noGenUtils, ISysUserService sysUserService, MinioStorageServiceImpl minioStorageService) {
        this.noGenUtils = noGenUtils;
        this.sysUserService = sysUserService;
        this.minioStorageService = minioStorageService;
    }
    
    /**
     * 生成订单号.
     *
     * @return Response
     */
    @PostMapping(value = "/genOrderNo")
    public Response<String> genOrderNo() {
        return Response.success(noGenUtils.genOrderNo());
    }
    
    /**
     * 测试延迟队列.
     *
     * @return Response
     */
    @PostMapping(value = "/delayMsg")
    public Response<String> delayMsg() {
        redissonDelayedUtil.offer("123", 5, TimeUnit.SECONDS, ExampleDelayHandler.DELAY_QUEUE);
        return Response.success("success");
    }
    
    //    /**
    //     * 测试事务消息.
    //     *
    //     * @return Response
    //     */
    //    @PostMapping(value = "/testTrx")
    //    public Response<String> testTrx() {
    //        String topicName = "machine";
    //        boolean result = messageSender.sendMq(RocketMqMessage.builder().bizId(String.valueOf(IdUtil.getSnowflakeNextId())).topicName(topicName).data("123").build());
    //        if (!result) {
    //            return Response.error("发送失败");
    //        }
    //        return Response.success("success");
    //    }
    
    /**
     * 测试token生成.
     *
     * @return Response
     */
    @PostMapping(value = "/saToken")
    public Response<String> saToken() {
        StpUtil.login(10001, SaLoginConfig.setExtra("name", "zhangsan").setExtra("age", 18).setExtra("role", "超级管理员"));
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("saToken: {}", tokenInfo);
        return Response.success(tokenInfo.getTokenValue());
    }
    
    /**
     * 测试token踢人下线.
     *
     * @return Response
     */
    @PostMapping(value = "/saTokenKickout")
    public Response<String> saTokenKickout() {
        StpUtil.kickout(10001);
        return Response.success("success");
    }
    
    /**
     * 测试token退出登录.
     *
     * @return Response
     */
    @PostMapping(value = "/saTokenLogout")
    public Response<String> saTokenLogout() {
        StpUtil.logout(10001);
        return Response.success("success");
    }
    
    /**
     * 测试sql打印.
     *
     * @return Response
     */
    @PostMapping(value = "/sqlPrint")
    public Response<String> sqlPrint() {
        sysUserService.queryPage(1, 10, SysUserEntity.builder().isEnabled(1).build());
        sysUserService.queryList(null);
        sysUserService.insert(
                SysUserEntity.builder().userName(RandomUtil.randomString(5)).isEnabled(1).userPassword("123").lastLoginIp("127.0.0.1").createdBy(1L).updatedBy(1L).build());
        sysUserService.update(SysUserEntity.builder().userId(100L).userName("zj").isEnabled(1).build());
        sysUserService.deleteById(100L);
        return Response.success("success");
    }
    
    @PostMapping(value = "/minioPut")
    public Response<String> minioPut() {
        // 创建临时文件
        File tmpFile = null;
        try {
            File tmpDir = new File("/usr/tmp");
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            tmpFile = FileUtils.createTmpFile(new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8)), "test", ".txt", tmpDir);
            LocalFileStorableObject localFileStorableObject = new LocalFileStorableObject(tmpDir.getPath(), tmpFile.getName());
            minioStorageService.put("test/tmp.txt", localFileStorableObject, localFileStorableObject.getPermission());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
        return Response.success();
    }
    
    @PostMapping(value = "/minioGet")
    public Response<String> minioGet() {
        StorableObject storableObject = minioStorageService.get("test/tmp.txt");
        try (InputStream inputStream = storableObject.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, bytesRead));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.success();
    }
    
}
