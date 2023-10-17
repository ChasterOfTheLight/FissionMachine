package com.devil.fission.machine.service.common.filter;

import org.apache.commons.lang3.StringUtils;
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

/**
 * XSS过滤.
 *
 * @author devil
 * @date Created in 2022/12/7 17:04
 */
public class XssFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XssFilter.class);
    
    private String[] prefixIgnores;
    
    private String ignoresParam;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
        ignoresParam = config.getInitParameter("exclusions");
        if (StringUtils.isNotEmpty(ignoresParam)) {
            prefixIgnores = ignoresParam.split(",");
        }
        LOGGER.info("XssFilter finish init");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 过滤
        HttpServletRequest req = (HttpServletRequest) request;
        if (canIgnore(req)) {
            chain.doFilter(req, response);
            return;
        }
        XssHttpServletRequestWrapper requestWrapper = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(requestWrapper, response);
    }
    
    @Override
    public void destroy() {
        prefixIgnores = null;
    }
    
    private boolean canIgnore(HttpServletRequest request) {
        boolean isExcludedPage = false;
        // 判断是否在过滤url之外
        if (prefixIgnores != null && prefixIgnores.length > 0) {
            for (String page : prefixIgnores) {
                if (request.getServletPath().equals(page)) {
                    isExcludedPage = true;
                    break;
                }
            }
        }
        return isExcludedPage;
    }
    
}