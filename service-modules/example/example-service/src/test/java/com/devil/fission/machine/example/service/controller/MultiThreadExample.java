package com.devil.fission.machine.example.service.controller;

import com.devil.fission.machine.common.util.HttpClientUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * .
 *
 * @author Devil
 * @date Created in 2025/6/16 16:19
 */
public class MultiThreadExample {
    
    // 模拟 HTTP 请求处理的共享资源类
    static class RequestHandler {
        
        private final ReentrantLock lock = new ReentrantLock();
        
        public void handleRequest(String requestId) {
            boolean isLocked = false;
            try {
                // 尝试获取锁
                isLocked = lock.tryLock();
                if (!isLocked) {
                    System.out.println("[" + requestId + "] 请稍后重试");
                    return;
                }
                
                // 打印开始信息
                System.out.println("[" + requestId + "] 正在请求百度首页...");
                
                CloseableHttpClient httpClient = HttpClientUtils.getConnection();
                HttpPost httpPost = new HttpPost();
                String url = "https://www.baidu.com";
                URIBuilder builder = new URIBuilder(url);
                httpPost.setURI(builder.build());
                
                // 请求头 json
                httpPost.setHeader("Content-Type", "application/json");
                
                // 请求体
                httpPost.setEntity(new StringEntity("{}"));
                
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.OK.value()) {
                    String responseString = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    // 打印
                    System.out.println("[" + requestId + "] 请求成功: " + responseString);
                }
            } catch (Exception e) {
                System.err.println("[" + requestId + "] 请求失败: " + e.getMessage());
            } finally {
                if (isLocked) {
                    lock.unlock();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        RequestHandler handler = new RequestHandler();
        
        // 创建多个线程模拟并发请求
        Thread req1 = new Thread(() -> handler.handleRequest("Request-001"));
        Thread req2 = new Thread(() -> handler.handleRequest("Request-002"));
        Thread req3 = new Thread(() -> handler.handleRequest("Request-003"));
        
        // 启动线程
        req1.start();
        req2.start();
        req3.start();
    }
}
