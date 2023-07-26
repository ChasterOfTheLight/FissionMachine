package com.devil.fission.machine.auth.service.common.feign;

import cn.hutool.core.net.URLEncodeUtil;
import com.devil.fission.machine.auth.service.common.support.ContextConstant;
import com.devil.fission.machine.common.support.MachineContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Map;

/**
 * feign调用拦截器(在请求前执行).
 *
 * @author devil
 * @date Created in 2022/12/13 11:57
 */
public class MachineFeignInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Object o = MachineContextHolder.getLocalMap().get(ContextConstant.CONTEXT_HEADER_MAP_KEY);
        if (o != null) {
            Map<String, String> headerMap = (Map<String, String>) o;
            headerMap.forEach((k, v) -> requestTemplate.header(k, URLEncodeUtil.encode(v)));
        }
    }
}
