package com.devil.fission.machine.auth.service.service;

import com.devil.fission.machine.auth.api.dto.TokenDto;
import com.devil.fission.machine.auth.api.dto.VerifySignDto;
import com.devil.fission.machine.auth.api.dto.VerifyTokenDto;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.auth.service.entity.TokenEntity;
import org.springframework.stereotype.Service;

/**
 * 认证服务.
 *
 * @author Devil
 * @date Created in 2022/12/27 14:07
 */
@Service
public class AuthService {
    
    private final TokenService tokenService;
    
    private final SignService signService;
    
    public AuthService(TokenService tokenService, SignService signService) {
        this.tokenService = tokenService;
        this.signService = signService;
    }
    
    /**
     * 生成token.
     */
    public TokenDto generateToken(TokenEntity tokenEntity, long expireTimeSeconds) {
        return tokenService.generateToken(tokenEntity, expireTimeSeconds);
    }
    
    /**
     * 校验token.
     */
    public Response<VerifyTokenDto> verifyToken(String token) {
        return tokenService.verifyToken(token);
    }
    
    /**
     * 删除token.
     */
    public Response<Boolean> deleteToken(String token) {
        tokenService.deleteToken(token);
        return Response.success(true);
    }
    
    /**
     * 根据tokenEntity删除token.
     */
    public Response<Boolean> deleteToken(TokenEntity tokenEntity) {
        tokenService.deleteToken(tokenEntity);
        return Response.success(true);
    }
    
    /**
     * 校验sign.
     */
    public Response<VerifySignDto> verifySign(String accessKey, String timestamp, String nonce, String sign, String requestUri) {
        return signService.verifySign(accessKey, timestamp, nonce, sign, requestUri);
    }
    
}
