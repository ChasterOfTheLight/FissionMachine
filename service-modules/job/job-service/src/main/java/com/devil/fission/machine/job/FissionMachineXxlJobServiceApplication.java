package com.devil.fission.machine.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * job-service启动器.
 *
 * @author devil
 * @date Created in 2023/9/19 10:10
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FissionMachineXxlJobServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FissionMachineXxlJobServiceApplication.class, args);
    }
    
}