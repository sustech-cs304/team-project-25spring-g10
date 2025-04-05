package com.g10.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TrashBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(
            name = "trash_photos",
            joinColumns = @JoinColumn(name = "trashbin_id")  // 外键指向TrashBin
    )
    private List<TrashedPhoto> trashedPhotos;  // 替换原来的photoIds

    private LocalDateTime deleteTime; //删除时间
}
