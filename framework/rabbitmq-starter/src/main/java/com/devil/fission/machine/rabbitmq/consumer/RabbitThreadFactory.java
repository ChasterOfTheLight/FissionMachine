package com.devil.fission.machine.rabbitmq.consumer;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RabbitThreadFactory.
 *
 * @author devil
 * @date Created in 2023/5/29 16:26
 */
public class RabbitThreadFactory implements ThreadFactory {
    
    private static final AtomicLong THREAD_NUMBER = new AtomicLong(1);
    
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("mq-t");
    
    private final boolean daemon;
    
    private final String namePrefix;
    
    private RabbitThreadFactory(final String namePrefix, final boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }
    
    /**
     * create custom thread factory.
     *
     * @param namePrefix prefix
     * @param daemon     daemon
     * @return {@linkplain ThreadFactory }
     */
    public static ThreadFactory create(final String namePrefix, final boolean daemon) {
        return new RabbitThreadFactory(namePrefix, daemon);
    }
    
    @Override
    public Thread newThread(final @NonNull Runnable runnable) {
        Thread thread = new Thread(THREAD_GROUP, runnable, THREAD_GROUP.getName() + "-" + namePrefix + "-" + THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
