package com.g10.service;

import com.g10.model.User;
import com.g10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 根据ID查找用户
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 根据用户名查找用户
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 创建新用户
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // 更新用户
    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);  // 确保ID保持不变
            return userRepository.save(user);
        }
        return null;
    }

    // TODO: 要把albums之类的一起删了？？
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
