package com.devil.fission.machine.auth.service.common.param;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 基础请求参数体.
 *
 * @author devil
 * @date Created in 2022/4/6 16:17
 */
public abstract class BaseRequestParam<T> {
    
    @ApiModelProperty(value = "客户端信息")
    protected ClientInfo clientInfo;
    
    @ApiModelProperty(value = "设备信息")
    protected DeviceInfo deviceInfo;
    
    @Valid
    @NotNull(message = "业务请求信息不能为空")
    @ApiModelProperty(value = "业务请求信息", required = true)
    protected T requestInfo;
    
    public BaseRequestParam() {
    }
    
    public BaseRequestParam(ClientInfo clientInfo, DeviceInfo deviceInfo, T requestInfo) {
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
        this.requestInfo = requestInfo;
    }
    
    /**
     * the clientInfo.
     */
    public ClientInfo getClientInfo() {
        return clientInfo;
    }
    
    /**
     * clientInfo : the clientInfo to set.
     */
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }
    
    /**
     * the deviceInfo.
     */
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
    
    /**
     * deviceInfo : the deviceInfo to set.
     */
    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    /**
     * the requestInfo.
     */
    public T getRequestInfo() {
        return requestInfo;
    }
    
    /**
     * requestInfo : the requestInfo to set.
     */
    public void setRequestInfo(T requestInfo) {
        this.requestInfo = requestInfo;
    }
}
