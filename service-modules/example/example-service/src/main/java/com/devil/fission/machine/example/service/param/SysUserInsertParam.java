package com.devil.fission.machine.example.service.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 运营用户表 插入数据请求参数.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表插入数据请求参数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserInsertParam {
    
    /**
     * 用户账号.
     */
    @ApiModelProperty(value = "用户账号", required = true)
    private String userName;
    
    /**
     * 密码.
     */
    @ApiModelProperty(value = "密码", required = true)
    private String userPassword;
    
    /**
     * 是否启用 1是0 否；默认1.
     */
    @ApiModelProperty(value = "是否启用 1是0 否；默认1", required = true)
    private Integer isEnabled;
    
    /**
     * 最后登录IP.
     */
    @ApiModelProperty(value = "最后登录IP")
    private String lastLoginIp;
    
    /**
     * 最后登录时间.
     */
    @ApiModelProperty(value = "最后登录时间")
    private Date lastLoginDate;
    
    /**
     * 创建人ID.
     */
    @ApiModelProperty(value = "创建人ID")
    private Long createdUserId;
    
    /**
     * 创建时间.
     */
    @ApiModelProperty(value = "创建时间")
    private Date createdTime;
    
    /**
     * 修改人ID.
     */
    @ApiModelProperty(value = "修改人ID")
    private Long updatedUserId;
    
    /**
     * 修改时间.
     */
    @ApiModelProperty(value = "修改时间")
    private Date updatedTime;
    
}