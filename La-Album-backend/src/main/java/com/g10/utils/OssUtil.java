package com.g10.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.g10.config.OssConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OssUtil {
    private final OssConfig ossConfig;
    private OSS ossClient;
    
    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
            ossConfig.getEndpoint(),
            ossConfig.getAccessKeyId(),
            ossConfig.getAccessKeySecret()
        );
    }
    
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
    
    public String uploadFile(String filename, InputStream inputStream) {
        try {
            // 生成唯一文件名
            String objectName = generateUniqueFileName(filename);
            
            // 设置上传元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(filename));
            
            // 上传文件
            ossClient.putObject(
                ossConfig.getBucketName(), 
                objectName, 
                inputStream, 
                metadata
            );
            
            // 生成访问URL
            return generateUrl(objectName);
        } catch (Exception e) {
            System.out.println("OSS配置信息：");
            System.out.println("Endpoint: " + ossConfig.getEndpoint());
            System.out.println("BucketName: " + ossConfig.getBucketName());
            System.out.println("OSS错误: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
    
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
    
    private String generateUrl(String objectName) {
        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
    }
} 