package com.devil.fission.machine.redis.delay;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.util.StringUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * redisson延迟队列工具类.
 *
 * @author Devil
 * @date Created in 2022/10/31 16:07
 */
public class RedissonDelayedUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonDelayedUtil.class);
    
    private final RedissonClient redissonClient;
    
    public RedissonDelayedUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    /**
     * 添加延时队列任务.
     *
     * @param value     对象
     * @param delay     时间
     * @param timeUnit  时间单位
     * @param queueName 队列名
     * @return 是否添加成功
     */
    public <T> boolean offer(@NonNull T value, @NonNull long delay, @NonNull TimeUnit timeUnit, @NonNull String queueName) {
        if (!StringUtils.isBlank(queueName) && delay > 0L) {
            try {
                RBlockingDeque<T> blockingDeque = this.redissonClient.getBlockingDeque(queueName);
                RDelayedQueue<T> delayedQueue = this.redissonClient.getDelayedQueue(blockingDeque);
                delayedQueue.offer(value, delay, timeUnit);
                return true;
            } catch (Exception e) {
                LOGGER.error("(添加延时队列失败) {}", e.getMessage());
                throw new ServiceException("(添加延时队列失败)");
            }
        } else {
            return false;
        }
    }
    
    /**
     * 获取队列消息.
     *
     * @param queueName 队列名
     * @return 对象（因为是poll可能返回null）
     */
    public <T> T take(@NonNull String queueName) {
        if (StringUtils.isBlank(queueName)) {
            return null;
        } else {
            RBlockingDeque<T> blockingDeque = this.redissonClient.getBlockingDeque(queueName);
            this.redissonClient.getDelayedQueue(blockingDeque);
            return (T) blockingDeque.poll();
        }
    }
    
    /**
     * 删除阻塞队列中的对象.
     *
     * @param t         对象
     * @param queueName 队列名
     * @return 是否删除成功
     */
    public <T> boolean remove(@NonNull T t, @NonNull String queueName) {
        if (!StringUtils.isBlank(queueName)) {
            RBlockingDeque<T> blockingDeque = this.redissonClient.getBlockingDeque(queueName);
            RDelayedQueue<T> delayedQueue = this.redissonClient.getDelayedQueue(blockingDeque);
            return delayedQueue.remove(t);
        } else {
            return false;
        }
    }
}
