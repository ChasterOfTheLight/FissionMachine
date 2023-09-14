package com.devil.fission.machine.gateway.support;

import com.devil.fission.machine.common.exception.ServiceException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

import java.io.IOException;

/**
 * feign异常处理.
 *
 * @author Devil
 * @date Created in 2023/1/3 10:55
 */
public class FeignExceptionDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String errMsg;
            if (response.body() != null) {
                String errorBody = Util.toString(response.body().asReader(Util.UTF_8));
                errMsg = "请求" + methodKey + "异常, 响应信息：" + errorBody;
            } else {
                errMsg = "请求" + methodKey + "异常";
            }
            int status = response.status();
            // feign调用异常统一交由GlobalExceptionHandler处理
            throw new ServiceException(status, errMsg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
