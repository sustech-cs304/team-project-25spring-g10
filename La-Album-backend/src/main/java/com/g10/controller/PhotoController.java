package com.g10.controller;
import com.g10.repository.UserRepository;
import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.PhotoService;
import com.g10.service.UserService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private AlbumService albumService;

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
                    photo.getAlbum() != null ? photo.getAlbum().getId() : null,
                    photo.getDate(),
                    photo.getDescription(),
                    photo.getAlbum() != null ? photo.getAlbum().getTitle(): "Unknown Album"
            );
        }).toList();

        return ResponseEntity.ok(photoDTOs);
    }

    // 通过 ID 获取单张照片
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
    public ResponseEntity<Long> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("albumId") Long albumId) {
        try {
            // 验证文件
            System.out.println("start uploading");
            validateFile(file);

            // 检查 album 是否存在
            Album album;
            if (albumId != null) {
                album = albumService.getAlbumById(albumId);
                if (album == null) {
                    return ResponseEntity.badRequest().build();
                }
            } 
            else {
                throw new RuntimeException("Album not found");
            }
            System.out.println("即将上传到OSS的文件名：" + file.getOriginalFilename());
            // 上传到 OSS
            String url = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());
            System.out.println("即将上传到OSS的文件网址："+url);
            // 创建照片记录
            Photo photo = new Photo();
            photo.setTitle(file.getOriginalFilename());
            photo.setUrl(url);
            photo.setAlbum(album);
            photo.setLocation("深圳");
            // 保存到数据库
            Photo savedPhoto = photoService.savePhoto(photo);
            return ResponseEntity.ok(savedPhoto.getId());
        } catch (Exception e) {
            System.out.println("Upload error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/copy")
public ResponseEntity<Photo> copyPhotoToAlbum(
        @RequestParam("photoId") Long photoId,
        @RequestParam("newAlbumId") Long newAlbumId) {
    try {
        // 获取原照片
        Optional<Photo> photoOptional = photoService.getPhotoEntityById(photoId);
        if (photoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Photo originalPhoto = photoOptional.get();

        // 获取新相册
        Album newAlbum = albumService.getAlbumById(newAlbumId);
        if (newAlbum == null) {
            return ResponseEntity.badRequest().build();
        }

        // 复制照片信息
        Photo newPhoto = new Photo();
        newPhoto.setTitle(originalPhoto.getTitle());
        newPhoto.setDescription(originalPhoto.getDescription());
        newPhoto.setDate(originalPhoto.getDate());
        newPhoto.setLocation(originalPhoto.getLocation());
        newPhoto.setTags(originalPhoto.getTags());
        newPhoto.setUrl(originalPhoto.getUrl());
        newPhoto.setAlbum(newAlbum);

        // 复制其他需要的字段（如有）
        newPhoto.setUploadTime(originalPhoto.getUploadTime());

        // 保存新照片
        Photo savedPhoto = photoService.savePhoto(newPhoto);

        return ResponseEntity.ok(savedPhoto);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    @PutMapping("/{id}/move")
    public ResponseEntity<Void> movePhoto(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long destAlbumId = body.get("id");
        photoService.movePhoto(id, destAlbumId);
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
                    photo.getAlbum() != null ? photo.getAlbum().getId() : null,
                    photo.getDate(),
                    photo.getDescription(),
                    photo.getAlbum() != null ? photo.getAlbum().getTitle(): "Unknown Album"
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }


    // 更新照片信息和图片
    // @PutMapping("/{id}")
    // public ResponseEntity<Photo> updatePhoto(
    //         @PathVariable Long id,
    //         @RequestParam(value = "image", required = false) MultipartFile file,
    //         @RequestParam(value = "title", required = false) String title,
    //         @RequestParam(value = "description", required = false) String description,
    //         @RequestParam(value = "date", required = false) String date,
    //         @RequestParam(value = "location", required = false) String location,
    //         @RequestParam(value = "albumId", required = false) Long albumId,
    //         @RequestParam(value = "tags", required = false) String tagsJson,
    //         @RequestParam(value = "saveAsNew", required = false, defaultValue = "false") Boolean saveAsNew) {

    //     try {
    //         // 获取照片
    //         Optional<Photo> photoOptional = photoService.getPhotoEntityById(id);
    //         if (photoOptional.isEmpty()) {
    //             return ResponseEntity.notFound().build();
    //         }
            
    //         Photo photo = photoOptional.get();
    //         Photo updatedPhoto;

    //         // 如果要另存为新文件
    //         if (saveAsNew && file != null && !file.isEmpty()) {
    //             // 创建新照片对象
    //             updatedPhoto = new Photo();
    //             updatedPhoto.setTitle(title != null ? title : photo.getTitle());
    //             updatedPhoto.setTags(photo.getTags());
    //             updatedPhoto.setDate(date != null ? date : photo.getDate());
    //             updatedPhoto.setLocation(location != null ? location : photo.getLocation());
    //             updatedPhoto.setDescription(description);

    //             // 设置相册
    //             if (albumId != null) {
    //                 Album album = albumService.getAlbumById(albumId);
    //                 if (album != null) {
    //                     updatedPhoto.setAlbum(album);
    //                 } else {
    //                     updatedPhoto.setAlbum(photo.getAlbum());
    //                 }
    //             } else {
    //                 updatedPhoto.setAlbum(photo.getAlbum());
    //             }

    //             // 生成新的文件名，保留原文件扩展名
    //             String originalFilename = file.getOriginalFilename();
    //             String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    //             String newFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."))
    //                 + "_edited_" + System.currentTimeMillis() + fileExtension;

    //             // 上传新文件
    //             String url = ossUtil.uploadFile(newFilename, file.getInputStream());
    //             updatedPhoto.setUrl(url);

    //             // 保存新照片
    //             updatedPhoto = photoService.savePhoto(updatedPhoto);
    //         } else {
    //             // 更新现有照片
    //             if (title != null) photo.setTitle(title);
    //             if (description != null) photo.setDescription(description);
    //             if (date != null) photo.setDate(date);
    //             if (location != null) photo.setLocation(location);

    //             // 更新相册
    //             if (albumId != null) {
    //                 Album album = albumService.getAlbumById(albumId);
    //                 if (album != null) {
    //                     photo.setAlbum(album);
    //                 }
    //             }

    //             // 更新图片
    //             if (file != null && !file.isEmpty()) {
    //                 validateFile(file);
    //                 String url = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());
    //                 photo.setUrl(url);
    //             }

    //             // 保存更新
    //             updatedPhoto = photoService.savePhoto(photo);
    //         }

    //         // 生成签名URL
    //         String signedUrl = ossUtil.generateSignedUrl(updatedPhoto.getUrl());
    //         updatedPhoto.setUrl(signedUrl);

    //         return ResponseEntity.ok(updatedPhoto);

    //     } catch (Exception e) {
    //         System.out.println("更新照片失败: " + e.getMessage());
    //         e.printStackTrace();
    //         return ResponseEntity.badRequest().build();
    //     }
    // }

    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhotoDescriptionAndTags(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        
        try {
            // 获取要更新的照片
            Optional<Photo> photoOptional = photoService.getPhotoEntityById(id);
            if (photoOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Photo photo = photoOptional.get();
            
            // 只更新 description 和 tags (如果提供了)
            if (updates.containsKey("description")) {
                photo.setDescription(updates.get("description"));
            }
            
            if (updates.containsKey("tags")) {
                photo.setTags(updates.get("tags"));
            }
            
            // 保存更新后的照片
            Photo updatedPhoto = photoService.savePhoto(photo);
            
            return ResponseEntity.ok(updatedPhoto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    @PostMapping("/api/photos")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("image") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "albumId", required = false) String albumId,
            @RequestParam(value = "tags", required = false) String tagsJson,
            HttpServletRequest request
    ) {
        try {
            Map<String, Object> userInfo = ThreadLocalUtil.get();
            Long userId = Long.valueOf(userInfo.get("id").toString());
            String ossUrl = ossUtil.uploadFile(file.getOriginalFilename(), file.getInputStream());

            Photo newPhoto = new Photo();
            newPhoto.setTitle(title);
            newPhoto.setDescription(description);
            newPhoto.setDate(date);
            newPhoto.setLocation(location);
            if (albumId != null && !albumId.isEmpty()) {
                Album album = albumService.getAlbumById(Long.parseLong(albumId));
                if (album != null) {
                    newPhoto.setAlbum(album);
                }
            }
            newPhoto.setTags(tagsJson);
            newPhoto.setUrl(ossUrl);

            photoService.savePhoto(newPhoto);
            return ResponseEntity.ok(newPhoto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败: " + e.getMessage());
        }
    }

}
