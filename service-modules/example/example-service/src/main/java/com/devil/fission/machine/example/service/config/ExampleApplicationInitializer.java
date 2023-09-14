package com.devil.fission.machine.example.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * ExampleApplicationInitializer.
 *
 * @author Devil
 * @date Created in 2023/9/11 17:52
 */
@Slf4j
public class ExampleApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("ExampleApplicationInitializer initialized");
    }
}
