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
          <button class="btn btn-secondary" @click="navigateTo('/album')">查看全部</button>
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
          
          <div class="action-card">
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

const router = useRouter();
const loading = ref(true);
const albums = ref([]);

// 模拟数据加载
onMounted(async () => {
  // 模拟API调用
  setTimeout(() => {
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
      }
    ];
    loading.value = false;
  }, 800);
});

const navigateTo = (path) => {
  router.push(path);
};

const createNewAlbum = () => {
  // 创建相册的逻辑
  console.log('创建新相册');
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
