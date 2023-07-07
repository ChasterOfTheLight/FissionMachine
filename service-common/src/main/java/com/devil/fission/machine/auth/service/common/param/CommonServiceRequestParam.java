package com.devil.fission.machine.auth.service.common.param;

import io.swagger.annotations.ApiModelProperty;

/**
 * service的普通请求参数体.
 *
 * @author devil
 * @date Created in 2022/4/6 16:18
 */
public class CommonServiceRequestParam<T> extends BaseRequestParam<T> {
    
    @ApiModelProperty(value = "用户信息")
    private UserInfo userInfo;
    
    public CommonServiceRequestParam() {
    }
    
    public CommonServiceRequestParam(ClientInfo clientInfo, DeviceInfo deviceInfo, T requestInfo, UserInfo userInfo) {
        super(clientInfo, deviceInfo, requestInfo);
        this.userInfo = userInfo;
    }
    
    /**
     * the userInfo.
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    /**
     * userInfo : the userInfo to set.
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
