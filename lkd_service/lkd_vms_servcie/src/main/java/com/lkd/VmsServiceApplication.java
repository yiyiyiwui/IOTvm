package com.lkd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lkd.dao")
public class VmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run( VmsServiceApplication.class, args);
    }
}
