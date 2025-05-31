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

    // private String extractObjectNameFromUrl(String url) {
    //     try {
    //         URL fullUrl = new URL(url);
    //         String host = ossConfig.getBucketName() + "." + ossConfig.getEndpoint();  // 例如 my-bucket.oss-cn-hangzhou.aliyuncs.com
    //         String path = fullUrl.getPath(); // /folder/filename.jpg
    //         return path.startsWith("/") ? path.substring(1) : path;
    //     } catch (Exception e) {
    //         System.out.println("提取 OSS 对象名称失败: " + e.getMessage());
    //         throw new RuntimeException("非法的 OSS URL: " + url, e);
    //     }
    // }

    public void deleteFile(String fileUrl) {
        try {
            // String objectName = extractObjectNameFromUrl(fileUrl);
            System.out.println("要删除的对象OSS URL: " + fileUrl);
            ossClient.deleteObject(ossConfig.getBucketName(), fileUrl);
            System.out.println("已从 OSS 删除对象: " + fileUrl);
        } catch (Exception e) {
            System.out.println("删除 OSS 文件失败: " + e.getMessage());
            throw new RuntimeException("删除 OSS 文件失败", e);
        }
    }
    
    public byte[] fetchImageData(String url) {
        try {
            // 如果是对象名称而不是完整URL，则生成签名URL
            if (!url.startsWith("http")) {
                url = generateSignedUrl(url);
            }
            
            // 建立连接
            java.net.URL imageUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            // 获取图像数据
            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            System.out.println("获取图像数据失败: " + e.getMessage());
            throw new RuntimeException("获取图像数据失败", e);
        }
    }
} 