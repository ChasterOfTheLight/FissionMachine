package com.devil.fission.machine.message.sms.emay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 亿美短信配置.
 *
 * @author Devil
 * @date Created in 2024/6/25 下午5:57
 */
@Data
@ConfigurationProperties(prefix = "message.sms.emay")
public class EmaySmsProperties {
    
    /**
     * 访问域名.
     */
    private String host;
    
    /**
     * 访问appId.
     */
    private String appId;
    
    /**
     * 访问secretKey.
     */
    private String secretKey;
    
}
