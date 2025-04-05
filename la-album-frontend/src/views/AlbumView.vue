<template>
  <div class="album-view">
    <div v-if="loading" class="loading-state">
      <el-skeleton animated />
    </div>
    
    <template v-else-if="album">
      <div class="album-header">
        <div class="container">
          <div class="album-info">
            <h1 class="album-title">{{ album.title }}</h1>
            <p class="album-description">{{ album.description }}</p>
            <div class="album-meta">
              <span class="album-date">创建于：{{ formatDate(album.createdAt) }}</span>
              <span class="photo-count">{{ album.photos.length }}张照片</span>
            </div>
            
            <div class="album-actions">
              <button class="btn btn-primary" @click="enterSelectionMode" v-if="!selectionMode">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                选择照片
              </button>
              
              <button class="btn" @click="uploadPhotos">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                上传照片
              </button>
              
              <button class="btn btn-secondary" @click="editAlbum">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"></path>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                </svg>
                编辑相册
              </button>
            </div>
            
            <div class="selection-actions" v-if="selectionMode">
              <span class="selection-count">已选择 {{ selectedPhotos.length }} 张照片</span>
              <button class="btn btn-secondary" @click="cancelSelection">取消</button>
              <button class="btn btn-danger" @click="deleteSelectedPhotos" :disabled="selectedPhotos.length === 0">删除选中</button>
              <button class="btn btn-primary" @click="shareSelectedPhotos" :disabled="selectedPhotos.length === 0">分享选中</button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="album-content">
        <div class="container">
          <div v-if="album.photos.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </div>
            <h3>相册中还没有照片</h3>
            <p>上传照片来开始丰富您的相册</p>
            <button class="btn" @click="uploadPhotos">上传照片</button>
          </div>
          
          <div v-else class="photos-grid">
            <PhotoThumbnail 
              v-for="photo in album.photos" 
              :key="photo.id" 
              :photo="photo"
              :selectable="selectionMode"
              :is-selected="selectedPhotos.includes(photo.id)"
              @toggle-selection="togglePhotoSelection"
              @click="viewPhoto(photo)"
            />
          </div>
        </div>
      </div>
    </template>
    
    <div v-else class="error-state">
      <div class="container">
        <h2>相册未找到</h2>
        <p>抱歉，找不到此相册或您没有权限访问</p>
        <button class="btn" @click="navigateToHome">返回主页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PhotoThumbnail from '@/components/photo/PhotoThumbnail.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const album = ref(null);
const selectionMode = ref(false);
const selectedPhotos = ref([]);

// 获取相册数据
onMounted(async () => {
  const albumId = parseInt(route.params.id);
  
  // 模拟API调用
  setTimeout(() => {
    // 这里将来会替换为真实的API调用
    album.value = {
      id: albumId,
      title: '2023夏日旅行',
      description: '2023年暑假的欧洲之旅，收集了各种美丽的风景和难忘的瞬间。',
      coverUrl: 'https://images.unsplash.com/photo-1506929562872-bb421503ef21?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      createdAt: '2023-07-15T12:00:00Z',
      photos: [
        {
          id: 1,
          albumId: albumId,
          title: '威尼斯大运河',
          imageUrl: 'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '意大利威尼斯大运河的美丽景色，拍摄于黄昏时分。',
          createdAt: '2023-07-15T14:30:00Z',
          location: '意大利，威尼斯'
        },
        {
          id: 2,
          albumId: albumId,
          title: '巴黎埃菲尔铁塔',
          imageUrl: 'https://images.unsplash.com/photo-1543349689-9a4d426bee8e?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '从香榭丽舍大街拍摄的埃菲尔铁塔夜景。',
          createdAt: '2023-07-18T20:15:00Z',
          location: '法国，巴黎'
        },
        {
          id: 3,
          albumId: albumId,
          title: '伦敦塔桥',
          imageUrl: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '伦敦标志性的塔桥，横跨泰晤士河。',
          createdAt: '2023-07-20T11:45:00Z',
          location: '英国，伦敦'
        },
        {
          id: 4,
          albumId: albumId,
          title: '雅典卫城',
          imageUrl: 'https://images.unsplash.com/photo-1555993539-1732b0258235?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '希腊雅典卫城，帕特农神庙的宏伟遗迹。',
          createdAt: '2023-07-22T09:30:00Z',
          location: '希腊，雅典'
        },
        {
          id: 5,
          albumId: albumId,
          title: '阿姆斯特丹运河',
          imageUrl: 'https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '荷兰阿姆斯特丹美丽的运河和自行车。',
          createdAt: '2023-07-24T16:20:00Z',
          location: '荷兰，阿姆斯特丹'
        },
        {
          id: 6,
          albumId: albumId,
          title: '布拉格城堡',
          imageUrl: 'https://images.unsplash.com/photo-1519677100203-a0e668c92439?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
          description: '捷克布拉格老城和城堡的全景图。',
          createdAt: '2023-07-26T13:10:00Z',
          location: '捷克，布拉格'
        }
      ]
    };
    loading.value = false;
  }, 800);
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

// 进入选择模式
const enterSelectionMode = () => {
  selectionMode.value = true;
};

// 取消选择模式
const cancelSelection = () => {
  selectionMode.value = false;
  selectedPhotos.value = [];
};

// 切换照片选择状态
const togglePhotoSelection = (photoId) => {
  const index = selectedPhotos.value.indexOf(photoId);
  if (index === -1) {
    selectedPhotos.value.push(photoId);
  } else {
    selectedPhotos.value.splice(index, 1);
  }
};

// 查看照片详情
const viewPhoto = (photo) => {
  if (!selectionMode.value) {
    router.push({ name: 'Photo', params: { id: photo.id } });
  }
};

// 编辑相册
const editAlbum = () => {
  // 编辑相册的逻辑
  console.log('编辑相册', album.value.id);
};

// 上传照片
const uploadPhotos = () => {
  // 上传照片的逻辑
  console.log('上传照片到相册', album.value.id);
};

// 删除选中的照片
const deleteSelectedPhotos = () => {
  // 删除照片的逻辑
  console.log('删除选中的照片', selectedPhotos.value);
  // 这里会调用API删除照片，并从当前列表中移除
  album.value.photos = album.value.photos.filter(photo => !selectedPhotos.value.includes(photo.id));
  selectedPhotos.value = [];
  selectionMode.value = false;
};

// 分享选中的照片
const shareSelectedPhotos = () => {
  // 分享照片的逻辑
  console.log('分享选中的照片', selectedPhotos.value);
};

// 返回主页
const navigateToHome = () => {
  router.push('/');
};
</script>

<style scoped>
.album-view {
  min-height: 100vh;
}

.album-header {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.album-info {
  max-width: 800px;
}

.album-title {
  margin-bottom: var(--space-sm);
  color: var(--neutral-900);
}

.album-description {
  margin-bottom: var(--space-md);
  color: var(--neutral-700);
  font-size: 1.1rem;
  line-height: 1.5;
}

.album-meta {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
  color: var(--neutral-600);
  font-size: 0.9rem;
}

.album-actions {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.selection-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-top: var(--space-md);
  padding: var(--space-md);
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.selection-count {
  margin-right: auto;
  font-weight: 500;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--space-md);
  margin-bottom: var(--space-xxl);
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

.error-state {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.loading-state {
  padding: var(--space-xxl) 0;
}

@media (max-width: 768px) {
  .album-header {
    padding: var(--space-lg) 0;
  }
  
  .album-actions, .selection-actions {
    flex-wrap: wrap;
  }
  
  .photos-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: var(--space-sm);
  }
}
</style>
  