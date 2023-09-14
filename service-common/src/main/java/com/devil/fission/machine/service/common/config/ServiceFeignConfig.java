package com.devil.fission.machine.service.common.config;

import com.devil.fission.machine.service.common.feign.MachineFeignExceptionDecoder;
import com.devil.fission.machine.service.common.feign.MachineFeignFormatter;
import com.devil.fission.machine.service.common.feign.MachineFeignInterceptor;
import com.devil.fission.machine.service.common.feign.MachineFeignLoggerFactory;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ServiceFeignConfig.
 *
 * @author Devil
 * @date Created in 2023/2/20 9:32
 */
@Configuration(proxyBeanMethods = false)
public class ServiceFeignConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public MachineFeignFormatter feignFormatter() {
        return new MachineFeignFormatter();
    }
    
    @Bean
    public FeignLoggerFactory feignLoggerFactory() {
        return new MachineFeignLoggerFactory();
    }
    
    @Bean
    public MachineFeignInterceptor machineFeignInterceptor() {
        return new MachineFeignInterceptor();
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new MachineFeignExceptionDecoder();
    }
    
}
