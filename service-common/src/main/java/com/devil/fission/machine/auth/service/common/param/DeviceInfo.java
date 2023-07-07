package com.devil.fission.machine.auth.service.common.param;

import io.swagger.annotations.ApiModelProperty;

/**
 * 客户端设备信息.
 *
 * @author devil
 * @date Created in 2022/4/6 16:21
 */
public class DeviceInfo {
    
    @ApiModelProperty(value = "操作系统类型 eg. ios windows")
    private String systemType;
    
    @ApiModelProperty(value = "操作系统版本 eg. ios12 win10")
    private String systemVersion;
    
    public DeviceInfo() {
    }
    
    public DeviceInfo(String systemType, String systemVersion) {
        this.systemType = systemType;
        this.systemVersion = systemVersion;
    }
    
    /**
     * the systemType.
     */
    public String getSystemType() {
        return systemType;
    }
    
    /**
     * systemType : the systemType to set.
     */
    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }
    
    /**
     * the systemVersion.
     */
    public String getSystemVersion() {
        return systemVersion;
    }
    
    /**
     * systemVersion : the systemVersion to set.
     */
    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }
}
