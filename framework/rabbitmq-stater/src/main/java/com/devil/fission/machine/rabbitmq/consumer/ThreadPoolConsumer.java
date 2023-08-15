package com.devil.fission.machine.rabbitmq.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolConsumer.
 *
 * @author devil
 * @date Created in 2023/5/29 16:27
 */
@Slf4j
public class ThreadPoolConsumer<T> {
    
    private final ScheduledExecutorService scheduledExecutorService;
    
    private final ThreadPoolConsumerBuilder<T> consumerBuilder;
    
    private boolean stop = false;
    
    public ThreadPoolConsumer(ThreadPoolConsumerBuilder<T> threadPoolConsumerBuilder) {
        this.consumerBuilder = threadPoolConsumerBuilder;
        ThreadFactory threadFactory = RabbitThreadFactory.create(consumerBuilder.getQueue(), true);
        // 每个consumer一个线程池，但核心线程限制为1个
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
    
    /**
     * 运行监听.
     */
    public void start() {
        // 构造messageConsumer
        log.info("开始监听rabbitmq队列[{}] ", consumerBuilder.getQueue());
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                this.scheduledExecutorService.execute(() -> {
                    MessageConsumer messageConsumer = consumerBuilder.getMessageConsumer();
                    while (!stop) {
                        try {
                            // 执行consume 消费
                            final MessageResult messageResult = messageConsumer.consume();
                            if (messageResult == null) {
                                break;
                            }
                            if (!messageResult.isSuccess()) {
                                log.warn("消费失败: 回执消息[{}] ", messageResult.getMessage());
                            }
                        } catch (Exception e) {
                            log.error("消费异常： ", e);
                        }
                    }
                });
            } catch (Exception e) {
                log.info("调度任务执行失败", e);
                shutdown();
            }
        }, 10, 1, TimeUnit.SECONDS);
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
    
    public void stop() {
        this.stop = true;
        try {
            Thread.sleep(1000L);
            shutdown();
        } catch (InterruptedException e) {
            log.info("休眠失败", e);
        }
    }
    
    private void shutdown() {
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
        log.info("Shutdown rmq adepts");
    }
    
    @Override
    public String toString() {
        return "ThreadPoolConsumer{" + "executor=" + scheduledExecutorService + ", infoHolder=" + consumerBuilder + ", stop=" + stop + '}';
    }
    
}
