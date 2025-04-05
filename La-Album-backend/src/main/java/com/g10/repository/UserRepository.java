package com.g10.repository;

import com.g10.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 你可以根据需要添加自定义查询方法
    User findByUsername(String username);
}
