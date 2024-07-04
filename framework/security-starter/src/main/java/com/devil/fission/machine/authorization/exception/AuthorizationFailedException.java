package com.devil.fission.machine.authorization.exception;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;

/**
 * 授权失败异常.
 *
 * @author Devil
 * @date Created in 2023/3/8 14:00
 */
public class AuthorizationFailedException extends ServiceException {
    
    private static final long serialVersionUID = -6689628232746818244L;
    
    public AuthorizationFailedException() {
        super(ResponseCode.UN_AUTHORIZED, "没有相关资源的授权");
    }
    
    public AuthorizationFailedException(String msg) {
        super(ResponseCode.UN_AUTHORIZED, msg);
    }
}
