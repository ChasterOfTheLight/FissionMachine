package com.devil.fission.machine.example.service;

import com.devil.fission.machine.redis.delay.EnableRedissonDelayed;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 例子服务启动器.
 *
 * @author devil
 * @date Created in 2022/12/12 10:09
 */
@EnableFeignClients(value = {"com.devil.fission.machine.example.service.feign"})
@EnableDiscoveryClient
@EnableRedissonDelayed
@MapperScan(basePackages = {"com.devil.fission.machine.example.service.mapper"})
@SpringBootApplication
public class ExampleServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExampleServiceApplication.class, args);
    }
    
}