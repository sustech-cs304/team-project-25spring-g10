package com.g10.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPhoto {
    private Long photoId;           // 照片ID
    private String thumbnailUrl;    // 照片缩略图URL
    private Integer displayDuration; // 照片显示时长（秒）
} 