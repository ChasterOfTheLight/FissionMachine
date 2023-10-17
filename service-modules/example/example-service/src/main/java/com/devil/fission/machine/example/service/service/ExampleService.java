package com.devil.fission.machine.example.service.service;

import cn.hutool.json.JSONException;
import com.alicp.jetcache.anno.Cached;
import com.devil.fission.machine.common.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * ExampleService.
 *
 * @author Devil
 * @date Created in 2023/10/10 13:36
 */
@Slf4j
@Service
public class ExampleService {
    
    /**
     * queryExample.
     */
    @Cached(name = "Example:", key = "#key", expire = 1800, postCondition = "#result!=null")
    public String queryExample(String key) {
        String url = "https://www.baidu.com";
        HttpPost httpPost = new HttpPost(url);
        return httpRequest(httpPost, responseString -> {
            try {
                return responseString;
            } catch (JSONException e) {
                log.error("响应：{}", responseString);
            }
            return null;
        });
    }
    
    /**
     * 请求封装.
     *
     * @param httpPost 发送的post实体
     * @param handler  回调方法
     * @return 具体响应类
     */
    private <T> T httpRequest(HttpPost httpPost, RequestSuccessHandler<T> handler) {
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        try {
            CloseableHttpClient httpClient = HttpClientUtils.getConnection();
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.OK.value()) {
                String responseString = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                log.info("调用接口: {} 成功, 返回结果: {}", httpPost.getURI().getPath(), responseString);
                try {
                    return handler.execute(responseString);
                } catch (Exception e) {
                    log.error("处理错误", e);
                }
            } else {
                log.error("调用错误 返回结果: {}", statusCode);
            }
        } catch (Exception e) {
            log.error("调用接口异常", e);
        } finally {
            httpPost.releaseConnection();
        }
        return null;
    }
    
    /**
     * 请求成功处理器接口.
     */
    interface RequestSuccessHandler<T> {
        
        /**
         * 具体执行回调方法.
         *
         * @param responseString 响应字符串
         * @return 具体的响应类
         */
        T execute(String responseString);
    }
    
}
