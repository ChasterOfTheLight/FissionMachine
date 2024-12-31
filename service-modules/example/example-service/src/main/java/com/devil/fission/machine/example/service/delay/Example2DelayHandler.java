package com.devil.fission.machine.example.service.delay;

import com.devil.fission.machine.example.api.vo.SysUserQueryVo;
import com.devil.fission.machine.redis.delay.RedissonDelayedHandler;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 实例延迟消息处理2.
 *
 * @author Devil
 * @date Created in 2024/6/14 下午2:12
 */
@Component
public class Example2DelayHandler implements RedissonDelayedHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Example2DelayHandler.class);
    
    public static final String DELAY_QUEUE = "exampleDelayQueue2";
    
    @PostConstruct
    public void init() {
        LOGGER.info("============= init");
    }
    
    @Override
    public String getQueueName() {
        return DELAY_QUEUE;
    }
    
    @Override
    public <T> void execute(T msg) {
        try {
            if (msg instanceof SysUserQueryVo) {
                // 转换为 VO
                SysUserQueryVo sysUserQueryVo = (SysUserQueryVo) msg;
                LOGGER.info("参数 = {}", new Gson().toJson(sysUserQueryVo));
            }
        } catch (Exception e) {
            LOGGER.error("执行延迟消息失败", e);
        }
    }
}
