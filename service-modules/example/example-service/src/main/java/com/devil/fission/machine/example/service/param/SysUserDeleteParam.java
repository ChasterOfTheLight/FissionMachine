package com.devil.fission.machine.example.service.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 运营用户表 删除记录请求参数.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表删除记录请求参数")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserDeleteParam {
    
    /**
     * 用户ID.
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    
}