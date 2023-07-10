package com.devil.fission.machine.redis.delay;

/**
 * redisson延迟队列执行器接口.
 *
 * @author Devil
 * @date Created in 2022/10/31 16:25
 */
public interface RedissonDelayedHandler {
    
    /**
     * 获取阻塞队列名称，方便后面runner使用.
     *
     * @return 队列名称
     */
    String getQueueName();
    
    /**
     * 执行方法.
     *
     * @param t 执行对象
     */
    <T> void execute(T t);
    
}