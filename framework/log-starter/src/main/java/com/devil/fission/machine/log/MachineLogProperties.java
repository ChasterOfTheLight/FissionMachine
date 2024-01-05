package com.devil.fission.machine.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置属性.
 *
 * @author Devil
 * @date Created in 2023/12/25 11:18
 */
@ConfigurationProperties(prefix = "fission.machine.log")
public class MachineLogProperties {
    
    /**
     * 日志级别.
     */
    private String level;
    
    /**
     * get the level .
     */
    public String getLevel() {
        return level;
    }
    
    /**
     * the level to set.
     */
    public void setLevel(String level) {
        this.level = level;
    }
}
