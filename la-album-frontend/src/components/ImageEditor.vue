<template>
  <div class="image-editor">
    <div class="preview-container">
      <div 
        class="preview"
        :style="previewStyle"
        ref="preview"
      >
        <img 
          :src="imageUrl" 
          :alt="alt"
          @load="handleImageLoad"
          ref="image"
        >
      </div>
      <div class="overlay" v-if="isCropping">
        <div class="crop-overlay">
          <Cropper
            ref="cropper"
            :src="imageUrl"
            :stencil-props="{ aspectRatio: 1 }"
            :autoZoom="true"
            :resizeImage="true"
            @change="onCropChange"
          />
        </div>
        <div class="crop-controls">
          <button @click="cancelCrop">取消</button>
          <button @click="applyCrop" :disabled="isSaving">应用</button>
        </div>
      </div>
    </div>

    <div class="editor-controls">
      <div class="control-group">
        <h3>基本调整</h3>
        <div class="control">
          <label>亮度</label>
          <input 
            type="range" 
            v-model="adjustments.brightness"
            min="-100"
            max="100"
            step="1"
            @input="updatePreview"
          >
          <span class="value">{{ adjustments.brightness }}</span>
        </div>
        <div class="control">
          <label>对比度</label>
          <input 
            type="range" 
            v-model="adjustments.contrast"
            min="-100"
            max="100"
            step="1"
            @input="updatePreview"
          >
          <span class="value">{{ adjustments.contrast }}</span>
        </div>
        <div class="control">
          <label>饱和度</label>
          <input 
            type="range" 
            v-model="adjustments.saturation"
            min="0"
            max="200"
            step="1"
            @input="updatePreview"
          >
          <span class="value">{{ adjustments.saturation }}%</span>
        </div>
      </div>

      <div class="control-group">
        <h3>变换</h3>
        <div class="transform-controls">
          <button 
            @click="rotate(-90)"
            title="向左旋转"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
          <button 
            @click="rotate(90)"
            title="向右旋转"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12a9 9 0 1 1-9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
            </svg>
          </button>
          <button 
            @click="flip('horizontal')"
            title="水平翻转"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3v18"/>
              <path d="m8 7-4-4-4 4"/>
              <path d="m8 17-4 4-4-4"/>
            </svg>
          </button>
          <button 
            @click="flip('vertical')"
            title="垂直翻转"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12h18"/>
              <path d="m7 8-4 4 4 4"/>
              <path d="m17 8 4 4-4 4"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="control-group">
        <h3>滤镜</h3>
        <div class="filters">
          <button 
            v-for="filter in filters"
            :key="filter.name"
            :class="{ active: currentFilter === filter.name }"
            @click="applyFilter(filter.name)"
            :title="filter.label"
          >
            <div 
              class="filter-preview"
              :style="{ filter: filter.style }"
            >
            <img
              :src="imageUrl"
              alt="Filter Preview"
              class="filter-preview-image"
              :style="{ filter: filter.value }"
            />
          </div>
            <span>{{ filter.label }}</span>
          </button>
        </div>
      </div>

      <div class="actions">
        <button 
          class="reset-button"
          @click="reset"
          :disabled="!hasChanges"
        >
          重置
        </button>
        <button 
          class="crop-button"
          @click="startCrop"
        >
          裁剪
        </button>
        <button 
          class="save-button"
          @click="save"
          :disabled="isSaving || !hasChanges"
        >
          <span v-if="isSaving">保存中...</span>
          <span v-else>保存</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { Cropper } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'

export default {
  name: 'ImageEditor',
  components: {
    Cropper
  },
  props: {
    imageUrl: {
      type: String,
      required: true
    },
    alt: {
      type: String,
      default: '编辑图片'
    },
    initialAdjustments: {
      type: Object,
      default: () => ({
        brightness: 0,
        contrast: 0,
        saturation: 100,
        rotation: 0,
        flipH: false,
        flipV: false
      })
    }
  },
  data() {
    return {
      adjustments: { ...this.initialAdjustments },
      currentFilter: null,
      isCropping: false,
      isSaving: false,
      originalImage: null,
      cropData: null,
      filters: [
        { name: 'normal', label: '原图', style: 'none' },
        { name: 'grayscale', label: '黑白', style: 'grayscale(100%)' },
        { name: 'sepia', label: '复古', style: 'sepia(100%)' },
        { name: 'vintage', label: '复古', style: 'sepia(50%) contrast(120%)' },
        { name: 'warm', label: '暖色', style: 'sepia(30%) saturate(140%)' },
        { name: 'cool', label: '冷色', style: 'saturate(80%) hue-rotate(180deg)' },
        { name: 'dramatic', label: '戏剧', style: 'contrast(130%) saturate(120%)' },
        { name: 'fade', label: '褪色', style: 'contrast(90%) saturate(80%)' }
      ]
    }
  },
  computed: {
    previewStyle() {
      const { brightness, contrast, saturation, rotation, flipH, flipV } = this.adjustments;
      const filter = this.currentFilter ? 
        this.filters.find(f => f.name === this.currentFilter).style : 
        'none';
      
      return {
        filter: `
          brightness(${100 + brightness}%)
          contrast(${100 + contrast}%)
          saturate(${saturation}%)
          ${filter}
        `,
        transform: `
          rotate(${rotation}deg)
          scaleX(${flipH ? -1 : 1})
          scaleY(${flipV ? -1 : 1})
        `
      }
    },
    hasChanges() {
      return (
        this.adjustments.brightness !== 0 ||
        this.adjustments.contrast !== 0 ||
        this.adjustments.saturation !== 100 ||
        this.adjustments.rotation !== 0 ||
        this.adjustments.flipH ||
        this.adjustments.flipV ||
        this.currentFilter !== null ||
        this.cropData !== null
      );
    }
  },
  mounted() {
    // 加载图像时使用代理方法
    this.loadImageWithProxy();
  },
  methods: {
    async loadImageWithProxy() {
      try {
        // 使用代理获取图像，避免CORS问题
        const proxiedUrl = await this.getProxiedImage(this.imageUrl);
        
        // 更新图像引用，使用代理URL
        if (this.$refs.image) {
          this.$refs.image.src = proxiedUrl;
        }
      } catch (error) {
        console.error('使用代理加载图像失败:', error);
        // 如果失败，继续使用原始URL
      }
    },
    handleImageLoad(event) {
      this.originalImage = event.target;
      this.$emit('preview-updated', this.imageUrl);
    },
    updatePreview() {
      this.$emit('preview-updated', this.imageUrl);
    },
    
    async getProxiedImage(url) {
      try {
        // 使用后端代理端点
        const encodedUrl = encodeURIComponent(url);
        const proxyUrl = `/api/photos/proxy?url=${encodedUrl}`;
        
        // 使用fetch进行图像请求，避免跨域问题
        const response = await fetch(proxyUrl, {
          headers: {
            'Cache-Control': 'no-cache',
            'Pragma': 'no-cache'
          }
        });
        
        if (!response.ok) {
          throw new Error(`Failed to fetch image: ${response.status}`);
        }
        
        // 将响应转换为blob
        const imageBlob = await response.blob();
        
        // 创建blob URL
        return URL.createObjectURL(imageBlob);
      } catch (error) {
        console.error('获取代理图像失败:', error);
        // 如果失败，返回原始URL
        return url;
      }
    },
    rotate(degrees) {
      this.adjustments.rotation = (this.adjustments.rotation + degrees) % 360;
      this.updatePreview();
    },
    flip(direction) {
      if (direction === 'horizontal') {
        this.adjustments.flipH = !this.adjustments.flipH;
      } else {
        this.adjustments.flipV = !this.adjustments.flipV;
      }
      this.updatePreview();
    },
    applyFilter(filterName) {
      this.currentFilter = this.currentFilter === filterName ? null : filterName;
      this.updatePreview();
    },
    reset() {
      this.adjustments = { ...this.initialAdjustments };
      this.currentFilter = null;
      this.cropData = null;
      this.updatePreview();
    },
    startCrop() {
      this.isCropping = true;
    },
    onCropChange({ coordinates, canvas }) {
      this.cropData = { coordinates, canvas };
    },
    async applyCrop() {
      try {
        this.isSaving = true;
        if (!this.cropData) {
          throw new Error('没有裁剪数据');
        }
        
        const { canvas } = this.cropData;
        const imageBlob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9));
        const imageUrl = URL.createObjectURL(imageBlob);
        
        // 为裁剪后的文件生成一个新的唯一文件名
        // 注意：需要处理URL可能包含查询参数的情况
        const fullUrl = this.imageUrl.split('?')[0]; // 去除URL中的查询参数
        const urlParts = fullUrl.split('/');
        const originalFilename = urlParts[urlParts.length - 1];
        const filenameParts = originalFilename.split('.');
        const extension = filenameParts.length > 1 ? filenameParts.pop() : 'jpg';
        const baseName = filenameParts.join('.');
        const newFilename = `${baseName}_cropped_${Date.now()}.${extension}`;
        
        // 创建一个文件对象，附加新文件名
        const imageFile = new File([imageBlob], newFilename, { type: 'image/jpeg' });
        
        // 发送裁剪后的图片和新文件名
        this.$emit('save-complete', { url: imageUrl, file: imageFile, filename: newFilename });
        this.isCropping = false;
      } catch (error) {
        console.error('裁剪失败:', error);
        this.$emit('error', error);
      } finally {
        this.isSaving = false;
      }
    },
    cancelCrop() {
      this.isCropping = false;
      this.cropData = null;
    },
    async save() {
      try {
        this.isSaving = true;
        
        let imageBlob;
        if (this.cropData) {
          // 使用裁剪后的图像
          const { canvas } = this.cropData;
          imageBlob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9));
        } else {
          // 使用当前预览图像
          // 创建一个新的图像元素从当前显示的图像
          const img = new Image();
          img.crossOrigin = "anonymous"; // 解决跨域问题
          
          // 获取代理图像URL以避免CORS问题
          const proxiedUrl = await this.getProxiedImage(this.imageUrl);
          
          // 使用promise等待图像加载
          await new Promise((resolve, reject) => {
            img.onload = resolve;
            img.onerror = reject;
            img.src = proxiedUrl;
          });
          
          const canvas = document.createElement('canvas');
          const ctx = canvas.getContext('2d');
          
          // 设置画布大小
          canvas.width = img.naturalWidth;
          canvas.height = img.naturalHeight;
          
          // 应用变换
          ctx.translate(canvas.width / 2, canvas.height / 2);
          ctx.rotate((this.adjustments.rotation * Math.PI) / 180);
          ctx.scale(
            this.adjustments.flipH ? -1 : 1,
            this.adjustments.flipV ? -1 : 1
          );
          ctx.translate(-canvas.width / 2, -canvas.height / 2);
          
          // 绘制图片
          ctx.drawImage(img, 0, 0);
          
          // 应用滤镜
          if (this.currentFilter) {
            const filter = this.filters.find(f => f.name === this.currentFilter);
            ctx.filter = filter.style;
            ctx.drawImage(canvas, 0, 0);
          }
          
          // 转换为blob，不经过像素级操作，避免CORS问题
          imageBlob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9));
          
          // 如果需要应用像素级调整（如果不会触发CORS错误）
          if (this.adjustments.brightness !== 0 || 
              this.adjustments.contrast !== 0 || 
              this.adjustments.saturation !== 100) {
            try {
              // 在新canvas上应用像素级调整
              const adjustCanvas = document.createElement('canvas');
              const adjustCtx = adjustCanvas.getContext('2d');
              adjustCanvas.width = canvas.width;
              adjustCanvas.height = canvas.height;
              
              // 将之前的图像绘制到新画布
              adjustCtx.drawImage(canvas, 0, 0);
              
              // 尝试获取像素数据进行处理
              const imageData = adjustCtx.getImageData(0, 0, adjustCanvas.width, adjustCanvas.height);
              const data = imageData.data;
              
              for (let i = 0; i < data.length; i += 4) {
                // 亮度
                if (this.adjustments.brightness !== 0) {
                  const factor = 1 + this.adjustments.brightness / 100;
                  data[i] *= factor;     // R
                  data[i + 1] *= factor; // G
                  data[i + 2] *= factor; // B
                }
                
                // 对比度
                if (this.adjustments.contrast !== 0) {
                  const factor = (259 * (this.adjustments.contrast + 100)) / (255 * (259 - this.adjustments.contrast));
                  data[i] = factor * (data[i] - 128) + 128;
                  data[i + 1] = factor * (data[i + 1] - 128) + 128;
                  data[i + 2] = factor * (data[i + 2] - 128) + 128;
                }
                
                // 饱和度
                if (this.adjustments.saturation !== 100) {
                  const gray = 0.2989 * data[i] + 0.5870 * data[i + 1] + 0.1140 * data[i + 2];
                  const factor = this.adjustments.saturation / 100;
                  data[i] = gray + factor * (data[i] - gray);
                  data[i + 1] = gray + factor * (data[i + 1] - gray);
                  data[i + 2] = gray + factor * (data[i + 2] - gray);
                }
              }
              
              adjustCtx.putImageData(imageData, 0, 0);
              imageBlob = await new Promise(resolve => adjustCanvas.toBlob(resolve, 'image/jpeg', 0.9));
            } catch (error) {
              console.warn('无法直接应用像素级调整，使用原始图像:', error);
              // 忽略错误，使用已有的imageBlob继续
            }
          }
        }
        
        // 创建object URL
        const imageUrl = URL.createObjectURL(imageBlob);
        
        // 为保存的文件生成一个新的唯一文件名
        // 注意：需要处理URL可能包含查询参数的情况
        const fullUrl = this.imageUrl.split('?')[0]; // 去除URL中的查询参数
        const urlParts = fullUrl.split('/');
        const originalFilename = urlParts[urlParts.length - 1];
        const filenameParts = originalFilename.split('.');
        const extension = filenameParts.length > 1 ? filenameParts.pop() : 'jpg';
        const baseName = filenameParts.join('.');
        const newFilename = `${baseName}_edited_${Date.now()}.${extension}`;
        
        // 创建一个文件对象，附加新文件名
        const imageFile = new File([imageBlob], newFilename, { type: 'image/jpeg' });
        
        // 发送编辑后的图片和新文件名
        this.$emit('save-complete', { url: imageUrl, file: imageFile, filename: newFilename });
      } catch (error) {
        console.error('保存失败:', error);
        this.$emit('error', error);
      } finally {
        this.isSaving = false;
      }
    }
  }
}
</script>

<style scoped>
.image-editor {
  display: flex;
  gap: 20px;
  height: 100%;
}

.preview-container {
  flex: 1;
  position: relative;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
}

.crop-overlay {
  flex: 1;
  position: relative;
}

.crop-controls {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background: rgba(0, 0, 0, 0.5);
}

.crop-controls button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.crop-controls button:first-child {
  background: #f5f5f5;
  color: #666;
}

.crop-controls button:last-child {
  background: #4caf50;
  color: white;
}

.crop-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.editor-controls {
  width: 300px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.control-group h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.control {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.control label {
  font-size: 14px;
  color: #666;
}

.control input[type="range"] {
  width: 100%;
  height: 4px;
  background: #ddd;
  border-radius: 2px;
  outline: none;
  -webkit-appearance: none;
}

.control input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  background: #4caf50;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
}

.control input[type="range"]::-webkit-slider-thumb:hover {
  transform: scale(1.2);
}

.control .value {
  font-size: 12px;
  color: #666;
  text-align: right;
}

.transform-controls {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.transform-controls button {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.transform-controls button:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.filters button {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.filters button:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.filters button.active {
  background: #e8f5e9;
  border-color: #4caf50;
  color: #4caf50;
}

.filter-preview {
  width: 100%;
  height: 40px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect width="100" height="100" fill="%23f5f5f5"/><circle cx="50" cy="50" r="40" fill="%23ddd"/></svg>') center/cover;
  border-radius: 4px;
}

.filters span {
  font-size: 12px;
  color: #666;
}

.filter-preview-image {
  max-width: 60px;
  max-height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
}

.actions button {
  flex: 1;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.reset-button {
  background: #f5f5f5;
  color: #666;
}

.reset-button:hover:not(:disabled) {
  background: #e0e0e0;
}

.crop-button {
  background: #2196f3;
  color: white;
}

.crop-button:hover {
  background: #1976d2;
}

.save-button {
  background: #4caf50;
  color: white;
}

.save-button:hover:not(:disabled) {
  background: #388e3c;
}

.actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .image-editor {
    flex-direction: column;
  }
  
  .editor-controls {
    width: 100%;
  }
  
  .filters {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .filters {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .transform-controls {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style> 