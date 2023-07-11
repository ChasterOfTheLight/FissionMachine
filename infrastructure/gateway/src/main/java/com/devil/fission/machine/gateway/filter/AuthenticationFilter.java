package com.devil.fission.machine.gateway.filter;

import com.devil.fission.machine.auth.api.AuthConstants;
import com.devil.fission.machine.auth.api.dto.VerifySignDto;
import com.devil.fission.machine.auth.api.dto.VerifyTokenDto;
import com.devil.fission.machine.auth.api.param.VerifySignParam;
import com.devil.fission.machine.common.Constants;
import com.devil.fission.machine.common.enums.PlatformEnum;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.gateway.config.GatewayConfiguration;
import com.devil.fission.machine.gateway.feign.AuthFeignClient;
import com.devil.fission.machine.gateway.util.IpUtils;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证过滤器.
 *
 * @author devil
 * @date Created in 2022/12/26 14:35
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    private final GatewayConfiguration gatewayConfiguration;
    
    private final AuthFeignClient authFeignClient;
    
    public AuthenticationFilter(GatewayConfiguration gatewayConfiguration, AuthFeignClient authFeignClient) {
        this.gatewayConfiguration = gatewayConfiguration;
        this.authFeignClient = authFeignClient;
    }
    
    @Override
    @Trace
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        URI requestUri = serverHttpRequest.getURI();
        String ip = null;
        try {
            ip = IpUtils.getIpAddress(serverHttpRequest);
        } catch (Exception e) {
            LOGGER.error("获取访问ip错误 {}", e.getMessage(), e);
        }
        
        String path = requestUri.getPath();
        // 在黑名单中，403
        if (gatewayConfiguration.getIpBlackList() != null && gatewayConfiguration.getIpBlackList().contains(ip)) {
            serverHttpResponse.setStatusCode(HttpStatus.FORBIDDEN);
            return unAuthorizedResponse(serverHttpResponse, Response.other(ResponseCode.FORBIDDEN), path);
        }
        
        // 判断是否包含系统请求头，请求时如果携带非法
        HttpHeaders requestHeaders = serverHttpRequest.getHeaders();
        if (requestHeaders.containsKey(Constants.HEADER_SERVICE_MARK) || requestHeaders.containsKey(Constants.HEADER_SW_ID)) {
            return unAuthorizedResponse(serverHttpResponse, Response.other(ResponseCode.FORBIDDEN), path);
        }
        
        ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
        // 添加请求头：ip，方便下游服务处理
        addRequestHeader(mutate, Constants.HEADER_REQUEST_IP, ip);
        // 添加请求头：来源，方便下游服务处理
        addRequestHeader(mutate, Constants.HEADER_SERVICE_MARK, "fission-machine");
        // 添加响应头：调用链id返给前端
        addResponseHeader(serverHttpResponse, Constants.HEADER_SW_ID, TraceContext.traceId());
        
        // 在uri白名单中，放行 在白名单会忽略token校验，拿不到token基本信息，这时候建议通过参数传递，而不是请求头传递
        if (gatewayConfiguration.getUriWhitelist() != null) {
            PathMatcher pathMatcher = new AntPathMatcher();
            for (String whiteUri : gatewayConfiguration.getUriWhitelist()) {
                // 判断是否匹配路径
                if (pathMatcher.match(whiteUri, requestUri.getPath())) {
                    // 剔除Authorization防止advice获取出错
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(exchange.getRequest().getHeaders());
                    headers.remove(Constants.REQUEST_HEADER_AUTHORIZATION);
                    serverHttpRequest = new ServerHttpRequestDecorator(serverHttpRequest) {
                        @Override
                        public HttpHeaders getHeaders() {
                            return headers;
                        }
                    };
                    exchange = exchange.mutate().request(serverHttpRequest).build();
                    return chain.filter(exchange);
                }
            }
        }
        
        // 核心校验处理
        return auth(exchange, chain);
    }
    
    /**
     * 核心认证逻辑.
     */
    private Mono<Void> auth(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        
        URI requestUri = serverHttpRequest.getURI();
        String path = requestUri.getPath();
        
        // sign认证
        String sign = serverHttpRequest.getHeaders().getFirst(AuthConstants.AUTH_SIGN);
        AuthResult authResult;
        if (StringUtils.isNotEmpty(sign)) {
            authResult = authSign(exchange, sign, path);
        } else {
            // token认证
            String token = serverHttpRequest.getHeaders().getFirst(Constants.REQUEST_HEADER_AUTHORIZATION);
            if (StringUtils.isBlank(token)) {
                return unAuthorizedResponse(serverHttpResponse, Response.other(ResponseCode.UN_AUTHORIZED, "token不能为空", null), path);
            }
            authResult = authToken(exchange, token);
            // 刷新token特殊判断
            if (authResult.isRefreshToken()) {
                Map<String, Object> refreshToken = new HashMap<>(2);
                refreshToken.put("newToken", authResult.getNewToken());
                return unAuthorizedResponse(serverHttpResponse,
                        Response.other(ResponseCode.REFRESH_TOKEN, authResult.getUnPassedInfo(), refreshToken), path);
            }
        }
        if (!authResult.isPassed()) {
            return unAuthorizedResponse(serverHttpResponse, Response.other(ResponseCode.UN_AUTHORIZED, authResult.getUnPassedInfo(), null), path);
        }
        return chain.filter(exchange);
    }
    
    /**
     * sign认证.
     */
    private AuthResult authSign(ServerWebExchange exchange, String sign, String path) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String accessKey = serverHttpRequest.getHeaders().getFirst(AuthConstants.AUTH_ACCESS_KEY);
        String timestamp = serverHttpRequest.getHeaders().getFirst(AuthConstants.AUTH_TIMESTAMP);
        String nonce = serverHttpRequest.getHeaders().getFirst(AuthConstants.AUTH_NONCE);
        
        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(nonce)) {
            return AuthResult.builder().unPassedInfo("缺少sign认证参数").build();
        }
        
        long requestTimestamp;
        try {
            requestTimestamp = Long.parseLong(timestamp);
        } catch (Exception e) {
            return AuthResult.builder().unPassedInfo("请求时间参数错误").build();
        }
        
        // 校验时间戳与随机数
        long currentTimeMillis = System.currentTimeMillis();
        // 过期时间1分钟
        long expireTime = 1000 * 60L;
        if (currentTimeMillis - requestTimestamp > expireTime) {
            return AuthResult.builder().unPassedInfo("请求超时").build();
        }
        
        Response<VerifySignDto> verifySignDtoResponse = authFeignClient.verifySign(
                VerifySignParam.builder().accessKey(accessKey).sign(sign).nonce(nonce).timestamp(timestamp).requestUri(path).build());
        if (ResponseCode.SUCCESS.getCode() != verifySignDtoResponse.getCode()) {
            return AuthResult.builder().unPassedInfo(verifySignDtoResponse.getMsg()).build();
        }
        VerifySignDto verifySignDto = verifySignDtoResponse.getData();
        if (verifySignDto == null) {
            return AuthResult.builder().unPassedInfo("请重新登录").build();
        }
        ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
        // 塞入访问来源名
        addRequestHeader(mutate, Constants.HEADER_REQUEST_SOURCE, verifySignDto.getAccessSource().getValue());
        addRequestHeader(mutate, Constants.HEADER_REQUEST_USER_PLATFORM, PlatformEnum.OPENAPI);
        return AuthResult.builder().isPassed(true).build();
    }
    
    /**
     * token认证.
     */
    private AuthResult authToken(ServerWebExchange exchange, String token) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (token.startsWith(Constants.REQUEST_HEADER_AUTHORIZATION_BEARER)) {
            token = token.replaceFirst(Constants.REQUEST_HEADER_AUTHORIZATION_BEARER, StringUtils.EMPTY);
        }
        if (StringUtils.isEmpty(token)) {
            return AuthResult.builder().unPassedInfo("token不能为空").build();
        }
        Response<VerifyTokenDto> verifyTokenResponse = authFeignClient.verifyToken(token);
        VerifyTokenDto verifyTokenDto = verifyTokenResponse.getData();
        // 先判断是否需要刷新token
        if (ResponseCode.REFRESH_TOKEN.getCode() == verifyTokenResponse.getCode()) {
            return AuthResult.builder().isRefreshToken(true).newToken(verifyTokenDto.getNewToken()).unPassedInfo(verifyTokenResponse.getMsg())
                    .build();
        }
        if (ResponseCode.SUCCESS.getCode() != verifyTokenResponse.getCode()) {
            return AuthResult.builder().unPassedInfo(verifyTokenResponse.getMsg()).build();
        }
        if (verifyTokenDto == null) {
            return AuthResult.builder().unPassedInfo("请重新登录").build();
        }
        
        // 设置用户信息到请求头(以下请求头不需要请求方传递,完全由token解析处理,注意:如果是白名单地址,不会塞请求头)
        ServerHttpRequest.Builder mutate = serverHttpRequest.mutate();
        addRequestHeader(mutate, Constants.HEADER_REQUEST_USER_ID, verifyTokenDto.getUserId());
        addRequestHeader(mutate, Constants.HEADER_REQUEST_USER_NAME, verifyTokenDto.getUserName());
        addRequestHeader(mutate, Constants.HEADER_REQUEST_USER_PLATFORM, verifyTokenDto.getPlatform());
        return AuthResult.builder().isPassed(true).build();
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    /**
     * 添加请求头.
     */
    private void addRequestHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = StringUtils.urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }
    
    /**
     * 添加响应头.
     */
    private void addResponseHeader(ServerHttpResponse response, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = StringUtils.urlEncode(valueStr);
        response.getHeaders().add(name, valueEncode);
    }
    
    /**
     * 处理认证失败返回json.
     */
    public Mono<Void> unAuthorizedResponse(ServerHttpResponse serverHttpResponse, Response response, String requestPath) {
        LOGGER.warn("网关认证失败请求: {} 失败信息: {}", requestPath, response.getMsg());
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        byte[] content = new Gson().toJson(response).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = serverHttpResponse.bufferFactory().wrap(content);
        return serverHttpResponse.writeWith(Flux.just(wrap));
    }
    
    /**
     * 认证返回响应.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class AuthResult {
        
        /**
         * 是否认证通过.
         */
        private boolean isPassed;
        
        /**
         * 是否需要刷新token.
         */
        private boolean isRefreshToken;
        
        /**
         * 新token.
         */
        private String newToken;
        
        /**
         * 不通过时的信息.
         */
        private String unPassedInfo;
        
    }
    
}