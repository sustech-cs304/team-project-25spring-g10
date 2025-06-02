package com.g10.service;

import com.g10.dto.PhotoDTO;
import com.g10.model.*;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<Album> getAllAlbumsByType(String typeName) {
        return albumRepository.findByType(typeName);
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

    public List<PhotoDTO> getPhotosInAlbum(Long albumId) {
        List<Photo> photos = photoRepository.findByAlbum_Id(albumId);
        return photos.stream().map(photo -> new PhotoDTO(
                photo.getId(),
                photo.getTitle(),
                photo.getUrl(),
                photo.getLocation(),
                photo.getTags(),
                photo.getUploadTime(),
                photo.getAlbum() != null ? photo.getAlbum().getId() : null,
                photo.getDate(),
                photo.getDescription(),
                photo.getAlbum() != null ? photo.getAlbum().getTitle() : null
        )).collect(Collectors.toList());
    }


    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        Album defaultAlbum = getDefaultAlbumForUser(album.getUser().getId());

        if (album.getId().equals(defaultAlbum.getId())) {
            throw new RuntimeException("默认相册不能被删除");
        }

        // 找到所有引用该 album 的 TrashedPhoto，并删除或断开引用
        List<TrashedPhoto> referencingTrashedPhotos =
                trashedPhotoRepository.findByOriginalAlbum(album);
        for (TrashedPhoto tp : referencingTrashedPhotos) {
            tp.setOriginalAlbum(defaultAlbum);
        }
        trashedPhotoRepository.saveAll(referencingTrashedPhotos);

        // move photos to trashBin, the original album of trashed photo will be default album
        for (Photo photo : album.getPhotos()) {
            TrashedPhoto trashed = new TrashedPhoto(photo);
            trashed.setOriginalAlbum(defaultAlbum);
            trashedPhotoRepository.save(trashed);
            photoRepository.delete(photo);
        }
        album.setUser(null);
        albumRepository.delete(album);
    }


    public Album getDefaultAlbumForUser(Long userId) {
        return albumRepository.findByUserIdAndTitle(userId, "全部照片");
    }
}
