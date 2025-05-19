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
            // 安全检查
            if (objectName == null || objectName.trim().isEmpty()) {
                System.out.println("警告: 尝试为空对象名生成签名URL");
                return "";
            }

            System.out.println("原始对象URL: " + objectName);
            
            // 如果objectName已经是完整URL，提取出对象名称
            if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
                try {
                    // 尝试创建URL对象来正确解析
                    java.net.URL url = new java.net.URL(objectName);
                    String path = url.getPath();
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    // 处理可能包含OSS域名后面的路径
                    if (path.contains("/")) {
                        // 可能是完整路径，保留整个路径
                        objectName = path;
                    } else {
                        // 简单文件名
                        objectName = path;
                    }
                } catch (Exception e) {
                    // URL解析失败，回退到简单的字符串处理
                    String[] parts = objectName.split("/");
                    objectName = parts[parts.length - 1];
                }
            } else if (objectName.contains("/")) {
                // 可能是对象存储中的相对路径，保留整个路径
                // 不做额外处理，保留完整的相对路径
            }
            
            System.out.println("处理后的对象名称: " + objectName);
            
            // 设置URL过期时间为1小时
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            // 生成带签名的URL
            URL signedUrl = ossClient.generatePresignedUrl(
                ossConfig.getBucketName(), 
                objectName, 
                expiration
            );
            
            String finalUrl = signedUrl.toString();
            System.out.println("生成的签名URL: " + finalUrl);
            
            return finalUrl;
        } catch (Exception e) {
            System.out.println("生成签名URL失败: " + e.getMessage());
            System.out.println("对象名称: " + objectName);
            // 错误情况下返回原始URL，而不是抛出异常
            return objectName;
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
    
    
} 