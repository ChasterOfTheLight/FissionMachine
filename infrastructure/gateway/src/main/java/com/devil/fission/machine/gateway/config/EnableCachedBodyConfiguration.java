package com.devil.fission.machine.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 缓存路由.
 *
 * @author Devil
 * @date Created in 2024/9/30 17:27
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class EnableCachedBodyConfiguration {
    
    @Resource
    private ApplicationEventPublisher publisher;
    
    @Resource
    private GatewayProperties gatewayProperties;
    
    /**
     * 初始化.
     */
    @PostConstruct
    public void init() {
        gatewayProperties.getRoutes().forEach(routeDefinition -> {
            // 对 spring cloud gateway 路由配置中的每个路由都启用 AdaptCachedBodyGlobalFilter
            EnableBodyCachingEvent enableBodyCachingEvent = new EnableBodyCachingEvent(new Object(), routeDefinition.getId());
            publisher.publishEvent(enableBodyCachingEvent);
        });
    }

}
