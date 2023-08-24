package com.devil.fission.machine.rabbitmq.consumer;

import com.devil.fission.machine.common.util.StringUtils;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * rabbitmq消息处理.
 *
 * @author devil
 * @date Created in 2023/5/29 15:53
 */
@Slf4j
public class RabbitmqConsumerHandler implements ApplicationContextAware, CommandLineRunner {
    
    private final ConnectionFactory connectionFactory;
    
    private final MessageConverter messageConverter;
    
    private ApplicationContext applicationContext;
    
    public RabbitmqConsumerHandler(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }
    
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }
    
    @SuppressWarnings("all")
    private <T> void initConsumerPool(String queueName, Object bean) {
        MessageConsumerProcess<T> consumerProcess = (MessageConsumerProcess<T>) bean;
        ThreadPoolConsumerBuilder<T> consumerBuilder = new ThreadPoolConsumerBuilder<T>().setQueue(queueName)
                .setMessageConsumerProcess(consumerProcess).setMessageConsumer(buildMessageConsumer(queueName, consumerProcess));
        ThreadPoolConsumer<T> consumer = consumerBuilder.build();
        consumer.start();
    }
    
    private <T> MessageConsumer buildMessageConsumer(String queueName, MessageConsumerProcess<T> messageConsumerProcess) {
        final Connection connection = connectionFactory.createConnection();
        // 设置message序列化方法
        final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
        return new MessageConsumer() {
            final Channel channel;
            
            {
                channel = connection.createChannel(false);
            }
            
            @Override
            public MessageResult consume() {
                try {
                    // 通过basicGet获取原始数据
                    GetResponse response = channel.basicGet(queueName, false);
                    try {
                        while (response == null) {
                            response = channel.basicGet(queueName, false);
                            Thread.sleep(1000L);
                        }
                    } catch (AmqpException e) {
                        log.error("rabbitmq获取数据异常", e);
                        return null;
                    }
                    Message message = new Message(response.getBody(),
                            messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8"));
                    MessageResult messageResult;
                    String messageId = message.getMessageProperties().getMessageId();
                    try {
                        // 将原始数据转换为特定类型的包
                        @SuppressWarnings("unchecked") T messageBean = (T) messageConverter.fromMessage(message);
                        String messageJson = new Gson().toJson(messageBean);
                        log.info("收到消息  消息id：{} 消息体：{}", messageId, messageJson);
                        // 处理数据返回结果
                        messageResult = messageConsumerProcess.process(messageBean);
                    } catch (Exception e) {
                        log.error("消息接收异常:\n", e);
                        messageResult = new MessageResult(false, "消息接收异常:\n " + e);
                    }
                    if (messageResult == null) {
                        messageResult = new MessageResult(false, "消息消费失败");
                    }
                    // 消息接收确认
                    if (messageResult.isSuccess()) {
                        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    } else {
                        Thread.sleep(1000L);
                        log.error("消息处理失败: " + messageResult.getMessage());
                        if (StringUtils.isEmpty(messageId)) {
                            // 如果消息id不存在，直接拒绝
                            channel.basicReject(response.getEnvelope().getDeliveryTag(), false);
                        } else {
                            // 消费失败，记录错误次数
                            int errorCount = errorCount(messageId);
                            int errorThreshold = 5;
                            if (errorCount >= errorThreshold) {
                                // 错误次数达到阈值，忽略消息，不再重新入队
                                channel.basicReject(response.getEnvelope().getDeliveryTag(), false);
                                clearErrorCount(messageId);
                            } else {
                                // 错误次数未达到阈值，将错误次数写回消息属性，并重新入队
                                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                            }
                        }
                    }
                    return messageResult;
                } catch (Exception e) {
                    log.error("rabbitmq消费异常", e);
                    try {
                        channel.close();
                    } catch (IOException | TimeoutException ex) {
                        return new MessageResult(false, "rabbitmq关闭或取消");
                    }
                    return new MessageResult(false, "rabbitmq消费出错");
                }
            }
        };
    }
    
    private final Map<String, Integer> errorMessageMap = new ConcurrentHashMap<>();
    
    private Integer errorCount(String messageId) {
        return errorMessageMap.getOrDefault(messageId, 0) + 1;
    }
    
    private void clearErrorCount(String messageId) {
        errorMessageMap.remove(messageId);
    }
    
    @Override
    public void run(String... args) throws Exception {
        Class<? extends RabbitmqConsumer> annotationClass = RabbitmqConsumer.class;
        Map<String, Object> annotation = applicationContext.getBeansWithAnnotation(annotationClass);
        Set<Map.Entry<String, Object>> entitySet = annotation.entrySet();
        for (Map.Entry<String, Object> entry : entitySet) {
            String name = entry.getValue().getClass().getName();
            log.info("load rabbitmq consumer: {}", name);
            Class<?> clazz = entry.getValue().getClass();
            RabbitmqConsumer rabbitmqConsumer = AnnotationUtils.findAnnotation(clazz, RabbitmqConsumer.class);
            if (Objects.isNull(rabbitmqConsumer)) {
                continue;
            }
            Object bean = getBean(clazz);
            // 构建消费者，开启监听
            initConsumerPool(rabbitmqConsumer.queue(), bean);
        }
    }
}
