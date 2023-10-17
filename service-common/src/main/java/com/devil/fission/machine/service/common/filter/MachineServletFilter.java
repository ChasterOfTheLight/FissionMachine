package com.devil.fission.machine.service.common.filter;

import cn.hutool.core.net.URLDecoder;
import com.devil.fission.machine.common.support.MachineContextHolder;
import com.devil.fission.machine.service.common.support.ContextConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * servlet拦截器.
 *
 * @author Devil
 * @date Created in 2023/1/16 16:30
 */
public class MachineServletFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MachineServletFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("ServletFilter finish init");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 处理请求头，传递到下游interceptor，advice和aop
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            if (headerNames != null) {
                Map<String, String> contextHeader = new HashMap<>(16);
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = httpRequest.getHeader(name);
                    // 将需要的请求头塞入上下文中
                    if (MachineContextHolder.supportHeader(name)) {
                        value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                        contextHeader.put(name, value);
                        MachineContextHolder.set(name, value);
                    }
                }
                // feign调用有可能是在lambda中调用，导致新起线程请求处理，无法直接在请求头中获取信息；
                // 所以改用寄存在上下文中，feign调用时从上下文中处理，上下文拥有线程传递性，可以兼容这个问题
                if (!contextHeader.isEmpty()) {
                    MachineContextHolder.set(ContextConstant.CONTEXT_HEADER_MAP_KEY, contextHeader);
                }
            }
            
            // continue filter chain
            chain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.error("MachineServletFilter Server failed: ", e);
        } finally {
            MachineContextHolder.remove();
        }
    }
}
