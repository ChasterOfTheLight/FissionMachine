package com.devil.fission.machine.redis.delay;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * redisson延时队列bean后置处理.
 *
 * @author Devil
 * @date Created in 2022/10/31 18:19
 */
public class RedissonDelayedInitializerPostProcessor implements BeanPostProcessor, Ordered {
    
    @Autowired
    private BeanFactory beanFactory;
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RedissonDelayedHandler) {
            // 放入runner
            RedissonDelayedHandlerExecutor handlerExecutor = this.beanFactory.getBean(RedissonDelayedHandlerExecutor.class);
            if (handlerExecutor.existHandler(beanName)) {
                throw new RuntimeException("已存在延时队列处理器" + beanName);
            }
            handlerExecutor.addHandler(beanName);
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }
}
