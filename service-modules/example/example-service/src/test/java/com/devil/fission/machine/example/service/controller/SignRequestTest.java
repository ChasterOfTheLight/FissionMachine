package com.devil.fission.machine.example.service.controller;

import com.alibaba.fastjson.JSON;
import com.devil.fission.machine.auth.api.AuthConstants;
import com.devil.fission.machine.common.util.HttpClientUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * SignRequestTest.
 *
 * @author Devil
 * @date Created in 2024/9/30 16:44
 */
public class SignRequestTest {
    
    private String generateSign(String timestamp, String param) {
        // 对参数排序
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("accessKey", "qsbnmjuiytrddsa");
        treeMap.put("nonce", timestamp);
        treeMap.put("timestamp", timestamp);
        treeMap.putAll(JSON.parseObject(param, Map.class));
        String argStr = treeMap.entrySet().stream().map(Object::toString).collect(Collectors.joining("&"));
        String accessSecret = "e6a4222df2f006977ee0efad050fb471";
        String secretStr = argStr + "&accessSecret=" + accessSecret;
        System.out.println(secretStr);
        return DigestUtils.md5DigestAsHex(secretStr.getBytes(StandardCharsets.UTF_8)).toUpperCase(Locale.ROOT);
    }
    
    @Test
    public void requestTest() {
        CloseableHttpClient httpClient = HttpClientUtils.getConnection();
        String url = "http://localhost:8080/example/test/saTokenLogout?id=777";
        // 替换真实的请求地址
        HttpPost httpPost = new HttpPost(url);
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        String key = "qsbnmjuiytrddsa";
        httpPost.setHeader(AuthConstants.AUTH_ACCESS_KEY, key);
        httpPost.setHeader(AuthConstants.AUTH_TIMESTAMP, currentTimeMillis);
        httpPost.setHeader(AuthConstants.AUTH_NONCE, currentTimeMillis);
        String param = "{\"id\":\"1\",\"name\":\"test\",\"age\":\"56\",\"address\":\"77778888\"}";
        String sign = generateSign(currentTimeMillis, param);
        httpPost.setHeader(AuthConstants.AUTH_SIGN, sign);
        CloseableHttpResponse httpResponse;
        try {
            httpPost.setEntity(new StringEntity(param, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String responseStr = EntityUtils.toString(httpResponse.getEntity());
                System.out.println(responseStr);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpPost.releaseConnection();
        }
    }
    
}
