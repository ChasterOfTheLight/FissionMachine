package com.devil.fission.machine.example.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

/**
 * NacosFlagService.
 * 加入RefreshScope后会自动刷新bean
 *
 * @author Devil
 * @date Created in 2023/8/28 14:38
 */
@RefreshScope
@Service
public class NacosFlagService {
    
    @Value("${example.configFlag:void}")
    private String configFlag;
    
    /**
     * nacos配置刷新实验.
     */
    public String flag() {
        return configFlag;
    }
    
}
