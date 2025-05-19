<template>
  <main class="memory-list-view">
    <div class="container">
      <div class="page-header">
        <h1>回忆视频</h1>
        <button class="btn btn-primary" @click="openCreateMemoryModal">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="23 7 16 12 23 17 23 7"></polygon>
            <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
          </svg>
          创建新回忆
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>正在加载回忆视频...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="memories.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="23 7 16 12 23 17 23 7"></polygon>
            <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
          </svg>
        </div>
        <h2>暂无回忆视频</h2>
        <p>从相册中创建精彩的回忆视频，为您的照片注入生命力</p>
        <button class="btn btn-primary" @click="openCreateMemoryModal">创建第一个回忆</button>
      </div>

      <!-- 视频列表 -->
      <div v-else class="memories-grid">
        <div v-for="memory in memories" :key="memory.id" class="memory-card">
          <div class="memory-thumbnail" @click="viewMemory(memory.id)">
            <img :src="memory.thumbnailUrl" alt="回忆视频缩略图" />
            <div class="play-overlay">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polygon points="10 8 16 12 10 16 10 8"></polygon>
              </svg>
            </div>
            <div class="memory-duration">{{ formatDuration(memory.duration) }}</div>
          </div>
          <div class="memory-info">
            <h3 class="memory-title">{{ memory.title }}</h3>
            <p class="memory-date">创建于 {{ formatDate(memory.createdAt) }}</p>
          </div>
          <div class="memory-actions">
            <button class="btn btn-icon" @click="editMemory(memory.id)" title="编辑">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
            </button>
            <button class="btn btn-icon" @click="shareMemory()" title="分享">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="18" cy="5" r="3"></circle>
                <circle cx="6" cy="12" r="3"></circle>
                <circle cx="18" cy="19" r="3"></circle>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
              </svg>
            </button>
            <button class="btn btn-icon" @click="confirmDelete(memory.id)" title="删除">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                <line x1="10" y1="11" x2="10" y2="17"></line>
                <line x1="14" y1="11" x2="14" y2="17"></line>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建回忆视频弹窗 -->
    <div v-if="showCreateModal" class="modal-backdrop">
      <div class="modal-container modal-lg">
        <div class="modal-header">
          <h2>创建回忆视频</h2>
          <button class="btn-close" @click="closeCreateMemoryModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-layout">
            <div class="form-main">
              <div class="form-group">
                <label for="memory-title">视频标题</label>
                <input
                  type="text"
                  id="memory-title"
                  v-model="newMemory.title"
                  placeholder="给回忆起个名字"
                  class="form-input"
                  required
                />
              </div>
              
              <div class="form-group">
                <label for="album-select">选择相册</label>
                <select 
                  id="album-select" 
                  v-model="newMemory.albumId" 
                  class="form-select" 
                  required
                  @change="onAlbumChange"
                >
                  <option value="" disabled>请选择相册</option>
                  <option v-for="album in albums" :key="album.id" :value="album.id">
                    {{ album.name }} ({{ album.photoCount }}张照片)
                  </option>
                </select>
              </div>
              
              <div class="form-group">
                <MusicSelector
                  ref="musicSelector"
                  :music-list="backgroundMusic"
                  v-model="newMemory.bgmId"
                />
              </div>
              
              <div class="form-group">
                <label for="transition-select">转场效果</label>
                <select id="transition-select" v-model="newMemory.transition" class="form-select">
                  <option value="fade">淡入淡出</option>
                  <option value="slide">滑动</option>
                  <option value="zoom">缩放</option>
                  <option value="rotate">旋转</option>
                  <option value="random">随机</option>
                </select>
              </div>
            </div>
            
            <div class="form-photo-settings" v-if="selectedAlbumPhotos.length > 0">
              <h3>照片设置</h3>
              <p class="info-text">为每张照片设置显示时长（秒）</p>
              
              <div class="total-duration" v-if="selectedAlbumPhotos.length > 0">
                <p>视频总时长: {{ formatDuration(calculateTotalDuration()) }}</p>
                <button class="btn btn-sm" @click="resetPhotoDurations">重置时长</button>
              </div>
              
              <div class="photos-list">
                <div v-for="(photo, index) in selectedAlbumPhotos" :key="photo.id" class="photo-item">
                  <div class="photo-thumbnail">
                    <img :src="photo.thumbnailUrl" :alt="photo.name" />
                    <div class="photo-order">{{ index + 1 }}</div>
                  </div>
                  <div class="photo-duration">
                    <label :for="`duration-${photo.id}`">显示时长 (秒):</label>
                    <input 
                      type="number" 
                      :id="`duration-${photo.id}`"
                      v-model="newMemory.photoDisplayDurations[photo.id]" 
                      min="1" 
                      max="60" 
                      @change="updatePhotoDisplayDuration(photo.id, newMemory.photoDisplayDurations[photo.id])"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeCreateMemoryModal">取消</button>
          <button 
            class="btn btn-primary" 
            @click="createMemory" 
            :disabled="!newMemory.title || !newMemory.albumId || isGenerating"
          >
            <span v-if="isGenerating">
              <span class="spinner-sm"></span> 正在生成...
            </span>
            <span v-else>生成回忆</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-backdrop">
      <div class="modal-container modal-sm">
        <div class="modal-header">
          <h2>确认删除</h2>
          <button class="btn-close" @click="showDeleteConfirm = false">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p>确定要删除这个回忆视频吗？此操作无法撤销。</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showDeleteConfirm = false">取消</button>
          <button class="btn btn-danger" @click="deleteMemory">删除</button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchMemories, deleteMemory as apiDeleteMemory, generateMemory, fetchAlbums, fetchBgMusic } from '../api/mockMemory';
import MusicSelector from '../components/memory/MusicSelector.vue';

const router = useRouter();
const memories = ref([]);
const albums = ref([]);
const backgroundMusic = ref([]);
const loading = ref(true);
const showCreateModal = ref(false);
const showDeleteConfirm = ref(false);
const memoryToDelete = ref(null);
const isGenerating = ref(false);
const selectedAlbumPhotos = ref([]); // 存储选择的相册中的照片
const musicSelector = ref(null);

const newMemory = ref({
  title: '',
  albumId: '',
  bgmId: '',
  transition: 'fade',
  photoDisplayDurations: {} // 存储每张照片的显示时长
});

// 加载回忆视频列表
const loadMemories = async () => {
  loading.value = true;
  try {
    memories.value = await fetchMemories();
  } catch (error) {
    console.error('Failed to load memories:', error);
    // 这里可以添加错误提示
  } finally {
    loading.value = false;
  }
};

// 加载相册列表
const loadAlbums = async () => {
  try {
    albums.value = await fetchAlbums();
  } catch (error) {
    console.error('Failed to load albums:', error);
  }
};

// 加载背景音乐
const loadBackgroundMusic = async () => {
  try {
    backgroundMusic.value = await fetchBgMusic();
    
    // 设置默认背景音乐
    if (backgroundMusic.value.length > 0) {
      newMemory.value.bgmId = backgroundMusic.value[0].id;
    }
  } catch (error) {
    console.error('Failed to load background music:', error);
  }
};

// 当选择相册变化时，加载相册中的照片
const onAlbumChange = async () => {
  if (!newMemory.value.albumId) {
    selectedAlbumPhotos.value = [];
    return;
  }
  
  try {
    // 找到选中的相册
    const album = albums.value.find(a => a.id === newMemory.value.albumId);
    if (!album) return;
    
    // 清空已有的照片时长设置
    newMemory.value.photoDisplayDurations = {};
    
    // 从API获取真实的相册照片
    const { fetchAlbumById } = await import('../api/album');
    const albumResponse = await fetchAlbumById(album.id);
    
    let albumPhotos = [];
    if (albumResponse && albumResponse.photos && albumResponse.photos.length > 0) {
      // 使用真实的相册照片
      albumPhotos = albumResponse.photos.map(photo => ({
        id: photo.id.toString(), // 确保ID是字符串
        name: photo.title || '未命名照片',
        thumbnailUrl: photo.url,
        url: photo.url,
        displayDuration: 5 // 默认5秒
      }));
    } else if (album.coverUrl) {
      // 如果没有真实照片但有封面，创建模拟照片
      for (let i = 0; i < Math.min(album.photoCount || 5, 10); i++) {
        const photoId = `photo_${i + 1}_${Date.now()}${Math.floor(Math.random() * 1000)}`;
        albumPhotos.push({
          id: photoId,
          name: `照片 ${i + 1}`,
          thumbnailUrl: album.coverUrl,
          url: album.coverUrl,
          displayDuration: 5
        });
      }
    }
    
    // 设置默认显示时长并保存到newMemory中
    albumPhotos.forEach(photo => {
      newMemory.value.photoDisplayDurations[photo.id] = 5;
    });
    
    selectedAlbumPhotos.value = albumPhotos;
    console.log('已加载照片:', albumPhotos);
    
    // 计算并更新预计总时长
    updateTotalDuration();
  } catch (error) {
    console.error('加载相册照片失败:', error);
    // 出错时可以设置一些默认照片
    selectedAlbumPhotos.value = [];
  }
};

// 更新照片显示时长
const updatePhotoDisplayDuration = (photoId, duration) => {
  const parsedDuration = parseInt(duration);
  if (isNaN(parsedDuration) || parsedDuration < 1) {
    // 限制最小值为1秒
    newMemory.value.photoDisplayDurations[photoId] = 1;
  } else if (parsedDuration > 60) {
    // 限制最大值为60秒
    newMemory.value.photoDisplayDurations[photoId] = 60;
  } else {
    newMemory.value.photoDisplayDurations[photoId] = parsedDuration;
  }
  
  // 更新总时长
  updateTotalDuration();
};

// 计算总时长
const calculateTotalDuration = () => {
  if (selectedAlbumPhotos.value.length === 0) return 0;
  
  return selectedAlbumPhotos.value.reduce((total, photo) => {
    return total + (parseInt(newMemory.value.photoDisplayDurations[photo.id]) || 0);
  }, 0);
};

// 更新总时长
const updateTotalDuration = () => {
  // 确保所有照片都已设置时长
  selectedAlbumPhotos.value.forEach(photo => {
    if (newMemory.value.photoDisplayDurations[photo.id] === undefined) {
      // 如果尚未设置，使用默认值5秒
      newMemory.value.photoDisplayDurations[photo.id] = 5;
    }
  });
};

// 重置所有照片的显示时长
const resetPhotoDurations = () => {
  selectedAlbumPhotos.value.forEach(photo => {
    newMemory.value.photoDisplayDurations[photo.id] = 5;
  });
};

// 打开创建回忆视频弹窗
const openCreateMemoryModal = () => {
  // 如果上次弹窗没有正确关闭，可能还有音乐在播放
  if (musicSelector.value) {
    musicSelector.value.stopPreview();
  }
  
  newMemory.value = {
    title: '',
    albumId: '',
    bgmId: backgroundMusic.value.length > 0 ? backgroundMusic.value[0].id : '',
    transition: 'fade',
    photoDisplayDurations: {}
  };
  selectedAlbumPhotos.value = [];
  showCreateModal.value = true;
};

// 关闭创建回忆视频弹窗
const closeCreateMemoryModal = () => {
  // 停止音乐预览
  if (musicSelector.value) {
    musicSelector.value.stopPreview();
  }
  showCreateModal.value = false;
};

// 创建回忆视频
const createMemory = async () => {
  if (!newMemory.value.title || !newMemory.value.albumId) return;
  
  // 停止音乐预览
  if (musicSelector.value) {
    musicSelector.value.stopPreview();
  }
  
  isGenerating.value = true;
  try {
    // 确保selectedAlbumPhotos中的照片时长设置正确
    selectedAlbumPhotos.value.forEach(photo => {
      if (!newMemory.value.photoDisplayDurations[photo.id]) {
        newMemory.value.photoDisplayDurations[photo.id] = 5;
      }
    });

    // 打印一下传递的数据，方便调试
    console.log('创建回忆的参数:', JSON.stringify({
      title: newMemory.value.title,
      albumId: newMemory.value.albumId,
      photoIds: selectedAlbumPhotos.value.map(p => p.id),
      photoDisplayDurations: newMemory.value.photoDisplayDurations
    }));
    
    const memoryData = {
      title: newMemory.value.title,
      albumId: newMemory.value.albumId,
      bgmId: newMemory.value.bgmId || 1,
      transition: newMemory.value.transition,
      photoIds: selectedAlbumPhotos.value.map(p => p.id), // 传递照片ID列表
      photoDisplayDurations: newMemory.value.photoDisplayDurations
    };
    
    await generateMemory(memoryData);
    closeCreateMemoryModal();
    // 重新加载回忆列表
    await loadMemories();
  } catch (error) {
    console.error('创建回忆失败:', error);
    alert('创建回忆失败，请稍后再试!');
  } finally {
    isGenerating.value = false;
  }
};

// 查看回忆视频
const viewMemory = (id) => {
  router.push({ name: 'Memory', params: { id } });
};

// 编辑回忆视频
const editMemory = (id) => {
  router.push({ name: 'EditMemory', params: { id } });
};

// 分享回忆视频
const shareMemory = () => {
  alert('分享功能模拟：已复制分享链接到剪贴板！');
};

// 确认删除回忆视频
const confirmDelete = (id) => {
  memoryToDelete.value = id;
  showDeleteConfirm.value = true;
};

// 删除回忆视频
const deleteMemory = async () => {
  if (!memoryToDelete.value) return;
  
  try {
    await apiDeleteMemory(memoryToDelete.value);
    await loadMemories();
    showDeleteConfirm.value = false;
  } catch (error) {
    console.error('Failed to delete memory:', error);
    // 这里可以添加错误提示
  }
};

// 格式化时间为 MM:SS
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });
};

onMounted(() => {
  loadMemories();
  loadAlbums();
  loadBackgroundMusic();
});
</script>

<style scoped>
.memory-list-view {
  padding: var(--space-lg) 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.page-header h1 {
  font-weight: 700;
  color: var(--neutral-900);
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl) 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--primary-color);
  animation: spin 1s ease-in-out infinite;
  margin-bottom: var(--space-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.spinner-sm {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: var(--space-xs);
  vertical-align: middle;
}

.empty-state {
  text-align: center;
  padding: var(--space-xl) 0;
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background-color: var(--neutral-200);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-lg);
}

.empty-state h2 {
  font-weight: 600;
  margin-bottom: var(--space-sm);
}

.empty-state p {
  color: var(--neutral-600);
  margin-bottom: var(--space-lg);
}

.memories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.memory-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  background-color: var(--neutral-100);
  transition: transform 0.2s, box-shadow 0.2s;
}

.memory-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.memory-thumbnail {
  position: relative;
  padding-top: 56.25%; /* 16:9 比例 */
  overflow: hidden;
  cursor: pointer;
}

.memory-thumbnail img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.play-overlay svg {
  color: white;
}

.memory-thumbnail:hover .play-overlay {
  opacity: 1;
}

.memory-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.memory-info {
  padding: var(--space-md);
}

.memory-title {
  margin: 0 0 var(--space-xs);
  font-weight: 600;
  font-size: 1rem;
}

.memory-date {
  margin: 0;
  font-size: 0.875rem;
  color: var(--neutral-600);
}

.memory-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 var(--space-md) var(--space-md);
  gap: var(--space-sm);
}

/* 对话框样式 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}

.modal-sm {
  max-width: 400px;
}

.modal-header {
  padding: var(--space-md);
  border-bottom: 1px solid var(--neutral-200);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-weight: 600;
}

.btn-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-xs);
  color: var(--neutral-600);
}

.modal-body {
  padding: var(--space-md);
}

.modal-footer {
  padding: var(--space-md);
  border-top: 1px solid var(--neutral-200);
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.form-group {
  margin-bottom: var(--space-md);
}

.form-group label {
  display: block;
  margin-bottom: var(--space-xs);
  font-weight: 500;
}

.form-input,
.form-select {
  width: 100%;
  padding: var(--space-sm);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-sm);
  font-size: 1rem;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.2);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .memories-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
}

@media (max-width: 480px) {
  .memories-grid {
    grid-template-columns: 1fr;
  }
}

.modal-lg {
  width: 90%;
  max-width: 1000px;
}

.form-layout {
  display: flex;
  gap: var(--space-lg);
}

.form-main {
  flex: 1;
  min-width: 280px;
}

.form-photo-settings {
  flex: 2;
  padding: var(--space-md);
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
}

.form-photo-settings h3 {
  margin-top: 0;
  margin-bottom: var(--space-sm);
  font-size: 1.1rem;
}

.info-text {
  color: var(--neutral-600);
  font-size: 0.9rem;
  margin-bottom: var(--space-md);
}

.total-duration {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-sm);
  background-color: var(--neutral-200);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-md);
}

.photos-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--space-md);
  max-height: 300px;
  overflow-y: auto;
  padding-right: var(--space-sm);
}

.photo-item {
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: var(--radius-sm);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.photo-thumbnail {
  position: relative;
  padding-top: 75%; /* 4:3 比例 */
  overflow: hidden;
}

.photo-thumbnail img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-order {
  position: absolute;
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 6px;
  font-size: 0.8rem;
  border-bottom-right-radius: var(--radius-sm);
}

.photo-duration {
  padding: var(--space-sm);
  display: flex;
  flex-direction: column;
}

.photo-duration label {
  font-size: 0.8rem;
  margin-bottom: 4px;
}

.photo-duration input {
  width: 100%;
  padding: 4px;
  text-align: center;
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-sm);
}

.btn-sm {
  padding: 4px 8px;
  font-size: 0.8rem;
}
</style> 