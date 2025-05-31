package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.UserService;
import com.g10.utils.JwtUtil;
import com.g10.utils.Md5Util;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AlbumService albumService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(Md5Util.getMD5String("password"));
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));
        Result<List<User>> result = userController.getAllUsers();
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("testuser", result.getData().get(0).getUsername());
    }

    @Test
    void testGetUserById_found() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        Result<User> result = userController.getUserById(1L);
        assertEquals(0, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
    }

    @Test
    void testGetUserById_notFound() {
        when(userService.getUserById(2L)).thenReturn(Optional.empty());
        Result<User> result = userController.getUserById(2L);
        assertEquals(1, result.getCode());
    }

    @Test
    void testRegister_userExists() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        Result result = userController.register("testuser", "password");
        assertEquals(1, result.getCode());
    }

    @Test
    void testRegister_success() {
        when(userService.getUserByUsername("testuser")).thenReturn(null);
        when(userService.createUser(any(User.class))).thenReturn(user);
        when(albumService.createAlbum(any(Album.class))).thenReturn(new Album());
        Result result = userController.register("testuser", "password");
        assertEquals(0, result.getCode());
    }

    @Test
    void testLogin_success() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Result<String> result = userController.login("testuser", "password");
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testLogin_wrongPassword() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        Result<String> result = userController.login("testuser", "wrongpass");
        assertEquals(1, result.getCode());
    }

    @Test
    void testDeleteUser_success() {
        when(userService.deleteUser(1L)).thenReturn(true);
        Result result = userController.deleteUser(1L);
        assertEquals(0, result.getCode());
    }

    @Test
    void testDeleteUser_failure() {
        when(userService.deleteUser(1L)).thenReturn(false);
        Result result = userController.deleteUser(1L);
        assertEquals(1, result.getCode());
    }

    // 你还可以继续写：updateAvatar, update, logout, getUserByUsername, userInfo 等方法的测试
}
