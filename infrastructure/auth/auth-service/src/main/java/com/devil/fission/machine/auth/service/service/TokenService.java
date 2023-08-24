package com.devil.fission.machine.auth.service.service;

import cn.hutool.core.util.IdUtil;
import com.devil.fission.machine.auth.api.dto.TokenDto;
import com.devil.fission.machine.auth.api.dto.VerifyTokenDto;
import com.devil.fission.machine.auth.api.AuthConstants;
import com.devil.fission.machine.auth.service.entity.TokenEntity;
import com.devil.fission.machine.auth.service.util.JwtUtils;
import com.devil.fission.machine.common.Constants;
import com.devil.fission.machine.common.enums.PlatformEnum;
import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.redis.service.RedisService;
import com.google.gson.Gson;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * token服务.
 *
 * @author devil
 * @date Created in 2022/3/24 13:43
 */
@Service
public class TokenService {
    
    private final RedisService redisService;
    
    private final RedissonClient redissonClient;
    
    private final String tokenPrefix = "FissionTokens:";
    
    private final String lockPrefix = "Lock:";
    
    private final String refreshTokenPrefix = "RefreshTokens:";
    
    private final String tokenWhileListPrefix = "TokenWhiteList:";
    
    public TokenService(RedisService redisService, RedissonClient redissonClient) {
        this.redisService = redisService;
        this.redissonClient = redissonClient;
    }
    
    /**
     * 生成token.
     *
     * @param tokenEntity       token实体
     * @param expireTimeSeconds 过期时间（秒）
     */
    public TokenDto generateToken(TokenEntity tokenEntity, long expireTimeSeconds) {
        if (StringUtils.isEmpty(tokenEntity.getUserId()) || StringUtils.isEmpty(tokenEntity.getUserName())
                || tokenEntity.getLoginPlatform() == null) {
            throw new ServiceException(ResponseCode.BAD_REQUEST.getCode(), "获取token出错，参数有误！");
        }
        
        String tokenKey = tokenEntity.getUserId();
        boolean isKicked = tokenEntity.getLoginPlatform().isKick();
        if (!isKicked) {
            // 如果不是可被踢，用userId + uuid作为token key
            tokenKey = tokenKey + Constants.SEPARATOR + IdUtil.fastUUID();
        }
        tokenEntity.setTokenKey(tokenKey);
        // 存储信息(存储信息越多，token长度越长，因为http请求头默认大小问题，一般4K/8K，存储信息不要太多)
        Map<String, Object> claimsMap = new HashMap<>(16);
        claimsMap.put(AuthConstants.AUTH_TOKEN_KEY, tokenKey);
        long currentTimeMillis = System.currentTimeMillis();
        // 利用RestControllerAspect处理，不再token的payload中单独定义
        claimsMap.put(AuthConstants.AUTH_TOKEN_CREATE_TIME, currentTimeMillis);
        claimsMap.put(AuthConstants.AUTH_TOKEN_EXPIRE_SECONDS, String.valueOf(expireTimeSeconds));
        claimsMap.put(AuthConstants.AUTH_USER_ID, tokenEntity.getUserId());
        claimsMap.put(AuthConstants.AUTH_USER_NAME, tokenEntity.getUserName());
        claimsMap.put(AuthConstants.AUTH_USER_PLATFORM, tokenEntity.getLoginPlatform());
        String token = JwtUtils.createToken(claimsMap, JwtUtils.JWT_DEFAULT_TTL * 1000L);
        tokenEntity.setToken(token);
        // 存储时间
        tokenEntity.setLoginTime(currentTimeMillis);
        long cacheExpiredTime = expireTimeSeconds * 1000L;
        long tokenExpiredTime = tokenEntity.getLoginTime() + cacheExpiredTime;
        tokenEntity.setExpireTime(tokenExpiredTime);
        // 加锁缓存token
        RLock lock = redissonClient.getLock(lockPrefix + tokenEntity.getLoginPlatform().name() + Constants.SEPARATOR + tokenEntity.getUserId());
        try {
            boolean locked = lock.tryLock(100L, 1000L, TimeUnit.MILLISECONDS);
            if (locked) {
                String cacheKey = buildCacheKey(tokenKey, tokenEntity.getLoginPlatform().name());
                redisService.setEx(cacheKey, new Gson().toJson(tokenEntity), cacheExpiredTime, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw new ServiceException("获取token出错，请重试！");
        } finally {
            lock.unlock();
        }
        // 封装dto
        TokenDto dto = TokenDto.builder().build();
        dto.setAccessToken(token);
        dto.setExpiresIn(tokenExpiredTime);
        return dto;
    }
    
    /**
     * 校验token.
     */
    public Response<VerifyTokenDto> verifyToken(String requestToken) {
        VerifyTokenDto verifyTokenDto = VerifyTokenDto.builder().build();
        if (StringUtils.isNotEmpty(requestToken)) {
            // 先不校验jwt有效性，先前置判断是否存在并发
            String tokenKey = JwtUtils.getTokenKey(requestToken);
            String userId = JwtUtils.getUserId(requestToken);
            String userName = JwtUtils.getUserName(requestToken);
            String platform = JwtUtils.getPlatform(requestToken);
            // 先填充返回值
            verifyTokenDto.setUserId(userId);
            verifyTokenDto.setUserName(userName);
            PlatformEnum platformEnum = PlatformEnum.getByValue(platform);
            verifyTokenDto.setPlatform(platformEnum);
            
            // 查询缓存，看是否有分布式session
            String tokenEntityInRedis = redisService.getAsString(tokenPrefix + platform + Constants.SEPARATOR + tokenKey);
            if (tokenEntityInRedis == null) {
                // 刷新token如果有并发，可能会有旧token的请求，但此时已经更换新token，导致旧token在缓存中找不到，需要放行这部分请求
                if (!redisService.hasKey(lockPrefix + refreshTokenPrefix + platform + Constants.SEPARATOR + tokenKey)) {
                    return Response.other(ResponseCode.UN_AUTHORIZED, "登录状态已过期,请重新登录!", null);
                }
                return Response.success(verifyTokenDto);
            }
            
            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(userName)) {
                return Response.other(ResponseCode.UN_AUTHORIZED, "token信息有误!", null);
            }
            
            try {
                JwtUtils.parseToken(requestToken);
            } catch (Exception e) {
                // 区分是否刷新token
                boolean isRefreshToken = platformEnum.isRefreshToken();
                if (isRefreshToken) {
                    // 在刷新token白名单中直接处理
                    String whiteListToken = redisService.getAsString(tokenWhileListPrefix + platform + Constants.SEPARATOR + tokenKey);
                    if (StringUtils.isNotEmpty(whiteListToken) && whiteListToken.equals(requestToken)) {
                        return Response.success(verifyTokenDto);
                    }
                    TokenEntity tokenEntity = new Gson().fromJson(tokenEntityInRedis, TokenEntity.class);
                    // token校验失败，有可能只是jwt过期，但redis中未过期，这时需要刷新token
                    if (requestToken.equals(tokenEntity.getToken())) {
                        String newToken = refreshToken(requestToken);
                        verifyTokenDto.setNewToken(newToken);
                    } else {
                        // 二次校验(放行更换token时的并发请求)
                        if (redisService.hasKey(lockPrefix + refreshTokenPrefix + tokenKey)) {
                            return Response.success(verifyTokenDto);
                        }
                        // 如果已过期，需要使用服务器token
                        verifyTokenDto.setNewToken(tokenEntity.getToken());
                    }
                    // 刷新token返回
                    return Response.other(ResponseCode.REFRESH_TOKEN, verifyTokenDto);
                } else {
                    return Response.other(ResponseCode.UN_AUTHORIZED, null);
                }
            }
            
            TokenEntity tokenEntity = new Gson().fromJson(tokenEntityInRedis, TokenEntity.class);
            // token没问题，但需要处理假token
            if (requestToken.equals(tokenEntity.getToken())) {
                return Response.success(verifyTokenDto);
            }
            return Response.other(ResponseCode.UN_AUTHORIZED, null);
        }
        // 要求业务方在账户状态变更时及时删除相应token
        return Response.other(ResponseCode.UN_AUTHORIZED, "token不能为空!", null);
    }
    
    /**
     * 根据旧token上锁获取新token.
     */
    private String refreshToken(String requestToken) {
        String tokenKey = JwtUtils.getTokenKey(requestToken);
        String userId = JwtUtils.getUserId(requestToken);
        String userName = JwtUtils.getUserName(requestToken);
        String platform = JwtUtils.getPlatform(requestToken);
        String expireSeconds = JwtUtils.getExpireSeconds(requestToken);
        
        Map<String, Object> claimsMap = new HashMap<>(16);
        claimsMap.put(AuthConstants.AUTH_TOKEN_KEY, tokenKey);
        long currentTimeMillis = System.currentTimeMillis();
        claimsMap.put(AuthConstants.AUTH_TOKEN_CREATE_TIME, currentTimeMillis);
        claimsMap.put(AuthConstants.AUTH_TOKEN_EXPIRE_SECONDS, expireSeconds);
        claimsMap.put(AuthConstants.AUTH_USER_ID, userId);
        claimsMap.put(AuthConstants.AUTH_USER_NAME, userName);
        claimsMap.put(AuthConstants.AUTH_USER_PLATFORM, platform);
        // 颁发新token，先生成，因为可能耗时导致锁失效
        String newToken = JwtUtils.createToken(claimsMap, JwtUtils.JWT_DEFAULT_TTL * 1000L);
        // 防并发加锁
        RLock lock = redissonClient.getLock(lockPrefix + refreshTokenPrefix + platform + Constants.SEPARATOR + tokenKey);
        try {
            boolean locked = lock.tryLock(100L, 1000L, TimeUnit.MILLISECONDS);
            if (locked) {
                TokenEntity tokenEntity = TokenEntity.builder().userId(userId).userName(userName).loginPlatform(PlatformEnum.getByValue(platform))
                        .tokenKey(tokenKey).token(newToken).loginTime(currentTimeMillis).expireTime(Constants.FRONT_USER_TOKEN_TTL).build();
                // 存储时间
                tokenEntity.setLoginTime(currentTimeMillis);
                String cacheKey = buildCacheKey(tokenKey, platform);
                // 缓存新token
                long cacheExpiredTime = (expireSeconds != null ? Long.parseLong(expireSeconds) : 1000) * 1000L;
                long tokenExpiredTime = currentTimeMillis + cacheExpiredTime;
                tokenEntity.setExpireTime(tokenExpiredTime);
                redisService.setEx(cacheKey, new Gson().toJson(tokenEntity), cacheExpiredTime, TimeUnit.MILLISECONDS);
                // 设置2s白名单（2s内该用户的请求都放行，不用token做key，因为token可能膨胀很大，存在大key风险）
                redisService.setEx(tokenWhileListPrefix + platform + Constants.SEPARATOR + tokenKey, requestToken, 2000L, TimeUnit.MILLISECONDS);
                // 判断是否正常塞入
                if (!redisService.hasKey(cacheKey) || !redisService.hasKey(tokenWhileListPrefix + platform + Constants.SEPARATOR + tokenKey)) {
                    // 返回请求token重试
                    return requestToken;
                }
                return newToken;
            }
        } catch (InterruptedException e) {
            throw new ServiceException("刷新token出错，请重试！");
        } finally {
            lock.unlock();
        }
        return requestToken;
    }
    
    /**
     * 删除token.
     */
    public void deleteToken(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String tokenKey = JwtUtils.getTokenKey(token);
            String platform = JwtUtils.getPlatform(token);
            deleteToken(tokenKey, PlatformEnum.getByValue(platform));
        }
    }
    
    /**
     * 删除token.
     */
    public void deleteToken(TokenEntity tokenEntity) {
        if (tokenEntity != null) {
            deleteToken(tokenEntity.getUserId(), tokenEntity.getLoginPlatform());
        }
    }
    
    private void deleteToken(String tokenKey, PlatformEnum platformEnum) {
        if (StringUtils.isNotEmpty(tokenKey) && platformEnum != null) {
            String platform = platformEnum.name();
            // 支持互踢的直接删除就可以，不支持互踢的需要删除相同前缀token
            String cacheKey = buildCacheKey(tokenKey, platform);
            if (platformEnum.isKick()) {
                redisService.delete(cacheKey);
            } else {
                RKeys keys = redissonClient.getKeys();
                Iterable<String> keysByPattern = keys.getKeysByPattern(cacheKey + ":*");
                for (String key : keysByPattern) {
                    redisService.delete(key);
                }
            }
        }
    }
    
    /**
     * 拼装缓存key.
     */
    private String buildCacheKey(String tokenKey, String platform) {
        return tokenPrefix + platform + Constants.SEPARATOR + tokenKey;
    }
    
}