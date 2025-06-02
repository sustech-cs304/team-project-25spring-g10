package com.g10.controller;

import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.UserService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumControllerTest {

    @InjectMocks
    private AlbumController albumController;

    @Mock
    private AlbumService albumService;

    @Mock
    private UserService userService;

    @Mock
    private OssUtil ossUtil;

    @BeforeEach
    void setUp() {
        // 模拟当前登录用户
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove(); // 清理 ThreadLocal，避免影响其他测试
    }

    @Test
    void testGetPhotosInAlbum_success() {
        // 准备模拟数据
        Long userId = 1000L;
        Long albumId = 2000L;

        // 1. 相册属于当前用户
        User user = new User();
        user.setId(userId);
        Album album = new Album();
        album.setId(albumId);
        album.setUser(user);

        // 2. PhotoDTO 列表
        PhotoDTO photoDTO = new PhotoDTO(
                3000L,                     // id
                "title",                  // title
                "originalUrl",            // url
                "location",               // location
                "tag1,tag2",              // tags
                LocalDateTime.now(),      // uploadTime
                albumId,                  // albumId
                "2024-06-01",             // date
                "description",            // description
                "albumTitle"              // albumTitle
        );

        // 3. 模拟依赖调用
        when(albumService.getAlbumById(albumId)).thenReturn(album);
        when(albumService.getPhotosInAlbum(albumId)).thenReturn(List.of(photoDTO));
        when(ossUtil.generateSignedUrl("originalUrl")).thenReturn("signedUrl");

        // 执行
        Result<List<PhotoDTO>> result = albumController.getPhotosInAlbum(albumId);

        // 验证返回
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());

        PhotoDTO returnedPhoto = result.getData().get(0);
        assertEquals("signedUrl", returnedPhoto.getUrl());
        assertEquals("title", returnedPhoto.getTitle());
        assertEquals("albumTitle", returnedPhoto.getAlbumTitle());
    }

    @Test
    void testGetPhotosInAlbum_albumNotExist() {
        when(albumService.getAlbumById(9999L)).thenReturn(null);

        Result<List<PhotoDTO>> result = albumController.getPhotosInAlbum(9999L);

        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals("相册不存在", result.getMessage());
    }

    @Test
    void testGetPhotosInAlbum_userMismatch() {
        // 当前用户 ID 是 1000，但相册属于 2000
        User anotherUser = new User();
        anotherUser.setId(2000L);
        Album album = new Album();
        album.setId(3000L);
        album.setUser(anotherUser);

        when(albumService.getAlbumById(3000L)).thenReturn(album);

        Result<List<PhotoDTO>> result = albumController.getPhotosInAlbum(3000L);

        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals("无权访问该相册", result.getMessage());
    }

    @Test
    void testGetAllAlbumsByType() {
        Photo photo = new Photo();
        photo.setUrl("url");

        Album album = new Album();
        album.setPhotos(List.of(photo));

        when(albumService.getAllAlbumsByType("default")).thenReturn(List.of(album));
        when(ossUtil.generateSignedUrl("url")).thenReturn("signedUrl");

        Result<List<Album>> result = albumController.getAllAlbums();

        assertEquals(1, result.getData().size());
        assertEquals("signedUrl", result.getData().get(0).getPhotos().get(0).getUrl());
    }

    @Test
    void testCreateAlbum_success() {
        // 模拟 ThreadLocal 当前用户
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        ThreadLocalUtil.set(userInfo);

        // 初始化参数
        Album album = new Album();
        User user = new User();
        user.setId(1L);

        // mock 行为
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(albumService.createAlbum(any(Album.class))).thenReturn(album);

        // 调用 controller
        Result<Album> result = albumController.createAlbum(album);

        // 断言
        assertEquals(album, result.getData());
        assertEquals("default", album.getType());
        assertEquals(user, album.getUser());

        ThreadLocalUtil.remove();
    }


    @Test
    void testCreateAlbum_userNotFound() {
        // 模拟 ThreadLocal 当前用户
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        ThreadLocalUtil.set(userInfo);

        // mock 行为
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // 断言异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            albumController.createAlbum(new Album());
        });

        assertEquals("User not found", exception.getMessage());

        ThreadLocalUtil.remove();
    }

    @Test
    void testCreateAlbumByType_success() {
        // 模拟 ThreadLocal 当前用户
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        ThreadLocalUtil.set(userInfo);

        // 初始化参数
        Album album = new Album();
        User user = new User();
        user.setId(1L);

        // mock 行为
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(albumService.createAlbum(any(Album.class))).thenReturn(album);

        // 调用 controller
        Result<Album> result = albumController.createAlbumByType(album);

        // 断言
        assertEquals(album, result.getData());
        assertEquals(user, album.getUser());

        ThreadLocalUtil.remove();
    }


    @Test
    void testGetAlbumById_success() {
        // 模拟 ThreadLocal 当前用户
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        ThreadLocalUtil.set(userInfo);

        // 准备数据
        Album album = new Album();
        album.setId(1L);
        User user = new User();
        user.setId(1L);
        album.setUser(user);

        Photo photo = new Photo();
        photo.setUrl("url");
        album.setPhotos(List.of(photo));

        // mock 行为
        when(albumService.getAlbumById(1L)).thenReturn(album);
        when(ossUtil.generateSignedUrl("url")).thenReturn("signedUrl");

        // 调用 controller
        Result<Album> result = albumController.getAlbumById(1L);

        // 断言
        assertEquals("signedUrl", result.getData().getPhotos().get(0).getUrl());
        assertEquals(1L, result.getData().getUser().getId());

        ThreadLocalUtil.remove();
    }

    @Test
    void testGetAlbumById_notFound() {
        when(albumService.getAlbumById(999L)).thenReturn(null);

        Result<Album> result = albumController.getAlbumById(999L);

        assertEquals("相册不存在", result.getMessage());
    }

    @Test
    void testGetAlbumById_unauthorized() {
        Album album = new Album();
        User user = new User();
        user.setId(2L); // 与 ThreadLocal 中 ID 不一致
        album.setUser(user);

        when(albumService.getAlbumById(1L)).thenReturn(album);

        Result<Album> result = albumController.getAlbumById(1L);

        assertEquals("无权访问该相册", result.getMessage());
    }

    @Test
    void testUpdateAlbum_success() {
        // 模拟 ThreadLocal 中的用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        // 原始相册
        Album existingAlbum = new Album();
        existingAlbum.setId(1000L);
        User user = new User();
        user.setId(1000L);
        existingAlbum.setUser(user);

        // 要更新的相册（只设置 title 模拟更新内容）
        Album updatedAlbum = new Album();
        updatedAlbum.setTitle("Updated Album");

        // 设置 Mock 行为
        when(albumService.getAlbumById(1000L)).thenReturn(existingAlbum);
        when(albumService.updateAlbum(eq(1000L), any(Album.class))).thenReturn(updatedAlbum);

        // 调用被测方法
        Result<Album> result = albumController.updateAlbum(1000L, updatedAlbum);

        // 断言结果
        assertEquals("Updated Album", result.getData().getTitle());
        assertEquals(user.getId(), updatedAlbum.getUser().getId());

        // 清理 ThreadLocal
        ThreadLocalUtil.remove();
    }


    @Test
    void testUpdateAlbum_notFound() {
        when(albumService.getAlbumById(99L)).thenReturn(null);

        Result<Album> result = albumController.updateAlbum(99L, new Album());

        assertEquals("相册不存在", result.getMessage());
    }

    @Test
    void testUpdateAlbum_unauthorized() {
        Album album = new Album();
        User user = new User();
        user.setId(2L);
        album.setUser(user);

        when(albumService.getAlbumById(1L)).thenReturn(album);

        Result<Album> result = albumController.updateAlbum(1L, new Album());

        assertEquals("无权修改该相册", result.getMessage());
    }

    @Test
    void testDeleteAlbum_success() {
        Album album = new Album();
        User user = new User();
        user.setId(1L);
        album.setUser(user);

        when(albumService.getAlbumById(1L)).thenReturn(album);

        Result result = albumController.deleteAlbum(1L);
        assertEquals(1, result.getCode());
    }

    @Test
    void testDeleteAlbum_notFound() {
        when(albumService.getAlbumById(1L)).thenReturn(null);

        Result result = albumController.deleteAlbum(1L);

        assertEquals("相册不存在", result.getMessage());
    }

    @Test
    void testDeleteAlbum_unauthorized() {
        Album album = new Album();
        User user = new User();
        user.setId(999L);
        album.setUser(user);

        when(albumService.getAlbumById(1L)).thenReturn(album);

        Result result = albumController.deleteAlbum(1L);
        assertEquals("无权删除该相册", result.getMessage());
    }

    @Test
    void testDeleteAlbum_exception() {
        Album album = new Album();
        User user = new User();
        user.setId(1L);
        album.setUser(user);

        when(albumService.getAlbumById(1L)).thenReturn(album);
        Result result = albumController.deleteAlbum(1L);
        assertEquals(1, result.getCode());
    }
}

