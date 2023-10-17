package com.devil.fission.machine.service.common.feign;

import com.devil.fission.machine.common.exception.ServiceException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.encoding.HttpEncoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * feign异常处理.
 *
 * @author Devil
 * @date Created in 2023/1/3 10:55
 */
public class MachineFeignExceptionDecoder implements ErrorDecoder {
    
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            String errMsg = "";
            if (response.body() != null) {
                // 如果存在gzip压缩，需要解压缩
                Collection<String> encoding = response.headers().getOrDefault(HttpEncoding.CONTENT_ENCODING_HEADER, null);
                
                boolean gzipFlag = false;
                if (encoding != null) {
                    if (encoding.contains(HttpEncoding.GZIP_ENCODING)) {
                        String decompressedBody = decompress(response);
                        if (decompressedBody != null) {
                            gzipFlag = true;
                            errMsg = "请求" + methodKey + "异常, 响应信息：" + decompressedBody;
                        }
                    }
                }
                if (!gzipFlag) {
                    String errorBody = Util.toString(response.body().asReader(Util.UTF_8));
                    errMsg = "请求" + methodKey + "异常, 响应信息：" + errorBody;
                }
            } else {
                errMsg = "请求" + methodKey + "异常";
            }
            int status = response.status();
            // feign调用异常统一交由GlobalExceptionHandler处理
            throw new ServiceException(status, errMsg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String decompress(Response response) throws IOException {
        if (response.body() == null) {
            return null;
        }
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(response.body().asInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8))) {
            String outputString = "";
            String line;
            while ((line = reader.readLine()) != null) {
                outputString += line;
            }
            return outputString;
        }
    }
}
