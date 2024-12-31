package com.devil.fission.machine.object.storage.minio;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Minio自动配置.
 *
 * @author Devil
 * @date Created in 2024/10/25 13:48
 */
@ConditionalOnProperty(value = "object.storage.minio.enable", havingValue = "true")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "object.storage.minio", value = {"accessKey", "secretKey"})
    public MinioStorageServiceImpl minioStorage(MinioProperties properties) {
        MinioClient minioClient = MinioClient.builder().endpoint(properties.getEndpoint()).credentials(properties.getAccessKey(), properties.getSecretKey()).build();
        return new MinioStorageServiceImpl(minioClient, properties);
    }
    
}
