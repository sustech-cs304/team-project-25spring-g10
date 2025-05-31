package com.g10.service;

import com.g10.dto.PhotoDTO;
import com.g10.model.Album;
import com.g10.model.Photo;
import com.g10.model.TrashedPhoto;
import com.g10.repository.AlbumRepository;
import com.g10.repository.PhotoRepository;
import com.g10.repository.TrashedPhotoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @InjectMocks
    private PhotoService photoService;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private TrashedPhotoRepository trashedPhotoRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Photo> criteriaQuery;

    @Mock
    private Root<Photo> root;

    @Mock
    private TypedQuery<Photo> typedQuery;

    private Photo photo;
    private Album album;

    @BeforeEach
    void setUp() {
        album = new Album();
        album.setTitle("Nature");
        photo = new Photo();
        photo.setId(1000L);
        photo.setTitle("Sunset");
        photo.setAlbum(album);

        // 用反射注入 entityManager（因为它不是构造注入的）
        try {
            Field emField = PhotoService.class.getDeclaredField("entityManager");
            emField.setAccessible(true);
            emField.set(photoService, entityManager);
        } catch (Exception e) {
            fail("Failed to inject mock entityManager");
        }
    }

    @Test
    void testGetAllPhotos() {
        when(photoRepository.findAll()).thenReturn(List.of(photo));
        List<Photo> result = photoService.getAllPhotos();
        assertEquals(1, result.size());
        assertEquals("Sunset", result.get(0).getTitle());
    }

    @Test
    void testGetPhotoById_found() {
        when(photoRepository.findById(1000L)).thenReturn(Optional.of(photo));
        Optional<PhotoDTO> dto = photoService.getPhotoById(1000L);
        assertTrue(dto.isPresent());
        assertEquals("Sunset", dto.get().getTitle());
        assertEquals("Nature", dto.get().getAlbumTitle());
    }

    @Test
    void testGetPhotoById_notFound() {
        when(photoRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<PhotoDTO> dto = photoService.getPhotoById(2L);
        assertTrue(dto.isEmpty());
    }

    @Test
    void testSavePhoto() {
        when(photoRepository.save(photo)).thenReturn(photo);
        Photo saved = photoService.savePhoto(photo);
        assertEquals("Sunset", saved.getTitle());
    }

    @Test
    void testDeletePhoto_success() {
        when(photoRepository.findById(1000L)).thenReturn(Optional.of(photo));
        doNothing().when(photoRepository).deleteById(1000L);

        photoService.deletePhoto(1000L);

        verify(trashedPhotoRepository, times(1)).save(any(TrashedPhoto.class));
        verify(photoRepository, times(1)).deleteById(1000L);
    }

    @Test
    void testDeletePhoto_notFound() {
        when(photoRepository.findById(1000L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () -> photoService.deletePhoto(1000L));
        assertEquals("Photo not found", ex.getMessage());
    }

    @Test
    void testMovePhoto_success() {
        Album newAlbum = new Album();
        newAlbum.setId(20L);
        newAlbum.setTitle("Vacation");
        newAlbum.setPhotos(new ArrayList<>());

        album.setPhotos(new ArrayList<>(List.of(photo)));

        when(photoRepository.findById(1000L)).thenReturn(Optional.of(photo));
        when(albumRepository.findById(20L)).thenReturn(Optional.of(newAlbum));
        when(photoRepository.save(photo)).thenReturn(photo);

        photoService.movePhoto(1000L, 20L);

        assertEquals(20L, photo.getAlbum().getId());
        assertTrue(newAlbum.getPhotos().contains(photo));
        assertFalse(album.getPhotos().contains(photo));
    }

    @Test
    void testSearchPhotos() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Photo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Photo.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(photo));

        List<Photo> result = photoService.searchPhotos("sun", null, null, null);
        assertEquals(1, result.size());
    }
}
