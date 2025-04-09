package com.g10.controller;

import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.service.TrashedPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashedPhotoController {

    private final TrashedPhotoService trashedPhotoService;

    // 获取垃圾桶列表
    @GetMapping("/{trashBinId}/photos")
    public List<TrashedPhoto> getTrashPhotos(@PathVariable Long trashBinId) {
        return trashedPhotoService.getPhotosInTrashBin(trashBinId);
    }

    // 还原一张照片
    @PostMapping("/restore/{id}")
    public Photo restorePhoto(@PathVariable Long id) {
        return trashedPhotoService.restorePhoto(id); // 如果抛异常，会被 GlobalExceptionHandler 处理
    }

    // 永久删除一张照片
    @DeleteMapping("/{id}")
    public void deleteFromTrash(@PathVariable Long id) {
        trashedPhotoService.deleteFromTrash(id);
    }
}

