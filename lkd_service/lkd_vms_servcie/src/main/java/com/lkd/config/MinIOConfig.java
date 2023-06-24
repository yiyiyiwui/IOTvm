package com.lkd.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("minio")
@Data
public class MinIOConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String readPath;
    
    @Bean
    public MinioClient buildMinioClient(){
        return MinioClient
                .builder()
                .credentials(accessKey,secretKey)
                .endpoint(endpoint)
                .build();
    }
}