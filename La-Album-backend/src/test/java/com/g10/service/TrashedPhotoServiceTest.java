package com.g10.service;

import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.model.User;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrashedPhotoServiceTest {

    @InjectMocks
    private TrashedPhotoService trashedPhotoService;

    @Mock
    private TrashedPhotoRepository trashedPhotoRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private AlbumRepository albumRepository;

    private TrashedPhoto trashedPhoto;
    private Album album;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        album = new Album();
        album.setId(100L);
        album.setUser(user);

        trashedPhoto = new TrashedPhoto();
        trashedPhoto.setId(1L);
        trashedPhoto.setTitle("Trash Photo");
        trashedPhoto.setUrl("http://trash.jpg");
        trashedPhoto.setUploadTime(LocalDateTime.now().minusDays(31));
        trashedPhoto.setDeletedAt(LocalDateTime.now().minusDays(31));
        trashedPhoto.setOriginalAlbum(album);
        trashedPhoto.setUser(user);
        trashedPhoto.setTags("test");
        trashedPhoto.setDescription("a description");
        trashedPhoto.setDate("2023-08-01");
    }

    @Test
    void testAutomaticDeletePhoto_deletesOldPhoto() {
        when(trashedPhotoRepository.findAll()).thenReturn(List.of(trashedPhoto));

        trashedPhotoService.automaticDeletePhoto();

        verify(trashedPhotoRepository, times(1)).delete(trashedPhoto);
    }

    @Test
    void testAutomaticDeletePhoto_doesNotDeleteRecentPhoto() {
        trashedPhoto.setDeletedAt(LocalDateTime.now().minusDays(5));
        when(trashedPhotoRepository.findAll()).thenReturn(List.of(trashedPhoto));

        trashedPhotoService.automaticDeletePhoto();

        verify(trashedPhotoRepository, never()).delete(any());
    }

    @Test
    void testRestorePhoto_success() {
        when(trashedPhotoRepository.findById(1L)).thenReturn(Optional.of(trashedPhoto));
        when(albumRepository.findById(100L)).thenReturn(Optional.of(album));
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Photo restored = trashedPhotoService.restorePhoto(1L);

        assertNotNull(restored);
        assertEquals("Trash Photo", restored.getTitle());
        assertEquals("http://trash.jpg", restored.getUrl());
        assertEquals(album, restored.getAlbum());
        assertEquals("test", restored.getTags());
        assertEquals("a description", restored.getDescription());
        assertEquals("2023-08-01", restored.getDate());

        verify(photoRepository, times(1)).save(any(Photo.class));
        verify(trashedPhotoRepository, times(1)).delete(trashedPhoto);
    }

    @Test
    void testRestorePhoto_trashedPhotoNotFound() {
        when(trashedPhotoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> trashedPhotoService.restorePhoto(1L));

        assertEquals("Trashed photo not found", ex.getMessage());
    }

    @Test
    void testRestorePhoto_albumNotFound() {
        when(trashedPhotoRepository.findById(1L)).thenReturn(Optional.of(trashedPhoto));
        when(albumRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> trashedPhotoService.restorePhoto(1L));

        assertEquals("Album not found", ex.getMessage());
    }

    @Test
    void testDeleteFromTrash() {
        trashedPhotoService.deleteFromTrash(1L);
        verify(trashedPhotoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetTrashedPhotosByUser() {
        when(trashedPhotoRepository.findByUserId(1L)).thenReturn(List.of(trashedPhoto));

        List<TrashedPhoto> result = trashedPhotoService.getTrashedPhotos(1L);

        assertEquals(1, result.size());
        assertEquals("Trash Photo", result.get(0).getTitle());
        assertEquals(user, result.get(0).getUser());
    }

    @Test
    void testGetTrashedPhotoById_success() {
        when(trashedPhotoRepository.findById(1L)).thenReturn(Optional.of(trashedPhoto));

        TrashedPhoto result = trashedPhotoService.getTrashedPhotoById(1L);
        assertEquals("Trash Photo", result.getTitle());
    }

    @Test
    void testGetTrashedPhotoById_notFound() {
        when(trashedPhotoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> trashedPhotoService.getTrashedPhotoById(1L));

        assertEquals("Trashed photo not found", ex.getMessage());
    }
}
