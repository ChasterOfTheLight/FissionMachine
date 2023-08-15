package com.devil.fission.machine.rabbitmq.consumer;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rabbitmq消费者.
 *
 * @author devil
 * @date Created in 2023/5/29 15:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Indexed
@Component
public @interface RabbitmqConsumer {
    
    /**
     * 消息队列名称.
     */
    String queue();
    
}
