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
import java.net.URL;
import java.util.Date;
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
            
            // 返回对象名称而不是完整URL
            return objectName;
        } catch (Exception e) {
            System.out.println("OSS配置信息：");
            System.out.println("Endpoint: " + ossConfig.getEndpoint());
            System.out.println("BucketName: " + ossConfig.getBucketName());
            System.out.println("OSS错误: " + e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    public String generateSignedUrl(String objectName) {
        try {
            // 如果objectName已经是完整URL，提取出对象名称
            if (objectName != null && objectName.contains("/")) {
                // 从URL中提取对象名称
                String[] parts = objectName.split("/");
                objectName = parts[parts.length - 1];
            }
            
            System.out.println("生成签名URL的对象名称: " + objectName);
            
            // 设置URL过期时间为1小时
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            // 生成带签名的URL
            URL signedUrl = ossClient.generatePresignedUrl(
                ossConfig.getBucketName(), 
                objectName, 
                expiration
            );
            return signedUrl.toString();
        } catch (Exception e) {
            System.out.println("生成签名URL失败: " + e.getMessage());
            System.out.println("对象名称: " + objectName);
            throw new RuntimeException("生成签名URL失败", e);
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
} 