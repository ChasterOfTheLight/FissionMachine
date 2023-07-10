package com.devil.fission.machine.auth.service.common.filter;

import com.devil.fission.machine.common.support.MachineContextHolder;
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
import java.util.Enumeration;

/**
 * servlet拦截器.
 *
 * @author Devil
 * @date Created in 2023/1/16 16:30
 */
public class MachineServletFilter implements Filter {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(MachineServletFilter.class);
    
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
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = httpRequest.getHeader(name);
                    // 将需要的请求头塞入上下文中
                    if (MachineContextHolder.supportHeader(name)) {
                        MachineContextHolder.set(name, value);
                    }
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
