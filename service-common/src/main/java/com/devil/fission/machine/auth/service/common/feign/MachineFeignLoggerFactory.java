package com.devil.fission.machine.auth.service.common.feign;

import feign.Logger;
import org.springframework.cloud.openfeign.FeignLoggerFactory;

/**
 * feign日志工厂.
 *
 * @author Devil
 * @date Created in 2023/1/16 11:43
 */
public class MachineFeignLoggerFactory implements FeignLoggerFactory {
    
    @Override
    public Logger create(Class<?> type) {
        return new MachineFeignLogger(type);
    }
}
