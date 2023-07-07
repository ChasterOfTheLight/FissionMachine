package com.devil.fission.machine.auth.service.common.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * http工具类.
 *
 * @author devil
 * @date Created in 2023/3/3 9:49
 */
public class HttpUtils {
    
    public static final ThreadLocal<HttpServletRequest> currentRequest = ThreadLocal.withInitial(() -> null);
    
    public static final ThreadLocal<HttpServletResponse> currentResponse = ThreadLocal.withInitial(() -> null);
    
    private HttpUtils() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * 当前线程的request，且子线程共享.
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            return getCurrentRequestAttributes().getRequest();
        } catch (Exception e) {
            return currentRequest.get();
        }
    }
    
    /**
     * 当前线程的response，且子线程共享.
     */
    private static HttpServletResponse getCurrentResponse() {
        return getCurrentRequestAttributes().getResponse();
    }
    
    private static ServletRequestAttributes getCurrentRequestAttributes() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
        return servletRequestAttributes;
    }
    
    /**
     * 读取当前request中对应名字的值的一个cookie.
     *
     * @param name cookie name
     * @return cookie
     */
    public static Cookie readSingleCookieInRequestByName(String name) {
        HttpServletRequest request = getCurrentRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName())).findFirst().orElse(null);
    }
    
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        if (request == null) {
            return map;
        }
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration == null) {
            return map;
        }
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            map.put(key, request.getHeader(key));
        }
        return map;
    }
    
    public static Map<String, String> getCurrentRequestHeaders() {
        return getHeaders(getCurrentRequest());
    }
    
    public static void returnResponse(HttpStatus status, Object object) throws IOException {
        returnResponse(status.value(), object);
    }
    
    public static void returnResponse(int status, Object object) throws IOException {
        HttpServletResponse response = getCurrentResponse();
        response.setStatus(status);
        response.getWriter().write(JSONObject.toJSONString(object));
        response.setContentType("application/json");
    }
    
}
