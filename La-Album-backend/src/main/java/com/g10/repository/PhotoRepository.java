package com.g10.repository;

import com.g10.model.Photo;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // 根据标签搜索照片
    List<Photo> findByTagsContaining(String tag);

    // 根据地点搜索照片
    List<Photo> findByLocation(String location);

    List<Photo> findByAlbum_Id(Long albumId);


    @Query("SELECT p FROM Photo p WHERE " +
            "(:q IS NULL OR p.title LIKE %:q% OR p.tags LIKE %:q%) AND " +
            "(:startDate IS NULL OR p.uploadTime >= :startDate) AND " +
            "(:endDate IS NULL OR p.uploadTime <= :endDate) AND " +
            "(:albumId IS NULL OR p.album.id = :albumId)")
    List<Photo> searchPhotos(@Param("q") String q,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("albumId") Long albumId);
}

