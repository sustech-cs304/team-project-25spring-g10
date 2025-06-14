package com.g10.controller;

import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.UserService;
import com.g10.utils.ThreadLocalUtil;
import com.g10.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final UserService userService;
    private final OssUtil ossUtil;

    // 获取所有相册
    @GetMapping
    public Result<List<Album>> getAllAlbums() {
        // Map<String, Object> userInfo = ThreadLocalUtil.get();
        // Long userId = Long.valueOf(userInfo.get("id").toString());
        // List<Album> albums = albumService.getAllAlbumsByUserId(userId);
        List<Album> albums = albumService.getAllAlbumsByType("default");
        
        // 为每个相册中的每张照片添加签名URL
        for (Album album : albums) {
            if (album.getPhotos() != null) {
                for (Photo photo : album.getPhotos()) {
                    String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
                    photo.setUrl(signedUrl);
                }
            }
        }
        return Result.success(albums);
    }

    @GetMapping("/type/{type}")
    public Result<List<Album>> getAllAlbumsByType(@PathVariable String type) {
        // Map<String, Object> userInfo = ThreadLocalUtil.get();
        // Long userId = Long.valueOf(userInfo.get("id").toString());
        // List<Album> albums = albumService.getAllAlbumsByUserId(userId);
        List<Album> albums = albumService.getAllAlbumsByType(type);
        
        // 为每个相册中的每张照片添加签名URL
        for (Album album : albums) {
            if (album.getPhotos() != null) {
                for (Photo photo : album.getPhotos()) {
                    String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
                    photo.setUrl(signedUrl);
                }
            }
        }
        return Result.success(albums);
    }

    @GetMapping("/title/{title}")
    public Result<List<Album>> getAllAlbumsByTitle(@PathVariable String title) {
        // Map<String, Object> userInfo = ThreadLocalUtil.get();
        // Long userId = Long.valueOf(userInfo.get("id").toString());
        // List<Album> albums = albumService.getAllAlbumsByUserId(userId);
        List<Album> albums = albumService.getAllAlbumsByTitle(title);
        
        // 为每个相册中的每张照片添加签名URL
        for (Album album : albums) {
            if (album.getPhotos() != null) {
                for (Photo photo : album.getPhotos()) {
                    String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
                    photo.setUrl(signedUrl);
                }
            }
        }
        return Result.success(albums);
    }

    // 创建相册
    @PostMapping
    public Result<Album> createAlbum(@RequestBody Album album) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        // 获取用户实体
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 设置相册所属用户
        album.setUser(user);
        album.setType("default");
        
        // 创建相册
        Album created = albumService.createAlbum(album);
        return Result.success(created);
    }

    //创建系统相册
    @PostMapping("/type")
    public Result<Album> createAlbumByType(@RequestBody Album album) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        // 获取用户实体
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 设置相册所属用户
        album.setUser(user);
        
        // 创建相册
        Album created = albumService.createAlbum(album);
        return Result.success(created);
    }

    // 根据ID获取相册
    @GetMapping("/{id}")
    public Result<Album> getAlbumById(@PathVariable Long id) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        Album album = albumService.getAlbumById(id);
        if (album == null) {
            return Result.error("相册不存在");
        }
        
        // 验证相册是否属于当前用户
        if (!album.getUser().getId().equals(userId)) {
            return Result.error("无权访问该相册");
        }
        
        // 为相册中的每张照片添加签名URL
        if (album.getPhotos() != null) {
            for (Photo photo : album.getPhotos()) {
                String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
                photo.setUrl(signedUrl);
                System.out.println("Photo URL: " + photo.getUrl() + "photo id: " + photo.getId());
            }
        }
        return Result.success(album);
    }

    // 更新相册
    @PutMapping("/{id}")
    public Result<Album> updateAlbum(@PathVariable Long id, @RequestBody Album updatedAlbum) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        // 验证相册是否属于当前用户
        Album existingAlbum = albumService.getAlbumById(id);
        if (existingAlbum == null) {
            return Result.error("相册不存在");
        }
        if (!existingAlbum.getUser().getId().equals(userId)) {
            return Result.error("无权修改该相册");
        }
        
        // 保持原有用户信息
        updatedAlbum.setId(id);
        updatedAlbum.setUser(existingAlbum.getUser());
        updatedAlbum.setType(existingAlbum.getType());
        
        Album result = albumService.updateAlbum(id, updatedAlbum);
        return Result.success(result);
    }

    // 获取相册中的所有照片
    @GetMapping("/{albumId}/photos")
    public Result<List<PhotoDTO>> getPhotosInAlbum(@PathVariable Long albumId) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());

        // 验证相册是否属于当前用户
        Album album = albumService.getAlbumById(albumId);
        if (album == null) {
            return Result.error("相册不存在");
        }
        if (!album.getUser().getId().equals(userId)) {
            return Result.error("无权访问该相册");
        }

        // 使用 DTO 方式获取照片信息
        List<PhotoDTO> photos = albumService.getPhotosInAlbum(albumId);

        // 为每个照片生成签名 URL
        for (PhotoDTO photodto : photos) {
            String signedUrl = ossUtil.generateSignedUrl(photodto.getUrl());
            photodto.setUrl(signedUrl);
        }

        return Result.success(photos);
    }


    // 删除相册（并将照片移动至垃圾桶）
    @DeleteMapping("/{id}")
    public Result deleteAlbum(@PathVariable Long id) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        // 验证相册是否属于当前用户
        Album album = albumService.getAlbumById(id);
        if (album == null) {
            return Result.error("相册不存在");
        }
        if (!album.getUser().getId().equals(userId)) {
            return Result.error("无权删除该相册");
        }
        try {
            albumService.deleteAlbum(id);
            return Result.success("相册已删除，照片已移至垃圾桶");
        } catch (Exception e) {
            return Result.error("删除相册失败：" + e.getMessage());
        }
    }


}
