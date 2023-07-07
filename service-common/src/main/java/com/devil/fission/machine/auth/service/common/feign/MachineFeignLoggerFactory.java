package com.devil.fission.machine.auth.service.common.feign;

import feign.Logger;

/**
 * feign日志工厂.
 *
 * @author Devil
 * @date Created in 2023/1/16 11:43
 */
public class MachineFeignLoggerFactory implements org.springframework.cloud.openfeign.FeignLoggerFactory {
    
    @Override
    public Logger create(Class<?> type) {
        return new MachineFeignLogger(type);
    }
}
