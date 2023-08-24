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
    
    /**
     * 验证token.
     *
     * @param token token
     * @return 验证token结果
     */
    @PostMapping(value = "/verifyToken")
    Response<VerifyTokenDto> verifyToken(@RequestParam(value = "token") String token);
    
    /**
     * 生成token.
     *
     * @param loginParam 登录参数
     * @return token
     */
    @PostMapping(value = "/generateToken")
    Response<TokenDto> generateToken(@RequestBody LoginParam loginParam);
    
    /**
     * 删除token.
     *
     * @param logoutParam 登出参数
     * @return 是否登出成功
     */
    @PostMapping(value = "/deleteToken")
    Response<Boolean> deleteToken(@RequestBody LogoutParam logoutParam);
    
    /**
     * 校验sign.
     *
     * @param verifySignParam 校验sign参数
     * @return 校验sign结果
     */
    @PostMapping(value = "/verifySign")
    Response<VerifySignDto> verifySign(@RequestBody VerifySignParam verifySignParam);
    
}
