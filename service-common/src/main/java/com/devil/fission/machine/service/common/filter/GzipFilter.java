package com.devil.fission.machine.service.common.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * GzipFilter.
 *
 * @author Devil
 * @date Created in 2023/12/1 10:21
 */
public class GzipFilter extends OncePerRequestFilter {
    
    private static final String GZIP_CONTENT_ENCODING = "gzip";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 检查请求头是否包含gzip压缩
        String contentEncoding = request.getHeader("Content-Encoding");
        if (GZIP_CONTENT_ENCODING.equals(contentEncoding)) {
            HttpServletRequestWrapper wrappedRequest = new GzipRequestWrapper(request);
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
    
    private static class GzipRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] requestBody;
        
        GzipRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.requestBody = readRequestBody(request);
        }
        
        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CustomServletInputStream(requestBody);
        }
        
        private byte[] readRequestBody(HttpServletRequest request) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(request.getInputStream())) {
                while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
    
    private static class CustomServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;
        
        CustomServletInputStream(byte[] bytes) {
            this.byteArrayInputStream = new ByteArrayInputStream(bytes);
        }
        
        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }
        
        @Override
        public boolean isFinished() {
            return false;
        }
        
        @Override
        public boolean isReady() {
            return false;
        }
        
        @Override
        public void setReadListener(ReadListener listener) {
        
        }
    }
}