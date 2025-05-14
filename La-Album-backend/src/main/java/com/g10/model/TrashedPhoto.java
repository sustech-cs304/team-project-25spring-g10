package com.g10.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrashedPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long originalPhotoId; // 原照片的 ID（可选）
    private String title;
    private String url;
    private String location;

    // @ElementCollection
    // @CollectionTable(name = "trashed_photo_tags", joinColumns = @JoinColumn(name = "trashed_photo_id"))
    // @Column(name = "tag")
    // @Cascade(CascadeType.REMOVE)
    private String tags;

    private LocalDateTime uploadTime;
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "albums_id")
    private Album originalAlbum; // 用于还原时知道是哪个相册（或改成 Album 实体也可以）

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Long getAlbumId() {
        return originalAlbum.getId();
    }


    @PrePersist
    protected void onDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public TrashedPhoto(Photo photo) {
        this.originalPhotoId = photo.getId();
        this.title = photo.getTitle();
        this.url = photo.getUrl();
        this.location = photo.getLocation();
        this.uploadTime = photo.getUploadTime();
        this.originalAlbum = photo.getAlbum();
        this.user = photo.getAlbum().getUser();
        this.deletedAt = LocalDateTime.now();
        this.tags = photo.getTags();
    }

}