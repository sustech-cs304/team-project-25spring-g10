package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashBinRepository;
import com.g10.repository.TrashedPhotoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrashBinService {
    @Autowired
    private TrashBinRepository trashBinRepository;
    private TrashedPhotoRepository trashedPhotoRepository;
    private PhotoRepository photoRepository;
    private AlbumRepository albumRepository;

    public TrashBinService(PhotoRepository photoRepository) {
    }

//    //TODO: find photo
//    public Photo findPhotoById(Long photoId) {
//        return null;
//    }


    @Transactional
    public void automaticDeletePhoto() {
        List<TrashedPhoto> trashedPhotos = trashedPhotoRepository.findAll();
        long currentTime = System.currentTimeMillis();
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000; // 30 天的毫秒数

        for (TrashedPhoto trashedPhoto : trashedPhotos) {
            if (trashedPhoto.getDeletedAt() != null) {
                long deletedAtMillis = trashedPhoto.getDeletedAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                if (currentTime - deletedAtMillis > thirtyDaysInMillis) {
                    trashedPhotoRepository.delete(trashedPhoto);
                }
            }
        }
    }


    // TODO: use to test automatic delete
    public void setDeleteDate(Long photoId) {}


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