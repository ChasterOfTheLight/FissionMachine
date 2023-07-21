package com.devil.fission.machine.example.api.client;

import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.api.dto.SysUserDto;
import com.devil.fission.machine.example.api.vo.SysUserQueryVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户接口.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
public interface SysUserClient {
    
    /**
     * 查询用户详情.
     *
     * @param dto 查询dto
     * @return 数据分页集合
     */
    @PostMapping("/sysUser/web/info")
    Response<SysUserQueryVo> info(@RequestBody(required = false) SysUserDto dto);
    
}

