package com.g10.controller;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.dto.PhotoDTO;
import com.g10.model.Result;
import com.g10.service.AlbumService;
import com.g10.service.PhotoService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoControllerTest {

    @Mock
    private PhotoService photoService;

    @Mock
    private AlbumService albumService;

    @Mock
    private OssUtil ossUtil;

    @InjectMocks
    private PhotoController photoController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
//        closeable = MockitoAnnotations.openMocks(this);
//        photoController = new PhotoController();
    }

    @AfterEach
    void tearDown() throws Exception {
//        ThreadLocalUtil.remove();
//        closeable.close();
    }

    @Test
    void testGetAllPhotos() {
        // 准备数据
        Album album = new Album();
        album.setId(1000L);
        album.setTitle("Test Album");

        Photo photo = new Photo();
        photo.setId(1000L);
        photo.setTitle("Sample Photo");
        photo.setUrl("originalUrl");
        photo.setLocation("Test Location");
        photo.setTags("tag1,tag2");
        photo.setUploadTime(LocalDateTime.now());
        photo.setAlbum(album);
        photo.setDate("2024-01-01");
        photo.setDescription("Test Description");

        List<Photo> photoList = List.of(photo);
        when(photoService.getAllPhotos()).thenReturn(photoList);
        when(ossUtil.generateSignedUrl("originalUrl")).thenReturn("signedUrl");

        // 调用 controller 方法
        ResponseEntity<List<PhotoDTO>> response = photoController.getAllPhotos();

        // 断言结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        PhotoDTO dto = response.getBody().get(0);
        assertEquals(1000L, dto.getId());
        assertEquals("Sample Photo", dto.getTitle());
        assertEquals("signedUrl", dto.getUrl());
        assertEquals("Test Location", dto.getLocation());
        assertEquals("tag1,tag2", dto.getTags());
        assertEquals("2024-01-01", dto.getDate());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(1000L, dto.getAlbumId());
        assertEquals("Test Album", dto.getAlbumTitle());
    }

    @Test
    void testGetPhoto_whenPhotoExists() {
        PhotoDTO dto = new PhotoDTO(
                1000L,
                "Sample Photo",
                "originalUrl",
                "Test Location",
                "tag1,tag2",
                LocalDateTime.now(),
                1000L,
                "2024-01-01",
                "Test Description",
                "Test Album"
        );

        when(photoService.getPhotoById(1000L)).thenReturn(Optional.of(dto));
        when(ossUtil.generateSignedUrl("originalUrl")).thenReturn("signedUrl");

        ResponseEntity<PhotoDTO> response = photoController.getPhoto(1000L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("signedUrl", response.getBody().getUrl());
        assertEquals("Sample Photo", response.getBody().getTitle());
    }

    @Test
    void testGetPhoto_whenNotFound() {
        when(photoService.getPhotoById(999L)).thenReturn(Optional.empty());

        ResponseEntity<PhotoDTO> response = photoController.getPhoto(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testDeletePhoto() {
        doNothing().when(photoService).deletePhoto(1000L);

        ResponseEntity<Void> response = photoController.deletePhoto(1000L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testMovePhoto() {
        Long photoId = 1000L;
        Long newAlbumId = 2000L;

        Map<String, Long> body = new HashMap<>();
        body.put("id", newAlbumId);

        doNothing().when(photoService).movePhoto(photoId, newAlbumId);

        ResponseEntity<Void> response = photoController.movePhoto(photoId, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdatePhotoDescriptionAndTags() {
        Photo photo = new Photo();
        photo.setId(1000L);
        photo.setTitle("Old Title");
        photo.setDescription("Old Description");
        photo.setTags("old");

        when(photoService.getPhotoEntityById(1000L)).thenReturn(Optional.of(photo));
        when(photoService.savePhoto(any(Photo.class))).thenReturn(photo);

        Map<String, String> updates = new HashMap<>();
        updates.put("description", "New Description");
        updates.put("tags", "tag1,tag2");

        ResponseEntity<Photo> response = photoController.updatePhotoDescriptionAndTags(1000L, updates);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Description", response.getBody().getDescription());
        assertEquals("tag1,tag2", response.getBody().getTags());
    }


    @Test
    void testSearchPhotos() {
        // 准备数据
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        String query = "sunset";
        Long albumId = 2L;

        Photo photo = new Photo();
        photo.setId(1000L);
        photo.setTitle("sunset in the beach");
        photo.setUrl("originalUrl");
        photo.setLocation("Hawaii");
        photo.setTags("sunset,beach");
        photo.setUploadTime(LocalDateTime.now());
        photo.setDate("2024-06-01");

        Album album = new Album();
        album.setId(albumId);
        album.setTitle("Summer Trip");
        photo.setAlbum(album);

        List<Photo> mockResults = List.of(photo);

        when(photoService.searchPhotos(query, startDate, endDate, albumId)).thenReturn(mockResults);
        when(ossUtil.generateSignedUrl("originalUrl")).thenReturn("signedUrl");

        // 调用 controller
        ResponseEntity<List<PhotoDTO>> response = photoController.searchPhotos(query, startDate, endDate, albumId);

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<PhotoDTO> dtos = response.getBody();
        assertNotNull(dtos);
        assertEquals(1, dtos.size());

        PhotoDTO dto = dtos.get(0);
        assertEquals("signedUrl", dto.getUrl());
        assertEquals("sunset in the beach", dto.getTitle());
        assertEquals(albumId, dto.getAlbumId());
        assertEquals("Summer Trip", dto.getAlbumTitle());
    }


    @Test
    void testCopyPhotoToAlbum_success() {
        // 构造原相册和目标相册
        Album originalAlbum = new Album();
        originalAlbum.setId(1L);
        originalAlbum.setTitle("原相册");

        Album targetAlbum = new Album();
        targetAlbum.setId(2L);
        targetAlbum.setTitle("目标相册");

        // 构造原照片
        Photo originalPhoto = new Photo();
        originalPhoto.setId(100L);
        originalPhoto.setTitle("原照片");
        originalPhoto.setDescription("描述");
        originalPhoto.setDate("2024-06-01");
        originalPhoto.setLocation("上海");
        originalPhoto.setTags("tag1,tag2");
        originalPhoto.setUrl("http://example.com/photo.jpg");
        originalPhoto.setUploadTime(LocalDateTime.of(2024, 6, 1, 10, 0));
        originalPhoto.setAlbum(originalAlbum);

        // 构造复制的新照片（保存后返回）
        Photo savedPhoto = new Photo();
        savedPhoto.setId(101L);
        savedPhoto.setTitle(originalPhoto.getTitle());
        savedPhoto.setDescription(originalPhoto.getDescription());
        savedPhoto.setDate(originalPhoto.getDate());
        savedPhoto.setLocation(originalPhoto.getLocation());
        savedPhoto.setTags(originalPhoto.getTags());
        savedPhoto.setUrl(originalPhoto.getUrl());
        savedPhoto.setUploadTime(originalPhoto.getUploadTime());
        savedPhoto.setAlbum(targetAlbum);

        // 模拟依赖
        when(photoService.getPhotoEntityById(100L)).thenReturn(Optional.of(originalPhoto));
        when(albumService.getAlbumById(2L)).thenReturn(targetAlbum);
        when(photoService.savePhoto(any(Photo.class))).thenReturn(savedPhoto);

        // 调用 controller 方法
        ResponseEntity<Photo> response = photoController.copyPhotoToAlbum(100L, 2L);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getId());
        assertEquals("原照片", response.getBody().getTitle());
        assertEquals("描述", response.getBody().getDescription());
        assertEquals("2024-06-01", response.getBody().getDate());
        assertEquals("上海", response.getBody().getLocation());
        assertEquals("tag1,tag2", response.getBody().getTags());
        assertEquals("http://example.com/photo.jpg", response.getBody().getUrl());
        assertEquals(targetAlbum, response.getBody().getAlbum());
    }

    @Test
    void testCopyPhotoToAlbum_photoNotFound() {
        when(photoService.getPhotoEntityById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Photo> response = photoController.copyPhotoToAlbum(999L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCopyPhotoToAlbum_albumNotFound() {
        Album originalAlbum = new Album();
        originalAlbum.setId(1L);

        Photo originalPhoto = new Photo();
        originalPhoto.setId(100L);
        originalPhoto.setTitle("原照片");
        originalPhoto.setAlbum(originalAlbum);

        when(photoService.getPhotoEntityById(100L)).thenReturn(Optional.of(originalPhoto));
        when(albumService.getAlbumById(999L)).thenReturn(null); // 相册不存在

        ResponseEntity<Photo> response = photoController.copyPhotoToAlbum(100L, 999L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testProxyImage_success() {
        String key = "test/image.jpg";
        String signedUrl = "http://oss.com/signed/image.jpg";
        byte[] imageData = new byte[]{1, 2, 3, 4};

        when(ossUtil.generateSignedUrl(key)).thenReturn(signedUrl);
        when(ossUtil.fetchImageData(signedUrl)).thenReturn(imageData);

        ResponseEntity<byte[]> response = photoController.proxyImage(key);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(imageData, response.getBody());
        assertEquals("image/jpeg", response.getHeaders().getFirst("Content-Type"));
        assertEquals("*", response.getHeaders().getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    void testProxyImage_whenExceptionThrown() {
        String key = "bad/image.jpg";

        when(ossUtil.generateSignedUrl(key)).thenThrow(new RuntimeException("OSS failed"));

        ResponseEntity<byte[]> response = photoController.proxyImage(key);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    void uploadPhoto_success() throws Exception {
        // 准备数据
        Long albumId = 1L;
        Album album = new Album();
        album.setId(albumId);

        byte[] data = "test image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", data);

        String uploadedUrl = "https://oss.com/photo.jpg";

        Photo savedPhoto = new Photo();
        savedPhoto.setId(10L);

        // Mock 行为
        when(albumService.getAlbumById(albumId)).thenReturn(album);
        when(ossUtil.uploadFile(anyString(), any())).thenReturn(uploadedUrl);
        when(photoService.savePhoto(any(Photo.class))).thenReturn(savedPhoto);

        // 执行上传
        ResponseEntity<Long> response = photoController.uploadPhoto(file, albumId);

        // 断言
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody());
    }

    @Test
    void uploadPhoto_albumNotFound() throws Exception {
        Long albumId = 99L;
        byte[] data = "test".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", data);

        when(albumService.getAlbumById(albumId)).thenReturn(null);

        ResponseEntity<Long> response = photoController.uploadPhoto(file, albumId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void uploadPhoto_fileIsNull() {
        MultipartFile file = null;
        Long albumId = 1L;

        ResponseEntity<Long> response = photoController.uploadPhoto(file, albumId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void uploadPhoto_exceptionDuringUpload() throws Exception {
        Long albumId = 1L;
        Album album = new Album();
        byte[] data = "test".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", data);

        when(albumService.getAlbumById(albumId)).thenReturn(album);
        when(ossUtil.uploadFile(anyString(), any())).thenThrow(new RuntimeException("OSS error"));

        ResponseEntity<Long> response = photoController.uploadPhoto(file, albumId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }



}
