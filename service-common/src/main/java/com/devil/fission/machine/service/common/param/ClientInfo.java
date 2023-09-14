package com.devil.fission.machine.service.common.param;

import io.swagger.annotations.ApiModelProperty;

/**
 * 客户端信息.
 *
 * @author devil
 * @date Created in 2022/4/6 16:21
 */
public class ClientInfo {
    
    @ApiModelProperty(value = "平台类型 eg. 苹果：APP_IOS,安卓：APP_ANDROID,运营后台：ADMIN,企业后台：BUSINESS_ADMIN,H5:H5")
    private String platform;
    
    @ApiModelProperty(value = "版本")
    private String clientVersion;
    
    @ApiModelProperty(value = "版本码")
    private String clientVersionCode;
    
    public ClientInfo() {
    }
    
    public ClientInfo(String platform, String clientVersion, String clientVersionCode) {
        this.platform = platform;
        this.clientVersion = clientVersion;
        this.clientVersionCode = clientVersionCode;
    }
    
    /**
     * the platform.
     */
    public String getPlatform() {
        return platform;
    }
    
    /**
     * platform : the platform to set.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /**
     * the clientVersion.
     */
    public String getClientVersion() {
        return clientVersion;
    }
    
    /**
     * clientVersion : the clientVersion to set.
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
    
    /**
     * the clientVersionCode.
     */
    public String getClientVersionCode() {
        return clientVersionCode;
    }
    
    /**
     * clientVersionCode : the clientVersionCode to set.
     */
    public void setClientVersionCode(String clientVersionCode) {
        this.clientVersionCode = clientVersionCode;
    }
}
