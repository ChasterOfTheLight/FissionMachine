package com.devil.fission.machine.authorization.interceptor;

import com.devil.fission.machine.authorization.annotation.ApiScope;
import com.devil.fission.machine.authorization.exception.AuthorizationFailedException;
import com.devil.fission.machine.common.Constants;
import com.devil.fission.machine.common.enums.PlatformEnum;
import com.devil.fission.machine.common.support.MachineContextHolder;
import com.devil.fission.machine.common.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 接口作用范围拦截器.
 *
 * @author devil
 * @date Created in 2023/3/21 9:46
 */
public class ApiScopeInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod;
        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (ClassCastException e) {
            return true;
        }
        Method method = handlerMethod.getMethod();
        // @AliasFor是spring的特殊注解！不是Java原生支持的，因此要用Spring的工具类取值
        ApiScope apiScope = AnnotationUtils.getAnnotation(method, ApiScope.class);
        if (Objects.nonNull(apiScope)) {
            // 获取授权范围
            PlatformEnum[] scopeEnums = apiScope.platforms();
            if (scopeEnums.length == 0) {
                return true;
            }
            
            String platform = MachineContextHolder.getAsString(Constants.HEADER_REQUEST_USER_PLATFORM);
            for (PlatformEnum scopeEnum : scopeEnums) {
                if (String.valueOf(scopeEnum).equals(platform)) {
                    return true;
                }
            }
            String requestUri = request.getRequestURI();
            // 认证失败，抛出对应异常信息
            String throwMsg = StringUtils.isNotEmpty(platform) ? "相关接口不在作用范围 [" + platform + "] 内，请授权后再试" : "没有该接口权限" + ", 请求uri: " + requestUri;
            throw new AuthorizationFailedException(throwMsg);
        }
        // 默认不配置不处理
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    
    }
}
