package com.g10.repository;

import com.g10.model.Album;
import com.g10.model.TrashedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrashedPhotoRepository extends JpaRepository<TrashedPhoto, Long> {
    public List<TrashedPhoto> findByUserId(Long UserId);

    public void deleteByUserId(Long UserId);

    List<TrashedPhoto> findByOriginalAlbum(Album album);
}
