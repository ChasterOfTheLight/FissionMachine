package com.devil.fission.machine.example.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 运营用户表 响应实体.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表响应实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserQueryVo {
    
    /**
     * 用户ID.
     */
    @ApiModelProperty("用户ID")
    private Long userId;
    
    /**
     * 用户账号.
     */
    @ApiModelProperty("用户账号")
    private String userName;
    
    /**
     * 密码.
     */
    @ApiModelProperty("密码")
    private String userPassword;
    
    /**
     * 是否启用 1是0 否；默认1.
     */
    @ApiModelProperty("是否启用 1是0 否；默认1")
    private Integer isEnabled;
    
    /**
     * 最后登录IP.
     */
    @ApiModelProperty("最后登录IP")
    private String lastLoginIp;
    
    /**
     * 最后登录时间.
     */
    @ApiModelProperty("最后登录时间")
    private Date lastLoginDate;
    
    /**
     * 创建人ID.
     */
    @ApiModelProperty("创建人ID")
    private Long createdUserId;
    
    /**
     * 创建时间.
     */
    @ApiModelProperty("创建时间")
    private Date createdTime;
    
    /**
     * 修改人ID.
     */
    @ApiModelProperty("修改人ID")
    private Long updatedUserId;
    
    /**
     * 修改时间.
     */
    @ApiModelProperty("修改时间")
    private Date updatedTime;
    
}