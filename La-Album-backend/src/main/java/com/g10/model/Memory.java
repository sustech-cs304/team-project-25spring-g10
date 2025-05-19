package com.g10.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "memories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;           // 视频标题
    private String thumbnailUrl;    // 视频缩略图 URL
    private Long duration;          // 视频时长（秒）
    private String transition;      // 转场效果（fade, slide, zoom, rotate, random）
    private Long bgmId;             // 背景音乐ID
    private String bgmName;         // 背景音乐名称
    private LocalDateTime createdAt; // 创建时间

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;            // 所属用户

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;          // 关联的相册

    // 记忆视频中包含的照片及其显示时长
    @ElementCollection
    @CollectionTable(name = "memory_photos", joinColumns = @JoinColumn(name = "memory_id"))
    private List<MemoryPhoto> photos;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
} 