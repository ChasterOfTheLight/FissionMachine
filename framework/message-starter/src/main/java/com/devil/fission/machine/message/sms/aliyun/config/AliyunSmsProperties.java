package com.devil.fission.machine.message.sms.aliyun.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云短信配置.
 *
 * @author Devil
 * @date Created in 2024/6/26 上午10:55
 */
@Data
@ConfigurationProperties(prefix = "message.sms.aliyun")
public class AliyunSmsProperties {
    
    /**
     * 访问域名.
     */
    private String endpoint;
    
    /**
     * 访问密钥ID.
     */
    private String accessKeyId;
    
    /**
     * 访问密钥.
     */
    private String accessKeySecret;
    
}
