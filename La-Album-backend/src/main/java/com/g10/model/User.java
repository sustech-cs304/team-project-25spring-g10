package com.g10.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Album> albums;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TrashBin trashBin;
}
