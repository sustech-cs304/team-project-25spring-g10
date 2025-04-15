package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.service.AlbumService;
import com.g10.service.PhotoService;
import com.g10.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/photo")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private AlbumService albumService;

    // 获取所有照片
    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos() {
        List<Photo> photos = photoService.getAllPhotos();
        // 为每张照片生成带签名的URL
        photos.forEach(photo -> {
            String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
            photo.setUrl(signedUrl);
        });
        return ResponseEntity.ok(photos);
    }

    // 通过 ID 获取单张照片
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        Optional<Photo> photo = photoService.getPhotoById(id);
        if (photo.isPresent()) {
            // 生成带签名的URL
            String signedUrl = ossUtil.generateSignedUrl(photo.get().getUrl());
            photo.get().setUrl(signedUrl);
            return ResponseEntity.ok(photo.get());
        }
        return ResponseEntity.notFound().build();
    }

    // 上传新照片
    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
        @RequestParam("file") MultipartFile file,
        @RequestParam("albumId") Long albumId
    ) {
        try {
            // 验证文件
            validateFile(file);
            
            // 上传到OSS
            String url = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());
            
            // 创建照片记录
            Photo photo = new Photo();
            photo.setTitle(file.getOriginalFilename());
            photo.setUrl(url);
            photo.setAlbum(albumService.getAlbumById(albumId));
            
            // 保存到数据库
            Photo savedPhoto = photoService.savePhoto(photo);
            
            return ResponseEntity.ok(savedPhoto);
        } catch (Exception e) {
            System.out.println("Upload error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 验证文件大小（限制为10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/jpeg") && 
             !contentType.startsWith("image/png") && 
             !contentType.startsWith("image/gif"))) {
            throw new IllegalArgumentException("只支持jpg、png、gif格式的图片");
        }
    }

    // 删除照片
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    // 移动照片到新相册
    @PutMapping("/{id}/move")
    public ResponseEntity<Photo> movePhoto(@PathVariable Long id, @RequestBody Album dest) {
        photoService.movePhoto(id, dest);
        return ResponseEntity.ok().build();
    }
}
