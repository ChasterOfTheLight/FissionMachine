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
    
    @Around("@within(org.springframework.stereotype.Service)")
    public Object around(ProceedingJoinPoint joinPoint) {
        // 类名
        String className = joinPoint.getSignature().getDeclaringTypeName();
        // 方法名
        String methodName = joinPoint.getSignature().getName();
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            if (e instanceof ServiceException) {
                ServiceException se = (ServiceException) e;
                if (se.getCode() == ResponseCode.FAIL.getCode()) {
                    LOGGER.error("{} - {} 执行错误", className, methodName, se);
                }
                throw (ServiceException) e;
            } else {
                LOGGER.error("{} - {} 执行错误", className, methodName, e);
                throw new ServiceException(ResponseCode.FAIL.getCode(), e.getLocalizedMessage(), e);
            }
        }
    }
    
}
