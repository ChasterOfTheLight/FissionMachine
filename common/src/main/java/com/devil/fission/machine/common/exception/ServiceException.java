package com.devil.fission.machine.common.exception;

import com.devil.fission.machine.common.response.ResponseCode;

/**
 * 服务异常类.
 *
 * @author devil
 * @date Created in 2022/12/5 16:04
 */
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = -7648839506179290022L;
    
    /**
     * 错误码.
     */
    private int code = ResponseCode.FAIL.getCode();
    
    /**
     * 错误信息.
     */
    private String message;
    
    public ServiceException() {
        super();
    }
    
    public ServiceException(String message) {
        super(message);
        this.message = message;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
    
    public ServiceException(Integer code, String message) {
        super(message);
        if (code != null) {
            this.code = code;
        }
        this.message = message;
    }
    
    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        if (code != null) {
            this.code = code;
        }
        this.message = message;
    }
    
    public ServiceException(ResponseCode responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
        this.message = responseCode.getMsg();
    }
    
    public ServiceException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
        this.message = message;
    }
    
    public ServiceException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMsg(), cause);
        this.code = responseCode.getCode();
        this.message = responseCode.getMsg();
    }
    
    public ServiceException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.code = responseCode.getCode();
        this.message = message;
    }
    
    /**
     * getCode.
     *
     * @return the code
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * getMessage.
     *
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }
    
}
