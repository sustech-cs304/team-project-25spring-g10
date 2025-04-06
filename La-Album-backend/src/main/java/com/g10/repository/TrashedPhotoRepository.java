package com.g10.repository;

import com.g10.model.TrashedPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrashedPhotoRepository extends JpaRepository<TrashedPhoto, Long> {
    public List<TrashedPhoto> findByTrashBinId(Long trashBinId);

    public void deleteByTrashBinId(Long id);
}
