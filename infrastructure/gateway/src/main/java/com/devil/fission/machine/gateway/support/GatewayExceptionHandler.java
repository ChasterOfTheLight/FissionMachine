package com.devil.fission.machine.gateway.support;

import com.devil.fission.machine.common.response.Response;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 网关异常处理 优先级默认高.
 *
 * @author devil
 * @date Created in 2022/12/26 14:21
 */
@Order(-1)
@Component
public class GatewayExceptionHandler implements WebExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayExceptionHandler.class);
    
    private static final Set<String> DISCONNECTED_CLIENT_EXCEPTIONS;
    
    // 排除部份系统级的异常
    static {
        Set<String> exceptions = new HashSet<>();
        exceptions.add("AbortedException");
        exceptions.add("ClientAbortException");
        exceptions.add("EOFException");
        exceptions.add("EofException");
        DISCONNECTED_CLIENT_EXCEPTIONS = Collections.unmodifiableSet(exceptions);
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted() || isDisconnectedClientError(ex)) {
            return Mono.error(ex);
        }
        // 后续封装优雅的返回信息
        ServerHttpRequest request = exchange.getRequest();
        String rawQuery = request.getURI().getRawQuery();
        String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
        String path = request.getPath() + query;
        
        String message;
        HttpStatus status = determineStatus(ex);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        // 通过状态码自定义异常信息
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            message = "请求地址" + path + "不存在";
        } else if (status.value() == HttpStatus.FORBIDDEN.value() || status.value() == HttpStatus.UNAUTHORIZED.value()) {
            message = "请求地址" + path + "无权限访问！";
        } else if (status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            message = "请求异常！" + ex.getLocalizedMessage() + " 当前请求访问地址：" + path;
            LOGGER.error(message, ex);
        } else {
            message = "请求地址" + path + "参数或类型异常，请检查后重试！";
        }
        
        ServerHttpResponse response = exchange.getResponse();
        return exceptionResponse(response, Response.other(status.value(), message, null));
    }
    
    @Nullable
    protected HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getStatus();
        }
        return null;
    }
    
    private boolean isDisconnectedClientError(Throwable ex) {
        return DISCONNECTED_CLIENT_EXCEPTIONS.contains(ex.getClass().getSimpleName()) || isDisconnectedClientErrorMessage(
                NestedExceptionUtils.getMostSpecificCause(ex).getMessage());
    }
    
    private boolean isDisconnectedClientErrorMessage(String message) {
        message = (message != null) ? message.toLowerCase() : "";
        return (message.contains("broken pipe") || message.contains("connection reset by peer"));
    }
    
    /**
     * 处理异常返回.
     */
    public Mono<Void> exceptionResponse(ServerHttpResponse serverHttpResponse, Response response) {
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        byte[] content = new Gson().toJson(response).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = serverHttpResponse.bufferFactory().wrap(content);
        return serverHttpResponse.writeWith(Flux.just(wrap));
    }
    
}
