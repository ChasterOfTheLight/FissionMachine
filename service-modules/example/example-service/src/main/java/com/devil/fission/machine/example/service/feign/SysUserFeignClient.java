package com.devil.fission.machine.example.service.feign;

import com.devil.fission.machine.example.api.ServiceConstant;
import com.devil.fission.machine.example.api.client.SysUserClient;
import com.devil.fission.machine.example.service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * SysUserFeignClient.
 *
 * @author Devil
 * @date Created in 2023/7/21 11:29
 */
@FeignClient(contextId = "sysUserFeignClient", name = ServiceConstant.SERVICE_NAME, configuration = FeignConfig.class)
public interface SysUserFeignClient extends SysUserClient {

}
