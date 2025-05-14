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
            <img :src="photo.url" :alt="photo.title">
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
  
  try {
    const token = localStorage.getItem('token');
    const photoResponse = await fetch(`/api/photos/${photoId}`, {
      headers: {
        'Authorization': `${token}`,
        'Content-Type': 'application/json'
      }
    });
    photo.value = await photoResponse.json();
    
    // 获取相册数据
    if (photo.value?.albumId) {
      const albumResponse = await fetch(`/api/albums/${photo.value.albumId}`);
      album.value = await albumResponse.json();
    }
    
    // 获取相册中的所有照片（用于前后导航）
    if (photo.value?.albumId) {
      const photosResponse = await fetch(`/api/albums/${photo.value.albumId}/photos`);
      albumPhotos.value = await photosResponse.json();
    }
  } catch (error) {
    console.error('获取照片数据失败:', error);
  } finally {
    loading.value = false;
  }
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
const deletePhoto = async () => {
  try {
    const response = await fetch(`/api/photos/${photo.value.id}`, {
      method: 'DELETE'
    });
    
    if (response.ok) {
      // 删除后返回相册页
      router.push({ name: 'Album', params: { id: photo.value.albumId } });
    } else {
      console.error('删除照片失败');
    }
  } catch (error) {
    console.error('删除照片时出错:', error);
  } finally {
    showDeleteModal.value = false;
  }
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

</style>