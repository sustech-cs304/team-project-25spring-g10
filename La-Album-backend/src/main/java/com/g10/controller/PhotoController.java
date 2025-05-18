package com.g10.controller;

import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.PhotoService;
import com.g10.service.UserService;
import com.g10.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<PhotoDTO>> getAllPhotos() {
        List<PhotoDTO> photoDTOs = photoService.getAllPhotos().stream().map(photo -> {
            String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
            return new PhotoDTO(
                    photo.getId(),
                    photo.getTitle(),
                    signedUrl,
                    photo.getLocation(),
                    photo.getTags(),
                    photo.getUploadTime(),
                    photo.getAlbum() != null ? photo.getAlbum().getId() : null
            );
        }).toList();

        return ResponseEntity.ok(photoDTOs);
    }


//    // 通过 ID 获取单张照片
//    @GetMapping("/{id}")
//    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
//        Optional<Photo> photo = photoService.getPhotoById(id);
//        if (photo.isPresent()) {
//            // 生成带签名的URL
//            String signedUrl = ossUtil.generateSignedUrl(photo.get().getUrl());
//            photo.get().setUrl(signedUrl);
//            return ResponseEntity.ok(photo.get());
//        }
//        return ResponseEntity.notFound().build();
//    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoDTO> getPhoto(@PathVariable Long id) {
        Optional<PhotoDTO> optionalDto = photoService.getPhotoById(id);
        if (optionalDto.isPresent()) {
            PhotoDTO dto = optionalDto.get();
            String signedUrl = ossUtil.generateSignedUrl(dto.getUrl());
            dto.setUrl(signedUrl);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("albumId") Long albumId,
            @RequestParam("userId") Long userId) {
        try {
            // 验证文件
            validateFile(file);

            // 检查 album 是否存在
            Album album;
            if (albumId != null) {
                album = albumService.getAlbumById(albumId);
                if (album == null) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                album = albumService.getDefaultAlbumForUser(userId);
            }
            if (album == null) {
                throw new RuntimeException("Album not found");
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

    @GetMapping("/search")
    public ResponseEntity<List<PhotoDTO>> searchPhotos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long albumId) {

        List<Photo> results = photoService.searchPhotos(q, startDate, endDate, albumId);
        List<PhotoDTO> dtos = results.stream().map(photo -> {
            String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
            return new PhotoDTO(
                    photo.getId(),
                    photo.getTitle(),
                    signedUrl,
                    photo.getLocation(),
                    photo.getTags(),
                    photo.getUploadTime(),
                    photo.getAlbum() != null ? photo.getAlbum().getId() : null
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

}
