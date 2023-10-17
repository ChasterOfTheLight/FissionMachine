package com.devil.fission.machine.service.common.feign;

import cn.hutool.core.net.URLEncodeUtil;
import com.devil.fission.machine.common.support.MachineContextHolder;
import com.devil.fission.machine.service.common.support.ContextConstant;
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
    
    /**
     * feign内部请求头标识.
     */
    public static final String FEIGN_REQUEST_FLAG = "machine-feign-request";
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Object o = MachineContextHolder.getLocalMap().get(ContextConstant.CONTEXT_HEADER_MAP_KEY);
        if (o != null) {
            Map<String, String> headerMap = (Map<String, String>) o;
            headerMap.forEach((k, v) -> requestTemplate.header(k, URLEncodeUtil.encode(v)));
        }
        // 塞入feign调用专有头，方便区分
        requestTemplate.header(FEIGN_REQUEST_FLAG, String.valueOf(true));
    }
}
