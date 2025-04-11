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
  // 模拟获取相册列表
  setTimeout(() => {
    albums.value = [
      { id: 1, title: '夏日旅行' },
      { id: 2, title: '家庭聚会' },
      { id: 3, title: '毕业典礼' },
      { id: 4, title: '城市风光' },
      { id: 5, title: '美食收藏' }
    ];
  }, 300);
});

// 搜索照片
const searchPhotos = async () => {
  if (!searchQuery.value && !startDate.value && !endDate.value && !selectedAlbum.value) {
    return;
  }
  
  loading.value = true;
  searchPerformed.value = true;
  
  // 模拟API调用
  setTimeout(() => {
    // 根据条件过滤照片
    const results = [
      {
        id: 1,
        albumId: 1,
        title: '威尼斯大运河',
        imageUrl: 'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        description: '意大利威尼斯大运河的美丽景色，拍摄于黄昏时分。',
        createdAt: '2023-07-15T14:30:00Z',
        location: '意大利，威尼斯'
      },
      {
        id: 2,
        albumId: 1,
        title: '巴黎埃菲尔铁塔',
        imageUrl: 'https://images.unsplash.com/photo-1543349689-9a4d426bee8e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        description: '从香榭丽舍大街拍摄的埃菲尔铁塔夜景。',
        createdAt: '2023-07-18T20:15:00Z',
        location: '法国，巴黎'
      },
      {
        id: 3,
        albumId: 1,
        title: '伦敦塔桥',
        imageUrl: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        description: '伦敦标志性的塔桥，横跨泰晤士河。',
        createdAt: '2023-07-20T11:45:00Z',
        location: '英国，伦敦'
      }
    ];
    
    // 根据搜索条件模拟过滤
    if (selectedAlbum.value) {
      photos.value = results.filter(photo => photo.albumId === parseInt(selectedAlbum.value));
    } else {
      photos.value = results;
    }
    
    loading.value = false;
  }, 800);
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
.search-view {
  min-height: 100vh;
}

.search-header {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.search-header h1 {
  margin-bottom: var(--space-lg);
  color: var(--neutral-900);
}

.search-form {
  max-width: 800px;
}

.search-input-container {
  display: flex;
  margin-bottom: var(--space-md);
}

.search-input {
  flex: 1;
  padding: var(--space-md);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  font-size: 1rem;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 0 var(--space-lg);
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  cursor: pointer;
  font-weight: 500;
}

.filter-options {
  display: flex;
  gap: var(--space-lg);
  margin-bottom: var(--space-md);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.filter-group label {
  font-size: 0.9rem;
  color: var(--neutral-700);
}

.date-range {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.date-range input, 
.filter-group select {
  padding: var(--space-sm);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-sm);
}

.search-results {
  padding-bottom: var(--space-xxl);
}

.results-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  color: var(--neutral-700);
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  background: none;
  border: none;
  color: var(--neutral-600);
  cursor: pointer;
}

.clear-btn:hover {
  color: var(--error);
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--space-md);
}

.empty-state,
.initial-state {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.empty-icon,
.initial-icon {
  margin-bottom: var(--space-md);
  color: var(--neutral-400);
}

.empty-state h3,
.initial-state h2 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-800);
}

.empty-state p,
.initial-state p {
  margin-bottom: var(--space-lg);
  max-width: 500px;
  margin: 0 auto var(--space-lg);
}

.empty-actions {
  margin-top: var(--space-lg);
}

.loading-state {
  padding: var(--space-lg);
}

@media (max-width: 768px) {
  .search-header {
    padding: var(--space-lg) 0;
  }
  
  .filter-options {
    flex-direction: column;
    gap: var(--space-md);
  }
  
  .photos-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: var(--space-sm);
  }
}
</style>
  