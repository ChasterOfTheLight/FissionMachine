package com.devil.fission.machine.example.service.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营用户表 详情请求参数.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表详情请求参数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserInfoQueryParam {
    
    /**
     * 用户ID.
     */
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    
    /**
     * content.
     */
    @ApiModelProperty(value = "content", required = true)
    private String content;
    
}