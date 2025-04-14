<template>
  <div class="home">
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1>我的相册集</h1>
          <p class="hero-description">收集和整理您珍贵的回忆</p>
          <div class="hero-actions">
            <button class="btn" @click="createNewAlbum">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 5v14M5 12h14"></path>
              </svg>
              创建新相册
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="albums-section">
      <div class="container">
        <div class="section-header">
          <h2>最近的相册</h2>
          <button class="btn btn-secondary" @click="navigateTo('/albums')">查看全部</button>
        </div>

        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="albums.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <h3>还没有相册</h3>
          <p>创建您的第一个相册来开始整理照片吧</p>
          <button class="btn" @click="createNewAlbum">创建相册</button>
        </div>
        
        <div v-else class="albums-grid">
          <AlbumCard 
            v-for="album in albums" 
            :key="album.id" 
            :album="album"
          />
        </div>
      </div>
    </section>
    
    <section class="quick-actions">
      <div class="container">
        <div class="actions-grid">
          <div class="action-card" @click="navigateTo('/search')">
            <div class="action-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <div class="action-text">搜索照片</div>
          </div>
          
          <div class="action-card" @click="navigateTo('/trash')">
            <div class="action-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
            </div>
            <div class="action-text">回收站</div>
          </div>
          
          <div class="action-card" @click="navigateTo('/upload')">
            <div class="action-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
            </div>
            <div class="action-text">批量上传</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AlbumCard from '@/components/album/AlbumCard.vue';
import { fetchRecentAlbums } from '@/api/album';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const albums = ref([]);

// 获取最近相册数据
onMounted(async () => {
  try {
    loading.value = true;
    // 调用API获取最近的相册
    const recentAlbums = await fetchRecentAlbums(4); // 获取最近的4个相册
    
    // 处理返回的数据
    if (Array.isArray(recentAlbums)) {
      albums.value = recentAlbums.map(album => {
        // 添加封面URL (如果相册有照片，使用第一张照片作为封面)
        const coverUrl = album.photos && album.photos.length > 0 
          ? album.photos[0].url 
          : 'https://images.unsplash.com/photo-1506929562872-bb421503ef21?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80';
        
        // 计算照片数量
        const photoCount = album.photos ? album.photos.length : 0;
        
        return {
          ...album,
          coverUrl,
          photoCount,
          createdAt: album.createTime // 使用createTime字段作为createdAt
        };
      });
    }
  } catch (error) {
    console.error('获取最近相册失败:', error);
    ElMessage.error('获取相册数据失败，请稍后再试');
    albums.value = [];
  } finally {
    loading.value = false;
  }
});

const navigateTo = (path) => {
  router.push(path);
};

const createNewAlbum = () => {
  // 导航到相册列表页并打开创建模式
  router.push({ 
    path: '/albums',
    query: { createNew: 'true' }
  });
};
</script>

<style scoped>
.home {
  min-height: 100vh;
}

.hero-section {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.hero-description {
  margin-bottom: var(--space-lg);
  color: var(--neutral-700);
  font-size: 1.2rem;
}

.hero-actions {
  margin-top: var(--space-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
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

.quick-actions {
  margin-bottom: var(--space-xxl);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-md);
}

.action-card {
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s;
  cursor: pointer;
}

.action-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.action-icon {
  margin-bottom: var(--space-md);
  color: var(--primary-color);
}

.action-text {
  font-weight: 500;
  color: var(--neutral-800);
}

.loading-state {
  padding: var(--space-lg);
}

@media (max-width: 768px) {
  .hero-section {
    padding: var(--space-lg) 0;
  }
  
  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: var(--space-md);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }
}
</style>
