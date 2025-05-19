package com.g10.repository;

import com.g10.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long> {
    
    // 通过用户ID查找记忆视频
    @Query("SELECT m FROM Memory m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<Memory> findByUserId(@Param("userId") Long userId);
    
    // 通过相册ID查找记忆视频
    @Query("SELECT m FROM Memory m WHERE m.album.id = :albumId")
    List<Memory> findByAlbumId(@Param("albumId") Long albumId);
    
    // 通过用户ID和相册ID查找记忆视频
    @Query("SELECT m FROM Memory m WHERE m.user.id = :userId AND m.album.id = :albumId")
    List<Memory> findByUserIdAndAlbumId(@Param("userId") Long userId, @Param("albumId") Long albumId);
} 