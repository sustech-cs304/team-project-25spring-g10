package com.g10.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PhotoDTO {
    private Long id;
    private String title;
    private String url;
    private String location;
    private String tags;
    private LocalDateTime uploadTime;
    private Long albumId;
    private String date;
    private String description;
    private String albumTitle;


    public PhotoDTO(Long id, String title, String url, String location, String tags,
                    LocalDateTime uploadTime, Long albumId, String date, String description,
                    String albumTitle) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.location = location;
        this.tags = tags;
        this.uploadTime = uploadTime;
        this.albumId = albumId;
        this.date = date;
        this.description = description;
        this.albumTitle = albumTitle;
    }
}
