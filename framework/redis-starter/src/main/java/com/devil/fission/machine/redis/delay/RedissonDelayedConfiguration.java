package com.devil.fission.machine.redis.delay;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * redisson延时队列配置.
 *
 * @author Devil
 * @date Created in 2022/10/31 18:33
 */
@Configuration
@EnableAsync
@Import({RedissonDelayedConfiguration.Registrar.class})
public class RedissonDelayedConfiguration {
    
    static class Registrar implements ImportBeanDefinitionRegistrar {
        
        private static final String BEAN_NAME = "redissonDelayedInitializerPostProcessor";
        
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            // 注册所用组件bean
            registry.registerBeanDefinition("redissonDelayedUtil", new RootBeanDefinition(RedissonDelayedUtil.class));
            registry.registerBeanDefinition("redissonDelayedHandlerExecutor", new RootBeanDefinition(RedissonDelayedHandlerExecutor.class));
            registry.registerBeanDefinition("redissonDelayedRunner", new RootBeanDefinition(RedissonDelayedRunner.class));
            // 注册bean后置处理器
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(RedissonDelayedInitializerPostProcessor.class);
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                // We don't need this one to be post processed otherwise it can cause a
                // cascade of bean instantiation that we would rather avoid.
                beanDefinition.setSynthetic(true);
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
            }
        }
    }
    
    @Bean(value = "redissonDelayedProcessTaskExecutor", destroyMethod = "destroy")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 设置核心线程数量。若池中的实际线程数小于该值，无论其中是否有空闲的线程，都会产生新的线程
        executor.setCorePoolSize(1);
        
        // 设置最大线程数量
        executor.setMaxPoolSize(4);
        
        // 设置阻塞任务队列大小
        executor.setQueueCapacity(100);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("redisson-delayed-");
        
        // 设置线程池中任务的等待时间，若超过等待时间仍未销毁则强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        executor.setAwaitTerminationSeconds(5);
        
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
