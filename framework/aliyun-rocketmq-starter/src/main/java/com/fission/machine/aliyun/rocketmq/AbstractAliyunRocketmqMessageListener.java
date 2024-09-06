package com.fission.machine.aliyun.rocketmq;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云rocketmq消息监听抽象类.
 *
 * @author Devil
 * @date Created in 2024/9/4 10:06
 */
@Slf4j
public abstract class AbstractAliyunRocketmqMessageListener implements MessageListener {
    
    @Override
    public Action consume(Message message, ConsumeContext context) {
        String messageBody = new String(message.getBody());
        log.info("consume message topic: {}, tag: {} body: {}", message.getTopic(), message.getTag(), messageBody);
        try {
            boolean result = doConsume(messageBody);
            if (!result) {
                log.error("consume message fail: {}", message);
            }
            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("consume message error: {}", message, e);
            return Action.ReconsumeLater;
        }
    }
    
    protected abstract boolean doConsume(String messageBody);
}
