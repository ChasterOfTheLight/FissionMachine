package com.devil.fission.machine.gateway.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
