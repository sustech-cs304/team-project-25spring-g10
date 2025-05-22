package com.g10.service;

import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import com.g10.repository.AlbumRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final TrashedPhotoRepository trashedPhotoRepository;
    private final AlbumRepository albumRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public PhotoService(PhotoRepository photoRepository, TrashedPhotoRepository trashedPhotoRepository, AlbumRepository albumRepository) {
        this.photoRepository = photoRepository;
        this.trashedPhotoRepository = trashedPhotoRepository;
        this.albumRepository = albumRepository;
    }

    // 获取所有照片
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }


    public Optional<PhotoDTO> getPhotoById(Long id) {
        return photoRepository.findById(id)
                .map(photo -> new PhotoDTO(
                        photo.getId(),
                        photo.getTitle(),
                        photo.getUrl(),
                        photo.getLocation(),
                        photo.getTags(),
                        photo.getUploadTime(),
                        photo.getAlbum() != null ? photo.getAlbum().getId() : null,
                        photo.getDate(),
                        photo.getDescription(),
                        photo.getAlbum() != null ? photo.getAlbum().getTitle(): "Unknown Album"
                ));
    }

    public Optional<Photo> getPhotoEntityById(Long id) {
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


    public void movePhoto(Long photoId, Long destAlbumId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        Album destAlbum = albumRepository.findById(destAlbumId)
                .orElseThrow(() -> new RuntimeException("Destination album not found"));

        Album oldAlbum = photo.getAlbum();
        if (oldAlbum != null) {
            oldAlbum.getPhotos().remove(photo);
        }

        destAlbum.getPhotos().add(photo);
        photo.setAlbum(destAlbum);

        photoRepository.save(photo);
    }


    public List<Photo> searchPhotos(String q, LocalDate startDate, LocalDate endDate, Long albumId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Photo> query = cb.createQuery(Photo.class);
        Root<Photo> root = query.from(Photo.class);

        List<Predicate> predicates = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            String pattern = "%" + q + "%";
            Predicate titleLike = cb.like(root.get("title"), pattern);
            Predicate descriptionLike = cb.like(root.get("tags"), pattern);
            predicates.add(cb.or(titleLike, descriptionLike));
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(root.get("uploadTime"), startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            predicates.add(cb.lessThanOrEqualTo(root.get("uploadTime"), endDateTime));
        }

        if (albumId != null) {
            predicates.add(cb.equal(root.get("album").get("id"), albumId));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        return entityManager.createQuery(query).getResultList();
    }
}
