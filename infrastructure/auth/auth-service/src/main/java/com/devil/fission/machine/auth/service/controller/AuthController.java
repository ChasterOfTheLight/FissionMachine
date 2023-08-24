package com.devil.fission.machine.auth.service.controller;

import com.devil.fission.machine.auth.api.dto.TokenDto;
import com.devil.fission.machine.auth.api.dto.VerifySignDto;
import com.devil.fission.machine.auth.api.dto.VerifyTokenDto;
import com.devil.fission.machine.auth.api.param.LoginParam;
import com.devil.fission.machine.auth.api.param.LogoutParam;
import com.devil.fission.machine.auth.api.param.VerifySignParam;
import com.devil.fission.machine.auth.service.entity.TokenEntity;
import com.devil.fission.machine.auth.service.service.AuthService;
import com.devil.fission.machine.common.response.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器.
 *
 * @author devil
 * @date Created in 2022/12/27 13:54
 */
@RestController
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 验证token.
     *
     * @param token token
     * @return 验证token结果
     */
    @PostMapping(value = "/verifyToken")
    public Response<VerifyTokenDto> verifyToken(@RequestParam(value = "token") String token) {
        return authService.verifyToken(token);
    }
    
    /**
     * 生成token.
     *
     * @param loginParam 登录参数
     * @return token
     */
    @PostMapping(value = "/generateToken")
    public Response<TokenDto> generateToken(@Valid @RequestBody LoginParam loginParam) {
        TokenEntity tokenEntity = TokenEntity.builder().userId(loginParam.getUserId()).userName(loginParam.getUserName())
                .loginPlatform(loginParam.getLoginPlatform()).build();
        TokenDto tokenDto = authService.generateToken(tokenEntity, loginParam.getExpireTimeSeconds());
        return Response.success(tokenDto);
    }
    
    /**
     * 删除token.
     *
     * @param logoutParam 登出参数
     * @return 是否登出成功
     */
    @PostMapping(value = "/deleteToken")
    public Response<Boolean> deleteToken(@Valid @RequestBody LogoutParam logoutParam) {
        TokenEntity tokenEntity = TokenEntity.builder().userId(logoutParam.getUserId()).loginPlatform(logoutParam.getLoginPlatform()).build();
        return authService.deleteToken(tokenEntity);
    }
    
    /**
     * 校验sign.
     *
     * @param verifySignParam 校验sign参数
     * @return 校验sign结果
     */
    @PostMapping(value = "/verifySign")
    public Response<VerifySignDto> verifySign(@Valid @RequestBody VerifySignParam verifySignParam) {
        return authService.verifySign(verifySignParam.getAccessKey(), verifySignParam.getTimestamp(), verifySignParam.getNonce(),
                verifySignParam.getSign(), verifySignParam.getRequestUri());
    }
}
