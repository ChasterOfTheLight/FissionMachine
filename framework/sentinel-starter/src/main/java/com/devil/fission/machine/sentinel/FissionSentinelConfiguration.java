package com.devil.fission.machine.sentinel;

import com.alibaba.cloud.sentinel.feign.SentinelFeign;
import com.alibaba.cloud.sentinel.feign.SentinelTargeterAspect;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import feign.Feign;
import feign.Feign.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * 云平台sentinel配置.
 *
 * @author Devil
 * @date Created in 2022/4/12 11:15
 */
@ConditionalOnClass({SphU.class, Feign.class})
public class FissionSentinelConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @Scope("prototype")
    public Builder feignSentinelBuilder() {
        return SentinelFeign.builder();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.cloud.openfeign.Targeter"})
    public SentinelTargeterAspect sentinelTargeterAspect() {
        return new SentinelTargeterAspect();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
    
}
