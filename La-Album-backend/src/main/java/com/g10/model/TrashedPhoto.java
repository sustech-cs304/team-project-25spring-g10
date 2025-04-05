package com.g10.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Embeddable
public class TrashedPhoto {
    @Column(name = "photo_id", nullable = false)
    private Long photoId;  // 照片ID（外键）

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 新增列：删除时间

    // Getters & Setters
    // 必须有无参构造函数
}