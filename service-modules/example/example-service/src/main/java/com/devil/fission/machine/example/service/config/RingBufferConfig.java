package com.devil.fission.machine.example.service.config;

import com.devil.fission.machine.example.service.event.ExampleEvent;
import com.devil.fission.machine.example.service.event.ExampleEventFactory;
import com.devil.fission.machine.example.service.event.ExampleEventHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 环形队列配置.
 *
 * @author Devil
 * @date Created in 2024/5/14 16:02
 */
@Configuration
public class RingBufferConfig {
    
    @Bean("exampleEventRingBuffer")
    public RingBuffer<ExampleEvent> ringBuffer() {
        Disruptor<ExampleEvent> disruptor = new Disruptor<>(new ExampleEventFactory(), 1024, new ExampleThreadFactory("example"), ProducerType.SINGLE, new BlockingWaitStrategy());
        ExampleEventHandler handler = new ExampleEventHandler();
        // 初始化消费者
        disruptor.handleEventsWith(handler);
        // 开启所有消费者监听数据
        disruptor.start();
        // 获取RingBuffer环，用于接取生产者生产的事件
        return disruptor.getRingBuffer();
    }
    
    static class ExampleThreadFactory implements ThreadFactory {
        
        private final String namePrefix;
        
        private final AtomicInteger nextId = new AtomicInteger(1);
        
        ExampleThreadFactory(String whatFeatureOfGroup) {
            namePrefix = whatFeatureOfGroup + "-send-";
        }
        
        @Override
        public Thread newThread(Runnable task) {
            String name = namePrefix + nextId.getAndIncrement();
            return new Thread(null, task, name, 0);
        }
        
    }
    
}