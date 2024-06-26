package com.devil.fission.machine.message.sms.tencent.config;

import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.message.sms.tencent.TencentSmsMessageService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云短信配置.
 *
 * @author Devil
 * @date Created in 2024/6/26 下午1:57
 */
@Configuration
@ConditionalOnProperty(prefix = "message.sms.tencent", value = {"endpoint", "sdkAppId", "regionId", "secretId", "secretKey"})
@EnableConfigurationProperties(TencentProperties.class)
public class TencentSmsConfiguration {
    
    @Bean
    public SmsClient client(TencentProperties tencentProperties) {
        Credential cred = new Credential(tencentProperties.getSecretId(), tencentProperties.getSecretKey());
        String endpoint = StringUtils.isEmpty(tencentProperties.getEndpoint()) ? "sms.tencentcloudapi.com" : tencentProperties.getEndpoint();
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = getHttpProfile(endpoint);
        
        /* 非必要步骤:
         * 实例化一个客户端配置对象，可以指定超时时间等配置 */
        ClientProfile clientProfile = new ClientProfile();
        /* SDK默认用TC3-HMAC-SHA256进行签名
         * 非必要请不要修改这个字段 */
        String signMethod = "HmacSHA256";
        clientProfile.setSignMethod(signMethod);
        clientProfile.setHttpProfile(httpProfile);
        /* 实例化要请求产品(以sms为例)的client对象
         * 第二个参数是地域信息，可以直接填写字符串ap-guangzhou，支持的地域列表参考 https://cloud.tencent.com/document/api/382/52071#.E5.9C.B0.E5.9F.9F.E5.88.97.E8.A1.A8 */
        String regionId = StringUtils.isEmpty(tencentProperties.getRegionId()) ? "ap-guangzhou" : tencentProperties.getRegionId();
        return new SmsClient(cred, regionId, clientProfile);
    }
    
    private static @NotNull HttpProfile getHttpProfile(String endpoint) {
        HttpProfile httpProfile = new HttpProfile();
        String httpMethod = "GET";
        httpProfile.setReqMethod(httpMethod);
        // 请求连接超时时间，单位为秒(默认60秒)
        httpProfile.setConnTimeout(10);
        // 设置写入超时时间，单位为秒(默认0秒)
        httpProfile.setWriteTimeout(10);
        // 设置读取超时时间，单位为秒(默认0秒)
        httpProfile.setReadTimeout(10);
        /* 指定接入地域域名，默认就近地域接入域名为 sms.tencentcloudapi.com ，也支持指定地域域名访问，例如广州地域的域名为 sms.ap-guangzhou.tencentcloudapi.com */
        httpProfile.setEndpoint(endpoint);
        return httpProfile;
    }
    
    @Bean
    public TencentSmsMessageService tencentSmsMessageService(TencentProperties tencentProperties, SmsClient client) {
        return new TencentSmsMessageService(tencentProperties, client);
    }
    
}
