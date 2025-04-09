package com.g10.controller;

import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.service.TrashBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashBinController {

    private final TrashBinService trashBinService;

    // 获取某个用户垃圾桶的所有照片
    @GetMapping("/{trashBinId}/photos")
    public ResponseEntity<List<TrashedPhoto>> getPhotosInTrashBin(@PathVariable Long trashBinId) {
        List<TrashedPhoto> trashedPhotos = trashBinService.getPhotosInTrashBin(trashBinId);
        if (trashedPhotos == null || trashedPhotos.isEmpty()) {
            throw new RuntimeException("No trashed photos found for trashBinId: " + trashBinId);
        }
        return ResponseEntity.ok(trashedPhotos);
    }

    // 还原照片
    @PostMapping("/restore/{trashedPhotoId}")
    public ResponseEntity<Photo> restorePhoto(@PathVariable Long trashedPhotoId) {
        Photo photo = trashBinService.restorePhoto(trashedPhotoId);
        if (photo == null) {
            throw new RuntimeException("Failed to restore photo with trashedPhotoId: " + trashedPhotoId);
        }
        return ResponseEntity.ok(photo);
    }

    // 永久删除某张垃圾桶中的照片
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFromTrash(@PathVariable Long id) {
        try {
            trashBinService.deleteFromTrash(id);
            return ResponseEntity.ok("Photo permanently deleted from trash.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete trashed photo with ID: " + id);
        }
    }

    // 手动触发自动删除（测试用）
    @PostMapping("/auto-delete")
    public ResponseEntity<String> automaticDeletePhoto() {
        try {
            trashBinService.automaticDeletePhoto();
            return ResponseEntity.ok("Automatic deletion executed.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform automatic delete.");
        }
    }

    // 设置删除时间（测试用）
    @PostMapping("/set-delete-date/{photoId}")
    public ResponseEntity<String> setDeleteDate(@PathVariable Long photoId) {
        try {
            trashBinService.setDeleteDate(photoId);
            return ResponseEntity.ok("Delete date set for photo ID: " + photoId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set delete date for photo ID: " + photoId);
        }
    }
}
