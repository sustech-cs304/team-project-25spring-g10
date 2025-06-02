<template>
  <div class="share-view">
    <div class="share-header">
      <div class="container">
        <h1>分享照片</h1>
        <p class="description">创建一个分享链接，以便他人可以查看您的照片。</p>
      </div>
    </div>
    
    <div class="share-content">
      <div class="container">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <template v-else>
          <div class="photo-preview-section">
            <div class="photo-preview" v-if="photo">
              <img :src="photo.url" :alt="photo.title">
              <div class="photo-info">
                <h2>{{ photo.title }}</h2>
                <p>{{ photo.description }}</p>
              </div>
            </div>
            <div class="photo-selection-error" v-else>
              <div class="error-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
              </div>
              <h3>未选择任何照片</h3>
              <p>请先从相册中选择一张照片进行分享</p>
              <button class="btn" @click="navigateToAlbums">浏览相册</button>
            </div>
          </div>
          
          <div class="share-options-section" v-if="photo">
            <div class="share-options">
              <h3>分享选项</h3>
              
              <div class="option-group">
                <label>分享有效期</label>
                <select v-model="shareSettings.expiryDays">
                  <option value="1">1天</option>
                  <option value="7">7天</option>
                  <option value="30">30天</option>
                  <option value="0">永久有效</option>
                </select>
              </div>
              
              
              <div class="option-group" v-if="shareSettings.accessType === 'restricted'">
                <label>访问密码</label>
                <input type="text" v-model="shareSettings.password" placeholder="设置一个访问密码">
              </div>
              
              <div class="generate-btn-container">
                <button class="btn btn-primary" @click="generateShareLink" :disabled="isGenerating">
                  <svg v-if="isGenerating" class="spin-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="2" x2="12" y2="6"></line>
                    <line x1="12" y1="18" x2="12" y2="22"></line>
                    <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                    <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                    <line x1="2" y1="12" x2="6" y2="12"></line>
                    <line x1="18" y1="12" x2="22" y2="12"></line>
                    <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                    <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                  生成分享链接
                </button>
              </div>
            </div>
            
            <div class="share-result" v-if="shareLink">
              <h3>分享链接已生成</h3>
              
              <div class="link-container">
                <input type="text" v-model="shareLink" readonly class="share-link-input">
                <button class="copy-btn" @click="copyShareLink" :class="{ 'copied': copied }">
                  <svg v-if="!copied" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  {{ copied ? '已复制' : '复制链接' }}
                </button>
              </div>
              
              
              <div class="share-info">
                <div class="info-item">
                  <span class="info-label">有效期至:</span>
                  <span class="info-value">{{ formatExpiryDate() }}</span>
                </div>
                <div class="info-item" v-if="shareSettings.accessType === 'restricted'">
                  <span class="info-label">访问密码:</span>
                  <span class="info-value">{{ shareSettings.password }}</span>
                </div>
              </div>
              
             
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getPhotoById} from '@/api/photo';

const router = useRouter();
const route = useRoute();
const loading = ref(true);
const photo = ref(null);
const album = ref(null);
const albumPhotos = ref([]);
const shareLink = ref('');
const copied = ref(false);
const isGenerating = ref(false);

// 分享设置
const shareSettings = ref({
  expiryDays: '7',
  accessType: 'public',
  password: ''
});

// 在组件加载时获取照片
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
    console.error('获取照片信息失败:', error);
    // 可以在这里添加错误提示
  } finally {
    loading.value = false;
  }
});

// 格式化有效期日期
const formatExpiryDate = () => {
  if (shareSettings.value.expiryDays === '0') {
    return '永久有效';
  }
  
  const today = new Date();
  const expiryDate = new Date(today);
  expiryDate.setDate(today.getDate() + parseInt(shareSettings.value.expiryDays));
  
  return `${expiryDate.getFullYear()}年${expiryDate.getMonth() + 1}月${expiryDate.getDate()}日`;
};

// 生成分享链接
const generateShareLink = async () => {
  if (!photo.value) return;
  
  isGenerating.value = true;
  
  try {
    // 获取照片详情
    const url = await getPhotoById(photo.value.id);
    
    // 假设 photoDetail 包含照片的完整信息，包括 URL
    if (url) {
      // 构建分享链接
      shareLink.value = url;
      
      console.log('生成的分享链接:', shareLink.value);
    } else {
      console.error('获取照片详情失败');
    }
  } catch (error) {
    console.error('生成分享链接出错:', error);
  } finally {
    isGenerating.value = false;
  }
};

// // 生成随机token用于分享链接的安全验证
// const generateRandomToken = () => {
//   return Math.random().toString(36).substring(2, 15) + 
//          Math.random().toString(36).substring(2, 15);
// };

// 生成随机令牌
// const generateRandomToken = () => {
//   return Math.random().toString(36).substr(2, 10);
// };

// 复制分享链接
const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  });
};

// 导航到相册页面
const navigateToAlbums = () => {
  router.push({ name: 'AlbumList' });
};
</script>

<style scoped>
.share-view {
  min-height: 100vh;
}

.share-header {
  background-color: var(--primary-light);
  padding: var(--space-xl) 0;
  margin-bottom: var(--space-xl);
}

.share-header h1 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-900);
}

.description {
  color: var(--neutral-600);
  max-width: 600px;
}

.share-content {
  padding-bottom: var(--space-xxl);
}

.loading-state {
  padding: var(--space-lg);
  max-width: 800px;
  margin: 0 auto;
}

.photo-preview-section {
  margin-bottom: var(--space-xl);
}

.photo-preview {
  max-width: 800px;
  margin: 0 auto;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  background-color: var(--neutral-100);
}

.photo-preview img {
  width: 100%;
  height: auto;
  max-height: 500px;
  object-fit: cover;
}

.photo-info {
  padding: var(--space-lg);
}

.photo-info h2 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-900);
}

.photo-info p {
  color: var(--neutral-600);
}

.photo-selection-error {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.error-icon {
  margin-bottom: var(--space-md);
  color: var(--neutral-400);
}

.photo-selection-error h3 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-800);
}

.photo-selection-error p {
  margin-bottom: var(--space-lg);
}

.share-options-section {
  display: flex;
  gap: var(--space-xl);
  max-width: 1000px;
  margin: 0 auto;
}

.share-options {
  flex: 1;
  background-color: var(--neutral-100);
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.share-options h3 {
  margin-bottom: var(--space-lg);
  color: var(--neutral-900);
}

.option-group {
  margin-bottom: var(--space-md);
}

.option-group label {
  display: block;
  margin-bottom: var(--space-xs);
  color: var(--neutral-700);
  font-weight: 500;
}

.option-group select,
.option-group input[type="text"] {
  width: 100%;
  padding: var(--space-sm);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-sm);
  background-color: white;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.radio-option {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}

.generate-btn-container {
  margin-top: var(--space-lg);
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  width: 100%;
  padding: var(--space-md);
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
  justify-content: center;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.share-result {
  flex: 1;
  background-color: var(--neutral-100);
  padding: var(--space-lg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.share-result h3 {
  margin-bottom: var(--space-lg);
  color: var(--neutral-900);
}

.link-container {
  display: flex;
  margin-bottom: var(--space-lg);
}

.share-link-input {
  flex: 1;
  padding: var(--space-sm);
  border: 1px solid var(--neutral-300);
  border-right: none;
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  background-color: white;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 0 var(--space-md);
  background-color: var(--neutral-200);
  border: 1px solid var(--neutral-300);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  cursor: pointer;
}

.copy-btn.copied {
  background-color: var(--success-light);
  color: var(--success);
  border-color: var(--success);
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: var(--space-md);
}

.mock-qr-code {
  width: 100%;
  height: 100%;
  background: repeating-conic-gradient(var(--neutral-900) 0% 25%, white 0% 50%) 50% / 10% 10%;
  border-radius: var(--radius-sm);
}

.download-qr-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  background-color: var(--neutral-200);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.share-info {
  margin-bottom: var(--space-lg);
  padding: var(--space-md);
  background-color: var(--neutral-50);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--primary-color);
}

.info-item {
  display: flex;
  margin-bottom: var(--space-xs);
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 80px;
  color: var(--neutral-700);
  font-weight: 500;
}

.info-value {
  color: var(--neutral-900);
}

.share-social h4 {
  margin-bottom: var(--space-md);
  color: var(--neutral-800);
}

.social-buttons {
  display: flex;
  gap: var(--space-md);
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-weight: 500;
}

.social-btn.wechat {
  background-color: #07C160;
  color: white;
}

.social-btn.weibo {
  background-color: #E6162D;
  color: white;
}

.social-btn.qq {
  background-color: #12B7F5;
  color: white;
}

@media (max-width: 768px) {
  .share-options-section {
    flex-direction: column;
  }
  
  .share-header {
    padding: var(--space-lg) 0;
  }
  
  .social-buttons {
    flex-wrap: wrap;
  }
  
  .social-btn {
    flex: 1;
    min-width: 100px;
  }
}
</style>
  