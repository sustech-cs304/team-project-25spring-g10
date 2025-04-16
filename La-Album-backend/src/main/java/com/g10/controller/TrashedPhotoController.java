package com.g10.controller;

import com.g10.model.User;
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

    // 获取该用户所有被删除的照片
    @GetMapping("/{userID}")
    public List<TrashedPhoto> getTrashPhotos(@PathVariable Long userID) {
        return trashedPhotoService.getTrashedPhotos(userID); 
    }

    // 还原一张照片
    @PostMapping("/restore/{id}")
    public Photo restorePhoto(@PathVariable Long id) {
        return trashedPhotoService.restorePhoto(id);
    }

    // 永久删除一张照片
    @DeleteMapping("/{id}")
    public void deleteFromTrash(@PathVariable Long id) {
        trashedPhotoService.deleteFromTrash(id);
    }
}

