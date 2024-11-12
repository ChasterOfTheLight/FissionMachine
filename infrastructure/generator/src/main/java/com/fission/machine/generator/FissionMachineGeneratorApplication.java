package com.fission.machine.generator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fission.machine.generator.dao")
public class FissionMachineGeneratorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FissionMachineGeneratorApplication.class, args);
    }
}
