package com.devil.fission.machine.message.sms.aliyun.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.message.sms.aliyun.AliyunSmsMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云sms配置.
 *
 * @author Devil
 * @date Created in 2024/6/26 上午11:09
 */
@Configuration
@ConditionalOnProperty(prefix = "message.sms.aliyun", value = {"endpoint", "regionId", "accessKeyId", "accessKeySecret"})
@EnableConfigurationProperties(AliyunSmsProperties.class)
public class AliyunSmsConfiguration {
    
    @Bean
    public Client client(AliyunSmsProperties aliyunSmsProperties) {
        Config config = new Config();
        config.setAccessKeyId(aliyunSmsProperties.getAccessKeyId());
        config.setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret());
        config.setEndpoint(StringUtils.isEmpty(aliyunSmsProperties.getEndpoint()) ? "dysmsapi.aliyuncs.com" : aliyunSmsProperties.getEndpoint());
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new ServiceException(ResponseCode.FAIL, "初始化阿里云sms失败", e);
        }
    }
    
    @Bean
    public AliyunSmsMessageService aliyunSmsMessageService(Client client) {
        return new AliyunSmsMessageService(client);
    }
    
}
