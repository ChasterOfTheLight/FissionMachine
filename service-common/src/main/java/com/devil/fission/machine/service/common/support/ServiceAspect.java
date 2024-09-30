package com.devil.fission.machine.service.common.support;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceAspect.
 *
 * @author Devil
 * @date Created in 2023/9/25 10:46
 */
@Aspect
public class ServiceAspect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAspect.class);
    
    /**
     * around.
     */
    @Around("@within(org.springframework.stereotype.Service)")
    public Object around(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else {
                throw new ServiceException(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), e.getLocalizedMessage(), e);
            }
        }
    }
    
}
