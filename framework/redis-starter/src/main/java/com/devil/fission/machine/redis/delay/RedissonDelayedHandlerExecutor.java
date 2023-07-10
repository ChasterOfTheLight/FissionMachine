package com.devil.fission.machine.redis.delay;

import com.devil.fission.machine.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redisson延迟队列执行器实际运行器.
 *
 * @author Devil
 * @date Created in 2022/11/3 18:23
 */
public class RedissonDelayedHandlerExecutor implements ApplicationContextAware {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonDelayedHandlerExecutor.class);
    
    private Set<String> handlerNames;
    
    private final RedissonDelayedUtil redissonDelayedUtil;
    
    public RedissonDelayedHandlerExecutor(RedissonDelayedUtil redissonDelayedUtil) {
        this.redissonDelayedUtil = redissonDelayedUtil;
    }
    
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        handlerNames = new HashSet<>();
    }
    
    /**
     * 添加处理器.
     *
     * @param handlerName 处理器名称
     */
    public void addHandler(String handlerName) {
        if (StringUtils.isNotBlank(handlerName)) {
            this.handlerNames.add(handlerName);
        }
    }
    
    /**
     * 判断是否已经存在对应处理器.
     *
     * @param handlerName 处理器名
     * @return 是否
     */
    public boolean existHandler(String handlerName) {
        if (StringUtils.isNotBlank(handlerName)) {
            return handlerNames.contains(handlerName);
        }
        return false;
    }
    
    /**
     * 延时执行器.
     */
    @Async(value = "redissonDelayedProcessTaskExecutor")
    public void execute() {
        boolean flag = true;
        LOGGER.info("Start Redisson Delayed Handler");
        while (flag) {
            try {
                // 根据spring容器内获取到的实现依次执行
                for (String handlerName : handlerNames) {
                    RedissonDelayedHandler handler = (RedissonDelayedHandler) applicationContext.getBean(handlerName);
                    Object o = redissonDelayedUtil.take(handler.getQueueName());
                    if (o != null) {
                        handler.execute(o);
                    }
                }
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                // 打断事件，一般是容器销毁有可能触发，这时不用处理这个异常
                flag = false;
            } catch (Exception e) {
                LOGGER.error("执行延时任务出错 {}", e.getMessage(), e);
                flag = false;
            }
        }
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
