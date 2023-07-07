package com.devil.fission.machine.auth.service.common.config;

import com.devil.fission.machine.auth.service.common.feign.MachineFeignExceptionDecoder;
import com.devil.fission.machine.auth.service.common.feign.MachineFeignFormatter;
import com.devil.fission.machine.auth.service.common.feign.MachineFeignInterceptor;
import com.devil.fission.machine.auth.service.common.feign.MachineFeignLoggerFactory;
import feign.codec.ErrorDecoder;
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
    public MachineFeignFormatter feignFormatter() {
        return new MachineFeignFormatter();
    }
    
    @Bean
    public org.springframework.cloud.openfeign.FeignLoggerFactory feignLoggerFactory() {
        return new MachineFeignLoggerFactory();
    }
    
    @Bean
    public MachineFeignInterceptor tojoyMallFeignInterceptor() {
        return new MachineFeignInterceptor();
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new MachineFeignExceptionDecoder();
    }
    
}
