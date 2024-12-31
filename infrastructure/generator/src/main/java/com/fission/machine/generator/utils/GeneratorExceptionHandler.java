package com.fission.machine.generator.utils;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常处理器.
 *
 * @author devil
 * @date Created in 2022/4/27 10:16
 */
@Component
public class GeneratorExceptionHandler implements HandlerExceptionResolver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorExceptionHandler.class);
    
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        R r = new R();
        try {
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            
            if (ex instanceof GeneratorException) {
                r.put("code", ((GeneratorException) ex).getCode());
                r.put("msg", ((GeneratorException) ex).getMessage());
            } else if (ex instanceof DuplicateKeyException) {
                r = R.error("数据库中已存在该记录");
            } else {
                r = R.error();
            }
            
            //记录异常日志
            LOGGER.error(ex.getMessage(), ex);
            
            String json = JSON.toJSONString(r);
            response.getWriter().print(json);
        } catch (Exception e) {
            LOGGER.error("RRExceptionHandler 异常处理失败", e);
        }
        return new ModelAndView();
    }
}
