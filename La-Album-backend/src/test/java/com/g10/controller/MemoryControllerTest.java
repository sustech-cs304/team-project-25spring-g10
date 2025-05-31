package com.g10.controller;

import com.g10.model.*;
import com.g10.utils.OssUtil;
import com.g10.service.MemoryService;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class MemoryControllerTest {

    @Mock
    private MemoryService memoryService;

    @Mock
    private OssUtil ossUtil;

    @InjectMocks
    private MemoryController memoryController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1000L);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", testUser.getId());
        ThreadLocalUtil.set(userInfo);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove();
    }


    @Test
    void testGetAllMemories() {
        // 准备 Memory 和 MemoryPhoto 模拟数据
        MemoryPhoto memoryPhoto = new MemoryPhoto();
        memoryPhoto.setPhotoId(100L);
        memoryPhoto.setDisplayDuration(3);
        memoryPhoto.setThumbnailUrl("test/photo/thumb.jpg");

        Memory memory = new Memory();
        memory.setId(1000L);
        memory.setTitle("Test Memory");
        memory.setDuration(6L);
        memory.setTransition("fade");
        memory.setBgmId(1000L);
        memory.setBgmName("test-music");
        memory.setCreatedAt(LocalDateTime.now());
        memory.setThumbnailUrl("test/thumb.jpg");
        memory.setPhotos(List.of(memoryPhoto));

        Album album = new Album();
        album.setId(1000L);
        album.setTitle("Vacation");
        memory.setAlbum(album);

        memory.setUser(testUser);

        // mock ThreadLocalUtil.get()
        ThreadLocalUtil.set(Map.of("id", 1000L));

        // mock memoryService 和 ossUtil
        when(memoryService.getAllMemoriesByUserId(1000L)).thenReturn(List.of(memory));
        when(ossUtil.generateSignedUrl("test/thumb.jpg")).thenReturn("signed/thumb.jpg");
        when(ossUtil.generateSignedUrl("test/photo/thumb.jpg")).thenReturn("signed/photo/thumb.jpg");

        // 调用 controller 方法
        Result<List<Map<String, Object>>> result = memoryController.getAllMemories();

        // 验证结果
        assertEquals(1, result.getData().size());

        Map<String, Object> memoryMap = result.getData().get(0);
        assertEquals(1000L, memoryMap.get("id"));
        assertEquals("Test Memory", memoryMap.get("title"));
        assertEquals("fade", memoryMap.get("transition"));
        assertEquals("test-music", memoryMap.get("bgmName"));
        assertEquals("signed/thumb.jpg", memoryMap.get("thumbnailUrl"));
        assertEquals("Vacation", memoryMap.get("albumName"));
        assertEquals(1, memoryMap.get("photoCount"));

        List<Map<String, Object>> photos = (List<Map<String, Object>>) memoryMap.get("photos");
        assertEquals(1, photos.size());
        assertEquals(100L, photos.get(0).get("id"));
        assertEquals(3, photos.get(0).get("displayDuration"));
        assertEquals("signed/photo/thumb.jpg", photos.get(0).get("thumbnailUrl"));
    }


    @Test
    void testGetMemoryById_withAuthorizedUser() {
        // 构造用户
        Long userId = 1000L;
        ThreadLocalUtil.set(Map.of("id", userId)); // 模拟登录用户

        // 构造照片
        MemoryPhoto memoryPhoto = new MemoryPhoto();
        memoryPhoto.setPhotoId(101000L);
        memoryPhoto.setDisplayDuration(3);
        memoryPhoto.setThumbnailUrl("photo/thumb.jpg");

        // 构造相册
        Album album = new Album();
        album.setId(1000L);
        album.setTitle("My Album");

        // 构造 Memory
        Memory memory = new Memory();
        memory.setId(1000L);
        memory.setTitle("Test Memory");
        memory.setDuration(60L);
        memory.setTransition("fade");
        memory.setBgmId(1000L);
        memory.setBgmName("Test Music");
        memory.setCreatedAt(LocalDateTime.now());
        memory.setThumbnailUrl("memory/thumb.jpg");
        memory.setPhotos(List.of(memoryPhoto));
        memory.setAlbum(album);
        memory.setUser(testUser); // testUser 的 ID 应为 1

        // mock service & oss
        when(memoryService.getMemoryById(1000L)).thenReturn(Optional.of(memory));
        when(ossUtil.generateSignedUrl("photo/thumb.jpg")).thenReturn("signed/photo/thumb.jpg");
        when(ossUtil.generateSignedUrl("memory/thumb.jpg")).thenReturn("signed/memory/thumb.jpg");

        // 调用方法
        Result<Map<String, Object>> result = memoryController.getMemoryById(1000L);
        Map<String, Object> data = result.getData();

        // 断言
        assertNotNull(data);
        assertEquals(1000L, data.get("id"));
        assertEquals("Test Memory", data.get("title"));
        assertEquals("signed/memory/thumb.jpg", data.get("thumbnailUrl"));
        assertEquals("fade", data.get("transition"));
        assertEquals("Test Music", data.get("bgmName"));
        assertEquals("My Album", data.get("albumName"));

        // 断言照片字段
        List<MemoryPhoto> photos = (List<MemoryPhoto>) data.get("photos");
        assertEquals(1, photos.size());
        assertEquals("signed/photo/thumb.jpg", photos.get(0).getThumbnailUrl());
    }


    @Test
    void testGetMemoryById_withUnauthorizedUser() {
        ThreadLocalUtil.set(Map.of("id", 1000L)); // 当前登录用户是 1000L

        User otherUser = new User();
        otherUser.setId(2L); // 记忆视频属于其他用户

        Memory memory = new Memory();
        memory.setId(1000L);
        memory.setTitle("Other's Memory");
        memory.setUser(otherUser);

        when(memoryService.getMemoryById(1000L)).thenReturn(Optional.of(memory));

        Result<Map<String, Object>> result = memoryController.getMemoryById(1000L);

        assertNull(result.getData());
        assertEquals("无权访问该记忆视频", result.getMessage());
    }

    @Test
    void testUpdateMemory_success() {
        Long memoryId = 1000L;

        // 模拟输入数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Updated Title");
        requestData.put("bgmId", 2L);
        requestData.put("bgmName", "Updated BGM");
        requestData.put("transition", "fade");

        requestData.put("photoIds", List.of("101", "102"));

        Map<String, Object> durations = new HashMap<>();
        durations.put("101", 3);
        durations.put("102", 5);
        requestData.put("photoDisplayDurations", durations);

        // 构造旧 Memory
        Memory oldMemory = new Memory();
        oldMemory.setId(memoryId);
        oldMemory.setUser(testUser);

        // 构造更新后 Memory
        Memory updatedMemory = new Memory();
        updatedMemory.setId(memoryId);
        updatedMemory.setTitle("Updated Title");
        updatedMemory.setBgmId(2L);
        updatedMemory.setBgmName("Updated BGM");
        updatedMemory.setTransition("fade");
        updatedMemory.setUser(testUser);
        updatedMemory.setThumbnailUrl("thumb.jpg");

        // 构造照片
        MemoryPhoto photo1 = new MemoryPhoto();
        photo1.setPhotoId(101000L);
        photo1.setThumbnailUrl("photo1_thumb.jpg");
        photo1.setDisplayDuration(3);

        MemoryPhoto photo2 = new MemoryPhoto();
        photo2.setPhotoId(102L);
        photo2.setThumbnailUrl("photo2_thumb.jpg");
        photo2.setDisplayDuration(5);

        updatedMemory.setPhotos(List.of(photo1, photo2));

        // mock
        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.of(oldMemory));
        when(memoryService.updateMemory(eq(memoryId), any(), any(), any())).thenReturn(updatedMemory);
        when(ossUtil.generateSignedUrl("thumb.jpg")).thenReturn("signed/thumb.jpg");
        when(ossUtil.generateSignedUrl("photo1_thumb.jpg")).thenReturn("signed/photo1_thumb.jpg");
        when(ossUtil.generateSignedUrl("photo2_thumb.jpg")).thenReturn("signed/photo2_thumb.jpg");

        // 执行
        Result<Memory> result = memoryController.updateMemory(memoryId, requestData);

        // 断言
        assertEquals(0, result.getCode());
        assertEquals("Updated Title", result.getData().getTitle());
        assertEquals("signed/thumb.jpg", result.getData().getThumbnailUrl());
        assertEquals(2, result.getData().getPhotos().size());
        assertEquals("signed/photo1_thumb.jpg", result.getData().getPhotos().get(0).getThumbnailUrl());
    }


    @Test
    void testUpdateMemory_noPermission() {
        Long memoryId = 1000L;
        User otherUser = new User();
        otherUser.setId(2L);

        Memory memory = new Memory();
        memory.setId(memoryId);
        memory.setUser(otherUser);

        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.of(memory));

        Result<Memory> result = memoryController.updateMemory(memoryId, new HashMap<>());

        assertEquals(1, result.getCode());
        assertEquals("无权修改该记忆视频", result.getMessage());
    }

    @Test
    void testUpdateMemory_notFound() {
        Long memoryId = 99L;

        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.empty());

        Result<Memory> result = memoryController.updateMemory(memoryId, new HashMap<>());

        assertEquals(1, result.getCode());
        assertTrue(result.getMessage().contains("记忆视频不存在"));
    }

    @Test
    void testDeleteMemory_notFound() {
        Long memoryId = 1000L;

        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.empty());

        Result<Void> result = memoryController.deleteMemory(memoryId);

        verify(memoryService, never()).deleteMemory(anyLong());
        assertEquals(1, result.getCode());
        assertTrue(result.getMessage().contains("记忆视频不存在"));
    }


    @Test
    void testDeleteMemory_noPermission() {
        Long memoryId = 1000L;

        User otherUser = new User();
        otherUser.setId(2L);

        Memory memory = new Memory();
        memory.setId(memoryId);
        memory.setUser(otherUser);

        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.of(memory));

        Result<Void> result = memoryController.deleteMemory(memoryId);

        verify(memoryService, never()).deleteMemory(anyLong());
        assertEquals(1, result.getCode());
        assertEquals("无权删除该记忆视频", result.getMessage());
    }


    @Test
    void testDeleteMemory_success() {
        Long memoryId = 1000L;

        Memory memory = new Memory();
        memory.setId(memoryId);
        memory.setUser(testUser);

        when(memoryService.getMemoryById(memoryId)).thenReturn(Optional.of(memory));

        Result<Void> result = memoryController.deleteMemory(memoryId);

        verify(memoryService).deleteMemory(memoryId);
        assertEquals(0, result.getCode());
        assertNull(result.getData());
    }

}
