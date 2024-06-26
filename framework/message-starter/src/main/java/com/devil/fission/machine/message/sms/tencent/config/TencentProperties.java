package com.devil.fission.machine.message.sms.tencent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 腾讯云短信配置.
 *
 * @author Devil
 * @date Created in 2024/6/26 下午1:53
 */
@Data
@ConfigurationProperties(prefix = "message.sms.tencent")
public class TencentProperties {
    
    /**
     * 访问域名.
     */
    private String endpoint;
    
    /**
     * sdk appid.
     */
    private String sdkAppId;
    
    /**
     * 地域ID.
     */
    private String regionId;
    
    /**
     * 访问密钥ID.
     */
    private String secretId;
    
    /**
     * 访问密钥.
     */
    private String secretKey;

}
