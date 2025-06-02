package com.g10.service;

import com.g10.dto.PhotoDTO;
import com.g10.model.*;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private TrashedPhotoRepository trashedPhotoRepository;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void testGetAllAlbums() {
        List<Album> albums = List.of(new Album(), new Album());
        when(albumRepository.findAll()).thenReturn(albums);

        List<Album> result = albumService.getAllAlbums();
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllAlbumsByUserId() {
        List<Album> albums = List.of(new Album());
        when(albumRepository.findByUserId(1L)).thenReturn(albums);

        List<Album> result = albumService.getAllAlbumsByUserId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllAlbumsByType() {
        List<Album> albums = List.of(new Album());
        when(albumRepository.findByType("type")).thenReturn(albums);

        List<Album> result = albumService.getAllAlbumsByType("type");
        assertEquals(1, result.size());
    }

    @Test
    void testCreateAlbum() {
        Album album = new Album();
        when(albumRepository.save(album)).thenReturn(album);

        Album result = albumService.createAlbum(album);
        assertEquals(album, result);
    }

    @Test
    void testGetAlbumById_found() {
        Album album = new Album();
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        Album result = albumService.getAlbumById(1L);
        assertEquals(album, result);
    }

    @Test
    void testGetAlbumById_notFound() {
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        Album result = albumService.getAlbumById(1L);
        assertNull(result);
    }

    @Test
    void testUpdateAlbum_exists() {
        Album updated = new Album();
        updated.setTitle("Updated");

        when(albumRepository.existsById(1L)).thenReturn(true);
        when(albumRepository.save(updated)).thenReturn(updated);

        Album result = albumService.updateAlbum(1L, updated);
        assertEquals("Updated", result.getTitle());
        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateAlbum_notExists() {
        when(albumRepository.existsById(1L)).thenReturn(false);

        Album result = albumService.updateAlbum(1L, new Album());
        assertNull(result);
    }

    @Test
    void testGetPhotosInAlbum() {
        Photo photo = new Photo();
        photo.setId(1L);
        photo.setTitle("Title");
        photo.setUrl("url");
        photo.setLocation("loc");
        photo.setTags("tag");
        photo.setUploadTime(java.time.LocalDateTime.now());
        photo.setDate("2020-01-01");
        photo.setDescription("desc");

        Album album = new Album();
        album.setId(10L);
        album.setTitle("My Album");
        photo.setAlbum(album);

        when(photoRepository.findByAlbum_Id(10L)).thenReturn(List.of(photo));

        List<PhotoDTO> result = albumService.getPhotosInAlbum(10L);
        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).getTitle());
        assertEquals("My Album", result.get(0).getAlbumTitle());
    }

    @Test
    void testDeleteAlbum_success() {
        // 当前 album
        Album album = new Album();
        album.setId(2L);
        album.setPhotos(List.of());
        User user = new User();
        user.setId(1L);
        album.setUser(user);

        // 默认相册
        Album defaultAlbum = new Album();
        defaultAlbum.setId(1L);
        defaultAlbum.setTitle("全部照片");

        when(albumRepository.findById(2L)).thenReturn(Optional.of(album));
        when(albumRepository.findByUserIdAndTitle(1L, "全部照片")).thenReturn(defaultAlbum);
        when(trashedPhotoRepository.findByOriginalAlbum(album)).thenReturn(List.of());

        albumService.deleteAlbum(2L);

        verify(trashedPhotoRepository).saveAll(anyList());
        verify(albumRepository).delete(album);
    }

    @Test
    void testDeleteAlbum_isDefaultAlbum_shouldThrow() {
        Album album = new Album();
        album.setId(1L);
        User user = new User();
        user.setId(1L);
        album.setUser(user);

        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumRepository.findByUserIdAndTitle(1L, "全部照片")).thenReturn(album);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            albumService.deleteAlbum(1L);
        });

        assertEquals("默认相册不能被删除", ex.getMessage());
    }

    @Test
    void testDeleteAlbum_albumNotFound() {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            albumService.deleteAlbum(999L);
        });

        assertEquals("Album not found", ex.getMessage());
    }

    @Test
    void testGetDefaultAlbumForUser() {
        Album defaultAlbum = new Album();
        when(albumRepository.findByUserIdAndTitle(1L, "全部照片")).thenReturn(defaultAlbum);

        Album result = albumService.getDefaultAlbumForUser(1L);
        assertEquals(defaultAlbum, result);
    }

}
