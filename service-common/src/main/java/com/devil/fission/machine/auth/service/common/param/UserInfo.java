package com.devil.fission.machine.auth.service.common.param;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 用户信息.
 *
 * @author Devil
 * @date Created in 2019-03-27 19:14
 */
public class UserInfo implements Serializable {
    
    @ApiModelProperty(value = "用户id")
    private String userId;
    
    @ApiModelProperty(value = "用户账号|手机号")
    private String userName;
    
    public UserInfo() {
    }
    
    public UserInfo(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
    
    /**
     * the userId.
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * userId : the userId to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * the userName.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * userName : the userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
