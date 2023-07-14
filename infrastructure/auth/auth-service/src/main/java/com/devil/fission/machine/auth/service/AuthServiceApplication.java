package com.devil.fission.machine.auth.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动器.
 *
 * @author devil
 * @date Created in 2023/7/12 14:48
 */
@EnableDiscoveryClient
@SpringBootApplication
public class AuthServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
    
}