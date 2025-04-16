package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final TrashedPhotoRepository trashedPhotoRepository;

    public PhotoService(PhotoRepository photoRepository, TrashedPhotoRepository trashedPhotoRepository) {
        this.photoRepository = photoRepository;
        this.trashedPhotoRepository = trashedPhotoRepository;
    }

    // 获取所有照片
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    // 通过 ID 获取单张照片
    public Optional<Photo> getPhotoById(Long id) {
        return photoRepository.findById(id);
    }

    // 保存（上传）照片
    public Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    public void deletePhoto(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        TrashedPhoto trashedPhoto = new TrashedPhoto(photo);
        trashedPhotoRepository.save(trashedPhoto);
        photoRepository.deleteById(id);
    }


    public void movePhoto(Long id, Album dest) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        photo.setAlbum(dest);
        photoRepository.save(photo);
    }
}
