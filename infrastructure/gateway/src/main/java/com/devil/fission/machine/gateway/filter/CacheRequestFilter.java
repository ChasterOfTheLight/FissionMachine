package com.devil.fission.machine.gateway.filter;

import com.devil.fission.machine.common.util.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 缓存请求拦截器.
 *
 * @author Devil
 * @date Created in 2024/9/30 16:09
 */
@Component
public class CacheRequestFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // GET DELETE 不过滤
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == null || method == HttpMethod.GET || method == HttpMethod.DELETE) {
            return chain.filter(exchange);
        }
        // 只缓存json类型请求
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean flag = StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
        // 不是json放行
        if (!flag) {
            return chain.filter(exchange);
        }
        return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
            if (serverHttpRequest == exchange.getRequest()) {
                return chain.filter(exchange);
            }
            return chain.filter(exchange.mutate().request(serverHttpRequest).build());
        });
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
