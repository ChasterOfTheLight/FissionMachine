package com.devil.fission.machine.example.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * NacosFlagService. 加入RefreshScope后会自动刷新bean
 *
 * @author Devil
 * @date Created in 2023/8/28 14:38
 */
@Slf4j
@RefreshScope
@Service
public class NacosFlagService {
    
    @Value("${example.configFlag:void}")
    private String configFlag;
    
    private final Environment environment;
    
    public NacosFlagService(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * nacos配置刷新实验.
     */
    public String flag() {
        log.info(environment.getProperty("example.configFlag"));
        return configFlag;
    }
    
}
