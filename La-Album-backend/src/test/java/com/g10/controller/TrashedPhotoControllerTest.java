package com.g10.controller;

import com.g10.model.Photo;
import com.g10.model.Result;
import com.g10.model.TrashedPhoto;
import com.g10.model.User;
import com.g10.service.TrashedPhotoService;
import com.g10.utils.OssUtil;
import com.g10.utils.ThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrashedPhotoControllerTest {

    @Mock
    private TrashedPhotoService trashedPhotoService;

    @Mock
    private OssUtil ossUtil;

    @InjectMocks
    private TrashedPhotoController trashedPhotoController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1000L);
        ThreadLocalUtil.set(Map.of("id", testUser.getId()));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalUtil.remove();
    }

    @Test
    void testGetTrashPhotos() {
        TrashedPhoto photo1 = new TrashedPhoto();
        photo1.setId(1L);
        photo1.setUrl("photo1.jpg");

        TrashedPhoto photo2 = new TrashedPhoto();
        photo2.setId(2L);
        photo2.setUrl("photo2.jpg");

        List<TrashedPhoto> mockedPhotos = List.of(photo1, photo2);

        when(trashedPhotoService.getTrashedPhotos(testUser.getId())).thenReturn(mockedPhotos);
        when(ossUtil.generateSignedUrl("photo1.jpg")).thenReturn("signed/photo1.jpg");
        when(ossUtil.generateSignedUrl("photo2.jpg")).thenReturn("signed/photo2.jpg");

        Result<List<TrashedPhoto>> result = trashedPhotoController.getTrashPhotos();

        assertEquals(2, result.getData().size());
        assertEquals("signed/photo1.jpg", result.getData().get(0).getUrl());
        assertEquals("signed/photo2.jpg", result.getData().get(1).getUrl());

        verify(trashedPhotoService).getTrashedPhotos(testUser.getId());
        verify(ossUtil, times(2)).generateSignedUrl(anyString());
    }

    @Test
    void testRestorePhoto() {
        Long photoId = 1L;
        Photo restoredPhoto = new Photo();
        restoredPhoto.setId(photoId);
        restoredPhoto.setUrl("restored.jpg");

        when(trashedPhotoService.restorePhoto(photoId)).thenReturn(restoredPhoto);
        when(ossUtil.generateSignedUrl("restored.jpg")).thenReturn("signed/restored.jpg");

        Result<Photo> result = trashedPhotoController.restorePhoto(photoId);

        assertNotNull(result.getData());
        assertEquals(photoId, result.getData().getId());
        assertEquals("signed/restored.jpg", result.getData().getUrl());

        verify(trashedPhotoService).restorePhoto(photoId);
        verify(ossUtil).generateSignedUrl("restored.jpg");
    }

    @Test
    void testDeleteFromTrash() {
        Long photoId = 1L;

        TrashedPhoto trashedPhoto = new TrashedPhoto();
        trashedPhoto.setId(photoId);
        trashedPhoto.setUrl("toDelete.jpg");

        when(trashedPhotoService.getTrashedPhotoById(photoId)).thenReturn(trashedPhoto);

        Result<Void> result = trashedPhotoController.deleteFromTrash(photoId);

        verify(trashedPhotoService).getTrashedPhotoById(photoId);
        verify(ossUtil).deleteFile("toDelete.jpg");
        verify(trashedPhotoService).deleteFromTrash(photoId);

        assertEquals(0, result.getCode());
        assertNull(result.getData());
    }
}
