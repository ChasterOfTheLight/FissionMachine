package com.devil.fission.machine.service.common.health;

import com.devil.fission.machine.common.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 健康检查默认接口.
 *
 * @author Devil
 * @date Created in 2023/9/14 9:50
 */
@Slf4j
@ApiIgnore
@RestController
public class ServerHealthController {
    
    @GetMapping(value = "serverHealth")
    public Response<Boolean> serverHealth() {
        return Response.success(true);
    }
    
}
