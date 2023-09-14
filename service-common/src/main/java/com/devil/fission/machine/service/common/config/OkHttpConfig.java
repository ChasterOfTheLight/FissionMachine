package com.devil.fission.machine.service.common.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * OkHttpConfig.
 *
 * @author Devil
 * @date Created in 2023/7/21 11:23
 */
@Slf4j
public class OkHttpConfig {
    
    /**
     * 忽略证书校验.
     */
    @Bean
    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }
            
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }
            
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
    
    /**
     * 信任所有 SSL 证书.
     */
    @Bean
    public SSLSocketFactory sslSocketFactory(X509TrustManager x509TrustManager) {
        try {
            TrustManager[] trustManagers = new TrustManager[]{x509TrustManager};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            log.error("KeyManagementException", e);
        }
        return null;
    }
    
    /**
     * OkHttp 客户端配置.
     *
     * @return OkHttp 客户端配
     */
    @SuppressWarnings("KotlinInternalInJava")
    @Bean
    public OkHttpClient okHttpClient(SSLSocketFactory sslSocketFactory, X509TrustManager x509TrustManager, HostnameVerifier hostnameVerifier) {
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, x509TrustManager)
                .hostnameVerifier(hostnameVerifier)
                // 是否开启缓存
                .retryOnConnectionFailure(false)
                // 最大连接数、连接存活时间、存活时间单位（分钟）
                .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES))
                // 连接超时时间
                .connectTimeout(5L, TimeUnit.SECONDS)
                // 读取超时时间
                .readTimeout(5L, TimeUnit.SECONDS)
                // 是否允许重定向
                .followRedirects(true)
                .build();
    }
    
    /**
     * 信任所有主机名.
     *
     * @return 主机名校验
     */
    @Bean
    public HostnameVerifier hostnameVerifier() {
        return (s, sslSession) -> true;
    }
    
}