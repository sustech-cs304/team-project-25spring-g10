package com.g10.controller;

import com.g10.model.Photo;
import com.g10.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*") // 允许跨域请求
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    // 获取所有照片
    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos() {
        List<Photo> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }

    // 通过 ID 获取单张照片
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        Optional<Photo> photo = photoService.getPhotoById(id);
        return photo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 上传新照片
    @PostMapping
    public ResponseEntity<Photo> uploadPhoto(@RequestBody Photo photo) {
        Photo savedPhoto = photoService.savePhoto(photo);
        return ResponseEntity.ok(savedPhoto);
    }

    // 删除照片
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
