package com.devil.fission.machine.example.service.feign;

import com.devil.fission.machine.auth.api.client.AuthClient;
import com.devil.fission.machine.example.service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * AuthFeignClient.
 *
 * @author Devil
 * @date Created in 2023/7/21 11:29
 */
@FeignClient(contextId = "authFeignClient", name = "auth-service", configuration = FeignConfig.class)
public interface AuthFeignClient extends AuthClient {

}
