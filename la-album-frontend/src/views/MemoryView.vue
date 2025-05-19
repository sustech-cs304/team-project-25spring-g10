<template>
  <main class="memory-view">
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

      <!-- 视频内容 -->
      <div v-else class="memory-content">
        <div class="memory-header">
          <div>
            <h1 class="memory-title">{{ memory.title }}</h1>
            <p class="memory-meta">
              创建于 {{ formatDate(memory.createdAt) }} · {{ formatDuration(memory.duration) }}
            </p>
          </div>
          <div class="memory-actions">
            <button class="btn" @click="goBack">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="19" y1="12" x2="5" y2="12"></line>
                <polyline points="12 19 5 12 12 5"></polyline>
              </svg>
              返回
            </button>
            <button class="btn btn-primary" @click="editMemory">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              编辑
            </button>
            <button class="btn btn-primary" @click="shareMemory">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="18" cy="5" r="3"></circle>
                <circle cx="6" cy="12" r="3"></circle>
                <circle cx="18" cy="19" r="3"></circle>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
              </svg>
              分享
            </button>
          </div>
        </div>

        <!-- 视频播放器替换为图像滑动器 -->
        <div class="video-container">
          <image-slider
            ref="videoPlayer"
            :photos="memory.photos || []"
            :transition="memory.transition"
            :duration="memory.duration"
            :autoplay="false"
            :bgm-url="memory.bgmUrl || ''"
            @update:currentTime="onTimeUpdate"
            @ended="onVideoEnded"
          />
        </div>

        <!-- 视频详情 -->
        <div class="memory-details">
          <div class="memory-info">
            <div class="info-group">
              <h3>视频信息</h3>
              <div class="info-list">
                <div class="info-item">
                  <span class="info-label">来源相册</span>
                  <span class="info-value">{{ memory.albumName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">照片数量</span>
                  <span class="info-value">{{ memory.photoCount }}张</span>
                </div>
                <div class="info-item">
                  <span class="info-label">背景音乐</span>
                  <span class="info-value">{{ memory.bgmName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">转场效果</span>
                  <span class="info-value">{{ formatTransition(memory.transition) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 相关相册照片 -->
            <div class="info-group">
              <h3>包含的照片</h3>
              <div class="photo-grid">
                <div v-for="photo in memory.photos" :key="photo.id" class="photo-item" @click="viewPhoto(photo.id)">
                  <img :src="photo.thumbnailUrl" :alt="photo.name" />
                </div>
              </div>
            </div>
          </div>
          
          <!-- 相关回忆 -->
          <div class="related-memories" v-if="relatedMemories.length > 0">
            <h3>相关回忆</h3>
            <div class="related-memories-list">
              <div v-for="relatedMemory in relatedMemories" :key="relatedMemory.id" 
                  class="related-memory-card" @click="viewMemory(relatedMemory.id)">
                <div class="related-memory-thumbnail">
                  <img :src="relatedMemory.thumbnailUrl" alt="相关回忆缩略图" />
                  <div class="play-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polygon points="5 3 19 12 5 21 5 3"></polygon>
                    </svg>
                  </div>
                </div>
                <div class="related-memory-info">
                  <h4>{{ relatedMemory.title }}</h4>
                  <p>{{ formatDuration(relatedMemory.duration) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getMemoryById } from '../api/mockMemory';
import ImageSlider from '../components/memory/ImageSlider.vue';

const route = useRoute();
const router = useRouter();
const videoPlayer = ref(null);
const memory = ref({});
const relatedMemories = ref([]);
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('');
const currentTime = ref(0);

// 加载回忆视频
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
    
    // 添加调试日志
    console.log('加载的记忆视频数据:', {
      id: response.id,
      title: response.title,
      duration: response.duration,
      photos: response.photos?.length || 0,
      photoDetails: response.photos?.map(p => ({
        id: p.id,
        duration: p.displayDuration
      })) || [],
      bgmUrl: response.bgmUrl
    });
    
    // 计算并验证总时长
    const calculatedDuration = response.photos?.reduce((total, photo) => 
      total + (parseInt(photo.displayDuration) || 0), 0) || 0;
    
    console.log(`记忆视频总时长: ${response.duration}秒, 计算得到的总时长: ${calculatedDuration}秒`);
    
    // 如果有相关回忆数据，加载相关回忆
    if (response.relatedMemories) {
      relatedMemories.value = response.relatedMemories;
    }
    
    // 添加一点延迟，等待组件挂载
    setTimeout(() => {
      loading.value = false;
      
      // 一秒后自动播放
      setTimeout(() => {
        if (videoPlayer.value) {
          console.log('准备自动播放视频，背景音乐URL:', memory.value.bgmUrl);
          
          // 恢复上次播放位置
          const savedTime = localStorage.getItem(`memory_${route.params.id}_time`);
          if (savedTime && videoPlayer.value.setCurrentTime) {
            videoPlayer.value.setCurrentTime(parseFloat(savedTime));
          }
          // 开始播放
          if (videoPlayer.value.play) {
            videoPlayer.value.play();
          }
        }
      }, 1000);
    }, 500);
  } catch (err) {
    console.error('加载回忆视频失败:', err);
    error.value = true;
    errorMessage.value = '无法加载回忆视频，请稍后再试';
    loading.value = false;
  }
};

// 视频播放状态跟踪
const onTimeUpdate = (time) => {
  currentTime.value = time;
  // 保存播放位置
  localStorage.setItem(`memory_${route.params.id}_time`, time);
};

// 视频播放结束
const onVideoEnded = () => {
  console.log('视频播放完成');
  // 清除播放进度
  localStorage.removeItem(`memory_${route.params.id}_time`);
};

// 格式化时间为 MM:SS
const formatDuration = (seconds) => {
  if (!seconds) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  });
};

// 格式化风格名称
const formatTransition = (transition) => {
  const transitionMap = {
    'fade': '淡入淡出',
    'slide': '滑动',
    'zoom': '缩放',
    'rotate': '旋转',
    'random': '随机'
  };
  
  return transitionMap[transition] || transition;
};

// 编辑回忆视频
const editMemory = () => {
  router.push({ name: 'EditMemory', params: { id: route.params.id } });
};

// 分享回忆视频
const shareMemory = () => {
  alert('分享功能模拟：已复制分享链接到剪贴板！');
};

// 查看照片
const viewPhoto = (photoId) => {
  router.push({ name: 'Photo', params: { id: photoId } });
};

// 查看相关回忆
const viewMemory = (memoryId) => {
  router.push({ name: 'Memory', params: { id: memoryId } });
};

// 返回上一页
const goBack = () => {
  router.back();
};

onMounted(() => {
  loadMemory();
});

// 监听路由ID变化，重新加载记忆
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    console.log(`记忆ID从 ${oldId} 变更为 ${newId}，重新加载记忆数据`);
    loadMemory();
  }
});

onBeforeUnmount(() => {
  // 保存最后的播放位置
  localStorage.setItem(`memory_${route.params.id}_time`, currentTime.value);
});
</script>

<style scoped>
.memory-view {
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon,
.loading-icon {
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

.memory-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-lg);
}

.memory-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 var(--space-xs);
  color: var(--neutral-900);
}

.memory-meta {
  font-size: 0.875rem;
  color: var(--neutral-600);
  margin: 0;
}

.memory-actions {
  display: flex;
  gap: var(--space-sm);
}

.video-container {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--space-xl);
  background-color: black;
  box-shadow: var(--shadow-md);
  height: 450px; /* 设置固定高度 */
}

.memory-details {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-xl);
}

.memory-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.info-group h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-md);
  color: var(--neutral-800);
}

.info-list {
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
  padding: var(--space-md);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-sm) 0;
  border-bottom: 1px solid var(--neutral-200);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: var(--neutral-700);
}

.info-value {
  color: var(--neutral-900);
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: var(--space-sm);
}

.photo-item {
  position: relative;
  padding-top: 100%;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.photo-item:hover {
  transform: scale(1.05);
}

.photo-item img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-memories h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-md);
  color: var(--neutral-800);
}

.related-memories-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.related-memory-card {
  display: flex;
  background-color: var(--neutral-100);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.related-memory-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.related-memory-thumbnail {
  position: relative;
  width: 120px;
  height: 70px;
}

.related-memory-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-icon {
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

.play-icon svg {
  color: white;
}

.related-memory-thumbnail:hover .play-icon {
  opacity: 1;
}

.related-memory-info {
  padding: var(--space-sm);
  flex-grow: 1;
}

.related-memory-info h4 {
  margin: 0 0 var(--space-xs);
  font-size: 0.875rem;
  font-weight: 600;
}

.related-memory-info p {
  margin: 0;
  font-size: 0.75rem;
  color: var(--neutral-600);
}

/* 响应式调整 */
@media (max-width: 968px) {
  .memory-details {
    grid-template-columns: 1fr;
    gap: var(--space-lg);
  }
}

@media (max-width: 768px) {
  .memory-header {
    flex-direction: column;
    gap: var(--space-md);
  }
  
  .memory-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .photo-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}
</style> 