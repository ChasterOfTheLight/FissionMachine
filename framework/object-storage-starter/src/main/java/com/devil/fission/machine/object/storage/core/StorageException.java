package com.devil.fission.machine.object.storage.core;

import com.devil.fission.machine.common.exception.ServiceException;

/**
 * 对象存储异常.
 *
 * @author Devil
 * @date Created in 2023/3/22 14:46
 */
public class StorageException extends ServiceException {
    
    private static final long serialVersionUID = -4624425100696558036L;
    
    public StorageException(StorageErrorCode errorCode) {
        super(errorCode.getErrCode(), errorCode.getErrMsg());
    }
    
    public StorageException(StorageErrorCode errorCode, String errMsg) {
        super(errorCode.getErrCode(), errMsg);
    }
    
    public StorageException(StorageErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrCode(), errorCode.getErrMsg(), cause);
    }
    
}