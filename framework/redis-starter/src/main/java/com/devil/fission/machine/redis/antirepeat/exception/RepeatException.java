package com.devil.fission.machine.redis.antirepeat.exception;

import com.devil.fission.machine.common.exception.ServiceException;

/**
 * 重复提交异常.
 *
 * @author devil
 * @date Created in `2024/6/17` 上午10:24
 */
public class RepeatException extends ServiceException {
    
    private static final long serialVersionUID = 769625040163156762L;
    
    public RepeatException() {
        super("禁止重复提交");
    }
}
