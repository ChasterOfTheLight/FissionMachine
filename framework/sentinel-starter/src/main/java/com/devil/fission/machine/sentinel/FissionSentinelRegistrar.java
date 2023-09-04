package com.devil.fission.machine.sentinel;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 云平台sentinel bean注册修改.
 *
 * @author Devil
 * @date Created in 2022/4/12 11:44
 */
public class FissionSentinelRegistrar implements ImportBeanDefinitionRegistrar {
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        // 注册Configuration
        registry.registerBeanDefinition("fissionSentinelConfiguration", new RootBeanDefinition(FissionSentinelConfiguration.class));
        registry.registerBeanDefinition("fissionSentinelBeanDefinitionRegistryPostProcessor",
                new RootBeanDefinition(FissionSentinelBeanDefinitionRegistryPostProcessor.class));
    }
}
