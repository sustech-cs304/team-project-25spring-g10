package com.g10.service;

import com.g10.model.*;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final TrashedPhotoRepository trashedPhotoRepository;

    public AlbumService(AlbumRepository albumRepository, 
                       PhotoRepository photoRepository,
                       TrashedPhotoRepository trashedPhotoRepository) {
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
        this.trashedPhotoRepository = trashedPhotoRepository;
    }

    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    public List<Album> getAllAlbumsByUserId(Long userId) {
        return albumRepository.findByUserId(userId);
    }

    public Album createAlbum(Album album) {
        return albumRepository.save(album);
    }

    public Album getAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    public Album updateAlbum(Long id, Album updatedAlbum) {
        if (albumRepository.existsById(id)) {
            updatedAlbum.setId(id);
            return albumRepository.save(updatedAlbum);
        }
        return null;
    }

    public List<Photo> getPhotosInAlbum(Long albumId) {
        return photoRepository.findByAlbumId(albumId);
    }

    //    TODO: 把属于这个album的照片放到trashBin里
    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        User user = album.getUser();
        TrashBin trashBin = user.getTrashBin();

        if (trashBin == null) {
            throw new RuntimeException("User does not have a trash bin.");
        }
        // 处理照片：移动到 TrashBin
        for (Photo photo : album.getPhotos()) {
            TrashedPhoto trashed = new TrashedPhoto(photo, trashBin);
            trashedPhotoRepository.save(trashed);
        }

        // 删除相册（级联删除照片）
        albumRepository.delete(album);
    }
}
