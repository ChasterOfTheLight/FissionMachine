package com.devil.fission.machine.auth.service.common.feign;

import com.devil.fission.machine.auth.service.common.util.HttpUtils;
import com.devil.fission.machine.common.support.MachineContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * feign调用拦截器(在请求前执行).
 *
 * @author devil
 * @date Created in 2022/12/13 11:57
 */
public class MachineFeignInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        HttpUtils.getCurrentRequestHeaders().forEach((headerName, headerValue) -> {
            if (MachineContextHolder.supportHeader(headerName)) {
                requestTemplate.header(headerName, headerValue);
            }
        });
    }
}
