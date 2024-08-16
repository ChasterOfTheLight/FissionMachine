package com.fission.machine.rocketmq.config;

import com.fission.machine.rocketmq.sender.RocketMqMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMqConfiguration.
 *
 * @author Devil
 * @date Created in 2024/8/15 下午4:53
 */
@Slf4j
@Configuration
public class RocketMqConfiguration {

    @Bean
    public RocketMqMessageSender messageSender(RocketMQTemplate rocketMqTemplate) {
        log.info("RocketMqMessageSender init =======");
        return new RocketMqMessageSender(rocketMqTemplate);
    }
    
    @Bean
    public RocketMQMessageConverter messageConverter() {
        return new RocketMQMessageConverter();
    }
    
}
