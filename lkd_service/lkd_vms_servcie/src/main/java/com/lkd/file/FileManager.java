package com.lkd.file;

import com.lkd.config.MinIOConfig;
import com.lkd.exception.LogicException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 封装minio客户端方法
 * 上传文件
 */
@Slf4j
@Component
public class FileManager {
    @Autowired
    private MinIOConfig minIOConfig;
    @Autowired
    private MinioClient minioClient;

    /**
     * 上传文件到MinIO
     *
     * @param file
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeyException
     */
    public String uploadFile(MultipartFile file) {
        try {
            String newFileName = new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) + UUID.randomUUID().toString() +
                    "." +
                    StringUtils.substringAfterLast(file.getOriginalFilename(), ".");

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(newFileName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)  // partSize -1表示整体(不分片)上传
                    .bucket(minIOConfig.getBucket())
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder stringBuilder = new StringBuilder(minIOConfig.getReadPath());
            stringBuilder.
                    append("/").
                    append(minIOConfig.getBucket()).
                    append("/").
                    append(newFileName);
            return stringBuilder.toString();
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new LogicException("上传文件失败");
        }
    }
}
