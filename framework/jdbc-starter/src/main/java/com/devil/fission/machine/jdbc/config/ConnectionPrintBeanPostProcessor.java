package com.devil.fission.machine.jdbc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * ConnectionPrintBeanPostProcessor.
 *
 * @author Devil
 * @date Created in 2023/11/15 18:43
 */
public class ConnectionPrintBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {
    
    private final Logger log = LoggerFactory.getLogger(ConnectionPrintBeanPostProcessor.class);
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource) {
            HikariDataSource dataSource = (HikariDataSource) bean;
            log.info("DataSource Url: {} ", dataSource.getJdbcUrl());
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
    
    @Override
    public int getOrder() {
        // 比`ConfigurationPropertiesBindingPostProcessor`执行晚
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
