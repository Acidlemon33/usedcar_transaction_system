package com.usedcar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.usedcar.repository")
public class UsedCarTransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsedCarTransactionApplication.class, args);
    }
}