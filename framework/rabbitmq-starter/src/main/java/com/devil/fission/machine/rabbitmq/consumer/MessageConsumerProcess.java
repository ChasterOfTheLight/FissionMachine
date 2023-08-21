package com.devil.fission.machine.rabbitmq.consumer;

/**
 * MessageConsumerProcess.
 *
 * @author devil
 * @date Created in 2023/5/29 16:20
 */
public interface MessageConsumerProcess<T> {

    /**
     * 消息接收处理.
     */
    MessageResult process(T message);
}
