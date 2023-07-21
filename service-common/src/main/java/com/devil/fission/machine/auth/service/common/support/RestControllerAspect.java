package com.devil.fission.machine.auth.service.common.support;

import cn.hutool.core.net.URLDecoder;
import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.support.MachineContextHolder;
import com.devil.fission.machine.common.util.IdGeneratorEnum;
import com.devil.fission.machine.common.util.StringUtils;
import com.google.gson.Gson;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/**
 * RestController切面（同时也处理Controller）.
 *
 * @author devil
 * @date Created in 2022/12/7 17:27
 */
@Aspect
public class RestControllerAspect implements Ordered {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerAspect.class);
    
    private final Gson gson = new Gson();
    
    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object around(ProceedingJoinPoint joinPoint) {
        return requestHandleAndLogPrint(joinPoint);
    }
    
    /**
     * 请求处理和日志打印.
     */
    private Object requestHandleAndLogPrint(ProceedingJoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Object[] args = joinPoint.getArgs();
        
        // 打印请求相关参数
        StringBuilder aspectLog = new StringBuilder();
        long requestId = IdGeneratorEnum.INSTANCE.getIdGenerator().nextId();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String paramsJson = getParams(args);
            // 开始打印请求日志拼装
            aspectLog.append(String.format(
                    "[Request Info] >>>> [Request Id]: %s [URL]: %s \r\n     [HTTP Method]: %s [Class Method]: %s.%s [IP]: %s \r\n     [Request Args]: %s",
                    requestId, request.getRequestURL().toString(), request.getMethod(), joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), request.getRemoteAddr(), paramsJson)).append("\r\n");
            // 打印requestParam参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (!parameterMap.isEmpty()) {
                aspectLog.append("     [Request ParameterMap]: \r\n");
                aspectLog.append("        ").append(gson.toJson(parameterMap)).append("\r\n");
            }
            // 请求头，塞入业务测关注的请求头到上下文，方便使用
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                aspectLog.append("     [Request Machine Headers]: \r\n");
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = request.getHeader(name);
                    // 处理需要传递的请求头，不是所有的都传递，content-length非必要不传
                    if (MachineContextHolder.supportHeader(name)) {
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                        aspectLog.append("        ").append(String.format("%s : %s", name, value)).append("\r\n");
                    }
                }
                aspectLog.delete(aspectLog.length() - 2, aspectLog.length());
            }
        }
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result;
        try {
            LOGGER.info(aspectLog.toString());
            result = joinPoint.proceed(args);
            // 结果字符串打印，最多打印1000字符（考虑日志的占用量，可以根据实际情况优化，但不建议不限制字符）
            String resultJson = StringUtils.substring(gson.toJson(result), 0, 1000);
            aspectLog = new StringBuilder();
            aspectLog.append(String.format("[Response Info] <<< [Request Id]: %s [Result]: %s", requestId, resultJson));
        } catch (Throwable e) {
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else {
                throw new ServiceException(ResponseCode.FAIL.getCode(), e.getLocalizedMessage(), e);
            }
        }
        stopWatch.stop();
        // 打印执行时间
        aspectLog.append(String.format("   [Execute Time]: %s ms", stopWatch.getTotalTimeMillis()));
        LOGGER.info(aspectLog.toString());
        
        return result;
    }
    
    private String getParams(Object[] args) {
        StringBuilder params = new StringBuilder();
        if (args != null) {
            // 如果
            for (Object arg : args) {
                if ((arg instanceof HttpServletResponse) || (arg instanceof HttpServletRequest) || (arg instanceof MultipartFile)
                        || (arg instanceof MultipartFile[])) {
                    continue;
                }
                try {
                    params.append(gson.toJson(arg));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return params.toString();
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
