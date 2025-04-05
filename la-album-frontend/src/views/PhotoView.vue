<template>
  <div class="photo-view">
    <div v-if="loading" class="loading-state">
      <el-skeleton animated />
    </div>
    
    <template v-else-if="photo">
      <div class="photo-header">
        <div class="container">
          <div class="header-content">
            <div class="breadcrumb">
              <router-link to="/">首页</router-link> / 
              <router-link :to="`/album/${photo.albumId}`">{{ album?.title || '相册' }}</router-link> / 
              <span>{{ photo.title }}</span>
            </div>
            
            <div class="photo-actions">
              <button class="btn btn-icon" @click="previousPhoto" v-if="hasPrevious">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
              </button>
              
              <button class="btn" @click="editPhoto">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"></path>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                </svg>
                编辑
              </button>
              
              <button class="btn btn-danger" @click="confirmDelete">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                删除
              </button>
              
              <button class="btn btn-primary" @click="sharePhoto">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="18" cy="5" r="3"></circle>
                  <circle cx="6" cy="12" r="3"></circle>
                  <circle cx="18" cy="19" r="3"></circle>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                </svg>
                分享
              </button>
              
              <button class="btn btn-icon" @click="nextPhoto" v-if="hasNext">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="photo-content">
        <div class="container">
          <div class="photo-display">
            <img :src="photo.imageUrl" :alt="photo.title">
          </div>
          
          <div class="photo-info">
            <h1 class="photo-title">{{ photo.title }}</h1>
            <p class="photo-description">{{ photo.description }}</p>
            
            <div class="photo-metadata">
              <div class="metadata-item">
                <span class="label">拍摄日期</span>
                <span class="value">{{ formatDate(photo.createdAt) }}</span>
              </div>
              
              <div class="metadata-item" v-if="photo.location">
                <span class="label">位置</span>
                <span class="value">{{ photo.location }}</span>
              </div>
              
              <div class="metadata-item">
                <span class="label">相册</span>
                <router-link :to="`/album/${photo.albumId}`" class="value link">
                  {{ album?.title || '未知相册' }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="delete-modal" v-if="showDeleteModal">
        <div class="modal-content">
          <h3>确认删除</h3>
          <p>您确定要删除照片"{{ photo.title }}"吗？此操作不可撤销。</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="cancelDelete">取消</button>
            <button class="btn btn-danger" @click="deletePhoto">删除</button>
          </div>
        </div>
      </div>
    </template>
    
    <div v-else class="error-state">
      <div class="container">
        <h2>照片未找到</h2>
        <p>抱歉，找不到此照片或您没有权限访问</p>
        <button class="btn" @click="goBack">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const photo = ref(null);
const album = ref(null);
const showDeleteModal = ref(false);
const albumPhotos = ref([]);

// 获取照片数据
onMounted(async () => {
  const photoId = parseInt(route.params.id);
  
  // 模拟API调用
  setTimeout(() => {
    // 模拟获取照片数据
    photo.value = {
      id: photoId,
      albumId: 1,
      title: '威尼斯大运河',
      imageUrl: 'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80',
      description: '意大利威尼斯大运河的美丽景色，拍摄于黄昏时分。这座城市的水道和建筑展现了独特的魅力和历史底蕴。',
      createdAt: '2023-07-15T14:30:00Z',
      location: '意大利，威尼斯'
    };
    
    // 模拟获取相册数据
    album.value = {
      id: 1,
      title: '2023夏日旅行',
      description: '2023年暑假的欧洲之旅，收集了各种美丽的风景和难忘的瞬间。',
      coverUrl: 'https://images.unsplash.com/photo-1506929562872-bb421503ef21?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      createdAt: '2023-07-15T12:00:00Z'
    };
    
    // 模拟获取相册中的所有照片（用于前后导航）
    albumPhotos.value = [
      {
        id: 1,
        albumId: 1,
        title: '威尼斯大运河',
        imageUrl: 'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
      },
      {
        id: 2,
        albumId: 1,
        title: '巴黎埃菲尔铁塔',
        imageUrl: 'https://images.unsplash.com/photo-1543349689-9a4d426bee8e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
      },
      {
        id: 3,
        albumId: 1,
        title: '伦敦塔桥',
        imageUrl: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'
      }
    ];
    
    loading.value = false;
  }, 800);
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  return date.toLocaleDateString('zh-CN', options);
};

// 当前照片在相册中的索引
const currentIndex = computed(() => {
  if (!photo.value || albumPhotos.value.length === 0) return -1;
  return albumPhotos.value.findIndex(p => p.id === photo.value.id);
});

// 是否有前一张照片
const hasPrevious = computed(() => {
  return currentIndex.value > 0;
});

// 是否有后一张照片
const hasNext = computed(() => {
  return currentIndex.value < albumPhotos.value.length - 1 && currentIndex.value !== -1;
});

// 查看前一张照片
const previousPhoto = () => {
  if (!hasPrevious.value) return;
  const prevPhoto = albumPhotos.value[currentIndex.value - 1];
  router.push({ name: 'Photo', params: { id: prevPhoto.id } });
};

// 查看后一张照片
const nextPhoto = () => {
  if (!hasNext.value) return;
  const nextPhoto = albumPhotos.value[currentIndex.value + 1];
  router.push({ name: 'Photo', params: { id: nextPhoto.id } });
};

// 编辑照片
const editPhoto = () => {
  router.push({ name: 'EditPhoto', params: { id: photo.value.id } });
};

// 分享照片
const sharePhoto = () => {
  router.push({ name: 'SharePhoto', params: { id: photo.value.id } });
};

// 确认删除
const confirmDelete = () => {
  showDeleteModal.value = true;
};

// 取消删除
const cancelDelete = () => {
  showDeleteModal.value = false;
};

// 删除照片
const deletePhoto = () => {
  // 删除照片的逻辑
  console.log('删除照片', photo.value.id);
  showDeleteModal.value = false;
  
  // 删除后返回相册页
  router.push({ name: 'Album', params: { id: photo.value.albumId } });
};

// 返回上一页
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.photo-view {
  min-height: 100vh;
  position: relative;
}

.photo-header {
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--neutral-200);
  margin-bottom: var(--space-lg);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  font-size: 0.9rem;
  color: var(--neutral-500);
}

.breadcrumb a {
  color: var(--primary-color);
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.photo-actions {
  display: flex;
  gap: var(--space-sm);
}

.btn-icon {
  padding: var(--space-xs);
  display: flex;
  align-items: center;
  justify-content: center;
}

.photo-content {
  margin-bottom: var(--space-xxl);
}

.photo-display {
  margin-bottom: var(--space-lg);
  text-align: center;
  background-color: var(--neutral-800);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.photo-display img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.photo-info {
  max-width: 800px;
  margin: 0 auto;
}

.photo-title {
  margin-bottom: var(--space-sm);
  color: var(--neutral-900);
}

.photo-description {
  margin-bottom: var(--space-lg);
  color: var(--neutral-700);
  line-height: 1.6;
}

.photo-metadata {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-lg);
  padding: var(--space-md);
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
}

.metadata-item {
  display: flex;
  flex-direction: column;
}

.metadata-item .label {
  font-size: 0.8rem;
  color: var(--neutral-500);
  margin-bottom: 2px;
}

.metadata-item .value {
  font-size: 0.95rem;
  color: var(--neutral-800);
}

.metadata-item .link {
  color: var(--primary-color);
  text-decoration: none;
}

.metadata-item .link:hover {
  text-decoration: underline;
}

.delete-modal {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  background-color: white;
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  max-width: 400px;
  width: 100%;
}

.modal-content h3 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-900);
}

.modal-content p {
  margin-bottom: var(--space-lg);
  color: var(--neutral-700);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-md);
}

.error-state {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.loading-state {
  padding: var(--space-xxl) 0;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .photo-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .photo-metadata {
    flex-direction: column;
    gap: var(--space-md);
  }
}
</style> 