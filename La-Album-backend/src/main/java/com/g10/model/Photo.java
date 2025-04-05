package com.g10.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Data // 使用 Lombok 生成 getter、setter、toString
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 照片 ID

    private String title;  // 照片标题
    private String url;  // 照片存储的 URL
    private String location;  // 拍摄地点
    private String tags;  // 照片标签（逗号分隔）
    private LocalDateTime uploadTime;  // 上传时间
    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @PrePersist
    protected void onCreate() {
        this.uploadTime = LocalDateTime.now(); // 自动设置上传时间
        this.isDeleted = false;
    }
}
