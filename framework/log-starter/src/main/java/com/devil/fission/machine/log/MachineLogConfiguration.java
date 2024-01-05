package com.devil.fission.machine.log;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 日志配置.
 *
 * @author Devil
 * @date Created in 2023/12/25 11:08
 */
@Configuration
@EnableConfigurationProperties(MachineLogProperties.class)
public class MachineLogConfiguration {
    
    @Bean
    public MachineLogPropertiesLogbackPostProcessor machineLogPostProcessor(LoggingSystem loggingSystem) {
        return new MachineLogPropertiesLogbackPostProcessor(loggingSystem);
    }
    
}
