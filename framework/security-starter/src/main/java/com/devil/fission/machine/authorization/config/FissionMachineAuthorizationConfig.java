package com.devil.fission.machine.authorization.config;

import com.devil.fission.machine.authorization.interceptor.ApiScopeInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 天九电商授权配置.
 *
 * @author Devil
 * @date Created in 2023/3/6 16:02
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class FissionMachineAuthorizationConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiScopeInterceptor()).addPathPatterns("/**");
    }
    
}
