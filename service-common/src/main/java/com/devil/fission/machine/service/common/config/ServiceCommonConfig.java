package com.devil.fission.machine.service.common.config;

import com.devil.fission.machine.service.common.filter.GzipFilter;
import com.devil.fission.machine.service.common.filter.MachineServletFilter;
import com.devil.fission.machine.service.common.filter.XssFilter;
import com.devil.fission.machine.service.common.support.MachineRestTemplateInterceptor;
import com.devil.fission.machine.service.common.support.RestControllerAspect;
import com.devil.fission.machine.service.common.support.ServiceAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

/**
 * 服务自动配置类.
 *
 * @author devil
 * @date Created in 2022/12/7 17:06
 */
@Configuration(proxyBeanMethods = false)
public class ServiceCommonConfig {
    
    @Bean
    public FilterRegistrationBean<Filter> gzipFilterRegistration(GzipFilter filter) {
        return createFilterRegistration(filter, 5);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<Filter> xssFilterRegistration(XssFilter filter) {
        return createFilterRegistration(filter, 6);
    }
    
    @Bean
    public FilterRegistrationBean<Filter> machineServletFilterRegistration(MachineServletFilter filter) {
        return createFilterRegistration(filter, 7);
    }
    
    private FilterRegistrationBean<Filter> createFilterRegistration(Filter filter, int order) {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>(filter);
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(order);
        return filterRegistrationBean;
    }
    
    @Bean
    public GzipFilter gzipFilter() {
        return new GzipFilter();
    }
    
    @Bean
    public XssFilter xssFilter() {
        return new XssFilter();
    }
    
    @Bean
    public MachineServletFilter machineServletFilter() {
        return new MachineServletFilter();
    }
    
    @Bean
    public RestControllerAspect restControllerAspect() {
        return new RestControllerAspect();
    }
    
    @Bean
    public ServiceAspect serviceAspect() {
        return new ServiceAspect();
    }
    
    @Autowired(required = false)
    @ConditionalOnBean(RestTemplate.class)
    public void machineRestTemplateInterceptor(RestTemplate restTemplate) {
        restTemplate.getInterceptors().add(new MachineRestTemplateInterceptor());
    }
    
}
