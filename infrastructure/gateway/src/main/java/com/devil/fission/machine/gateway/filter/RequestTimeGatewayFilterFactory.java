package com.devil.fission.machine.gateway.filter;

import com.devil.fission.machine.gateway.swagger.SwaggerProvider;
import com.devil.fission.machine.gateway.util.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 请求时间拦截器.
 *
 * @author devil
 * @date Created in 2022/12/26 14:28
 */
@Component
public class RequestTimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTimeGatewayFilterFactory.Config> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTimeGatewayFilterFactory.class);
    
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    
    private static final String KEY = "withParams";
    
    public RequestTimeGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(KEY);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 塞入请求开始执行时间
            exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            
            String ipAddress = "";
            try {
                ipAddress = IpUtils.getIpAddress(serverHttpRequest);
            } catch (Exception e) {
                LOGGER.error("获取ip地址出错 {}", e.getMessage(), e);
            }
            // 请求前打印
            StringBuilder stringBuilder = new StringBuilder();
            String method = serverHttpRequest.getMethodValue();
            stringBuilder.append(String.format("[Request Start] HttpMethod: %s 请求IP: %s 请求URI: %s", method, ipAddress,
                    serverHttpRequest.getURI().getRawPath()));
            
            // 配置开关  比如：RequestTime=true开启参数打印
            if (config.isWithParams()) {
                // 只打印地址栏请求参数，body请求参数见服务侧的advice（RestControllerAspect）
                if (!serverHttpRequest.getURI().getRawPath().contains(SwaggerProvider.API_URI)) {
                    if (!CollectionUtils.isEmpty(serverHttpRequest.getQueryParams())) {
                        stringBuilder.append("    ").append(String.format("请求参数: %s", serverHttpRequest.getQueryParams()));
                    }
                }
            }
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                int statusCode = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 500;
                stringBuilder.append("    ").append("[Request End]");
                if (startTime != null) {
                    stringBuilder.append(" 响应码: ").append(statusCode).append(" 执行时间: ").append(System.currentTimeMillis() - startTime)
                            .append("ms");
                    LOGGER.info(stringBuilder.toString());
                }
            }));
        };
    }
    
    public static class Config {
        
        private boolean withParams;
        
        public boolean isWithParams() {
            return withParams;
        }
        
        public void setWithParams(boolean withParams) {
            this.withParams = withParams;
        }
    }
    
}
