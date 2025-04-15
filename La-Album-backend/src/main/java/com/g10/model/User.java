package com.g10.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
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
    
    @NotEmpty(message = "用户名不能为空")
    // @Pattern(regexp = "^\\S{5,16}$", message = "用户名必须是5-16位非空字符")
    @Column(unique = true)
    private String username;
    
    @JsonIgnore // 避免在JSON序列化时暴露密码
    @NotEmpty(message = "密码不能为空")
    // @Pattern(regexp = "^\\S{5,16}$", message = "密码必须是5-16位非空字符")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String userPic; // 用户头像URL
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Album> albums;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TrashBin trashBin;
    
    // 预设创建和更新时间
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
