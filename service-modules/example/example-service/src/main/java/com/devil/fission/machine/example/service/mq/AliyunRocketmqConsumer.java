package com.devil.fission.machine.example.service.mq;

import com.fission.machine.aliyun.rocketmq.AbstractAliyunRocketmqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AliyunRocketmqConsumer.
 *
 * @author Devil
 * @date Created in 2024/9/4 10:49
 */
@Slf4j
@Component
public class AliyunRocketmqConsumer extends AbstractAliyunRocketmqMessageListener {
    
    @Override
    protected boolean doConsume(String messageBody) {
        log.info("messageBody:{}", messageBody);
        return true;
    }
}
