package com.fission.machine.aliyun.rocketmq;

import cn.hutool.core.util.IdUtil;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.devil.fission.machine.common.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * MessageSender.
 *
 * @author devil
 * @date Created in 2024/8/15 下午4:52
 */
@Slf4j
public class AliyunRocketMqMessageSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunRocketMqMessageSender.class);
    
    private final ProducerBean producerBean;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AliyunRocketMqMessageSender(ProducerBean producerBean) {
        this.producerBean = producerBean;
    }
    
    /**
     * 发送rocketmq消息.
     *
     * @param rocketMqMessage 消息实体
     * @return 发送结果
     */
    public boolean sendMessage(AliyunRocketMqMessage rocketMqMessage) {
        if (rocketMqMessage == null || StringUtils.isBlank(rocketMqMessage.getTopicName()) || StringUtils.isBlank(rocketMqMessage.getData())) {
            LOGGER.error("发送aliyun rocketmq消息失败: 缺失必要数据 rocketMqMessage:{}", rocketMqMessage);
            return false;
        }
        String messageId = IdUtil.simpleUUID();
        LOGGER.info("[Aliyun Rocket MQ Message Send] msgId: {}, msg: {}", messageId, rocketMqMessage);
        Message msg = null;
        try {
            msg = new Message(rocketMqMessage.getTopicName(), rocketMqMessage.getTags(), objectMapper.writeValueAsString(rocketMqMessage).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            log.error("发送aliyun rocketmq消息失败: json序列化失败", e);
            return false;
        }
        // 设置代表消息的业务关键属性，请尽可能全局唯一
        // 以方便您在无法正常收到消息情况下，可通过MQ 控制台查询消息并补发
        // 注意：不设置也不会影响消息正常收发
        msg.setKey(messageId);
        
        boolean result = false;
        try {
            // 同步发送 超时5秒
            SendResult sendResult = producerBean.send(msg);
            result = sendResult != null;
            if (!result) {
                LOGGER.error("[Aliyun Rocket MQ Message Send fail]");
            } else {
                LOGGER.info("[Aliyun Rocket MQ Message Send success] result: {}", new Gson().toJson(sendResult));
            }
        } catch (Exception e) {
            LOGGER.error("[Aliyun Rocket MQ Message Send fail error] reason: {}", e.getLocalizedMessage(), e);
        }
        return result;
    }
}
