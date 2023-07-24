package com.devil.fission.machine.auth.service.common.support;

import com.devil.fission.machine.common.support.MachineContextHolder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * RestTemplateInterceptor.
 *
 * @author Devil
 * @date Created in 2023/3/3 9:28
 */
public class MachineRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Object o = MachineContextHolder.getLocalMap().get(ContextConstant.CONTEXT_HEADER_MAP_KEY);
        if (o != null) {
            Map<String, String> headerMap = (Map<String, String>) o;
            headerMap.forEach((k, v) -> request.getHeaders().set(k, v));
        }
        return execution.execute(request, body);
    }
}
