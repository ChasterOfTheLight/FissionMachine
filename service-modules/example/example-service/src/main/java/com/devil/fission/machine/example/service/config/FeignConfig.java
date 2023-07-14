package com.devil.fission.machine.example.service.config;

import feign.Retryer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * feign config.
 *
 * @author devil
 * @date Created in 2022/12/12 14:11
 */
@Configuration
public class FeignConfig {
    
    @Bean
    @ConditionalOnMissingBean
    Retryer feignRetryer() {
        // 默认不重试
        return Retryer.NEVER_RETRY;
    }
}
