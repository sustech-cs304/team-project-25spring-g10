package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashBin;
import com.g10.model.User;
import com.g10.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private AlbumRepository albumRepository;
    private PhotoRepository photoRepository;
    private TrashBinRepository trashBinRepository;
    private TrashedPhotoRepository trashedPhotoRepository;

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

    /**
     * 删除用户及其所有相关的相册、照片和垃圾桶
     * @param userId 用户 ID
     */
    @Transactional
    public boolean deleteUser(Long userId) {
        boolean isDeleted = !userRepository.existsById(userId);
        // 通过 userId 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 删除用户的 TrashBin（级联删除）
        TrashBin trashBin = user.getTrashBin();
        if (trashBin != null) {
            trashedPhotoRepository.deleteByTrashBinId(trashBin.getId());  // 删除所有垃圾照片
            trashBinRepository.delete(trashBin);  // 删除垃圾桶
        }

        // 删除用户的所有相册及其中的所有照片（级联删除）
        for (Album album : user.getAlbums()) {
            for (Photo photo : album.getPhotos()) {
                photoRepository.delete(photo);  // 删除相册中的照片
            }
            albumRepository.delete(album);  // 删除相册
        }

        // 删除该用户所有被删除的照片（TrashedPhoto）
        assert user.getTrashBin() != null;
        trashedPhotoRepository.deleteByTrashBinId(user.getTrashBin().getId());

        // 最后删除用户
        userRepository.delete(user);
        return !isDeleted;
    }
}
