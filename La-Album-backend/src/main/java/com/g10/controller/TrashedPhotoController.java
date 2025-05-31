package com.g10.controller;

import com.g10.model.User;
import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.model.TrashedPhoto;
import com.g10.service.TrashedPhotoService;
import com.g10.utils.ThreadLocalUtil;
import com.g10.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashedPhotoController {

    @Autowired
    private TrashedPhotoService trashedPhotoService;

    @Autowired
    private OssUtil ossUtil;

    @GetMapping
    public Result<List<TrashedPhoto>> getTrashPhotos() {
        // 从 ThreadLocal 或 SecurityContextHolder 中获取当前登录用户 ID
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        List<TrashedPhoto> trashedPhotos = trashedPhotoService.getTrashedPhotos(userId);
        trashedPhotos.forEach(photo -> {
            String signedUrl = ossUtil.generateSignedUrl(photo.getUrl());
            photo.setUrl(signedUrl);
        });
        return Result.success(trashedPhotos);
    }


    // 还原一张照片
    @PostMapping("/restore/{id}")
    public Result<Photo> restorePhoto(@PathVariable Long id) {
        Photo restoredPhoto = trashedPhotoService.restorePhoto(id);
        String signedUrl = ossUtil.generateSignedUrl(restoredPhoto.getUrl());
        restoredPhoto.setUrl(signedUrl);
        return Result.success(restoredPhoto);
    }

    // 永久删除一张照片
    @DeleteMapping("/{id}")
    public Result<Void> deleteFromTrash(@PathVariable Long id) {
        TrashedPhoto trashedPhoto = trashedPhotoService.getTrashedPhotoById(id);
        System.out.println("要删除的对象的 trashedPhoto.URL: " + trashedPhoto.getUrl());
        ossUtil.deleteFile(trashedPhoto.getUrl());

        trashedPhotoService.deleteFromTrash(id);
        return Result.success(null);
    }
}

