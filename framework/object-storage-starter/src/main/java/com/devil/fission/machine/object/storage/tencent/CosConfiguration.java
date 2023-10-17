package com.devil.fission.machine.object.storage.tencent;

import com.devil.fission.machine.object.storage.util.LogUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 腾讯云cos配置.
 *
 * @author devil
 * @date Created in 2022/4/26 11:35
 */
@ConditionalOnProperty(value = "object.storage.tencent.cos.enable", havingValue = "true")
@EnableConfigurationProperties(CosProperties.class)
public class CosConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "object.storage.tencent.cos", value = {"secret-id", "secret-key"})
    public CosStorageServiceImpl tencentStorage(CosProperties properties) {
        COSCredentials cred = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
        Region region = new Region(properties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(properties.getHttpProtocol());
        clientConfig.setEndpointBuilder(
                new CosEndpointBuilder(properties.getGeneralApi(), properties.getServiceApi(), clientConfig.getEndpointBuilder()));
        COSClient cosClient = new COSClient(cred, clientConfig);
        LogUtils.TENCENT_LOG.info("Tencent Cos Client init success");
        return new CosStorageServiceImpl(cosClient, properties);
    }
}
