package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Memory;
import com.g10.model.MemoryPhoto;
import com.g10.model.Photo;
import com.g10.model.User;
import com.g10.repository.AlbumRepository;
import com.g10.repository.MemoryRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MemoryService {
    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    @Autowired
    public MemoryService(MemoryRepository memoryRepository, UserRepository userRepository,
                         AlbumRepository albumRepository, PhotoRepository photoRepository) {
        this.memoryRepository = memoryRepository;
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    // 获取用户的所有记忆视频
    public List<Memory> getAllMemoriesByUserId(Long userId) {
        return memoryRepository.findByUserId(userId);
    }

    // 创建记忆视频
    @Transactional
    public Memory createMemory(Memory memory, Long userId, Long albumId, List<Long> photoIds, Map<Long, Integer> photoDisplayDurations) {
        // 设置用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        memory.setUser(user);

        // 设置相册
        if (albumId != null) {
            Album album = albumRepository.findById(albumId)
                    .orElseThrow(() -> new RuntimeException("相册不存在"));
            memory.setAlbum(album);
        }

        // 初始化照片列表
        List<MemoryPhoto> memoryPhotos = new ArrayList<>();

        // 如果提供了照片ID列表，则添加这些照片
        if (photoIds != null && !photoIds.isEmpty()) {
            for (Long photoId : photoIds) {
                Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException("照片不存在: " + photoId));
                
                // 创建新的MemoryPhoto对象
                MemoryPhoto memoryPhoto = new MemoryPhoto();
                memoryPhoto.setPhotoId(photoId);
                memoryPhoto.setThumbnailUrl(photo.getUrl());
                
                // 设置显示时长，如果没有指定，则默认为5秒
                Integer duration = (photoDisplayDurations != null && photoDisplayDurations.containsKey(photoId)) 
                        ? photoDisplayDurations.get(photoId) 
                        : 5;
                memoryPhoto.setDisplayDuration(duration);
                
                memoryPhotos.add(memoryPhoto);
            }
        }

        memory.setPhotos(memoryPhotos);
        
        // 计算总时长
        long totalDuration = memoryPhotos.stream()
                .mapToInt(MemoryPhoto::getDisplayDuration)
                .sum();
        memory.setDuration(totalDuration);
        
        // 设置缩略图URL（使用第一张照片）
        if (!memoryPhotos.isEmpty()) {
            memory.setThumbnailUrl(memoryPhotos.get(0).getThumbnailUrl());
        }
        
        // 保存记忆视频
        return memoryRepository.save(memory);
    }

    // 获取记忆视频详情
    public Optional<Memory> getMemoryById(Long id) {
        return memoryRepository.findById(id);
    }

    // 更新记忆视频
    @Transactional
    public Memory updateMemory(Long id, Memory updateData, List<Long> photoIds, Map<Long, Integer> photoDisplayDurations) {
        Memory existingMemory = memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记忆视频不存在"));
        
        // 更新基本信息
        existingMemory.setTitle(updateData.getTitle());
        existingMemory.setBgmId(updateData.getBgmId());
        existingMemory.setBgmName(updateData.getBgmName());
        existingMemory.setTransition(updateData.getTransition());
        
        // 如果提供了新的照片列表，则更新
        if (photoIds != null && !photoIds.isEmpty()) {
            List<MemoryPhoto> newMemoryPhotos = new ArrayList<>();
            
            for (Long photoId : photoIds) {
                Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new RuntimeException("照片不存在: " + photoId));
                
                // 创建新的MemoryPhoto对象
                MemoryPhoto memoryPhoto = new MemoryPhoto();
                memoryPhoto.setPhotoId(photoId);
                memoryPhoto.setThumbnailUrl(photo.getUrl());
                
                // 设置显示时长，如果没有指定，则默认为5秒
                Integer duration = (photoDisplayDurations != null && photoDisplayDurations.containsKey(photoId)) 
                        ? photoDisplayDurations.get(photoId) 
                        : 5;
                memoryPhoto.setDisplayDuration(duration);
                
                newMemoryPhotos.add(memoryPhoto);
            }
            
            existingMemory.setPhotos(newMemoryPhotos);
            
            // 更新缩略图（使用第一张照片）
            if (!newMemoryPhotos.isEmpty()) {
                existingMemory.setThumbnailUrl(newMemoryPhotos.get(0).getThumbnailUrl());
            }
            
            // 重新计算总时长
            long totalDuration = newMemoryPhotos.stream()
                    .mapToInt(MemoryPhoto::getDisplayDuration)
                    .sum();
            existingMemory.setDuration(totalDuration);
        }
        
        // 保存更新
        return memoryRepository.save(existingMemory);
    }

    // 删除记忆视频
    @Transactional
    public void deleteMemory(Long id) {
        if (memoryRepository.existsById(id)) {
            memoryRepository.deleteById(id);
        } else {
            throw new RuntimeException("记忆视频不存在");
        }
    }
} 