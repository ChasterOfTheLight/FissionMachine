package com.fission.machine.aliyun.rocketmq.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * AliRocketMqProperties.
 *
 * @author Devil
 * @date Created in 2024/8/19 下午3:22
 */
@Data
@ConfigurationProperties(prefix = "aliyun-rocketmq")
public class AliyunRocketMqProperties {
    
    /**
     * 访问key.
     */
    private String accessKey;
    
    /**
     * 访问密钥.
     */
    private String secretKey;
    
    /**
     * NameSrv地址.
     */
    private String nameSrvAddr;
    
    /**
     * 默认topic.
     */
    private String topic;
    
    /**
     * 默认group.
     */
    private String groupId;
    
    /**
     * 默认tag.
     */
    private String tag;
    
    /**
     * 顺序topic.
     */
    private String orderTopic;
    
    /**
     * 顺序group.
     */
    private String orderGroupId;
    
    /**
     * 顺序tag.
     */
    private String orderTag;
    
    public Properties getAliyunProperties() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }
    
}
