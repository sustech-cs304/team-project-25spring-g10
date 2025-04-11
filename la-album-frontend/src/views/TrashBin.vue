<template>
  <div class="trash-view">
    <div class="trash-header">
      <div class="container">
        <div class="header-content">
          <h1>回收站</h1>
          <div class="header-actions" v-if="selectedPhotos.length > 0">
            <button class="btn btn-secondary" @click="cancelSelection">取消选择</button>
            <button class="btn" @click="restoreSelectedPhotos">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10"></polyline>
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
              </svg>
              恢复选中
            </button>
            <button class="btn btn-danger" @click="confirmDeleteSelected">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              永久删除选中
            </button>
          </div>
          <div class="header-actions" v-else>
            <button class="btn" @click="selectAllPhotos" :disabled="deletedPhotos.length === 0">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              全选
            </button>
            <button class="btn btn-danger" @click="confirmEmptyTrash" :disabled="deletedPhotos.length === 0">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              清空回收站
            </button>
          </div>
        </div>
        
        <div class="trash-info">
          <p>删除的照片将保留在回收站 30 天，之后将被永久删除。</p>
          <div class="selection-summary" v-if="selectedPhotos.length > 0">
            已选择 {{ selectedPhotos.length }} 张照片
          </div>
        </div>
      </div>
    </div>
    
    <div class="trash-content">
      <div class="container">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="deletedPhotos.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </div>
          <h2>回收站为空</h2>
          <p>您删除的照片将会显示在这里</p>
        </div>
        
        <div v-else class="photos-grid">
          <div 
            v-for="photo in deletedPhotos" 
            :key="photo.id" 
            class="photo-item"
            :class="{ 'selected': selectedPhotos.includes(photo.id) }"
            @click="togglePhotoSelection(photo.id)"
          >
            <div class="photo-thumbnail">
              <img :src="photo.imageUrl" :alt="photo.title">
              <div class="photo-overlay">
                <div class="overlay-actions">
                  <button class="action-btn restore" @click.stop="restorePhoto(photo.id)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="1 4 1 10 7 10"></polyline>
                      <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
                    </svg>
                  </button>
                  <button class="action-btn delete" @click.stop="confirmDeletePhoto(photo.id)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              </div>
              <div class="selection-indicator">
                <input 
                  type="checkbox" 
                  :checked="selectedPhotos.includes(photo.id)" 
                  @click.stop="togglePhotoSelection(photo.id)"
                >
              </div>
            </div>
            <div class="photo-info">
              <div class="photo-title">{{ photo.title }}</div>
              <div class="delete-info">
                <span class="delete-date">删除于：{{ formatDate(photo.deletedAt) }}</span>
                <span class="expire-date">将在 {{ getDaysLeft(photo.deletedAt) }} 天后永久删除</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认删除对话框 -->
    <div class="confirm-dialog" v-if="showDeleteConfirm">
      <div class="dialog-content">
        <h3>确认永久删除</h3>
        <p>{{ deleteConfirmMessage }}</p>
        <p class="warning-text">此操作无法撤销，照片将无法恢复。</p>
        <div class="dialog-actions">
          <button class="btn btn-secondary" @click="cancelDeleteConfirm">取消</button>
          <button class="btn btn-danger" @click="executeDelete">永久删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const loading = ref(true);
const deletedPhotos = ref([]);
const selectedPhotos = ref([]);
const showDeleteConfirm = ref(false);
const deleteTarget = ref(null); // 'single', 'selected', 'all'
const photoToDelete = ref(null);

// 获取已删除照片
onMounted(async () => {
  // 模拟API调用
  setTimeout(() => {
    deletedPhotos.value = [
      {
        id: 101,
        title: '旧金山金门大桥',
        imageUrl: 'https://images.unsplash.com/photo-1501594907352-04cda38ebc29?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        deletedAt: '2023-04-01T09:15:00Z',
        originalAlbumId: 1,
        originalAlbumName: '美国旅行'
      },
      {
        id: 102,
        title: '纽约中央公园',
        imageUrl: 'https://images.unsplash.com/photo-1534270804882-6b5048b1c1fc?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        deletedAt: '2023-04-05T14:30:00Z',
        originalAlbumId: 1,
        originalAlbumName: '美国旅行'
      },
      {
        id: 103,
        title: '东京塔',
        imageUrl: 'https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
        deletedAt: '2023-04-10T10:45:00Z',
        originalAlbumId: 2,
        originalAlbumName: '亚洲之旅'
      }
    ];
    loading.value = false;
  }, 800);
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

// 计算剩余天数
const getDaysLeft = (deletedDateString) => {
  const deletedDate = new Date(deletedDateString);
  const expiryDate = new Date(deletedDate);
  expiryDate.setDate(expiryDate.getDate() + 30);
  
  const today = new Date();
  const daysLeft = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24));
  
  return Math.max(0, daysLeft);
};

// 切换照片选择
const togglePhotoSelection = (photoId) => {
  const index = selectedPhotos.value.indexOf(photoId);
  if (index === -1) {
    selectedPhotos.value.push(photoId);
  } else {
    selectedPhotos.value.splice(index, 1);
  }
};

// 选择所有照片
const selectAllPhotos = () => {
  selectedPhotos.value = deletedPhotos.value.map(photo => photo.id);
};

// 取消选择
const cancelSelection = () => {
  selectedPhotos.value = [];
};

// 恢复单张照片
const restorePhoto = (photoId) => {
  // 这里将来会调用API恢复照片
  console.log('恢复照片', photoId);
  
  // 从已删除照片列表中移除
  deletedPhotos.value = deletedPhotos.value.filter(photo => photo.id !== photoId);
  // 从选中照片中移除
  const index = selectedPhotos.value.indexOf(photoId);
  if (index !== -1) {
    selectedPhotos.value.splice(index, 1);
  }
};

// 恢复选中照片
const restoreSelectedPhotos = () => {
  // 这里将来会调用API恢复选中的照片
  console.log('恢复选中照片', selectedPhotos.value);
  
  // 从已删除照片列表中移除
  deletedPhotos.value = deletedPhotos.value.filter(photo => !selectedPhotos.value.includes(photo.id));
  // 清空选中列表
  selectedPhotos.value = [];
};

// 确认删除信息
const deleteConfirmMessage = computed(() => {
  if (deleteTarget.value === 'single') {
    const photo = deletedPhotos.value.find(p => p.id === photoToDelete.value);
    return `您确定要永久删除照片"${photo?.title || ''}"吗？`;
  } else if (deleteTarget.value === 'selected') {
    return `您确定要永久删除选中的 ${selectedPhotos.value.length} 张照片吗？`;
  } else if (deleteTarget.value === 'all') {
    return `您确定要清空回收站，永久删除所有 ${deletedPhotos.value.length} 张照片吗？`;
  }
  return '';
});

// 确认删除单张照片
const confirmDeletePhoto = (photoId) => {
  photoToDelete.value = photoId;
  deleteTarget.value = 'single';
  showDeleteConfirm.value = true;
};

// 确认删除选中照片
const confirmDeleteSelected = () => {
  if (selectedPhotos.value.length === 0) return;
  
  deleteTarget.value = 'selected';
  showDeleteConfirm.value = true;
};

// 确认清空回收站
const confirmEmptyTrash = () => {
  if (deletedPhotos.value.length === 0) return;
  
  deleteTarget.value = 'all';
  showDeleteConfirm.value = true;
};

// 取消删除确认
const cancelDeleteConfirm = () => {
  showDeleteConfirm.value = false;
  deleteTarget.value = null;
  photoToDelete.value = null;
};

// 执行删除
const executeDelete = () => {
  // 根据不同的删除目标执行不同的删除操作
  if (deleteTarget.value === 'single') {
    // 这里将来会调用API永久删除单张照片
    console.log('永久删除照片', photoToDelete.value);
    
    // 从已删除照片列表中移除
    deletedPhotos.value = deletedPhotos.value.filter(photo => photo.id !== photoToDelete.value);
    // 从选中照片中移除
    const index = selectedPhotos.value.indexOf(photoToDelete.value);
    if (index !== -1) {
      selectedPhotos.value.splice(index, 1);
    }
  } else if (deleteTarget.value === 'selected') {
    // 这里将来会调用API永久删除选中的照片
    console.log('永久删除选中照片', selectedPhotos.value);
    
    // 从已删除照片列表中移除
    deletedPhotos.value = deletedPhotos.value.filter(photo => !selectedPhotos.value.includes(photo.id));
    // 清空选中列表
    selectedPhotos.value = [];
  } else if (deleteTarget.value === 'all') {
    // 这里将来会调用API清空回收站
    console.log('清空回收站');
    
    // 清空已删除照片列表和选中列表
    deletedPhotos.value = [];
    selectedPhotos.value = [];
  }
  
  // 关闭确认对话框
  showDeleteConfirm.value = false;
  deleteTarget.value = null;
  photoToDelete.value = null;
};
</script>

<style scoped>
.trash-view {
  min-height: 100vh;
}

.trash-header {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.header-actions {
  display: flex;
  gap: var(--space-sm);
}

.trash-info {
  color: var(--neutral-600);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selection-summary {
  font-weight: 500;
  color: var(--primary-color);
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
  margin-bottom: var(--space-xxl);
}

.photo-item {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background-color: var(--neutral-100);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
  cursor: pointer;
  position: relative;
}

.photo-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.photo-item.selected {
  box-shadow: 0 0 0 3px var(--primary-color);
}

.photo-thumbnail {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.photo-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.photo-item:hover .photo-thumbnail img {
  transform: scale(1.05);
}

.photo-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.photo-item:hover .photo-overlay {
  opacity: 1;
}

.overlay-actions {
  display: flex;
  gap: var(--space-sm);
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: scale(1.1);
}

.action-btn.restore:hover {
  background-color: var(--success);
}

.action-btn.delete:hover {
  background-color: var(--error);
}

.selection-indicator {
  position: absolute;
  top: var(--space-xs);
  right: var(--space-xs);
  z-index: 2;
}

.selection-indicator input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.photo-info {
  padding: var(--space-md);
}

.photo-title {
  font-weight: 500;
  margin-bottom: var(--space-xs);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delete-info {
  display: flex;
  flex-direction: column;
  font-size: 0.85rem;
  color: var(--neutral-600);
}

.expire-date {
  color: var(--error);
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

.empty-state h2 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-800);
}

.loading-state {
  padding: var(--space-lg);
}

.confirm-dialog {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.dialog-content {
  background-color: white;
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  width: 100%;
  max-width: 400px;
  box-shadow: var(--shadow-lg);
}

.dialog-content h3 {
  margin-bottom: var(--space-md);
  color: var(--neutral-900);
}

.dialog-content p {
  margin-bottom: var(--space-md);
  color: var(--neutral-700);
}

.warning-text {
  color: var(--error);
  font-weight: 500;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-md);
  margin-top: var(--space-lg);
}

@media (max-width: 768px) {
  .trash-header {
    padding: var(--space-lg) 0;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .trash-info {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-xs);
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .photos-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: var(--space-md);
  }
}
</style>
  