package com.devil.fission.machine.redis.antirepeat.config;

import com.devil.fission.machine.redis.antirepeat.aop.AntiRepeatAop;
import com.devil.fission.machine.redis.antirepeat.lock.AntiRepeatLock;
import com.devil.fission.machine.redis.antirepeat.lock.RedissonAntiRepeatLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 防重复提交配置.
 *
 * @author devil
 * @date Created in 2024/6/17 上午10:24
 */
@Slf4j
@Configuration
public class AntiRepeatConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(AntiRepeatLock.class)
    public RedissonAntiRepeatLock redissonAntiRepeatLock(RedissonClient redissonClient) {
        log.info("RedissonAntiRepeatLock init");
        return new RedissonAntiRepeatLock(redissonClient);
    }
    
    @Bean
    public AntiRepeatAop antiRepeatAop() {
        log.info("AntiRepeatAop init");
        return new AntiRepeatAop();
    }
    
}
