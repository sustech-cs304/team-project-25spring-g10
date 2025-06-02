<template>
  <main class="edit-memory-view">
    <div class="container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>正在加载回忆视频...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
        </div>
        <h2>无法加载视频</h2>
        <p>{{ errorMessage }}</p>
        <button class="btn btn-primary" @click="goBack">返回</button>
      </div>

      <!-- 编辑表单 -->
      <div v-else class="edit-content">
        <div class="page-header">
          <h1>编辑回忆视频</h1>
          <div class="header-actions">
            <button class="btn" @click="goBack">取消</button>
            <button 
              class="btn btn-primary" 
              @click="saveChanges"
              :disabled="isSaving || !isFormValid"
            >
              <span v-if="isSaving">
                <span class="spinner-sm"></span> 保存中...
              </span>
              <span v-else>保存更改</span>
            </button>
          </div>
        </div>

        <div class="edit-form">
          <div class="form-section basic-info">
            <h2>基本信息</h2>
            <div class="form-group">
              <label for="title">视频标题</label>
              <input 
                type="text" 
                id="title" 
                v-model="editedMemory.title" 
                class="form-input"
                placeholder="给回忆起个名字"
                required
              />
            </div>
            
            <div class="form-group">
              <MusicSelector
                ref="musicSelector"
                :music-list="backgroundMusic"
                v-model="editedMemory.bgmId"
              />
            </div>
            
            <div class="form-group">
              <label for="transition">转场效果</label>
              <select id="transition" v-model="editedMemory.transition" class="form-select">
                <option value="fade">淡入淡出</option>
                <option value="slide">滑动</option>
                <option value="zoom">缩放</option>
                <option value="rotate">旋转</option>
                <option value="random">随机</option>
              </select>
            </div>
          </div>

          <div class="form-section preview">
            <h2>视频预览</h2>
            <div class="video-preview">
              <image-slider
                ref="videoPlayer"
                :photos="editedMemory.photos.filter(p => p.included)"
                :transition="editedMemory.transition"
                :duration="calculateTotalDuration()"
                :autoplay="false"
                :bgm-url="getBgmUrlById(editedMemory.bgmId)"
              />
            </div>
            <div class="preview-info">
              <p>总时长: {{ formatTime(calculateTotalDuration()) }}</p>
              <p>包含照片: {{ editedMemory.photos.filter(p => p.included).length }} / {{ editedMemory.photos.length }}</p>
            </div>
            <div class="preview-actions">
              <button class="btn btn-primary" @click="regenerateVideo" :disabled="isRegenerating">
                <span v-if="isRegenerating">
                  <span class="spinner-sm"></span> 重新生成中...
                </span>
                <span v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"></path>
                  </svg>
                  重新生成视频
                </span>
              </button>
              <button class="btn" @click="previewPlayback" v-if="videoPlayer">
                <span v-if="isPlaying">
                  暂停预览
                </span>
                <span v-else>
                  播放预览
                </span>
              </button>
            </div>
          </div>

          <div class="form-section photo-management">
            <h2>照片管理</h2>
            <p class="section-desc">拖动可调整照片顺序，勾选可选择要包含在视频中的照片，设置每张照片的显示时长</p>
            
            <div class="photo-selection">
              <div 
                v-for="(photo, index) in editedMemory.photos" 
                :key="photo.id" 
                class="photo-item"
                :class="{ 'excluded': !photo.included }"
                draggable="true"
                @dragstart="dragStart(index, $event)"
                @dragover.prevent
                @dragenter.prevent
                @drop="drop(index, $event)"
              >
                <div class="photo-thumbnail">
                  <img :src="photo.thumbnailUrl" :alt="photo.name" />
                  <div class="photo-controls">
                    <label class="checkbox">
                      <input type="checkbox" v-model="photo.included" />
                      <span class="checkmark"></span>
                    </label>
                  </div>
                  <div class="drag-handle">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="3" y1="12" x2="21" y2="12"></line>
                      <line x1="3" y1="6" x2="21" y2="6"></line>
                      <line x1="3" y1="18" x2="21" y2="18"></line>
                    </svg>
                  </div>
                </div>
                <div class="photo-details">
                  <div class="photo-order">{{ index + 1 }}</div>
                  <div class="photo-duration">
                    <label>显示时长 (秒):</label>
                    <input 
                      type="number" 
                      min="1" 
                      max="60" 
                      :value="photo.displayDuration"
                      @input="updatePhotoDuration(photo, $event)" 
                      @change="updateTotalDuration"
                    />
                  </div>
                </div>
              </div>
            </div>
            
            <div class="total-duration">
              <p>视频总时长: {{ formatTime(calculateTotalDuration()) }}</p>
            </div>
            
            <div class="photo-actions">
              <button class="btn" @click="selectAllPhotos">全选</button>
              <button class="btn" @click="deselectAllPhotos">全不选</button>
              <button class="btn" @click="resetPhotoOrder">重置顺序</button>
              <button class="btn" @click="resetPhotoDurations">重置时长</button>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn" @click="goBack">取消</button>
          <button 
            class="btn btn-primary" 
            @click="saveChanges"
            :disabled="isSaving || !isFormValid"
          >
            <span v-if="isSaving">
              <span class="spinner-sm"></span> 保存中...
            </span>
            <span v-else>保存更改</span>
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getMemoryById, updateMemory, fetchBgMusic } from '../api/memory';
import ImageSlider from '../components/memory/ImageSlider.vue';
import MusicSelector from '../components/memory/MusicSelector.vue';

const route = useRoute();
const router = useRouter();
const videoPlayer = ref(null);
const musicSelector = ref(null);
const memory = ref({});
const editedMemory = ref({
  title: '',
  bgmId: 1,
  transition: 'fade',
  photos: []
});
const originalPhotos = ref([]);
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('');
const isSaving = ref(false);
const isRegenerating = ref(false);
const isPlaying = ref(false);

// 背景音乐列表
const backgroundMusic = ref([]);

// 加载背景音乐
const loadBackgroundMusic = async () => {
  try {
    backgroundMusic.value = await fetchBgMusic();
  } catch (err) {
    console.error('加载背景音乐失败:', err);
  }
};

// 根据ID获取背景音乐URL
const getBgmUrlById = (bgmId) => {
  if (!backgroundMusic.value || backgroundMusic.value.length === 0) return '';
  
  const bgm = backgroundMusic.value.find(item => item.id === bgmId);
  return bgm ? bgm.previewUrl : '';
};

// 加载回忆视频数据
const loadMemory = async () => {
  const memoryId = route.params.id;
  if (!memoryId) {
    error.value = true;
    errorMessage.value = '未指定视频ID';
    loading.value = false;
    return;
  }

  try {
    loading.value = true;
    const response = await getMemoryById(memoryId);
    memory.value = response;
    
    // 初始化编辑表单
    editedMemory.value = {
      title: response.title,
      bgmId: response.bgmId,
      transition: response.transition,
      photos: response.photos ? response.photos.map(photo => ({
        // 统一使用id字段，处理后端可能返回photoId的情况
        id: photo.id || photo.photoId,
        thumbnailUrl: photo.thumbnailUrl,
        displayDuration: photo.displayDuration || 5, // 如果没有时长，设置默认值
        included: true
      })) : []
    };
    
    console.log('加载的记忆视频数据:', editedMemory.value);
    console.log('照片数据:', editedMemory.value.photos);
    
    // 保存原始照片顺序，用于重置
    originalPhotos.value = JSON.parse(JSON.stringify(editedMemory.value.photos));
    
    loading.value = false;
  } catch (err) {
    console.error('加载回忆视频失败:', err);
    error.value = true;
    errorMessage.value = '无法加载回忆视频，请稍后再试';
    loading.value = false;
  }
};

// 保存修改
const saveChanges = async () => {
  if (!isFormValid.value) return;
  
  // 停止音乐预览
  if (musicSelector.value) {
    musicSelector.value.stopPreview();
  }
  
  isSaving.value = true;
  try {
    // 过滤出包含的照片，并确保它们都有有效的ID
    const includedPhotos = editedMemory.value.photos.filter(photo => {
      if (!photo.included) return false;
      if (!photo.id && !photo.photoId) {
        console.warn('发现没有ID的照片:', photo);
        return false;
      }
      return true;
    });
    
    // 准备提交数据
    const memoryData = {
      title: editedMemory.value.title,
      bgmId: parseInt(editedMemory.value.bgmId) || 1, // 确保是数字类型
      transition: editedMemory.value.transition,
      photoIds: includedPhotos.map(photo => {
        const photoId = photo.id || photo.photoId;
        return photoId.toString();
      }),
      photoDisplayDurations: {}
    };
    
    // 填充照片时长信息，确保数据类型正确
    includedPhotos.forEach(photo => {
      // 确保photo.id是字符串，displayDuration是数字
      const photoId = (photo.id || photo.photoId).toString();
      const duration = parseInt(photo.displayDuration) || 5;
      memoryData.photoDisplayDurations[photoId] = duration;
    });
    
    console.log('提交的数据:', memoryData);
    
    // 提交更新
    await updateMemory(route.params.id, memoryData);
    
    // 更新成功后返回详情页
    router.push({ name: 'Memory', params: { id: route.params.id } });
  } catch (err) {
    console.error('保存视频失败:', err);
    alert('保存失败，请稍后再试');
  } finally {
    isSaving.value = false;
  }
};

// 重新生成视频
const regenerateVideo = async () => {
  if (!confirm('确定要重新生成视频吗？这将使用当前设置重新生成视频内容。')) {
    return;
  }
  
  isRegenerating.value = true;
  try {
    // 获取已包含照片的ID列表，保持当前顺序
    const includedPhotos = editedMemory.value.photos.filter(photo => photo.included);
    const orderedPhotoIds = includedPhotos.map(photo => photo.id);
    
    // 准备提交数据
    const memoryData = {
      title: editedMemory.value.title,
      bgmId: editedMemory.value.bgmId,
      transition: editedMemory.value.transition,
      photoIds: orderedPhotoIds,
      regenerate: true,
      photoDisplayDurations: {}
    };
    
    // 填充照片时长信息
    editedMemory.value.photos.forEach(photo => {
      memoryData.photoDisplayDurations[photo.id] = photo.displayDuration;
    });
    
    console.log('重新生成视频的参数:', memoryData);
    
    // 提交更新
    const updatedMemory = await updateMemory(route.params.id, memoryData);
    
    // 保存用户当前的编辑状态和照片顺序
    const currentEdits = {
      title: editedMemory.value.title,
      bgmId: editedMemory.value.bgmId,
      transition: editedMemory.value.transition
    };

    // 创建一个映射，保存用户对每张照片的编辑
    const photoEdits = {};
    const photoOrderMap = {}; // 映射照片ID到当前顺序
    
    // 记录当前照片的索引位置
    editedMemory.value.photos.forEach((photo, index) => {
      photoEdits[photo.id] = {
        included: photo.included,
        displayDuration: parseInt(photo.displayDuration) || 5
      };
      photoOrderMap[photo.id] = index;
    });
    
    // 更新本地内存数据
    memory.value = updatedMemory;
    
    // 使用更新后的数据，但保留用户的编辑和排序
    const updatedPhotos = [...updatedMemory.photos];
    
    // 根据用户当前的排序重新排列照片
    if (orderedPhotoIds.length > 0) {
      updatedPhotos.sort((a, b) => {
        const indexA = orderedPhotoIds.indexOf(a.id);
        const indexB = orderedPhotoIds.indexOf(b.id);
        
        // 如果两个ID都在orderedPhotoIds中，按照它们在orderedPhotoIds中的顺序排序
        if (indexA !== -1 && indexB !== -1) {
          return indexA - indexB;
        }
        
        // 如果只有一个ID在orderedPhotoIds中，将其排在前面
        if (indexA !== -1) return -1;
        if (indexB !== -1) return 1;
        
        // 如果两个ID都不在orderedPhotoIds中，保持原来的顺序
        return 0;
      });
    }
    
    // 更新编辑的记忆对象
    editedMemory.value = {
      ...currentEdits,
      photos: updatedPhotos.map(photo => ({
        ...photo,
        // 如果用户之前编辑过这张照片，使用用户的设置
        included: photoEdits[photo.id] ? photoEdits[photo.id].included : true,
        displayDuration: photoEdits[photo.id] ? photoEdits[photo.id].displayDuration : photo.displayDuration
      }))
    };
    
    // 保存原始照片顺序，用于重置
    originalPhotos.value = JSON.parse(JSON.stringify(editedMemory.value.photos));
    
    // 更新视频播放器
    if (videoPlayer.value) {
      videoPlayer.value.restart();
    }
    
    alert('视频已重新生成');
  } catch (err) {
    console.error('重新生成视频失败:', err);
    alert('重新生成失败，请稍后再试');
  } finally {
    isRegenerating.value = false;
  }
};

// 拖拽开始
const dragStart = (index, event) => {
  event.dataTransfer.setData('text/plain', index);
};

// 放置处理
const drop = (index, event) => {
  const draggedIndex = parseInt(event.dataTransfer.getData('text/plain'));
  if (draggedIndex === index) return;
  
  // 移动照片位置
  const photos = [...editedMemory.value.photos];
  const draggedPhoto = photos[draggedIndex];
  photos.splice(draggedIndex, 1);
  photos.splice(index, 0, draggedPhoto);
  editedMemory.value.photos = photos;
};

// 全选照片
const selectAllPhotos = () => {
  editedMemory.value.photos = editedMemory.value.photos.map(photo => ({
    ...photo,
    included: true
  }));
};

// 全不选照片
const deselectAllPhotos = () => {
  editedMemory.value.photos = editedMemory.value.photos.map(photo => ({
    ...photo,
    included: false
  }));
};

// 重置照片顺序
const resetPhotoOrder = () => {
  editedMemory.value.photos = [...originalPhotos.value];
};

// 计算总时长
const calculateTotalDuration = () => {
  const includedPhotos = editedMemory.value.photos.filter(photo => photo.included);
  return includedPhotos.reduce((total, photo) => total + (parseInt(photo.displayDuration) || 0), 0);
};

// 更新总时长
const updateTotalDuration = () => {
  // 忽略照片ID格式无效的警告，仅处理有效照片
  console.log('更新总时长');
};

// 格式化时间为 MM:SS
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 重置所有照片的显示时长
const resetPhotoDurations = () => {
  // 使用原始照片的时长
  editedMemory.value.photos.forEach(photo => {
    const originalPhoto = originalPhotos.value.find(p => p.id === photo.id);
    if (originalPhoto) {
      photo.displayDuration = originalPhoto.displayDuration;
    }
  });
  updateTotalDuration();
};

// 表单有效性检查
const isFormValid = computed(() => {
  return (
    editedMemory.value.title &&
    editedMemory.value.photos.some(photo => photo.included)
  );
});

// 返回上一页
const goBack = () => {
  // 停止音乐预览
  if (musicSelector.value) {
    musicSelector.value.stopPreview();
  }
  
  router.back();
};

// 播放/暂停预览
const previewPlayback = () => {
  if (!videoPlayer.value) return;
  
  if (isPlaying.value) {
    videoPlayer.value.pause();
    isPlaying.value = false;
  } else {
    videoPlayer.value.play();
    isPlaying.value = true;
  }
};

// 监听照片选择变化，更新预览
watch(() => editedMemory.value.photos, () => {
  // 当照片选择变化时，重置滑动器到第一张照片
  if (videoPlayer.value && videoPlayer.value.restart) {
    videoPlayer.value.restart();
  }
}, { deep: true });

// 更新特定照片的显示时长
const updatePhotoDuration = (photo, event) => {
  const value = parseInt(event.target.value);
  if (!isNaN(value) && value > 0) {
    // 确保值在1-60之间，并且是数字类型
    photo.displayDuration = Math.max(1, Math.min(60, value));
    console.log(`更新照片 ${photo.id} 的时长为 ${photo.displayDuration}秒`);
  } else {
    // 如果输入无效，重置为默认值5秒
    photo.displayDuration = 5;
    event.target.value = 5;
  }
};

onMounted(() => {
  loadMemory();
  loadBackgroundMusic();
});
</script>

<style scoped>
.edit-memory-view {
  padding: var(--space-lg) 0;
}

.loading-container,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl) 0;
  text-align: center;
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  width: 80px;
  height: 80px;
  background-color: var(--neutral-200);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-lg);
}

.error-icon svg {
  color: var(--error-color);
}

.error-state h2 {
  margin-bottom: var(--space-sm);
  font-weight: 600;
}

.error-state p {
  margin-bottom: var(--space-lg);
  color: var(--neutral-600);
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

.header-actions {
  display: flex;
  gap: var(--space-sm);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.form-section {
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
  padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
}

.form-section h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-md);
  color: var(--neutral-800);
}

.section-desc {
  margin: 0 0 var(--space-md);
  color: var(--neutral-600);
  font-size: 0.875rem;
}

.form-group {
  margin-bottom: var(--space-md);
}

.form-group:last-child {
  margin-bottom: 0;
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

.video-preview {
  margin-bottom: var(--space-md);
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: black;
  height: 300px; /* 设置固定高度 */
}

.preview-info {
  display: flex;
  justify-content: space-between;
  margin: var(--space-sm) 0;
  font-size: 0.9rem;
  color: var(--neutral-600);
}

.preview-info p {
  margin: 0;
}

.preview-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-md);
}

.photo-selection {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.photo-item {
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background-color: white;
  box-shadow: var(--shadow-xs);
  cursor: move;
  transition: transform 0.2s, opacity 0.2s;
}

.photo-item.excluded {
  opacity: 0.6;
}

.photo-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.photo-thumbnail {
  position: relative;
  padding-top: 100%;
}

.photo-thumbnail img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-controls {
  position: absolute;
  top: var(--space-xs);
  right: var(--space-xs);
  z-index: 2;
}

.drag-handle {
  position: absolute;
  top: var(--space-xs);
  left: var(--space-xs);
  color: white;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: var(--radius-sm);
  padding: 2px;
  cursor: grab;
}

.checkbox {
  display: inline-block;
  position: relative;
  cursor: pointer;
  user-select: none;
  width: 24px;
  height: 24px;
}

.checkbox input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: absolute;
  top: 0;
  left: 0;
  height: 24px;
  width: 24px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-sm);
}

.checkbox:hover input ~ .checkmark {
  background-color: white;
}

.checkbox input:checked ~ .checkmark {
  background-color: var(--primary-color);
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox input:checked ~ .checkmark:after {
  display: block;
}

.checkbox .checkmark:after {
  left: 9px;
  top: 5px;
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.photo-details {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  width: 100%;
}

.photo-order {
  font-weight: bold;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.photo-duration {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.photo-duration label {
  font-size: 0.8rem;
  margin-bottom: 3px;
}

.photo-duration input {
  width: 60px;
  text-align: center;
  padding: 4px;
  border: 1px solid #ddd;
  border-radius: 3px;
}

.total-duration {
  margin: 15px 0;
  text-align: right;
  font-weight: bold;
}

.photo-actions {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-xl);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .header-actions {
    width: 100%;
  }
  
  .photo-selection {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}

@media (max-width: 480px) {
  .photo-selection {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
}
</style> 