package com.devil.fission.machine.auth.api.client;

import com.devil.fission.machine.auth.api.dto.TokenDto;
import com.devil.fission.machine.auth.api.dto.VerifySignDto;
import com.devil.fission.machine.auth.api.dto.VerifyTokenDto;
import com.devil.fission.machine.auth.api.param.LoginParam;
import com.devil.fission.machine.auth.api.param.LogoutParam;
import com.devil.fission.machine.auth.api.param.VerifySignParam;
import com.devil.fission.machine.common.response.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * auth接口.
 *
 * @author Devil
 * @date Created in 2022/12/29 11:21
 */
public interface AuthClient {
    
    @PostMapping(value = "/verifyToken")
    Response<VerifyTokenDto> verifyToken(@RequestParam(value = "token") String token);
    
    @PostMapping(value = "/generateToken")
    Response<TokenDto> generateToken(@RequestBody LoginParam loginParam);
    
    @PostMapping(value = "/deleteToken")
    Response<Boolean> deleteToken(@RequestBody LogoutParam logoutParam);
    
    @PostMapping(value = "/verifySign")
    Response<VerifySignDto> verifySign(@RequestBody VerifySignParam verifySignParam);
    
}
