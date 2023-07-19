package com.devil.fission.machine.object.storage.alibaba;

import com.aliyun.oss.common.comm.Protocol;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 云平台阿里云oss配置.
 *
 * @author devil
 * @date Created in 2022/4/26 14:49
 */
@ConfigurationProperties(prefix = "object.storage.alibaba.oss")
public class OssProperties {
    
    private String accessKeyId;
    
    private String secretAccessKey;
    
    private String securityToken;
    
    private String bucket;
    
    /**
     * 参考：https://help.aliyun.com/document_detail/31837.html.
     */
    private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    
    private Protocol protocol = Protocol.HTTPS;
    
    private Boolean supportCname;
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    public String getSecretAccessKey() {
        return secretAccessKey;
    }
    
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
    
    public String getSecurityToken() {
        return securityToken;
    }
    
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    
    public String getBucket() {
        return bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public Protocol getProtocol() {
        return protocol;
    }
    
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
    
    public Boolean getSupportCname() {
        return supportCname;
    }
    
    public void setSupportCname(Boolean supportCname) {
        this.supportCname = supportCname;
    }
}
