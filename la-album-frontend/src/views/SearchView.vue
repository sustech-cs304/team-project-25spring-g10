<template>
  <div class="search-view">
    <div class="search-header">
      <div class="container">
        <h1>搜索照片</h1>
        <div class="search-form">
          <div class="search-input-container">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="输入关键词、位置或日期..." 
              @keyup.enter="searchPhotos"
              class="search-input"
            >
            <button class="search-btn" @click="searchPhotos">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              搜索
            </button>
          </div>
          
          <div class="filter-options">
            <div class="filter-group">
              <label>日期范围:</label>
              <div class="date-range">
                <input type="date" v-model="startDate">
                <span>至</span>
                <input type="date" v-model="endDate">
              </div>
            </div>
            
            <div class="filter-group">
              <label>相册:</label>
              <select v-model="selectedAlbum">
                <option value="">全部相册</option>
                <option v-for="album in albums" :key="album.id" :value="album.id">
                  {{ album.title }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="search-results">
      <div class="container">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="searchPerformed && photos.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <h3>未找到符合条件的照片</h3>
          <p>尝试使用不同的关键词或筛选条件</p>
          <div class="empty-actions">
            <button class="btn btn-secondary" @click="clearSearch">清除筛选条件</button>
          </div>
        </div>
        
        <template v-else>
          <div v-if="searchPerformed" class="results-summary">
            找到 {{ photos.length }} 张照片
            <button v-if="searchPerformed" class="clear-btn" @click="clearSearch">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              清除
            </button>
          </div>
          
          <div v-if="photos.length > 0" class="photos-grid">
            <PhotoThumbnail 
              v-for="photo in photos" 
              :key="photo.id" 
              :photo="photo"
              @click="viewPhoto(photo)"
            />
          </div>
          
          <div v-else-if="!searchPerformed" class="initial-state">
            <div class="initial-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <h2>搜索您的照片</h2>
            <p>您可以按关键词、位置、日期或相册搜索照片</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import PhotoThumbnail from '@/components/photo/PhotoThumbnail.vue';

const router = useRouter();
const searchQuery = ref('');
const startDate = ref('');
const endDate = ref('');
const selectedAlbum = ref('');
const loading = ref(false);
const photos = ref([]);
const albums = ref([]);
const searchPerformed = ref(false);

// 获取相册数据
onMounted(async () => {
  try {
    const token = localStorage.getItem('authToken');
    const response = await fetch('/api/albums', {
      headers: {
        'Authorization': `${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      albums.value = await response.json();
    }
  } catch (error) {
    console.error('获取相册列表失败:', error);
  }
});

// 搜索照片
const searchPhotos = async () => {
  if (!searchQuery.value && !startDate.value && !endDate.value && !selectedAlbum.value) {
    return;
  }
  
  loading.value = true;
  searchPerformed.value = true;
  
  try {
    const token = localStorage.getItem('authToken');
    const params = new URLSearchParams();
    
    if (searchQuery.value) params.append('q', searchQuery.value);
    if (startDate.value) params.append('startDate', startDate.value);
    if (endDate.value) params.append('endDate', endDate.value);
    if (selectedAlbum.value) params.append('albumId', selectedAlbum.value);
    
    const response = await fetch(`/api/photos/search?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      photos.value = await response.json();
    } else {
      throw new Error(`搜索失败: ${response.status}`);
    }
  } catch (error) {
    console.error('搜索照片失败:', error);
    photos.value = [];
  } finally {
    loading.value = false;
  }
};

// 清除搜索
const clearSearch = () => {
  searchQuery.value = '';
  startDate.value = '';
  endDate.value = '';
  selectedAlbum.value = '';
  photos.value = [];
  searchPerformed.value = false;
};

// 查看照片详情
const viewPhoto = (photo) => {
  router.push({ name: 'Photo', params: { id: photo.id } });
};
</script>

<style scoped>
/* 样式部分保持不变 */
</style>