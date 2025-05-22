package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.model.User;
import com.g10.service.AlbumService;
import com.g10.service.UserService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    @Mock
    private UserService userService;

    @Mock
    private OssUtil ossUtil;

    @InjectMocks
    private AlbumController albumController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
//        albumService = mock(AlbumService.class);
//        userService = mock(UserService.class);
//        ossUtil = mock(OssUtil.class);
//        albumController = new AlbumController(albumService, userService, ossUtil);
    }

    @AfterEach
    void tearDown() throws Exception {
        ThreadLocalUtil.remove();
//        closeable.close();
    }

    @Test
    void testGetAllAlbums() {
        // 模拟 ThreadLocal 里存储的当前用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        // 构造 Photo 实例
        Photo photo = new Photo(
                1000L, // id
                "photo", // title
                "description", // description
                "url", // url
                "location", // location
                "2024-01-01", // date
                "tag1,tag2", // tags
                LocalDateTime.now(), // uploadTime
                null // album
        );

        Album album = new Album();
        album.setId(1000L);
        album.setPhotos(List.of(photo));

        // 当调用 service 获取相册时，返回上述 album
        when(albumService.getAllAlbumsByUserId(1000L)).thenReturn(List.of(album));
        // 模拟 ossUtil 生成签名 URL
        when(ossUtil.generateSignedUrl("url")).thenReturn("signedUrl");

        // 断言：调用之前，原 url 是 "url"
        assertEquals("url", album.getPhotos().get(0).getUrl());

        // 调用控制器方法
        Result<List<Album>> result = albumController.getAllAlbums();

        // 断言返回的结果包含了处理过的签名 URL
        assertEquals(1, result.getData().size());
        assertEquals("signedUrl", result.getData().get(0).getPhotos().get(0).getUrl());

        // 测试完清理 ThreadLocal，避免影响其他测试
        ThreadLocalUtil.remove();
    }


    @Test
    void testCreateAlbum() {
        // 准备 ThreadLocal 中的用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        // 模拟 UserService 返回的用户实体
        User user = new User();
        user.setId(1000L);
        when(userService.getUserById(1000L)).thenReturn(Optional.of(user));

        // 创建输入的 Album
        Album album = new Album();

        // 模拟 albumService.createAlbum 返回创建的相册（带有用户）
        Album createdAlbum = new Album();
        createdAlbum.setUser(user);
        when(albumService.createAlbum(album)).thenReturn(createdAlbum);

        // 调用被测试方法
        Result<Album> result = albumController.createAlbum(album);

        // 断言返回的相册带有正确的用户
        assertNotNull(result);
        assertEquals(user, result.getData().getUser());

        // 清理 ThreadLocal 避免影响其他测试
        ThreadLocalUtil.remove();
    }


    @Test
    void testGetAlbumById_withValidUser() {
        // 模拟 ThreadLocal 中存当前用户ID
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        User user = new User();
        user.setId(1000L);

        Photo photo = new Photo(
                1000L,
                "photo",
                "description",
                "url",
                "location",
                "2024-01-01",
                "tag1,tag2",
                LocalDateTime.now(),
                null
        );

        Album album = new Album();
        album.setId(1000L);
        album.setUser(user);
        album.setPhotos(List.of(photo));

        when(albumService.getAlbumById(1000L)).thenReturn(album);
        when(ossUtil.generateSignedUrl("url")).thenReturn("signedUrl");

        Result<Album> result = albumController.getAlbumById(1000L);
        assertEquals("signedUrl", result.getData().getPhotos().get(0).getUrl());

        ThreadLocalUtil.remove();
    }

    @Test
    void testUpdateAlbum_withValidUser() {
        // 模拟 ThreadLocal 中存当前用户ID
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        Album existingAlbum = new Album();
        existingAlbum.setId(1000L);
        User user = new User();
        user.setId(1000L);
        existingAlbum.setUser(user);

        Album updatedAlbum = new Album();

        when(albumService.getAlbumById(1000L)).thenReturn(existingAlbum);
        when(albumService.updateAlbum(eq(1000L), any(Album.class))).thenReturn(existingAlbum);

        Result<Album> result = albumController.updateAlbum(1000L, updatedAlbum);
        assertEquals(1000L, result.getData().getUser().getId());

        ThreadLocalUtil.remove();
    }


    @Test
    void testDeleteAlbum_withValidUser() {
        // 模拟当前登录用户放入 ThreadLocal
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        Album album = new Album();
        album.setId(1000L);
        User user = new User();
        user.setId(1000L);
        album.setUser(user);

        when(albumService.getAlbumById(1000L)).thenReturn(album);
        doNothing().when(albumService).deleteAlbum(1000L);

        Result<?> result = albumController.deleteAlbum(1000L);
        assertTrue(result.getData().toString().contains("相册已删除"));

        // 清理 ThreadLocal，防止对其他测试影响
        ThreadLocalUtil.remove();
    }

    @Test
    void testGetPhotosInAlbum() {
        // 模拟当前登录用户放入 ThreadLocal
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1000L);
        ThreadLocalUtil.set(userInfo);

        Album album = new Album();
        album.setId(1000L);
        User user = new User();
        user.setId(1000L);
        album.setUser(user);

        Photo photo = new Photo();
        photo.setId(1000L);
        photo.setUrl("originalUrl");

        when(albumService.getAlbumById(1000L)).thenReturn(album);
        when(albumService.getPhotosInAlbum(1000L)).thenReturn(List.of(photo));
        when(ossUtil.generateSignedUrl("originalUrl")).thenReturn("signedUrl");

        Result<List<Photo>> result = albumController.getPhotosInAlbum(1000L);
        assertEquals("signedUrl", result.getData().get(0).getUrl());

        // 清理 ThreadLocal，防止对其他测试影响
        ThreadLocalUtil.remove();
    }

}
