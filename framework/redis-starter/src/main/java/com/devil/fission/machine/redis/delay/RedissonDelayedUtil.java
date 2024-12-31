package com.devil.fission.machine.redis.delay;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.util.StringUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redisson延迟队列工具类.
 *
 * @author devil
 * @date Created in 2022/10/31 16:07
 */
public class RedissonDelayedUtil implements InitializingBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonDelayedUtil.class);
    
    private final RedissonClient redissonClient;
    
    private Map<String, RBlockingDeque> blockingDequeMap;
    
    private Map<String, RDelayedQueue> delayedQueueMap;
    
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
                RDelayedQueue delayedQueue = delayedQueueMap.get(queueName);
                if (delayedQueue != null) {
                    delayedQueue.offer(value, delay, timeUnit);
                    return true;
                }
            } catch (Exception e) {
                LOGGER.error("(添加延时队列失败) {}", e.getMessage());
                throw new ServiceException("(添加延时队列失败)");
            }
        }
        return false;
    }
    
    /**
     * 获取队列消息.
     *
     * @param queueName 队列名
     * @return 对象（因为是poll可能返回null）
     */
    public <T> T take(@NonNull String queueName) {
        if (!StringUtils.isBlank(queueName)) {
            RBlockingDeque blockingDeque = blockingDequeMap.get(queueName);
            if (blockingDeque != null) {
                return (T) blockingDeque.poll();
            }
        }
        return null;
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
            RDelayedQueue delayedQueue = delayedQueueMap.get(queueName);
            if (delayedQueue != null) {
                return delayedQueue.remove(t);
            }
        }
        return false;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        blockingDequeMap = new LinkedHashMap<>();
        delayedQueueMap = new LinkedHashMap<>();
    }
    
    /**
     * 增加队列缓存.
     */
    public void addHandler(RedissonDelayedHandler handler) {
        String key = handler.getQueueName();
        RBlockingDeque blockingDeque = this.redissonClient.getBlockingDeque(key);
        RDelayedQueue delayedQueue = this.redissonClient.getDelayedQueue(blockingDeque);
        blockingDequeMap.put(key, blockingDeque);
        delayedQueueMap.put(key, delayedQueue);
    }
    
}
