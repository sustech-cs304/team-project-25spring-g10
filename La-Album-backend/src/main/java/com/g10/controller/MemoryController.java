package com.g10.controller;

import com.g10.model.Memory;
import com.g10.model.MemoryPhoto;
import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.service.MemoryService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/memory")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class MemoryController {

    private final MemoryService memoryService;
    private final OssUtil ossUtil;

    @Autowired
    public MemoryController(MemoryService memoryService, OssUtil ossUtil) {
        this.memoryService = memoryService;
        this.ossUtil = ossUtil;
    }

    // 获取用户的所有记忆视频
    @GetMapping
    public Result<List<Map<String, Object>>> getAllMemories() {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        List<Memory> memories = memoryService.getAllMemoriesByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 处理每个Memory对象
        for (Memory memory : memories) {
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("id", memory.getId());
            memoryData.put("title", memory.getTitle());
            memoryData.put("duration", memory.getDuration());
            memoryData.put("transition", memory.getTransition());
            memoryData.put("bgmId", memory.getBgmId());
            memoryData.put("bgmName", memory.getBgmName() != null ? memory.getBgmName() : "默认音乐");
            memoryData.put("createdAt", memory.getCreatedAt());
            
            // 处理缩略图URL
            if (memory.getThumbnailUrl() != null && !memory.getThumbnailUrl().isEmpty()) {
                memoryData.put("thumbnailUrl", ossUtil.generateSignedUrl(memory.getThumbnailUrl()));
            } else {
                memoryData.put("thumbnailUrl", "");
            }
            
            // 设置照片数量
            int photoCount = 0;
            List<Map<String, Object>> photosList = new ArrayList<>();
            
            if (memory.getPhotos() != null) {
                photoCount = memory.getPhotos().size();
                
                // 处理照片URL
                for (MemoryPhoto photo : memory.getPhotos()) {
                    Map<String, Object> photoData = new HashMap<>();
                    photoData.put("id", photo.getPhotoId());
                    photoData.put("displayDuration", photo.getDisplayDuration());
                    
                    // 生成签名URL
                    String signedUrl = ossUtil.generateSignedUrl(photo.getThumbnailUrl());
                    photoData.put("thumbnailUrl", signedUrl);
                    
                    photosList.add(photoData);
                }
            }
            
            memoryData.put("photos", photosList);
            memoryData.put("photoCount", photoCount);
            
            // 设置相册名称
            if (memory.getAlbum() != null) {
                memoryData.put("albumName", memory.getAlbum().getTitle());
                memoryData.put("albumId", memory.getAlbum().getId());
            } else {
                memoryData.put("albumName", "默认相册");
                memoryData.put("albumId", null);
            }
            
            result.add(memoryData);
        }
        
        return Result.success(result);
    }

    // 获取记忆视频详情
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getMemoryById(@PathVariable Long id) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        Memory memory = memoryService.getMemoryById(id)
                .orElse(null);
        
        if (memory == null) {
            return Result.error("记忆视频不存在");
        }
        
        // 验证视频归属
        if (!memory.getUser().getId().equals(userId)) {
            return Result.error("无权访问该记忆视频");
        }
        
        // 处理照片数据，转换为前端期望的格式
        List<Map<String, Object>> photosData = new ArrayList<>();
        if (memory.getPhotos() != null) {
            for (MemoryPhoto memoryPhoto : memory.getPhotos()) {
                Map<String, Object> photoData = new HashMap<>();
                photoData.put("id", memoryPhoto.getPhotoId()); // 统一使用id字段
                photoData.put("photoId", memoryPhoto.getPhotoId()); // 保留photoId以兼容
                photoData.put("displayDuration", memoryPhoto.getDisplayDuration());
                
                // 生成签名URL
                String signedUrl = ossUtil.generateSignedUrl(memoryPhoto.getThumbnailUrl());
                photoData.put("thumbnailUrl", signedUrl);
                
                photosData.add(photoData);
            }
        }
        
        // 处理缩略图URL
        if (memory.getThumbnailUrl() != null && !memory.getThumbnailUrl().isEmpty()) {
            memory.setThumbnailUrl(ossUtil.generateSignedUrl(memory.getThumbnailUrl()));
        }
        
        // 根据bgmId生成背景音乐URL
        String bgmUrl = generateBgmUrl(memory.getBgmId());
        
        // 创建扩展版本的响应，包含前端需要的额外字段
        Map<String, Object> response = new HashMap<>();
        response.put("id", memory.getId());
        response.put("title", memory.getTitle());
        response.put("thumbnailUrl", memory.getThumbnailUrl());
        response.put("duration", memory.getDuration());
        response.put("transition", memory.getTransition());
        response.put("bgmId", memory.getBgmId());
        response.put("bgmName", memory.getBgmName() != null ? memory.getBgmName() : "默认音乐");
        response.put("bgmUrl", bgmUrl);  // 添加背景音乐URL
        response.put("createdAt", memory.getCreatedAt());
        response.put("photos", photosData); // 使用处理过的照片数据
        
        // 添加前端需要的额外字段
        response.put("photoCount", photosData.size());
        response.put("albumName", memory.getAlbum() != null ? memory.getAlbum().getTitle() : "默认相册");
        
        return Result.success(response);
    }
    
    // 根据背景音乐ID生成音乐URL的辅助方法
    private String generateBgmUrl(Long bgmId) {
        // 这里可以根据实际的音乐存储方式来生成URL
        // 目前提供一个简单的映射，后续可以替换为实际的音乐文件URL
        if (bgmId == null) {
            bgmId = 1L;
        }
        
        // 简单的URL映射，实际使用中应该从数据库或配置文件获取
        switch (bgmId.intValue()) {
            case 1:
                return "/api/audio/startmenu0.mp3";
            case 2:
                return "/api/audio/八方来财.mp3";
            case 3:
                return "/api/audio/The Fox.mp3";
            case 4:
                return "/api/audio/startmenu3.mp3";
            case 5:
                return "/api/audio/startmenu4.mp3";
            case 6:
                return "/api/audio/startmenu5.mp3";
            case 7:
                return "/api/audio/startmenu6.mp3";
            case 8:
                return "/api/audio/startmenu7.mp3";
            default:
                return "/api/audio/startmenu0.mp3";
        }
    }

    // 生成记忆视频
    @PostMapping("/generate")
    public Result<Memory> generateMemory(@RequestBody Map<String, Object> requestData) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        try {
            // 从请求中获取信息
            String title = (String) requestData.get("title");
            Long albumId = Long.valueOf(requestData.get("albumId").toString());
            Long bgmId = requestData.get("bgmId") != null ? Long.valueOf(requestData.get("bgmId").toString()) : 1L;
            String bgmName = (String) requestData.get("bgmName");
            String transition = (String) requestData.get("transition");
            
            // 从请求中获取照片ID列表
            @SuppressWarnings("unchecked")
            List<String> photoIdStrings = (List<String>) requestData.get("photoIds");
            List<Long> photoIds = photoIdStrings.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            
            // 从请求中获取照片显示时长信息
            @SuppressWarnings("unchecked")
            Map<String, Object> photoDisplayDurationsRaw = (Map<String, Object>) requestData.get("photoDisplayDurations");
            Map<Long, Integer> photoDisplayDurations = new HashMap<>();
            
            if (photoDisplayDurationsRaw != null) {
                photoDisplayDurationsRaw.forEach((key, value) -> {
                    Long photoId = Long.valueOf(key);
                    Integer duration = Integer.valueOf(value.toString());
                    photoDisplayDurations.put(photoId, duration);
                });
            }
            
            // 创建记忆视频
            Memory memory = new Memory();
            memory.setTitle(title);
            memory.setBgmId(bgmId);
            memory.setBgmName(bgmName);
            memory.setTransition(transition);
            
            // 调用服务创建记忆视频
            Memory createdMemory = memoryService.createMemory(memory, userId, albumId, photoIds, photoDisplayDurations);
            
            // 处理图片URL，添加签名
            if (createdMemory.getPhotos() != null) {
                createdMemory.getPhotos().forEach(memoryPhoto -> {
                    String signedUrl = ossUtil.generateSignedUrl(memoryPhoto.getThumbnailUrl());
                    memoryPhoto.setThumbnailUrl(signedUrl);
                });
            }
            
            // 处理缩略图URL
            if (createdMemory.getThumbnailUrl() != null && !createdMemory.getThumbnailUrl().isEmpty()) {
                createdMemory.setThumbnailUrl(ossUtil.generateSignedUrl(createdMemory.getThumbnailUrl()));
            }
            
            return Result.success(createdMemory);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成记忆视频失败: " + e.getMessage());
        }
    }

    // 更新记忆视频
    @PutMapping("/{id}")
    public Result<Memory> updateMemory(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        try {
            // 验证视频归属
            Memory existingMemory = memoryService.getMemoryById(id)
                    .orElseThrow(() -> new RuntimeException("记忆视频不存在"));
            
            if (!existingMemory.getUser().getId().equals(userId)) {
                return Result.error("无权修改该记忆视频");
            }
            
            // 从请求中获取信息，添加null值检查
            String title = (String) requestData.get("title");
            
            // 安全地获取bgmId
            Long bgmId = 1L; // 默认值
            Object bgmIdObj = requestData.get("bgmId");
            if (bgmIdObj != null) {
                try {
                    bgmId = Long.valueOf(bgmIdObj.toString());
                } catch (NumberFormatException e) {
                    bgmId = 1L; // 如果转换失败，使用默认值
                }
            }
            
            String bgmName = (String) requestData.get("bgmName");
            String transition = (String) requestData.get("transition");
            
            // 从请求中获取照片ID列表，添加null值检查
            @SuppressWarnings("unchecked")
            List<String> photoIdStrings = (List<String>) requestData.get("photoIds");
            List<Long> photoIds = null;
            if (photoIdStrings != null && !photoIdStrings.isEmpty()) {
                photoIds = photoIdStrings.stream()
                        .filter(Objects::nonNull) // 过滤null值
                        .map(idStr -> {
                            try {
                                return Long.valueOf(idStr);
                            } catch (NumberFormatException e) {
                                System.err.println("无法转换照片ID: " + idStr);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull) // 再次过滤转换失败的null值
                        .collect(Collectors.toList());
            }
            
            // 从请求中获取照片显示时长信息，添加null值检查
            @SuppressWarnings("unchecked")
            Map<String, Object> photoDisplayDurationsRaw = (Map<String, Object>) requestData.get("photoDisplayDurations");
            Map<Long, Integer> photoDisplayDurations = new HashMap<>();
            
            if (photoDisplayDurationsRaw != null) {
                photoDisplayDurationsRaw.forEach((key, value) -> {
                    if (key != null && value != null) {
                        try {
                            Long photoId = Long.valueOf(key);
                            Integer duration = Integer.valueOf(value.toString());
                            photoDisplayDurations.put(photoId, duration);
                        } catch (NumberFormatException e) {
                            System.err.println("无法转换照片时长数据: key=" + key + ", value=" + value);
                        }
                    }
                });
            }
            
            // 创建更新数据
            Memory updateData = new Memory();
            updateData.setTitle(title);
            updateData.setBgmId(bgmId);
            updateData.setBgmName(bgmName);
            updateData.setTransition(transition);
            
            // 调用服务更新记忆视频
            Memory updatedMemory = memoryService.updateMemory(id, updateData, photoIds, photoDisplayDurations);
            
            // 处理图片URL，添加签名
            if (updatedMemory.getPhotos() != null) {
                updatedMemory.getPhotos().forEach(memoryPhoto -> {
                    String signedUrl = ossUtil.generateSignedUrl(memoryPhoto.getThumbnailUrl());
                    memoryPhoto.setThumbnailUrl(signedUrl);
                });
            }
            
            // 处理缩略图URL
            if (updatedMemory.getThumbnailUrl() != null && !updatedMemory.getThumbnailUrl().isEmpty()) {
                updatedMemory.setThumbnailUrl(ossUtil.generateSignedUrl(updatedMemory.getThumbnailUrl()));
            }
            
            return Result.success(updatedMemory);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新记忆视频失败: " + e.getMessage());
        }
    }

    // 删除记忆视频
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemory(@PathVariable Long id) {
        // 从ThreadLocal中获取当前用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        Long userId = Long.valueOf(userInfo.get("id").toString());
        
        try {
            // 验证视频归属
            Memory existingMemory = memoryService.getMemoryById(id)
                    .orElseThrow(() -> new RuntimeException("记忆视频不存在"));
            
            if (!existingMemory.getUser().getId().equals(userId)) {
                return Result.error("无权删除该记忆视频");
            }
            
            // 调用服务删除记忆视频
            memoryService.deleteMemory(id);
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除记忆视频失败: " + e.getMessage());
        }
    }
} 