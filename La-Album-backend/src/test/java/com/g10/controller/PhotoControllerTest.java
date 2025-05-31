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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
        // 构造 PhotoDTO
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

        // 调用 controller 方法
        ResponseEntity<PhotoDTO> response = photoController.getPhoto(1000L);

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("signedUrl", response.getBody().getUrl());
        assertEquals(1000L, response.getBody().getId());
        assertEquals("Sample Photo", response.getBody().getTitle());
    }

    @Test
    void testGetPhoto_whenPhotoDoesNotExist() {
        when(photoService.getPhotoById(999L)).thenReturn(Optional.empty());

        // 调用 controller 方法
        ResponseEntity<PhotoDTO> response = photoController.getPhoto(999L);

        // 验证返回 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    void testDeletePhoto() {
        Long photoId = 1000L;

        // doNothing 是默认行为，这里为了表达清楚加上
        doNothing().when(photoService).deletePhoto(photoId);

        ResponseEntity<Void> response = photoController.deletePhoto(photoId);

        // 验证
        verify(photoService, times(1)).deletePhoto(photoId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    void testMovePhoto() {
        Long photoId = 1000L;
        Long destAlbumId = 2L;
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("id", destAlbumId);

        doNothing().when(photoService).movePhoto(photoId, destAlbumId);

        ResponseEntity<Void> response = photoController.movePhoto(photoId, requestBody);

        // 验证
        verify(photoService, times(1)).movePhoto(photoId, destAlbumId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
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
    void testUpdatePhoto_UpdateExistingPhoto() throws Exception {
        Long photoId = 1000L;
        String newTitle = "New Title";
        String newDesc = "New Description";
        String newDate = "2024-05-01";
        String newLocation = "New York";

        Photo existingPhoto = new Photo();
        existingPhoto.setId(photoId);
        existingPhoto.setTitle("Old Title");
        existingPhoto.setDescription("Old Description");
        existingPhoto.setDate("2023-01-01");
        existingPhoto.setLocation("Old Location");
        existingPhoto.setUrl("originalUrl");

        Photo updatedPhoto = new Photo();
        updatedPhoto.setId(photoId);
        updatedPhoto.setTitle(newTitle);
        updatedPhoto.setDescription(newDesc);
        updatedPhoto.setDate(newDate);
        updatedPhoto.setLocation(newLocation);
        updatedPhoto.setUrl("newUrl");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true); // 文件为空，则不更新 URL

        when(photoService.getPhotoEntityById(photoId)).thenReturn(Optional.of(existingPhoto));
        when(photoService.savePhoto(any(Photo.class))).thenReturn(updatedPhoto);
        when(ossUtil.generateSignedUrl("newUrl")).thenReturn("signedSignedUrl");

        ResponseEntity<Photo> response = photoController.updatePhoto(
                photoId, mockFile, newTitle, newDesc, newDate, newLocation, null, null, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Photo result = response.getBody();
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("signedSignedUrl", result.getUrl());
    }


    @Test
    void testUpdatePhoto_SaveAsNew() throws Exception {
        Long photoId = 1000L;
        String newTitle = "New Title";
        String newDesc = "New Description";
        String newDate = "2024-05-01";
        String newLocation = "Paris";

        Photo oldPhoto = new Photo();
        oldPhoto.setId(photoId);
        oldPhoto.setTitle("Old Title");
        oldPhoto.setDescription("Old Desc");
        oldPhoto.setDate("2022-01-01");
        oldPhoto.setLocation("Old Location");
        oldPhoto.setTags("nature");
        oldPhoto.setUrl("oldUrl");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        Photo savedNewPhoto = new Photo();
        savedNewPhoto.setId(2L);
        savedNewPhoto.setTitle(newTitle);
        savedNewPhoto.setUrl("newUploadedUrl");

        when(photoService.getPhotoEntityById(photoId)).thenReturn(Optional.of(oldPhoto));
        when(ossUtil.uploadFile(anyString(), any(InputStream.class))).thenReturn("newUploadedUrl");
        when(photoService.savePhoto(any(Photo.class))).thenReturn(savedNewPhoto);
        when(ossUtil.generateSignedUrl("newUploadedUrl")).thenReturn("signedUploadedUrl");

        ResponseEntity<Photo> response = photoController.updatePhoto(
                photoId, mockFile, newTitle, newDesc, newDate, newLocation, null, null, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("signedUploadedUrl", response.getBody().getUrl());
        assertEquals("New Title", response.getBody().getTitle());
    }

    @Test
    void testUpdatePhoto_NotFound() throws Exception {
        Long photoId = 99L;

        when(photoService.getPhotoEntityById(photoId)).thenReturn(Optional.empty());

        ResponseEntity<Photo> response = photoController.updatePhoto(
                photoId, null, null, null, null, null, null, null, false);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


}
