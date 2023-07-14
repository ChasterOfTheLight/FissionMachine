package com.devil.fission.machine.example.service.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 运营用户表 更新数据请求参数.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表更新数据请求参数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserUpdateParam {
    
    /**
     * 用户ID.
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    
    /**
     * 用户账号.
     */
    @NotBlank(message = "用户账号不能为空")
    @ApiModelProperty(value = "用户账号", required = true)
    private String userName;
    
    /**
     * 密码.
     */
    @ApiModelProperty(value = "密码")
    private String userPassword;
    
    /**
     * 是否启用 1是0 否；默认1.
     */
    @ApiModelProperty(value = "是否启用 1是0 否；默认1")
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