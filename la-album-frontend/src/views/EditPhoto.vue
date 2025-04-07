<template>
  <div class="edit-photo-view">
    <div class="edit-header">
      <div class="container">
        <div class="header-content">
          <h1>编辑照片</h1>
          <div class="header-actions">
            <button class="btn btn-secondary" @click="cancelEdit">取消</button>
            <button class="btn btn-primary" @click="savePhoto" :disabled="isSaving">
              <svg v-if="isSaving" class="spin-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                <polyline points="17 21 17 13 7 13 7 21"></polyline>
                <polyline points="7 3 7 8 15 8"></polyline>
              </svg>
              保存
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="edit-content">
      <div class="container">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="!photo" class="error-state">
          <div class="error-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          </div>
          <h3>照片未找到</h3>
          <p>无法找到要编辑的照片，可能已被删除或移动</p>
          <button class="btn" @click="navigateToAlbums">返回相册列表</button>
        </div>
        
        <div v-else class="edit-form">
          <div class="edit-preview">
            <img :src="photo.imageUrl" :alt="photo.title">
            <div class="edit-tools">
              <div class="tool-group">
                <h3>基本调整</h3>
                <div class="tool-item">
                  <label>亮度</label>
                  <input type="range" min="-100" max="100" v-model.number="adjustments.brightness">
                  <span class="value">{{ adjustments.brightness }}</span>
                </div>
                <div class="tool-item">
                  <label>对比度</label>
                  <input type="range" min="-100" max="100" v-model.number="adjustments.contrast">
                  <span class="value">{{ adjustments.contrast }}</span>
                </div>
                <div class="tool-item">
                  <label>饱和度</label>
                  <input type="range" min="-100" max="100" v-model.number="adjustments.saturation">
                  <span class="value">{{ adjustments.saturation }}</span>
                </div>
              </div>
              
              <div class="tool-group">
                <h3>滤镜</h3>
                <div class="filters-grid">
                  <div 
                    v-for="filter in availableFilters" 
                    :key="filter.id"
                    class="filter-item"
                    :class="{ 'active': selectedFilter === filter.id }"
                    @click="selectFilter(filter.id)"
                  >
                    <div class="filter-preview" :style="{ filter: filter.style }"></div>
                    <span class="filter-name">{{ filter.name }}</span>
                  </div>
                </div>
              </div>
              
              <div class="tool-group">
                <h3>裁剪旋转</h3>
                <div class="crop-tools">
                  <button class="tool-btn" @click="rotateCCW">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="1 4 1 10 7 10"></polyline>
                      <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
                    </svg>
                  </button>
                  <button class="tool-btn" @click="rotateCW">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="23 4 23 10 17 10"></polyline>
                      <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                    </svg>
                  </button>
                  <button class="tool-btn" @click="flipH">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 3v18"></path>
                      <path d="M4 7l8-4 8 4"></path>
                      <path d="M4 17l8 4 8-4"></path>
                    </svg>
                  </button>
                  <button class="tool-btn" @click="flipV">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M3 12h18"></path>
                      <path d="M7 4l-4 8 4 8"></path>
                      <path d="M17 4l4 8-4 8"></path>
                    </svg>
                  </button>
                  <button class="tool-btn" @click="openCropTool">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M6 2v14a2 2 0 0 0 2 2h14"></path>
                      <path d="M18 22V8a2 2 0 0 0-2-2H2"></path>
                    </svg>
                  </button>
                </div>
              </div>
              
              <button class="btn btn-secondary reset-btn" @click="resetAdjustments">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3 2v6h6"></path>
                  <path d="M21 12A9 9 0 0 0 6 5.3L3 8"></path>
                  <path d="M21 22v-6h-6"></path>
                  <path d="M3 12a9 9 0 0 0 15 6.7l3-2.7"></path>
                </svg>
                重置所有调整
              </button>
            </div>
          </div>
          
          <div class="edit-details">
            <div class="form-group">
              <label for="photo-title">照片标题</label>
              <input 
                type="text" 
                id="photo-title" 
                v-model="photo.title" 
                placeholder="输入照片标题"
              >
            </div>
            
            <div class="form-group">
              <label for="photo-description">照片描述</label>
              <textarea 
                id="photo-description" 
                v-model="photo.description" 
                placeholder="添加描述，帮助您更好地回忆这张照片"
                rows="4"
              ></textarea>
            </div>
            
            <div class="form-group">
              <label for="photo-date">拍摄日期</label>
              <input 
                type="date" 
                id="photo-date" 
                v-model="photoDate"
              >
            </div>
            
            <div class="form-group">
              <label for="photo-location">位置</label>
              <input 
                type="text" 
                id="photo-location" 
                v-model="photo.location" 
                placeholder="照片拍摄地点"
              >
            </div>
            
            <div class="form-group">
              <label for="photo-album">所属相册</label>
              <select id="photo-album" v-model="photo.albumId">
                <option 
                  v-for="album in albums" 
                  :key="album.id" 
                  :value="album.id"
                >
                  {{ album.title }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const isSaving = ref(false);
const photo = ref(null);
const albums = ref([]);
const selectedFilter = ref('none');
const photoDate = ref('');

// 调整选项
const adjustments = ref({
  brightness: 0,
  contrast: 0,
  saturation: 0,
  rotation: 0,
  flipH: false,
  flipV: false
});

// 可用滤镜列表
const availableFilters = [
  { id: 'none', name: '原图', style: '' },
  { id: 'grayscale', name: '黑白', style: 'grayscale(100%)' },
  { id: 'sepia', name: '怀旧', style: 'sepia(100%)' },
  { id: 'warm', name: '暖色', style: 'sepia(30%) saturate(140%) hue-rotate(-10deg)' },
  { id: 'cool', name: '冷色', style: 'saturate(80%) hue-rotate(20deg)' },
  { id: 'vintage', name: '复古', style: 'sepia(30%) contrast(110%) brightness(90%) saturate(85%)' },
  { id: 'dramatic', name: '戏剧', style: 'contrast(150%) saturate(120%)' },
  { id: 'fade', name: '褪色', style: 'brightness(110%) saturate(60%) contrast(90%)' },
  { id: 'muted', name: '柔和', style: 'brightness(105%) saturate(70%)' }
];

// 获取照片数据
onMounted(async () => {
  const photoId = parseInt(route.params.id);
  
  // 模拟获取相册列表
  setTimeout(() => {
    albums.value = [
      { id: 1, title: '旅行相册' },
      { id: 2, title: '家庭相册' },
      { id: 3, title: '美食收藏' },
      { id: 4, title: '风景照片' }
    ];
  }, 500);
  
  // 模拟API调用获取照片信息
  setTimeout(() => {
    photo.value = {
      id: photoId,
      title: '威尼斯大运河',
      description: '意大利威尼斯大运河的美丽景色，拍摄于黄昏时分。',
      imageUrl: 'https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80',
      albumId: 1,
      location: '意大利，威尼斯',
      createdAt: '2023-07-15T14:30:00Z'
    };
    
    // 初始化日期
    if (photo.value.createdAt) {
      const date = new Date(photo.value.createdAt);
      photoDate.value = date.toISOString().split('T')[0];
    }
    
    loading.value = false;
  }, 800);
});

// 应用滤镜
const selectFilter = (filterId) => {
  selectedFilter.value = filterId;
};

// 旋转和翻转函数
const rotateCCW = () => {
  adjustments.value.rotation -= 90;
  if (adjustments.value.rotation < 0) {
    adjustments.value.rotation += 360;
  }
};

const rotateCW = () => {
  adjustments.value.rotation = (adjustments.value.rotation + 90) % 360;
};

const flipH = () => {
  adjustments.value.flipH = !adjustments.value.flipH;
};

const flipV = () => {
  adjustments.value.flipV = !adjustments.value.flipV;
};

// 打开裁剪工具
const openCropTool = () => {
  // 实际项目中会实现裁剪功能
  console.log('打开裁剪工具');
};

// 重置所有调整
const resetAdjustments = () => {
  adjustments.value = {
    brightness: 0,
    contrast: 0,
    saturation: 0,
    rotation: 0,
    flipH: false,
    flipV: false
  };
  selectedFilter.value = 'none';
};

// 保存照片
const savePhoto = async () => {
  isSaving.value = true;
  
  // 更新照片创建日期
  if (photoDate.value) {
    const time = photo.value.createdAt ? new Date(photo.value.createdAt).toISOString().split('T')[1] : '12:00:00Z';
    photo.value.createdAt = `${photoDate.value}T${time}`;
  }
  
  // 模拟API调用保存照片
  setTimeout(() => {
    console.log('保存照片:', {
      ...photo.value,
      adjustments: adjustments.value,
      filter: selectedFilter.value
    });
    
    isSaving.value = false;
    
    // 返回照片详情页
    router.push({ name: 'Photo', params: { id: photo.value.id } });
  }, 1500);
};

// 取消编辑
const cancelEdit = () => {
  // 返回上一页
  router.back();
};

// 导航到相册列表
const navigateToAlbums = () => {
  router.push({ name: 'AlbumList' });
};
</script>

<style scoped>
.edit-photo-view {
  min-height: 100vh;
}

.edit-header {
  background-color: var(--primary-light);
  padding: var(--space-lg) 0;
  margin-bottom: var(--space-xl);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: var(--space-md);
}

.edit-content {
  padding-bottom: var(--space-xxl);
}

.loading-state {
  padding: var(--space-lg);
  max-width: 800px;
  margin: 0 auto;
}

.error-state {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--neutral-600);
}

.error-icon {
  margin-bottom: var(--space-md);
  color: var(--neutral-400);
}

.error-state h3 {
  margin-bottom: var(--space-sm);
  color: var(--neutral-800);
}

.error-state p {
  margin-bottom: var(--space-lg);
}

.edit-form {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-xl);
  max-width: 1200px;
  margin: 0 auto;
}

.edit-preview {
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.edit-preview img {
  width: 100%;
  height: auto;
  max-height: 500px;
  object-fit: contain;
  background-color: var(--neutral-900);
  transform-origin: center;
  transform: rotate(var(--rotation)) scaleX(var(--flip-h)) scaleY(var(--flip-v));
  filter: var(--filter);
}

.edit-preview img {
  --brightness: brightness(calc(100% + var(--brightness-value)));
  --contrast: contrast(calc(100% + var(--contrast-value)));
  --saturation: saturate(calc(100% + var(--saturation-value)));
  --rotation: 0deg;
  --flip-h: 1;
  --flip-v: 1;
  --filter: var(--brightness) var(--contrast) var(--saturation);
}

.edit-tools {
  padding: var(--space-lg);
}

.tool-group {
  margin-bottom: var(--space-lg);
  border-bottom: 1px solid var(--neutral-200);
  padding-bottom: var(--space-lg);
}

.tool-group:last-child {
  border-bottom: none;
}

.tool-group h3 {
  margin-bottom: var(--space-md);
  color: var(--neutral-800);
  font-size: 1rem;
}

.tool-item {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-sm);
}

.tool-item label {
  width: 70px;
  color: var(--neutral-700);
}

.tool-item input[type="range"] {
  flex: 1;
  margin: 0 var(--space-sm);
}

.tool-item .value {
  width: 40px;
  text-align: right;
  color: var(--neutral-700);
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: var(--space-sm);
}

.filter-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-item:hover {
  transform: translateY(-2px);
}

.filter-item.active {
  transform: scale(1.05);
}

.filter-item.active .filter-preview {
  border-color: var(--primary-color);
}

.filter-preview {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-sm);
  background-image: url('https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80');
  background-size: cover;
  background-position: center;
  margin-bottom: var(--space-xs);
  border: 2px solid transparent;
  transition: all 0.2s;
}

.filter-name {
  font-size: 0.8rem;
  color: var(--neutral-700);
  text-align: center;
}

.crop-tools {
  display: flex;
  justify-content: center;
  gap: var(--space-sm);
}

.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--neutral-300);
  background-color: var(--neutral-50);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-btn:hover {
  background-color: var(--neutral-200);
  transform: translateY(-2px);
}

.reset-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  margin-top: var(--space-md);
  width: 100%;
}

.edit-details {
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  height: fit-content;
  box-shadow: var(--shadow-sm);
}

.form-group {
  margin-bottom: var(--space-md);
}

.form-group label {
  display: block;
  margin-bottom: var(--space-xs);
  color: var(--neutral-700);
  font-weight: 500;
}

.form-group input[type="text"],
.form-group input[type="date"],
.form-group textarea,
.form-group select {
  width: 100%;
  padding: var(--space-sm);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-sm);
  background-color: white;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .edit-form {
    grid-template-columns: 1fr;
  }
  
  .edit-preview {
    margin-bottom: var(--space-lg);
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
  }
  
  .header-actions {
    width: 100%;
  }
  
  .filters-grid {
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  }
  
  .filter-preview {
    width: 50px;
    height: 50px;
  }
}
</style>
  