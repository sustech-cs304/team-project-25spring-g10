package com.g10.service;

import com.g10.model.*;
import com.g10.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MemoryServiceTest {

    @InjectMocks
    private MemoryService memoryService;

    @Mock
    private MemoryRepository memoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private PhotoRepository photoRepository;

    private User mockUser;
    private Album mockAlbum;
    private Photo photo1, photo2;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockAlbum = new Album();
        mockAlbum.setId(10L);
        mockAlbum.setTitle("Test Album");
        mockAlbum.setUser(mockUser);

        photo1 = new Photo();
        photo1.setId(100L);
        photo1.setUrl("url1");

        photo2 = new Photo();
        photo2.setId(101L);
        photo2.setUrl("url2");
    }

    @Test
    void testCreateMemory_success() {
        Memory memory = new Memory();
        memory.setTitle("My Memory");

        List<Long> photoIds = Arrays.asList(photo1.getId(), photo2.getId());
        Map<Long, Integer> durations = new HashMap<>();
        durations.put(photo1.getId(), 6);
        durations.put(photo2.getId(), 4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(albumRepository.findById(10L)).thenReturn(Optional.of(mockAlbum));
        when(photoRepository.findById(100L)).thenReturn(Optional.of(photo1));
        when(photoRepository.findById(101L)).thenReturn(Optional.of(photo2));
        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Memory saved = memoryService.createMemory(memory, 1L, 10L, photoIds, durations);

        assertEquals("My Memory", saved.getTitle());
        assertEquals(mockAlbum.getId(), saved.getAlbum().getId());
        assertEquals(2, saved.getPhotos().size());
        assertEquals(10, saved.getDuration());
        assertEquals("url1", saved.getThumbnailUrl());
    }

    @Test
    void testGetMemoryById_found() {
        Memory memory = new Memory();
        memory.setId(1L);

        when(memoryRepository.findById(1L)).thenReturn(Optional.of(memory));

        Optional<Memory> result = memoryService.getMemoryById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testUpdateMemory_success() {
        Memory original = new Memory();
        original.setId(1L);
        original.setTitle("Old");

        Photo photo = new Photo();
        photo.setId(100L);
        photo.setUrl("url-new");

        Memory updated = new Memory();
        updated.setTitle("New Title");

        List<Long> photoIds = List.of(100L);
        Map<Long, Integer> durations = Map.of(100L, 7);

        when(memoryRepository.findById(1L)).thenReturn(Optional.of(original));
        when(photoRepository.findById(100L)).thenReturn(Optional.of(photo));
        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Memory result = memoryService.updateMemory(1L, updated, photoIds, durations);

        assertEquals("New Title", result.getTitle());
        assertEquals(1, result.getPhotos().size());
        assertEquals(7, result.getDuration());
    }

    @Test
    void testDeleteMemory_success() {
        when(memoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(memoryRepository).deleteById(1L);

        assertDoesNotThrow(() -> memoryService.deleteMemory(1L));
    }

    @Test
    void testDeleteMemory_notFound() {
        when(memoryRepository.existsById(2L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memoryService.deleteMemory(2L);
        });

        assertEquals("记忆视频不存在", exception.getMessage());
    }
}
