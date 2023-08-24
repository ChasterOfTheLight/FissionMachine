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
     *
     * @param message 消息体
     * @return 消息状态返回
     */
    MessageResult process(T message);
}
