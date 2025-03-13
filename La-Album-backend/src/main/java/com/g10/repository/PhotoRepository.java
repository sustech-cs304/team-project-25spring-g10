package com.g10.repository;

import com.g10.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // 根据标签搜索照片
    List<Photo> findByTagsContaining(String tag);

    // 根据地点搜索照片
    List<Photo> findByLocation(String location);
}
