package com.devil.fission.machine.auth.service.common.support;

import com.devil.fission.machine.common.support.MachineContextHolder;
import com.devil.fission.machine.auth.service.common.util.HttpUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestTemplateInterceptor.
 *
 * @author Devil
 * @date Created in 2023/3/3 9:28
 */
public class MachineRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpUtils.getCurrentRequestHeaders().forEach((headerName, headerValue) -> {
            if (MachineContextHolder.supportHeader(headerName)) {
                request.getHeaders().set(headerName, headerValue);
            }
        });
        return execution.execute(request, body);
    }
}
