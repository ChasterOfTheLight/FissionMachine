package com.devil.fission.machine.auth.service.common.param;

/**
 * 默认请求参数.
 *
 * @author devil
 * @date Created in 2022/4/6 16:19
 */
public class DefaultRequestParam<T> extends BaseRequestParam<T> {
    
    public DefaultRequestParam() {
    }
    
    public DefaultRequestParam(ClientInfo clientInfo, DeviceInfo deviceInfo, T requestInfo) {
        super(clientInfo, deviceInfo, requestInfo);
    }
    
}
