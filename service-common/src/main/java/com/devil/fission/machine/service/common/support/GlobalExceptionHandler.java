package com.devil.fission.machine.service.common.support;

import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.common.response.ResponseCode;
import com.devil.fission.machine.common.util.StringUtils;
import com.devil.fission.machine.service.common.feign.MachineFeignInterceptor;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Iterator;
import java.util.Optional;

/**
 * 全局RestController异常处理器.
 *
 * @author devil
 * @date Created in 2022/12/7 17:09
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final Gson gson = new Gson();
    
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]不支持方法:%s", requestUri, ex.getMethod());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]不支持类型:%s", requestUri, ex.getContentType());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]不接受类型:%s", requestUri, gson.toJson(ex.getSupportedMediaTypes()));
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]缺少地址参数:%s", requestUri, ex.getVariableName());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]缺少servlet参数:%s", requestUri, ex.getParameterName());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]参数异常:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]不支持会话%s", requestUri, ex.getPropertyName());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]缺少类型,异常信息:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]消息不可读:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]消息不可写:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String errorMsg = String.format("请求地址%s,[参数错误]参数异常:%s", requestUri, message);
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[参数错误]请求体异常:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String message = ex.getAllErrors().get(0).getDefaultMessage();
        String errorMsg = String.format("请求地址%s,[参数错误]参数异常:%s", requestUri, message);
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.BAD_REQUEST.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[404]没有处理器:%s", requestUri, ex.getHttpMethod());
        LOGGER.warn(errorMsg);
        Response<Object> response = Response.other(HttpStatus.NOT_FOUND.value(), errorMsg, null);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
    
    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = String.format("请求地址%s,[500]异步请求超时:%s", requestUri, ex.getLocalizedMessage());
        LOGGER.error(errorMsg);
        Response<Object> response = Response.error(errorMsg);
        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    
    /**
     * 业务异常.
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException e, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        // 非500业务异常不打印错误日志
        String errorMsg;
        Response<Object> response;
        if (e.getCode() != ResponseCode.INTERNAL_SERVER_ERROR.getCode()) {
            errorMsg = Optional.ofNullable(e.getMessage()).orElse("请求地址" + requestUri + ",发生业务异常(非阻断)");
            LOGGER.warn(errorMsg, e);
            printHeaderInfo(request, Level.WARN.toString());
            response = Response.other(e.getCode(), errorMsg, null);
        } else {
            errorMsg = Optional.ofNullable(e.getMessage()).orElse("请求地址" + requestUri + ",发生业务异常");
            LOGGER.error(errorMsg, e);
            printHeaderInfo(request, Level.ERROR.toString());
            response = Response.error(errorMsg, null);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        // content-type: application/json;charset=UTF-8
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        // 返回状态码200 但实际Response内code是错误码 方便调用端获取code并进行处理
        HttpStatus httpStatus = isFeignRequest(request) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
        return handleExceptionInternal(e, response, responseHeaders, httpStatus, request);
    }
    
    /**
     * 拦截未知的运行时异常.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = "请求地址" + requestUri + ",发生未知异常";
        LOGGER.error(errorMsg, e);
        Response<Object> response = Response.error(errorMsg);
        HttpStatus httpStatus = isFeignRequest(request) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
        return handleExceptionInternal(e, response, new HttpHeaders(), httpStatus, request);
    }
    
    /**
     * 系统异常.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleSystemException(Exception e, WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        String errorMsg = "请求地址" + requestUri + ",发生系统异常";
        LOGGER.error(errorMsg, e);
        Response<Object> response = Response.error(errorMsg);
        HttpStatus httpStatus = isFeignRequest(request) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
        return handleExceptionInternal(e, response, new HttpHeaders(), httpStatus, request);
    }
    
    /**
     * 判断是否是内部feign请求.
     *
     * @param request 请求
     * @return 是/否
     */
    private boolean isFeignRequest(WebRequest request) {
        String header = request.getHeader(MachineFeignInterceptor.FEIGN_REQUEST_FLAG);
        return !StringUtils.isEmpty(header) && String.valueOf(true).equals(header);
    }
    
    /**
     * 打印请求头信息.
     *
     * @param request 请求
     */
    private void printHeaderInfo(WebRequest request, String level) {
        Iterator<String> headerNames = request.getHeaderNames();
        StringBuilder headerInfo = new StringBuilder();
        while (headerNames.hasNext()) {
            String headerName = headerNames.next();
            String headerValue = request.getHeader(headerName);
            // 筛选请求头，排除cookie
            if (StringUtils.isNotEmpty(headerName) && !headerName.equalsIgnoreCase(HttpHeaders.COOKIE)) {
                headerInfo.append("    ").append(headerName).append(": ").append(headerValue).append(" \r\n");
            }
        }
        if (Level.ERROR.toString().equals(level)) {
            LOGGER.error("Request Header In Exception: {}", headerInfo);
        } else {
            LOGGER.warn("Request Header In Exception: {}", headerInfo);
        }
    }
    
}
