package com.devil.fission.machine.rabbitmq.consumer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * ThreadPoolConsumerBuilder.
 *
 * @author devil
 * @date Created in 2023/5/29 16:24
 */
@Slf4j
@Getter
public class ThreadPoolConsumerBuilder<T> {
    
    private String queue;
    
    private MessageConsumerProcess<T> messageConsumerProcess;
    
    private MessageConsumer messageConsumer;
    
    public ThreadPoolConsumerBuilder<T> setQueue(String queue) {
        this.queue = queue;
        return this;
    }
    
    public ThreadPoolConsumerBuilder<T> setMessageConsumerProcess(MessageConsumerProcess<T> messageConsumerProcess) {
        this.messageConsumerProcess = messageConsumerProcess;
        return this;
    }
    
    /**
     * the messageConsumer to set.
     */
    public ThreadPoolConsumerBuilder<T> setMessageConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
        return this;
    }
    
    public ThreadPoolConsumer<T> build() {
        return new ThreadPoolConsumer<>(this);
    }
    
    @Override
    public String toString() {
        return "ThreadPoolConsumerBuilder{" + "queue='" + queue + '\'' + ", messageConsumerProcess=" + messageConsumerProcess + ", messageConsumer="
                + messageConsumer + '}';
    }
}