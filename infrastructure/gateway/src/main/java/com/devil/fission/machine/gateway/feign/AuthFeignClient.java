package com.devil.fission.machine.gateway.feign;

import com.devil.fission.machine.auth.api.AuthConstants;
import com.devil.fission.machine.auth.api.client.AuthClient;
import com.devil.fission.machine.gateway.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * auth feign client.
 *
 * @author Devil
 * @date Created in 2022/12/29 11:30
 */
@FeignClient(contextId = "authFeignClient", name = AuthConstants.SERVICE_REGISTER_NAME, configuration = FeignConfig.class)
public interface AuthFeignClient extends AuthClient {

}
