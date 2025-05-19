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
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/photos")
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

    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("albumId") Long albumId) {
        try {
            // 验证文件
            validateFile(file);

            // 检查 album 是否存在
            Album album = albumService.getAlbumById(albumId);
            if (album == null) {
                return ResponseEntity.badRequest().build();
            }

            // 上传到 OSS
            String url = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());

            // 创建照片记录
            Photo photo = new Photo();
            photo.setTitle(file.getOriginalFilename());
            photo.setUrl(url);
            photo.setAlbum(album);

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

    // 更新照片信息和图片
    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "tags", required = false) String tagsJson,
            @RequestParam(value = "saveAsNew", required = false, defaultValue = "false") Boolean saveAsNew) {
        
        try {
            // 获取照片
            Optional<Photo> photoOptional = photoService.getPhotoById(id);
            if (photoOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Photo photo = photoOptional.get();
            Photo updatedPhoto;
            
            // 如果要另存为新文件
            if (saveAsNew && file != null && !file.isEmpty()) {
                // 创建新照片对象
                updatedPhoto = new Photo();
                updatedPhoto.setTitle(title != null ? title : photo.getTitle());
                updatedPhoto.setDescription(description != null ? description : photo.getDescription());
                updatedPhoto.setDate(date != null ? date : photo.getDate());
                updatedPhoto.setLocation(location != null ? location : photo.getLocation());
                
                // 设置相册
                if (albumId != null) {
                    Album album = albumService.getAlbumById(albumId);
                    if (album != null) {
                        updatedPhoto.setAlbum(album);
                    } else {
                        updatedPhoto.setAlbum(photo.getAlbum());
                    }
                } else {
                    updatedPhoto.setAlbum(photo.getAlbum());
                }
                
                // 生成新的文件名，保留原文件扩展名
                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = originalFilename.substring(0, originalFilename.lastIndexOf(".")) 
                    + "_edited_" + System.currentTimeMillis() + fileExtension;
                
                // 上传新文件
                String url = ossUtil.uploadFile(newFilename, file.getInputStream());
                updatedPhoto.setUrl(url);
                
                // 保存新照片
                updatedPhoto = photoService.savePhoto(updatedPhoto);
            } else {
                // 更新现有照片
                if (title != null) photo.setTitle(title);
                if (description != null) photo.setDescription(description);
                if (date != null) photo.setDate(date);
                if (location != null) photo.setLocation(location);
                
                // 更新相册
                if (albumId != null) {
                    Album album = albumService.getAlbumById(albumId);
                    if (album != null) {
                        photo.setAlbum(album);
                    }
                }
                
                // 更新图片
                if (file != null && !file.isEmpty()) {
                    validateFile(file);
                    String url = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());
                    photo.setUrl(url);
                }
                
                // 保存更新
                updatedPhoto = photoService.savePhoto(photo);
            }
            
            // 生成签名URL
            String signedUrl = ossUtil.generateSignedUrl(updatedPhoto.getUrl());
            updatedPhoto.setUrl(signedUrl);
            
            return ResponseEntity.ok(updatedPhoto);
            
        } catch (Exception e) {
            System.out.println("更新照片失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

   @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("key") String key) {
        try {
            String signedUrl = ossUtil.generateSignedUrl(key);
            byte[] imageData = ossUtil.fetchImageData(signedUrl);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .header("Access-Control-Allow-Origin", "*")
                    .body(imageData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
