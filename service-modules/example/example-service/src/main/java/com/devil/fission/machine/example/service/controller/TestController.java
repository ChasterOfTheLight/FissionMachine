package com.devil.fission.machine.example.service.controller;

import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.service.delay.ExampleDelayHandler;
import com.devil.fission.machine.example.service.utils.NoGenUtils;
import com.devil.fission.machine.redis.delay.RedissonDelayedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * TestController.
 *
 * @author Devil
 * @date Created in 2024/6/21 下午3:22
 */
@RequestMapping(value = "/test")
@RestController
public class TestController {
    
    private final NoGenUtils noGenUtils;
    
    @Autowired
    @Lazy
    private RedissonDelayedUtil redissonDelayedUtil;
    
    public TestController(NoGenUtils noGenUtils) {
        this.noGenUtils = noGenUtils;
    }
    
    @PostMapping(value = "/genOrderNo")
    public Response<String> genOrderNo() {
        return Response.success(noGenUtils.genOrderNo());
    }
    
    @PostMapping(value = "/delayMsg")
    public Response<String> delayMsg() {
        redissonDelayedUtil.offer("123", 5, TimeUnit.SECONDS, ExampleDelayHandler.DELAY_QUEUE);
        return Response.success("success");
    }
    
    @PostMapping(value = "/testTrx")
    public Response<String> testTrx() {
        return Response.success("success");
    }
}
