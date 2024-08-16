package com.fission.machine.rocketmq.sender;

import cn.hutool.core.util.IdUtil;
import com.google.gson.Gson;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageSender.
 *
 * @author Devil
 * @date Created in 2024/8/15 下午4:52
 */
public class RocketMqMessageSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqMessageSender.class);
    
    private final RocketMQTemplate rocketMqTemplate;
    
    public RocketMqMessageSender(RocketMQTemplate rocketMqTemplate) {
        this.rocketMqTemplate = rocketMqTemplate;
    }
    
    /**
     * 发送rocketmq消息.
     *
     * @param rocketMqMessage 消息实体
     * @return 发送结果
     */
    public boolean sendMq(RocketMqMessage rocketMqMessage) {
        if (rocketMqMessage == null || StringUtils.isEmpty(rocketMqMessage.getTopicName()) || StringUtils.isEmpty(rocketMqMessage.getData())) {
            LOGGER.error("发送rocketmq消息失败: 缺失必要数据 rocketMqMessage:{}", new Gson().toJson(rocketMqMessage));
            return false;
        }
        
        rocketMqMessage.setMessageId(IdUtil.simpleUUID());
        LOGGER.info("[Rocket MQ Message Send] body：{}", new Gson().toJson(rocketMqMessage));
        List<Message<RocketMqMessage>> messageList = new ArrayList<>();
        
        // 发送消息均为json格式 需要带上key方便查找
        Message<RocketMqMessage> message = MessageBuilder.withPayload(rocketMqMessage).setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getMessageId()).build();
        if (rocketMqMessage.getTags() != null && rocketMqMessage.getTags().length > 0) {
            message.getHeaders().put(RocketMQHeaders.TAGS, rocketMqMessage.getTags());
        }
        messageList.add(message);
        
        boolean result = false;
        try {
            // 同步发送 超时5秒
            SendResult sr = rocketMqTemplate.syncSend(rocketMqMessage.getTopicName(), messageList, 5000);
            result = sr.getSendStatus().equals(SendStatus.SEND_OK);
            if (!result) {
                LOGGER.error("[Rocket MQ Message Send fail] reason: {}", sr.getSendStatus());
            }
        } catch (Exception e) {
            LOGGER.error("[Rocket MQ Message Send fail error] reason: {}", e.getLocalizedMessage(), e);
        }
        return result;
    }
}
