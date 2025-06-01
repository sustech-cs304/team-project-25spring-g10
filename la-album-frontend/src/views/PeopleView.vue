<template>
    <div class="album-list-view">
      <div class="page-header">
        <div class="container">
          <div class="header-content">
            <h1>可爱的人们</h1>
            <div class="header-actions">
            </div>
          </div>
        </div>
      </div>
  
      <div class="album-list-content">
        <div class="container">
          
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
            <h3>{{ searchQuery ? '没有找到匹配的相册' : '没有找到您的美照' }}</h3>
           
          </div>
          
          <div v-else class="albums-grid">
            <AlbumCard 
              v-for="album in filteredAlbums" 
              :key="album.id" 
              :album="album"
              @deleted="handleAlbumDeleted"
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
  
      <!-- 创建相册对话框 -->
      <div class="modal" v-if="showCreateAlbumModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>创建新相册</h3>
            <button class="close-btn" @click="showCreateAlbumModal = false">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="album-title">相册标题</label>
              <input 
                type="text" 
                id="album-title" 
                v-model="newAlbum.title" 
                placeholder="输入相册标题"
              >
            </div>
            
            <div class="form-group">
              <label for="album-description">相册描述</label>
              <textarea 
                id="album-description" 
                v-model="newAlbum.description" 
                placeholder="添加相册描述"
                rows="3"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateAlbumModal = false">取消</button>
            <button 
              class="btn btn-primary" 
              @click="saveNewAlbum"
              :disabled="!newAlbum.title || isCreatingAlbum"
            >
              <svg v-if="isCreatingAlbum" class="spin-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="2" x2="12" y2="6"></line>
                <line x1="12" y1="18" x2="12" y2="22"></line>
                <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                <line x1="2" y1="12" x2="6" y2="12"></line>
                <line x1="18" y1="12" x2="22" y2="12"></line>
                <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
              </svg>
              创建相册
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import AlbumCard from '@/components/album/AlbumCard.vue';
  import { createAlbum, fetchAlbumByType } from '@/api/album';
  import { ElMessage } from 'element-plus';
  
  const loading = ref(true);
  const albums = ref([]);
  const searchQuery = ref('');
  const sortOption = ref('newest');
  const currentPage = ref(1);
  const pageSize = 12;
  const showCreateAlbumModal = ref(false);
  const newAlbum = ref({ title: '', description: '' });
  const isCreatingAlbum = ref(false);
  
  // 获取路由参数
  const route = useRoute();
  const router = useRouter();
  
  // 获取所有相册数据
  onMounted(async () => {
    // 检查是否需要打开创建相册对话框
    if (route.query.createNew === 'true') {
      showCreateAlbumModal.value = true;
      // 清除URL参数以避免刷新页面时再次打开
      router.replace({ path: route.path });
    }
    
    try {
      loading.value = true;
      // 调用API获取所有相册
      const response = await fetchAlbumByType("face");
      
      console.log('获取相册列表响应:', response);
      console.log("response code:", response.code);
      if (response && Array.isArray(response)) {
        // 处理相册数据
        albums.value = response.map(album => {
          // 添加封面URL (如果相册有照片，使用第一张照片作为封面)
          const coverUrl = album.photos && album.photos.length > 0 
            ? album.photos[0].url 
            : 'https://images.unsplash.com/photo-1533669955142-6a73332af4db?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80';
          
          // 计算照片数量
          const photoCount = album.photos ? album.photos.length : 0;
          
          return {
            ...album,
            coverUrl,
            photoCount,
            createdAt: album.createTime || new Date().toISOString() // 使用createTime字段作为createdAt
          };
        });
      } else {
        console.error('相册列表数据格式不正确:', response);
        ElMessage.error('获取相册列表失败，请稍后再试');
        albums.value = [];
      }
    } catch (error) {
      console.error('获取相册列表失败:', error);
      ElMessage.error('获取相册列表失败，请稍后再试');
      albums.value = [];
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

  // 切换页码
  const changePage = (page) => {
    currentPage.value = page;
  };
  
  
  // 保存新相册
  const saveNewAlbum = async () => {
    if (!newAlbum.value.title.trim()) {
      ElMessage.warning('请输入相册标题');
      return;
    }
    
    isCreatingAlbum.value = true;
    try {
      // 准备相册数据
      const albumData = {
        title: newAlbum.value.title,
        description: newAlbum.value.description || ''
      };
      
      // 调用API创建相册
      const response = await createAlbum(albumData);
      console.log('创建相册响应:', response);
      
      if (response && response.code === 0 && response.data) {
        // 处理新创建的相册数据
        const createdAlbum = response.data;
        const newAlbumData = {
          ...createdAlbum,
          coverUrl: 'https://images.unsplash.com/photo-1533669955142-6a73332af4db?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80', // 默认封面
          photoCount: 0,
          createdAt: createdAlbum.createTime || new Date().toISOString()
        };
        
        // 添加到列表开头
        albums.value.unshift(newAlbumData);
        
        // 关闭对话框并重置表单
        showCreateAlbumModal.value = false;
        newAlbum.value = { title: '', description: '' };
        
        ElMessage.success('相册创建成功');
        
        // 重新应用排序
        if (sortOption.value !== 'newest') {
          filterAlbums();
        }
      } else {
        console.error('创建相册返回格式不正确:', response);
        ElMessage.error('创建相册失败，请稍后再试');
      }
    } catch (error) {
      console.error('创建相册失败:', error);
      ElMessage.error('创建相册失败，请稍后再试');
    } finally {
      isCreatingAlbum.value = false;
    }
  };
  
  const handleAlbumDeleted = (id) => {
    albums.value = albums.value.filter(album => album.id !== id);
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
  
  /* 模态框样式 */
  .modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
  }
  
  .modal-content {
    background-color: white;
    border-radius: var(--radius-lg);
    width: 100%;
    max-width: 500px;
    box-shadow: var(--shadow-lg);
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--space-lg);
    border-bottom: 1px solid var(--neutral-200);
  }
  
  .modal-header h3 {
    margin: 0;
    color: var(--neutral-900);
  }
  
  .close-btn {
    background: none;
    border: none;
    cursor: pointer;
    color: var(--neutral-500);
  }
  
  .close-btn:hover {
    color: var(--neutral-700);
  }
  
  .modal-body {
    padding: var(--space-lg);
  }
  
  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: var(--space-md);
    padding: var(--space-lg);
    border-top: 1px solid var(--neutral-200);
  }
  
  .form-group {
    margin-bottom: var(--space-lg);
  }
  
  .form-group:last-child {
    margin-bottom: 0;
  }
  
  .form-group label {
    display: block;
    margin-bottom: var(--space-sm);
    color: var(--neutral-700);
    font-weight: 500;
  }
  
  .form-group input,
  .form-group textarea {
    width: 100%;
    padding: var(--space-sm);
    border: 1px solid var(--neutral-300);
    border-radius: var(--radius-sm);
    background-color: white;
  }
  
  .spin-icon {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
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