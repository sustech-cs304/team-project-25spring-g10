package com.g10.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AudioController {

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getAudioFile(@PathVariable String filename) {
        try {
            // 从类路径的静态资源中加载音频文件
            // 假设音频文件放在 src/main/resources/static/audio/ 目录下
            Resource resource = new ClassPathResource("static/audio/" + filename);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 设置适当的内容类型
            String contentType = "audio/mpeg"; // 对于 MP3 文件
            if (filename.toLowerCase().endsWith(".wav")) {
                contentType = "audio/wav";
            } else if (filename.toLowerCase().endsWith(".ogg")) {
                contentType = "audio/ogg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 