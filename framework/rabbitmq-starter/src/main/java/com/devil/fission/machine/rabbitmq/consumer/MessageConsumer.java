package com.devil.fission.machine.rabbitmq.consumer;

/**
 * MessageConsumer.
 *
 * @author Devil
 * @date Created in 2023/5/29 16:08
 */
public interface MessageConsumer {
    
    /**
     * 消费消息.
     */
    MessageResult consume();
    
}
