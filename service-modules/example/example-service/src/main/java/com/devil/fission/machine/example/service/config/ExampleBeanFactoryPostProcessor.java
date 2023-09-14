package com.devil.fission.machine.example.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * ExampleBeanFactoryPostProcessor.
 *
 * @author Devil
 * @date Created in 2023/9/11 15:41
 */
@Slf4j
@Component
public class ExampleBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("ExampleBeanFactoryPostProcessor handled");
    }
}
