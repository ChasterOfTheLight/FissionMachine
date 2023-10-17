package com.devil.fission.machine.gateway.config;

import com.devil.fission.machine.gateway.support.FeignExceptionDecoder;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FeignConfig.
 *
 * @author devil
 * @date Created in 2023/8/24 16:28
 */
@Configuration
public class FeignConfig {
    
    @Bean
    Retryer feignRetryer() {
        // 不重试
        return Retryer.NEVER_RETRY;
    }
    
    @Bean
    ErrorDecoder errorDecoder() {
        return new FeignExceptionDecoder();
    }
    
}
