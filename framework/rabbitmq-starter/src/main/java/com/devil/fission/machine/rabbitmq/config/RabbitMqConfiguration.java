package com.devil.fission.machine.rabbitmq.config;

import com.devil.fission.machine.rabbitmq.consumer.RabbitmqConsumerHandler;
import com.devil.fission.machine.rabbitmq.sender.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Objects;

/**
 * rabbitmq配置.
 *
 * @author devil
 * @date Created in 2023/5/29 14:29
 */
@Configuration
public class RabbitMqConfiguration {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConfiguration.class);
    
    private final RabbitProperties rabbitProperties;
    
    public RabbitMqConfiguration(RabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }
    
    @Bean
    public MessageSender messageSender(RabbitTemplate rabbitTemplate) {
        return new MessageSender(rabbitTemplate);
    }
    
    @Bean
    public RabbitmqConsumerHandler rabbitmqConsumerHandler(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        return new RabbitmqConsumerHandler(connectionFactory, messageConverter);
    }
    
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        // 走完整个流程 防止消息丢失
        rabbitTemplate.setMandatory(true);
        
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *  ConfirmCallback机制只确认消息是否到达exchange(交换器)，不保证消息可以路由到正确的queue;
             *  需要设置：publisher-confirm-type: CORRELATED；
             *  springboot版本较低 参数设置改成：publisher-confirms: true
             *  以实现方法confirm中ack属性为标准，true到达
             *  config : 需要开启rabbitmq得ack publisher-confirm-type.
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // ack判断消息发送到交换机是否成功
                if (ack) {
                    LOGGER.info("投递消息成功 消息唯一标识 : {} ", correlationData.getId());
                } else {
                    LOGGER.error("投递消息失败 消息唯一标识 : {} 失败原因 ：{}", correlationData.getId(), cause);
                }
            }
        });
        
        // 消息路由失败，回调
        // 消息(带有路由键routingKey)到达交换机，与交换机的所有绑定键进行匹配，匹配不到触发回调
        // 实现接口ReturnCallback，重写 returnedMessage() 方法，
        // 方法有五个参数
        // message（消息体）、
        // replyCode（响应code）、
        // replyText（响应内容）、
        // exchange（交换机）、
        // routingKey（队列）。
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            LOGGER.error("匹配不到路由键 message: {} replyCode: {} replyText: {} exchange: {} routingKey: {}", message, replyCode, replyText,
                    exchange, routingKey);
        });
        
        LOGGER.info("init rabbitTemplate =====================");
        return rabbitTemplate;
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    @Primary
    public ConnectionFactory getConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(rabbitProperties.getAddresses());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        connectionFactory.setPublisherReturns(true);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        
        if (Objects.nonNull(rabbitProperties.getCache())){
            if(Objects.nonNull(rabbitProperties.getCache().getConnection())) {
                connectionFactory.setCacheMode(rabbitProperties.getCache().getConnection().getMode());
            }
            if (Objects.nonNull(rabbitProperties.getCache().getChannel())&&Objects.nonNull(rabbitProperties.getCache().getChannel().getSize())) {
                connectionFactory.setChannelCacheSize(rabbitProperties.getCache().getChannel().getSize());
            }
        }
        LOGGER.info("init rabbitmq connection ========================");
        return connectionFactory;
    }
    
}
