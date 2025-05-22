package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.User;
import com.g10.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private AlbumRepository albumRepository;
    private PhotoRepository photoRepository;
    private TrashedPhotoRepository trashedPhotoRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        albumRepository = mock(AlbumRepository.class);
        photoRepository = mock(PhotoRepository.class);
        trashedPhotoRepository = mock(TrashedPhotoRepository.class);

        // 用反射注入依赖（也可以用构造函数注入所有依赖）
        userService = new UserService(userRepository);
        userService.getClass().getDeclaredFields();
        userService.getClass().getDeclaredFields();
        // 使用反射设置其他 repository
        try {
            var albumField = UserService.class.getDeclaredField("albumRepository");
            albumField.setAccessible(true);
            albumField.set(userService, albumRepository);

            var photoField = UserService.class.getDeclaredField("photoRepository");
            photoField.setAccessible(true);
            photoField.set(userService, photoRepository);

            var trashedPhotoField = UserService.class.getDeclaredField("trashedPhotoRepository");
            trashedPhotoField.setAccessible(true);
            trashedPhotoField.set(userService, trashedPhotoRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        var result = userService.getAllUsers();
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetUserByUsername() {
        User user = new User();
        user.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        var result = userService.getUserByUsername("testUser");
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("newUser");
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.createUser(user);
        assertEquals("newUser", result.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserWhenExists() {
        User user = new User();
        user.setUsername("updated");
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.updateUser(1L, user);
        assertEquals("updated", result.getUsername());
        assertEquals(1L, result.getId());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserWhenNotExists() {
        User user = new User();
        when(userRepository.existsById(1L)).thenReturn(false);

        var result = userService.updateUser(1L, user);
        assertNull(result);
    }

    @Test
    void testDeleteUser() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);

        Photo photo = new Photo();
        Album album = new Album();
        album.setPhotos(Collections.singletonList(photo));
        user.setAlbums(Collections.singletonList(album));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);

        // 执行
        boolean result = userService.deleteUser(1L);

        // 验证调用链
        verify(photoRepository).delete(photo);
        verify(albumRepository).delete(album);
        verify(trashedPhotoRepository).deleteByUserId(1L);
        verify(userRepository).delete(user);

        assertTrue(result);
    }
}
