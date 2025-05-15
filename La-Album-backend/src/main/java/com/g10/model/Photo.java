package com.g10.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name = "photos")
@Data // 使用 Lombok 生成 getter、setter、toString
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 照片 ID

    private String title;  // 照片标题
    private String description;  // 照片描述
    private String url;  // 照片存储的 URL
    private String location;  // 拍摄地点
    private String date;  // 拍摄日期

    // @ElementCollection
    // @CollectionTable(name = "photo_tags", joinColumns = @JoinColumn(name = "photo_id"))
    // @Column(name = "tag")
    // @Cascade(CascadeType.REMOVE)
    private String tags;

    private LocalDateTime uploadTime;  // 上传时间

    @ManyToOne
    @JoinColumn(name = "albums_id")
    @JsonBackReference
    private Album album;

    @PrePersist
    protected void onCreate() {
        this.uploadTime = LocalDateTime.now();
    }
}
