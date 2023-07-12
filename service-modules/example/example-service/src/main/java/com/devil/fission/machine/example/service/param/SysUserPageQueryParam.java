package com.devil.fission.machine.example.service.param;

import com.devil.fission.machine.common.param.CommonPageParam;
import io.swagger.annotations.ApiModel;

/**
 * 运营用户表 分页数据参数.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@ApiModel(value = "运营用户表 分页数据参数")
public class SysUserPageQueryParam extends CommonPageParam {
    
    public SysUserPageQueryParam() {
    }
}
