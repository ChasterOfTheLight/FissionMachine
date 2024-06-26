package com.devil.fission.machine.message.sms.emay.config;

import com.devil.fission.machine.message.sms.emay.EmaySmsMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 亿美短信配置.
 *
 * @author Devil
 * @date Created in 2024/6/25 下午5:54
 */
@ConditionalOnProperty(prefix = "message.sms.emay", value = {"host", "appId", "secretKey"})
@EnableConfigurationProperties(EmaySmsProperties.class)
public class EmaySmsConfiguration {
    
    @Bean
    public EmaySmsMessageService emaySmsMessageService(EmaySmsProperties emaySmsProperties) {
        return new EmaySmsMessageService(emaySmsProperties);
    }
    
}
