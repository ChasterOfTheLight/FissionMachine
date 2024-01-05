package com.devil.fission.machine.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * MachineLogPropertiesLogbackPostProcessor.
 *
 * @author Devil
 * @date Created in 2023/12/25 14:27
 */
public class MachineLogPropertiesLogbackPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    
    private static final Logger log = LoggerFactory.getLogger(MachineLogPropertiesLogbackPostProcessor.class);
    
    private final LoggingSystem loggingSystem;
    
    private ApplicationContext applicationContext;
    
    public MachineLogPropertiesLogbackPostProcessor(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MachineLogProperties) {
            MachineLogProperties machineLogProperties = (MachineLogProperties) bean;
            // 更新LoggerSystem的level
            if (machineLogProperties.getLevel() != null) {
                String levelProperty = machineLogProperties.getLevel().toUpperCase();
                log.info("fission machine log level: {}", levelProperty);
                // pre和pro环境不允许修改日志级别为DEBUG和TRACE
                String activeProfile = applicationContext.getEnvironment().getActiveProfiles()[0];
                boolean isPreOrPro = "pre".equals(activeProfile) || "pro".equals(activeProfile);
                boolean isDebugOrTrace = "DEBUG".equals(levelProperty) || "TRACE".equals(levelProperty);
                if (!isPreOrPro && !isDebugOrTrace) {
                    try {
                        LogLevel logLevel = LogLevel.valueOf(levelProperty);
                        loggingSystem.setLogLevel(Logger.ROOT_LOGGER_NAME, logLevel);
                    } catch (IllegalArgumentException ignore) {
                        // 忽略配置填写错误
                    }
                }
            }
        } return bean;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
