package com.devil.fission.machine.common.util;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * http客户端工具类
 *
 * <p>使用：</p>
 * <p>CloseableHttpClient httpClient = HttpClientUtils.getConnection();</p>
 * <p>HttpPost httpPost = new HttpPost(url);</p>
 * <p>httpPost.addHeader(xxx);</p>
 * <p>httpPost.setEntity(xxx);</p>
 * <p>CloseableHttpResponse httpResponse = httpClient.execute(httpPost);</p>
 * <p>int statusCode = httpResponse.getStatusLine().getStatusCode();</p>
 * <p>if (statusCode == HttpStatus.SC_OK) {</p>
 * <p>String responseString = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);</p>
 * <p>}</p>
 * <p>注意：需要最后关闭连接 调用对应方法 releaseConnection()，否则系统会出现大量CLOSE_WAIT的TCP连接，占用连接数</p>.
 *
 * @author Devil
 * @date Created in 2022/12/6 09:54
 */
public class HttpClientUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);
    
    /**
     * 池化管理.
     */
    private static PoolingHttpClientConnectionManager poolConnManager = null;
    
    /**
     * 它是线程安全的，所有的线程都可以使用它一起发送http请求.
     */
    private static CloseableHttpClient httpClient;
    
    private static final int DEFAULT_TOTAL = 20;
    
    private static final int MAX_TOTAL = 100;
    
    /**
     * 连接超时3s（毫秒）.
     */
    private static final int CONNECTION_TIMEOUT = 3000;
    
    /**
     * 请求超时5s （毫秒）.
     */
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    
    /**
     * socket超时5min（毫秒）.
     */
    private static final int SOCKET_TIMEOUT = 60000 * 5;
    
    static {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build());
            // 配置同时支持 http 和 https
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", socketFactory).build();
            // 初始化连接管理器
            poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 同时最多连接数
            poolConnManager.setMaxTotal(MAX_TOTAL);
            // 设置每个路由最大连接
            poolConnManager.setDefaultMaxPerRoute(DEFAULT_TOTAL);
            // 初始化httpClient
            httpClient = getConnection();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            LOGGER.error("初始化httpclient连接池失败：{}", e.getMessage(), e);
        }
    }
    
    public static CloseableHttpClient getConnection() {
        // 设置连接超时初始值
        RequestConfig config = RequestConfig.custom()
                // 设置连接超时时间
                .setConnectTimeout(CONNECTION_TIMEOUT)
                // 设置获取数据的最大等待时间
                .setSocketTimeout(SOCKET_TIMEOUT)
                // 设置请求超时时间
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).build();
        return HttpClients.custom().setDefaultRequestConfig(config).addInterceptorFirst(new HttpClientRequestInterceptor())
                .addInterceptorLast(new HttpClientResponseInterceptor())
                // 设置连接池管理
                .setConnectionManager(poolConnManager).setRetryHandler((exception, executionCount, context) -> {
                    // 不重试
                    return false;
                    //                    if (executionCount > 3) {
                    //                        log.warn("Maximum tries reached for client http pool ");
                    //                        return false;
                    //                    }
                    //                    //NoHttpResponseException 重试
                    //                    if (exception instanceof NoHttpResponseException
                    //                            || exception instanceof ConnectTimeoutException //连接超时重试
                    //                    ) {
                    //                        log.warn("NoHttpResponseException on " + executionCount + " call");
                    //                        return true;
                    //                    }
                    //                    return false;
                }).build();
    }
    
    /**
     * 一般特殊操作client才会使用.
     */
    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }
    
    static class HttpClientRequestInterceptor implements HttpRequestInterceptor {
        
        @Override
        public void process(HttpRequest request, HttpContext context) throws IOException {
            String message = buildRequestEntry(request, context) + buildHeadersEntry(request.getAllHeaders()) + buildEntityEntry(request);
            LOGGER.info("HttpClientRequestInterceptor" + message);
        }
        
        
        private String buildRequestEntry(HttpRequest request, HttpContext context) {
            return "\n请求信息 - " + request.getRequestLine().getMethod() + " " + context.getAttribute("http.target_host") + request.getRequestLine()
                    .getUri();
        }
        
        private String buildHeadersEntry(Header[] headers) {
            return "\n请求头: [" + Arrays.stream(headers).map(header -> header.getName() + ": " + header.getValue())
                    .collect(Collectors.joining(", ")) + "]";
        }
        
        private String buildEntityEntry(HttpRequest request) throws IOException {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    entity.writeTo(bs);
                    return "\n请求体:\n" + new String(bs.toByteArray());
                }
            }
            return "";
        }
    }
    
    static class HttpClientResponseInterceptor implements HttpResponseInterceptor {
        
        @Override
        public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
            String message = buildStatusEntry(response) + buildHeadersEntry(response.getAllHeaders()) + buildEntityEntry(response);
            LOGGER.info("HttpClientResponseInterceptor" + message);
        }
        
        private String buildStatusEntry(HttpResponse response) {
            return "\n响应信息 - " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
        }
        
        private String buildHeadersEntry(Header[] headers) {
            return "\n响应头: [" + Arrays.asList(headers).stream().map(header -> header.getName() + ": " + header.getValue())
                    .collect(Collectors.joining(", ")) + "]";
        }
        
        private String buildEntityEntry(HttpResponse response) throws IOException {
            HttpEntity entity = response.getEntity();
            if (entity.isRepeatable()) {
                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(response.getEntity());
                BufferedReader buffer = new BufferedReader(new InputStreamReader(bufferedHttpEntity.getContent()));
                String payload = buffer.lines().collect(Collectors.joining("\n"));
                response.setEntity(bufferedHttpEntity);
                return "\n响应体: \n" + payload;
            } else {
                // 无法读取响应，否则会出错
                return "";
            }
        }
    }
}
