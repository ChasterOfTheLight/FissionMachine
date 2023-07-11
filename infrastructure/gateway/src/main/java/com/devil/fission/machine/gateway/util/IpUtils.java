package com.devil.fission.machine.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * IP地址工具类.
 *
 * @author devil
 * @date Created in 2022/12/26 14:31
 */
public class IpUtils {
    
    /**
     * 获取IP地址.
     */
    public static String getIpAddress(ServerHttpRequest request) throws Exception {
        String ip = null;
        HttpHeaders headers = request.getHeaders();
        try {
            ip = headers.getFirst("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddress().getAddress().getHostAddress();
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return ip;
    }
    
}
