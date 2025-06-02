<template>
  <div class="album-view">
    <div v-if="loading" class="loading-state">
      <el-skeleton animated />
    </div>
    
    <template v-else-if="album && album.photos">
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
              
              <button class="btn" @click="editAlbum">
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

  <el-dialog
    v-model="showEditDialog"
    title="编辑相册"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="editingAlbum" label-width="80px">
      <el-form-item label="名称" required>
        <el-input v-model="editingAlbum.title" placeholder="请输入相册名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="editingAlbum.description"
          type="textarea"
          rows="4"
          placeholder="请输入相册描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button 
          type="primary" 
          @click="saveAlbumChanges"
          :loading="isSubmitting"
        >
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted} from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PhotoThumbnail from '@/components/photo/PhotoThumbnail.vue';
import { fetchAlbumById,updateAlbum } from '@/api/album';
import { ElMessage} from 'element-plus';


const route = useRoute();
const router = useRouter();
const loading = ref(true);
const album = ref(null);
const selectionMode = ref(false);
const selectedPhotos = ref([]);
const showEditDialog = ref(false);
const editingAlbum = ref({
  title: '',
  description: ''
});
const isSubmitting = ref(false);

// 获取相册数据
onMounted(async () => {
  const albumId = parseInt(route.params.id);
  
  // 获取特定相册的API调用
  try {
    loading.value = true;
    const response = await fetchAlbumById(albumId);
    console.log("album ID: ", albumId);
    // 检查返回格式，正确解析数据
    // 后端返回的格式可能是 { code: 0, message: "操作成功", data: 相册数据 }
    console.log('获取相册响应 in AlbumView:', response);
    
    // if (response && response.code === 0 && response.data) {
    if (response) {
      // 正确处理相册数据
      album.value = {
        ...response,
        createdAt: response.createTime || new Date().toISOString(), // 保持字段一致性
        photos: response.photos || []
      };
      console.log('处理后的相册数据 in AlbumView:', album.value);
    } else {
      console.error('相册数据格式不正确:', response);
      ElMessage.error('相册不存在或无法访问');
    }
  } catch (error) {
    console.error("加载相册数据失败:", error);
    ElMessage.error('加载相册数据失败，请稍后再试');
  } finally {
    loading.value = false;
  }
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
    router.push({
      name: 'Photo',
      params: { id: photo.id },
      query: { albumId: album.value.id }
    });
  }
};


// 编辑相册
const editAlbum = () => {
  // 初始化编辑表单数据
  editingAlbum.value = {
    title: album.value.title,
    description: album.value.description || ''
  };
  
  // 显示编辑对话框
  showEditDialog.value = true;
};

// 保存相册信息
const saveAlbumChanges = async () => {
  if (!editingAlbum.value.title.trim()) {
    ElMessage.warning('相册名称不能为空');
    return;
  }
  
  try {
    isSubmitting.value = true;
    
    // 调用API更新相册
    const response = await updateAlbum(album.value.id, {
      title: editingAlbum.value.title,
      description: editingAlbum.value.description
    });
    
    if (response && response.code === 0) {
      // 更新本地数据
      album.value.title = editingAlbum.value.title;
      album.value.description = editingAlbum.value.description;
      
      // 关闭对话框
      showEditDialog.value = false;
      
      // 显示成功消息
      ElMessage.success('相册更新成功');
    } else {
      ElMessage.error(`更新失败: ${response?.message || '未知错误'}`);
    }
  } catch (error) {
    console.error('更新相册失败:', error);
    ElMessage.error('更新相册时发生错误，请稍后重试');
  } finally {
    isSubmitting.value = false;
  }
};

// 取消编辑
const cancelEdit = () => {
  showEditDialog.value = false;
};

// 上传照片
const uploadPhotos = () => {
  router.push({
    name: 'PhotoUpload',
    query: { albumId: album.value.id }
  });
};

// 删除选中的照片
const deleteSelectedPhotos = () => {
  album.value.photos = album.value.photos.filter(photo => !selectedPhotos.value.includes(photo.id));
  selectedPhotos.value = [];
  selectionMode.value = false;
};

// 分享选中的照片
const shareSelectedPhotos = () => {
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
  