package com.devil.fission.common.util;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时器构建器.
 *
 * @author Devil
 * @date Created in 2023/3/3 10:12
 */
public class TaskBuilder {
    
    private static final AtomicInteger taskNumber = new AtomicInteger(1);
    
    private static ScheduledExecutorService timer;
    
    static {
        create();
    }
    
    private TaskBuilder() {
        throw new UnsupportedOperationException();
    }
    
    public static ScheduledFuture<?> schedule(Runnable task, long period, TimeUnit timeUnit) {
        return period == 0L ? null : timer.scheduleAtFixedRate(task, 0L, period, timeUnit);
    }
    
    public static ScheduledFuture<?> scheduleDelay(Runnable task, long delay, long period, TimeUnit timeUnit) {
        return period == 0L ? scheduleOnceDelay(task, delay, timeUnit) : timer.scheduleAtFixedRate(task, delay, period, timeUnit);
    }
    
    public static ScheduledFuture<?> scheduleOnce(Runnable task) {
        return timer.schedule(task, 0L, TimeUnit.MICROSECONDS);
    }
    
    public static ScheduledFuture<?> scheduleOnceDelay(Runnable task, long delay, TimeUnit timeUnit) {
        return delay == 0L ? scheduleOnce(task) : timer.schedule(task, delay, timeUnit);
    }
    
    public static void create() {
        if (null != timer) {
            shutdownNow();
        }
        
        timer = new ScheduledThreadPoolExecutor(1, (r) -> newThread(r, String.format("Timer-%s", taskNumber.getAndIncrement())));
    }
    
    public static void shutdown() {
        if (null != timer) {
            timer.shutdown();
        }
        
    }
    
    public static List<Runnable> shutdownNow() {
        return null != timer ? timer.shutdownNow() : null;
    }
    
    private static Thread newThread(Runnable runnable, String name) {
        Thread t = new Thread((ThreadGroup) null, runnable, name);
        t.setDaemon(false);
        return t;
    }
    
}
