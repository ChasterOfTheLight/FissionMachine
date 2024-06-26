package com.devil.fission.machine.redis.antirepeat.exception;

import com.devil.fission.machine.common.exception.ServiceException;

/**
 * 没有防重复提交实现异常.
 *
 * @author devil
 * @date Created in 2024/6/17 上午10:22
 */
public class NoLockImplException extends ServiceException {
    
    private static final long serialVersionUID = 769625040163156762L;
    
    public NoLockImplException() {
        super("未找到防重复提交锁的实现");
    }
}
