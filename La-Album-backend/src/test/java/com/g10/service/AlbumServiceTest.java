package com.g10.service;

import com.g10.model.*;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlbumServiceTest {

    private AlbumRepository albumRepository;
    private PhotoRepository photoRepository;
    private TrashedPhotoRepository trashedPhotoRepository;
    private AlbumService albumService;

    @BeforeEach
    void setUp() {
        albumRepository = mock(AlbumRepository.class);
        photoRepository = mock(PhotoRepository.class);
        trashedPhotoRepository = mock(TrashedPhotoRepository.class);
        albumService = new AlbumService(albumRepository, photoRepository, trashedPhotoRepository);
    }

    @Test
    void testGetAllAlbums() {
        when(albumRepository.findAll()).thenReturn(List.of(new Album()));
        List<Album> result = albumService.getAllAlbums();
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllAlbumsByUserId() {
        when(albumRepository.findByUserId(1000L)).thenReturn(List.of(new Album()));
        List<Album> result = albumService.getAllAlbumsByUserId(1000L);
        assertEquals(1, result.size());
    }

    @Test
    void testCreateAlbum() {
        Album album = new Album();
        album.setTitle("Test Album");
        when(albumRepository.save(album)).thenReturn(album);
        Album result = albumService.createAlbum(album);
        assertEquals("Test Album", result.getTitle());
    }

    @Test
    void testGetAlbumByIdFound() {
        Album album = new Album();
        album.setId(1000L);
        when(albumRepository.findById(1000L)).thenReturn(Optional.of(album));
        Album result = albumService.getAlbumById(1000L);
        assertEquals(1000L, result.getId());
    }

    @Test
    void testGetAlbumByIdNotFound() {
        when(albumRepository.findById(1000L)).thenReturn(Optional.empty());
        Album result = albumService.getAlbumById(1000L);
        assertNull(result);
    }

    @Test
    void testUpdateAlbumExists() {
        Album updated = new Album();
        updated.setTitle("Updated");
        when(albumRepository.existsById(1000L)).thenReturn(true);
        when(albumRepository.save(updated)).thenReturn(updated);

        Album result = albumService.updateAlbum(1000L, updated);
        assertEquals("Updated", result.getTitle());
        assertEquals(1000L, result.getId());
    }

    @Test
    void testUpdateAlbumNotExists() {
        Album updated = new Album();
        when(albumRepository.existsById(1000L)).thenReturn(false);
        Album result = albumService.updateAlbum(1000L, updated);
        assertNull(result);
    }

    @Test
    void testGetPhotosInAlbum() {
        when(photoRepository.findByAlbumId(1000L)).thenReturn(List.of(new Photo()));
        List<Photo> result = albumService.getPhotosInAlbum(1000L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetDefaultAlbumForUser() {
        Album defaultAlbum = new Album();
        when(albumRepository.findByUserIdAndTitle(1000L, "Default Album")).thenReturn(defaultAlbum);
        Album result = albumService.getDefaultAlbumForUser(1000L);
        assertEquals(defaultAlbum, result);
    }

    @Test
    void testDeleteAlbum() {
        // 创建 album 和 user
        User user = new User();
        user.setId(1000L);

        Photo photo = new Photo();
        Album album = new Album();
        album.setId(1000L);
        album.setUser(user);
        album.setPhotos(List.of(photo));
        photo.setAlbum(album);

        // default album
        Album defaultAlbum = new Album();
        defaultAlbum.setId(99L);
        defaultAlbum.setUser(user);

        // 关联
        when(albumRepository.findById(1000L)).thenReturn(Optional.of(album));
        when(albumRepository.findByUserIdAndTitle(1000L, "Default Album")).thenReturn(defaultAlbum);
        when(trashedPhotoRepository.findByOriginalAlbum(album)).thenReturn(List.of());

        // 调用
        albumService.deleteAlbum(1000L);

        // 验证照片被删除、trashed 被保存、相册被删除
        verify(photoRepository).delete(photo);
        verify(trashedPhotoRepository).save(any(TrashedPhoto.class));
        verify(albumRepository).delete(album);
    }

    @Test
    void testDeleteDefaultAlbumThrowsException() {
        User user = new User();
        user.setId(1000L);

        Album defaultAlbum = new Album();
        defaultAlbum.setId(10L);
        defaultAlbum.setUser(user);

        when(albumRepository.findById(10L)).thenReturn(Optional.of(defaultAlbum));
        when(albumRepository.findByUserIdAndTitle(1000L, "Default Album")).thenReturn(defaultAlbum);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            albumService.deleteAlbum(10L);
        });

        assertEquals("默认相册不能被删除", ex.getMessage());
    }
}
