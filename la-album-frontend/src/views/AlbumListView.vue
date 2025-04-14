<template>
  <div class="album-list-view">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <h1>我的相册</h1>
          <div class="header-actions">
            <button class="btn btn-primary" @click="createNewAlbum">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 5v14M5 12h14"></path>
              </svg>
              创建新相册
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="album-list-content">
      <div class="container">
        <div class="filter-bar">
          <div class="search-input">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索相册..." 
              @input="filterAlbums"
            >
            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </div>
          
          <div class="filter-options">
            <select v-model="sortOption" @change="filterAlbums">
              <option value="newest">最新创建</option>
              <option value="oldest">最早创建</option>
              <option value="az">名称 A-Z</option>
              <option value="za">名称 Z-A</option>
              <option value="most">照片最多</option>
              <option value="least">照片最少</option>
            </select>
          </div>
        </div>
        
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="filteredAlbums.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <h3>{{ searchQuery ? '没有找到匹配的相册' : '还没有相册' }}</h3>
          <p>{{ searchQuery ? '尝试使用不同的搜索词或清除搜索' : '创建您的第一个相册来开始整理照片吧' }}</p>
          <div class="empty-actions">
            <button v-if="searchQuery" class="btn btn-secondary" @click="clearSearch">清除搜索</button>
            <button class="btn" @click="createNewAlbum">创建相册</button>
          </div>
        </div>
        
        <div v-else class="albums-grid">
          <AlbumCard 
            v-for="album in filteredAlbums" 
            :key="album.id" 
            :album="album"
          />
        </div>
        
        <div class="pagination" v-if="totalPages > 1">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>
          
          <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
          
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import AlbumCard from '@/components/album/AlbumCard.vue';

const loading = ref(true);
const albums = ref([]);
const searchQuery = ref('');
const sortOption = ref('newest');
const currentPage = ref(1);
const pageSize = 12;

// 获取所有相册数据
onMounted(async () => {
  // 模拟API调用
  setTimeout(() => {
    // 这里将来会替换为真实的API调用
    albums.value = [
      {
        id: 1,
        title: '夏日旅行',
        description: '2023年暑假的欧洲之旅，收集了各种美丽的风景和难忘的瞬间。',
        coverUrl: 'https://images.unsplash.com/photo-1506929562872-bb421503ef21?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 48,
        createdAt: '2023-07-15T12:00:00Z'
      },
      {
        id: 2,
        title: '家庭聚会',
        description: '春节期间与家人的欢乐时光，记录了我们一起制作饺子和看春晚的瞬间。',
        coverUrl: 'https://images.unsplash.com/photo-1493612276216-ee3925520721?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 36,
        createdAt: '2023-01-22T18:30:00Z'
      },
      {
        id: 3,
        title: '毕业典礼',
        description: '大学毕业典礼的照片集锦，包含了与同学们的合照和校园美景。',
        coverUrl: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 24,
        createdAt: '2022-06-30T10:15:00Z'
      },
      {
        id: 4,
        title: '城市风光',
        description: '城市建筑和街头摄影作品集，展现都市生活的多样性和活力。',
        coverUrl: 'https://images.unsplash.com/photo-1519501025264-65ba15a82390?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 52,
        createdAt: '2023-04-10T14:20:00Z'
      },
      {
        id: 5,
        title: '美食收藏',
        description: '各种美食的照片记录，包括家常菜、餐厅美食和旅行中尝试的异国料理。',
        coverUrl: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 29,
        createdAt: '2023-02-15T08:45:00Z'
      },
      {
        id: 6,
        title: '宠物日常',
        description: '宠物猫咪和狗狗的日常生活照片，记录它们的成长和有趣瞬间。',
        coverUrl: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 43,
        createdAt: '2023-03-05T16:30:00Z'
      },
      {
        id: 7,
        title: '花卉植物',
        description: '各种花卉和植物的特写照片，记录大自然的美丽细节和季节变化。',
        coverUrl: 'https://images.unsplash.com/photo-1457089328109-e5d9bd499191?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 18,
        createdAt: '2022-11-20T10:00:00Z'
      },
      {
        id: 8,
        title: '户外探险',
        description: '徒步旅行、露营和其他户外活动的照片集，展示大自然的壮丽景色。',
        coverUrl: 'https://images.unsplash.com/photo-1551632811-561732d1e306?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        photoCount: 32,
        createdAt: '2023-05-08T09:15:00Z'
      }
    ];
    loading.value = false;
  }, 800);
});

// 根据搜索和排序筛选相册
const sortedAlbums = computed(() => {
  let result = [...albums.value];
  
  // 根据选项排序
  switch (sortOption.value) {
    case 'newest':
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      break;
    case 'oldest':
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      break;
    case 'az':
      result.sort((a, b) => a.title.localeCompare(b.title));
      break;
    case 'za':
      result.sort((a, b) => b.title.localeCompare(a.title));
      break;
    case 'most':
      result.sort((a, b) => b.photoCount - a.photoCount);
      break;
    case 'least':
      result.sort((a, b) => a.photoCount - b.photoCount);
      break;
  }
  
  // 如果有搜索词，进行筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(album => 
      album.title.toLowerCase().includes(query) || 
      album.description.toLowerCase().includes(query)
    );
  }
  
  return result;
});

// 分页后的相册列表
const filteredAlbums = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return sortedAlbums.value.slice(start, end);
});

// 总页数
const totalPages = computed(() => {
  return Math.ceil(sortedAlbums.value.length / pageSize);
});

// 筛选相册（重置到第一页）
const filterAlbums = () => {
  currentPage.value = 1;
};

// 清除搜索
const clearSearch = () => {
  searchQuery.value = '';
  filterAlbums();
};

// 切换页码
const changePage = (page) => {
  currentPage.value = page;
};

// 创建新相册
const createNewAlbum = () => {
  // 创建相册的逻辑
  console.log('创建新相册');
};
</script>

<style scoped>
.album-list-view {
  min-height: 100vh;
}

.page-header {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
  align-items: center;
}

.search-input {
  position: relative;
  width: 300px;
}

.search-input input {
  width: 100%;
  padding: var(--space-sm) var(--space-md);
  padding-left: 36px;
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md);
  font-size: 0.9rem;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--neutral-500);
}

.filter-options select {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md);
  background-color: white;
  font-size: 0.9rem;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.empty-state {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.empty-icon {
  margin-bottom: var(--space-md);
  color: var(--neutral-400);
}

.empty-state h3 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-800);
}

.empty-state p {
  margin-bottom: var(--space-lg);
}

.empty-actions {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: var(--space-xl);
  margin-bottom: var(--space-xxl);
}

.pagination-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md);
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: var(--neutral-100);
  border-color: var(--neutral-400);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 var(--space-md);
  font-size: 0.9rem;
  color: var(--neutral-700);
}

.loading-state {
  padding: var(--space-lg);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .filter-bar {
    flex-direction: column;
    gap: var(--space-md);
    align-items: flex-start;
  }
  
  .search-input {
    width: 100%;
  }
  
  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: var(--space-md);
  }
}
</style> 