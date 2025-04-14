package com.g10.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.io.IOException;
import com.g10.service.AliyunOssService;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    @Autowired
    private AliyunOssService aliyunOssService;

    @PostMapping
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = aliyunOssService.uploadFile(file);
            return Map.of("url", url);
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of("error", "上传失败");
        }
    }
}
