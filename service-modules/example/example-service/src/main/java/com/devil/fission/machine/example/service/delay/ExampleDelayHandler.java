package com.devil.fission.machine.example.service.delay;

import com.devil.fission.machine.redis.delay.RedissonDelayedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 实例延迟消息处理.
 *
 * @author Devil
 * @date Created in 2024/6/14 下午2:12
 */
@Component
public class ExampleDelayHandler implements RedissonDelayedHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDelayHandler.class);
    
    public static final String DELAY_QUEUE = "exampleQueue";
    
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
            if (msg instanceof String) {
                // 具体的业务逻辑，参数类型可根据业务需要自行替换
                LOGGER.info("参数 = {}", msg);
            }
        } catch (Exception e) {
            LOGGER.error("执行延迟消息失败", e);
        }
    }
}
