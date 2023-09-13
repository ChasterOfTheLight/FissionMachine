package com.devil.fission.machine.auth.service.common.feign;

import cn.hutool.http.HttpStatus;
import com.devil.fission.machine.common.util.GzipUtils;
import com.devil.fission.machine.common.util.StringUtils;
import feign.Request;
import feign.Response;
import feign.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import static feign.Util.CONTENT_ENCODING;
import static feign.Util.ENCODING_GZIP;
import static feign.Util.UTF_8;
import static feign.Util.decodeOrDefault;
import static feign.Util.valuesOrEmpty;

/**
 * feign调用的日志打印.
 *
 * @author Devil
 * @date Created in 2023/1/16 10:43
 */
public class MachineFeignLogger extends feign.Logger {
    
    private final Logger logger;
    
    public MachineFeignLogger(Logger logger) {
        this.logger = logger;
    }
    
    public MachineFeignLogger() {
        this(feign.Logger.class);
    }
    
    public MachineFeignLogger(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }
    
    public MachineFeignLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }
    
    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        // 拼装请求日志
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Feign Request HTTP/1.1 ---> %s %s ", request.httpMethod().name(), request.url())).append("\r\n");
        
        // 请求头拼装
        if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
            stringBuilder.append("   ").append("Request Headers:").append("\r\n");
            for (String field : request.headers().keySet()) {
                for (String value : valuesOrEmpty(request.headers(), field)) {
                    stringBuilder.append("      ").append(String.format("%s: %s", field, value)).append("\r\n");
                }
            }
            
            // 拼装请求体
            int bodyLength = 0;
            if (request.body() != null) {
                bodyLength = request.length();
                if (logLevel.ordinal() >= Level.FULL.ordinal()) {
                    String bodyText = request.charset() != null ? new String(request.body(), request.charset()) : null;
                    stringBuilder.append("   ").append("Request Body: ").append(String.format("%s", bodyText != null ? bodyText : "Binary data"));
                }
            }
            
            // 结束请求日志拼装
            stringBuilder.append("   ").append(String.format("---> END Feign Request HTTP (%s-byte body)", bodyLength));
        }
        log(configKey, stringBuilder.toString());
    }
    
    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
        String reason = response.reason() != null && logLevel.compareTo(Level.NONE) > 0 ? " " + response.reason() : "";
        int status = response.status();
        StringBuilder stringBuilder = new StringBuilder();
        // 返回日志拼装
        stringBuilder.append(String.format("<--- Feign Response HTTP/1.1 %s%s (%sms)", status, reason, elapsedTime)).append("\r\n");
        
        // 拼装返回请求头
        if (logLevel.ordinal() >= Level.HEADERS.ordinal()) {
            stringBuilder.append("   ").append("Response Headers:").append("\r\n");
            for (String field : response.headers().keySet()) {
                for (String value : valuesOrEmpty(response.headers(), field)) {
                    stringBuilder.append("      ").append(String.format("%s: %s", field, value)).append("\r\n");
                }
            }
            
            // 拼装返回体，限制字符1000（考虑日志的占用量，可以根据实际情况优化，但不建议不限制字符）
            int bodyLength = 0;
            boolean notSpecialStatus = !(status == HttpStatus.HTTP_NO_CONTENT || status == HttpStatus.HTTP_RESET);
            if (response.body() != null && notSpecialStatus) {
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                bodyLength = bodyData.length;
                if (logLevel.ordinal() >= Level.FULL.ordinal() && bodyLength > 0) {
                    String binaryData;
                    if (isGzip(response.headers().get(CONTENT_ENCODING))) {
                        binaryData = GzipUtils.uncompressToString(bodyData);
                    } else {
                        binaryData = decodeOrDefault(bodyData, UTF_8, "Binary data");
                    }
                    String resultData = StringUtils.substring(binaryData, 0, 1000);
                    stringBuilder.append("   ").append("Response Body: ").append(resultData).append("\r\n");
                }
                
                // 结束返回日志拼装
                stringBuilder.append("   ").append(String.format("---> END Feign Response HTTP (%s-byte body)", bodyLength));
                log(configKey, stringBuilder.toString());
                return response.toBuilder().body(bodyData).build();
            } else {
                stringBuilder.append("   ").append(String.format("---> END Feign Response HTTP (%s-byte body)", bodyLength));
                log(configKey, stringBuilder.toString());
            }
        }
        return response;
    }
    
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        StringBuilder stringBuilder = new StringBuilder();
        // 返回日志拼装
        stringBuilder.append(String.format("<--- Feign ERROR %s: %s (%sms)", ioe.getClass().getSimpleName(), ioe.getMessage(), elapsedTime))
                .append("\r\n");
        if (logLevel.ordinal() >= Level.FULL.ordinal()) {
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            stringBuilder.append(String.format("%s", sw.toString())).append("\r\n");
            stringBuilder.append("<--- END ERROR");
        }
        log(configKey, stringBuilder.toString());
        return ioe;
    }
    
    @Override
    protected void log(String configKey, String format, Object... args) {
        // 默认info日志
        logger.info(methodTag(configKey) + format);
    }
    
    /**
     * 判断是否是gzip压缩.
     */
    private boolean isGzip(Collection<String> contentEncodingValues) {
        return contentEncodingValues != null
                && !contentEncodingValues.isEmpty()
                && contentEncodingValues.contains(ENCODING_GZIP);
    }
}
