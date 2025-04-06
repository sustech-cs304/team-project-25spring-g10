package com.g10.repository;

import com.g10.model.Album;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT COUNT(a) > 0 FROM Album a WHERE a.id = :albumId AND a.user.id = :userId")
    boolean existsByIdAndUserId(@Param("albumId") Long albumId, @Param("userId") Long userId);
}
