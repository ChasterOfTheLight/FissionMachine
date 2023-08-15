package com.devil.fission.machine.rabbitmq.sender;

import com.devil.fission.machine.common.util.IdGenerator;
import com.devil.fission.machine.common.util.IdGeneratorEnum;
import com.devil.fission.machine.common.util.StringUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 消息发送.
 *
 * @author devil
 * @date Created in 2023/5/29 14:36
 */
public class MessageSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    private final IdGenerator idGenerator = IdGeneratorEnum.INSTANCE.getIdGenerator();
    
    /**
     * 发送直连消息.
     *
     * @param exchangeName 交换机名称
     * @param routingKey   交换机绑定的key
     * @param message      消息
     */
    public boolean sendDirectMessage(String exchangeName, String routingKey, Object message) {
        if (StringUtils.isEmpty(exchangeName) || StringUtils.isEmpty(routingKey) || message == null) {
            LOGGER.error("发送Direct队列失败: 缺失必要数据 exchange: {} routeKey: {} data: {}", exchangeName, routingKey, new Gson().toJson(message));
            return false;
        }
        try {
            long id = idGenerator.nextId();
            String messageId = String.valueOf(id);
            LOGGER.info("准备发送消息 消息唯一标识 : {} 消息体: {}", messageId, new Gson().toJson(message));
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                m.getMessageProperties().setMessageId(messageId);
                return m;
            }, new CorrelationData(messageId));
        } catch (Exception e) {
            LOGGER.error("发送Direct队列失败: {}", e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }
}