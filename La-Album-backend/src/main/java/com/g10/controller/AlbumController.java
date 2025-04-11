package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // 获取所有相册
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albums);
    }

    // 创建相册
    @PostMapping
    public ResponseEntity<Album> createAlbum(@RequestBody Album album) {
        Album created = albumService.createAlbum(album);
        return ResponseEntity.ok(created);
    }

    // 根据ID获取相册
    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Long id) {
        Album album = albumService.getAlbumById(id);
        if (album == null) {
            throw new RuntimeException("Album not found with ID: " + id);
        }
        return ResponseEntity.ok(album);
    }

    // 更新相册
    @PutMapping("/{id}")
    public ResponseEntity<Album> updateAlbum(@PathVariable Long id, @RequestBody Album updatedAlbum) {
        Album result = albumService.updateAlbum(id, updatedAlbum);
        if (result == null) {
            throw new RuntimeException("Failed to update album. Album not found with ID: " + id);
        }
        return ResponseEntity.ok(result);
    }

    // 获取相册中的所有照片
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<List<Photo>> getPhotosInAlbum(@PathVariable Long albumId) {
        List<Photo> photos = albumService.getPhotosInAlbum(albumId);
        if (photos == null || photos.isEmpty()) {
            throw new RuntimeException("No photos found for album ID: " + albumId);
        }
        return ResponseEntity.ok(photos);
    }

    // 删除相册（并将照片移动至垃圾桶）
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAlbum(@PathVariable Long id) {
        try {
            albumService.deleteAlbum(id);
            return ResponseEntity.ok("Album deleted and photos moved to trash.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete album with ID: " + id + ". Reason: " + e.getMessage());
        }
    }
}
