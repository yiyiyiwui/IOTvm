package com.lkd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.lkd.dao")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run( UserServiceApplication.class, args);
    }
}
