<template>
    <div class="album-list-view">
      <div class="page-header">
        <div class="container">
          <div class="header-content">
            <h1>可爱的人们</h1>
            <div class="header-actions">
              <!-- <button class="btn btn-primary" @click="createNewAlbum">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 5v14M5 12h14"></path>
                </svg>
                创建新相册
              </button> -->
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
                placeholder="搜索人物" 
                @input="filterAlbums"
              >
              <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
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
          
          <!-- 使用albumCard组件渲染每一个相册 -->
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
//   import AlbumCard from '@/components/album/AlbumCard.vue';
  import AlbumCard from '@/components/album/AlbumCard.vue';
  import axios from 'axios';
  const loading = ref(true);
  const albums = ref([]);
  const searchQuery = ref('');
  const sortOption = ref('newest');
  const currentPage = ref(1);
  const pageSize = 12;
  
  // 获取所有相册数据
  onMounted(async () => {
    try {
    // 调用后端 API 获取相册数据，并添加 Authorization 头
    const response = await axios.get('http://localhost:9090/api/albums', {
      headers: {
        Authorization: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbXMiOnsiaWQiOjIsInVzZXJuYW1lIjoieWhqMTExIn0sImV4cCI6MTc0NDYzNzA0MX0.GZLUaYnvdf1sj8VdI3f4e3IjZmFZOGeftKDvWTmajJ0' // 替换 YOUR_ACCESS_TOKEN 为实际的令牌
      }
    });
      // 获取相册数据
      albums.value = response.data.data.map(album => {
      // 如果 photos 数组存在且有内容，设置 url 为第一张照片的 url
      if (album.photos && album.photos.length > 0) {
        album.url = album.photos[0].url
        console.log(album.url)
      } else {
        // 如果没有照片，设置一个占位图片
        album.url = 'https://via.placeholder.com/150';
      }
      return album;
    });
    console.log(albums.value)
  } catch (error) {
    console.error('Failed to fetch albums:', error);
  } finally {
    loading.value = false;
  }

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