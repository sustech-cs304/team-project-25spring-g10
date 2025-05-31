package com.g10.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDateTime createTime;
    private String type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Photo> photos;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}

