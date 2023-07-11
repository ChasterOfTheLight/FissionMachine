package com.devil.fission.machine.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 跨域配置.
 *
 * @author devil
 * @date Created in 2022/12/26 14:19
 */
@Configuration
public class CorsConfig {
    
    private static final String MAX_AGE = "3600L";
    
    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                }
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                }
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, HEAD, DELETE, PUT");
                }
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                }
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                }
                if (headers.getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE) == null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                }
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(exchange);
        };
    }
    
}
