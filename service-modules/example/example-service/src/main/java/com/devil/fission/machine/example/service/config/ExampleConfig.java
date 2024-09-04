package com.devil.fission.machine.example.service.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置.
 *
 * @author Devil
 * @date Created in 2024/9/4 13:45
 */
@Configuration
public class ExampleConfig {
    
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

}
