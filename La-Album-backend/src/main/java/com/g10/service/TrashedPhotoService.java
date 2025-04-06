package com.g10.service;

import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.model.TrashBin;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import com.g10.repository.AlbumRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashedPhotoService {

    private final TrashedPhotoRepository trashedPhotoRepository;
    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;

    // 移动照片到垃圾桶
    @Transactional
    public void moveToTrash(Photo photo, TrashBin trashBin) {
        TrashedPhoto trash = new TrashedPhoto(photo, trashBin);
        trashedPhotoRepository.save(trash);
        photoRepository.delete(photo);
    }

    // 还原照片
    @Transactional
    public Photo restorePhoto(Long trashedPhotoId) {
        TrashedPhoto trash = trashedPhotoRepository.findById(trashedPhotoId)
                .orElseThrow(() -> new RuntimeException("Trashed photo not found"));

        Photo photo = new Photo();
        photo.setTitle(trash.getTitle());
        photo.setUrl(trash.getUrl());
        photo.setLocation(trash.getLocation());
        photo.setTags(trash.getTags());
        photo.setUploadTime(trash.getUploadTime());
        photo.setAlbum(albumRepository.findById(trash.getAlbumId())
                .orElseThrow(() -> new RuntimeException("Album not found")));

        photoRepository.save(photo);
        trashedPhotoRepository.delete(trash);

        return photo;
    }

    // 获取某个用户垃圾桶的所有照片
    public List<TrashedPhoto> getPhotosInTrashBin(Long trashBinId) {
        return trashedPhotoRepository.findByTrashBinId(trashBinId);
    }

    // 永久删除垃圾桶中的某张照片
    public void deleteFromTrash(Long id) {
        trashedPhotoRepository.deleteById(id);
    }
}
