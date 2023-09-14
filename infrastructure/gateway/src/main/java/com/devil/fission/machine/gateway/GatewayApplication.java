package com.devil.fission.machine.gateway;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.stream.Collectors;

/**
 * GatewayApplication.
 *
 * @author devil
 * @date Created in 2023/8/24 16:28
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.devil.fission.machine.gateway.rpc"})
@EnableDiscoveryClient
public class GatewayApplication {
    
    public static void main(String[] args) {
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(GatewayApplication.class);
        springApplicationBuilder.run(args);
    }
    
    /**
     * 使用feign调用接口 需要一个转换器 reactive的application默认不加载HttpMessageConvertersAutoConfiguration.
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
    
}
