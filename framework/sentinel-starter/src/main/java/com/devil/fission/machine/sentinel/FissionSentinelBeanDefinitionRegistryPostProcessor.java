package com.devil.fission.machine.sentinel;

import com.alibaba.cloud.sentinel.SentinelConstants;
import com.devil.fission.machine.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * bean注册后置处理.
 *
 * @author Devil
 * @date Created in 2022/4/12 16:52
 */
public class FissionSentinelBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FissionSentinelBeanDefinitionRegistryPostProcessor.class);
    
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
    
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        try {
            BeanDefinition sentinelPropertyBeanDefinition = beanFactory.getBeanDefinition(
                    "spring.cloud.sentinel-com.alibaba.cloud.sentinel.SentinelProperties");
            MutablePropertyValues sentinelPropertyPropertyValues = sentinelPropertyBeanDefinition.getPropertyValues();
            // 提前心跳，让控制台看到数据
            sentinelPropertyPropertyValues.add("eager", "true");
            // 控制台地址 bean的实例化晚于bean工厂的后置，所以这时拿到的bean只是一个初始化后的bean，属性判断需要利用环境变量获取判断
            String dashboardKey = "transport.dashboard";
            if (StringUtils.isEmpty(environment.getProperty(SentinelConstants.PROPERTY_PREFIX + Constants.DOT + dashboardKey))) {
                sentinelPropertyPropertyValues.add(dashboardKey, "sentinel-dashboard");
            }
            LOGGER.info("register sentinelProperties，start sentinel config");
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.warn("没有找到sentinelProperties的bean,sentinel 开启失败");
        }
    }
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
