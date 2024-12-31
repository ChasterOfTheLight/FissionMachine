package com.devil.fission.machine.object.storage.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Minio配置.
 *
 * @author devil
 * @date Created in 2022/4/26 14:49
 */
@Data
@ConfigurationProperties(prefix = "object.storage.minio")
public class MinioProperties {

    private String accessKey;
    
    private String secretKey;
    
    private String endpoint;
    
    private String bucket;

}
