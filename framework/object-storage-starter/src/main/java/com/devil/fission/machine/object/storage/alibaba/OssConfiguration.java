package com.devil.fission.machine.object.storage.alibaba;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.devil.fission.machine.object.storage.util.LogUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 阿里云oss自动配置.
 *
 * @author devil
 * @date Created in 2022/4/26 14:49
 */
@ConditionalOnProperty(value = "object.storage.alibaba.oss.enable", havingValue = "true")
@EnableConfigurationProperties(OssProperties.class)
public class OssConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "object.storage.alibaba.oss", value = {"access-key-id", "secret-access-key"})
    public OssStorageServiceImpl alibabaStorage(OssProperties properties) {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        if (properties.getProtocol() != null) {
            conf.setProtocol(properties.getProtocol());
        }
        if (properties.getSupportCname() != null) {
            conf.setSupportCname(properties.getSupportCname());
        }
        
        OSS ossClient = new OSSClientBuilder().build(properties.getEndpoint(), properties.getAccessKeyId(), properties.getSecretAccessKey(), conf);
        LogUtils.ALIBABA_LOG.info("Alibaba Oss Client init success");
        return new OssStorageServiceImpl(ossClient, properties);
    }
}
