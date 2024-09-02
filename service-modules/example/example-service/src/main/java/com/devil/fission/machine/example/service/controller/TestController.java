package com.devil.fission.machine.example.service.controller;

import cn.hutool.core.util.IdUtil;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.service.delay.ExampleDelayHandler;
import com.devil.fission.machine.example.service.utils.NoGenUtils;
import com.devil.fission.machine.redis.delay.RedissonDelayedUtil;
import com.fission.machine.rocketmq.sender.RocketMqMessage;
import com.fission.machine.rocketmq.sender.RocketMqMessageSender;
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
    
    private final RocketMqMessageSender messageSender;
    
    @Autowired
    @Lazy
    private RedissonDelayedUtil redissonDelayedUtil;
    
    /**
     * TestController.
     *
     * @param noGenUtils      noGenUtils
     * @param messageSender messageSender
     */
    public TestController(NoGenUtils noGenUtils, RocketMqMessageSender messageSender) {
        this.noGenUtils = noGenUtils;
        this.messageSender = messageSender;
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
    
    /**
     * 测试事务消息.
     *
     * @return Response
     */
    @PostMapping(value = "/testTrx")
    public Response<String> testTrx() {
        String topicName = "machine";
        boolean result = messageSender.sendMq(RocketMqMessage.builder().bizId(String.valueOf(IdUtil.getSnowflakeNextId())).topicName(topicName).data("123").build());
        if (!result) {
            return Response.error("发送失败");
        }
        return Response.success("success");
    }
}
