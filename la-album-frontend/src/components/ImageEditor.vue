<template>
  <div class="image-editor">
    <canvas ref="canvas" class="editor-canvas"></canvas>
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
          <button @click="applyCrop" :disabled="isSaving">应用</button>
        </div>
      </div>


    <div class="editor-controls">
      <div class="control-group">
        <h3>基本调整</h3>
        <div class="adjustment-strip">
          <div class="scroll-container">
            <button
              v-for="item in adjustmentDefs"
              :key="item.key"
              :class="{ active: activeAdjustment === item.key }"
              @click="activeAdjustment = item.key"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div v-if="activeAdjustment" class="adjustment-slider">
          <input
            type="range"
            :min="getActiveDef().min"
            :max="getActiveDef().max"
            :step="getActiveDef().step"
            v-model.number="adjustments[activeAdjustment]"
            @input="updatePreview"
          /><span class="value">
            {{ typeof adjustments[activeAdjustment] === 'number' ? adjustments[activeAdjustment].toFixed(2) : '—' }}
          </span>
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
            @click="startCrop"
            title="裁剪"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2v14a2 2 0 0 0 2 2h14"/>
              <path d="M18 22V8a2 2 0 0 0-2-2H2"/>
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
        <div class="filters scroll-container">
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

      <div class="control-group">
        <h3>贴纸</h3>
        <div class="stickers scroll-container">
          <button v-for="sticker in stickers" :key="sticker.label" @click="addSticker(sticker.src)">
            <img :src="sticker.src" :alt="sticker.label" class="sticker-thumb" />
          </button>
        </div>
      </div>

      <div class="control-group">
        <h3>绘图</h3>
        <div class="drawing-tools">
          <label class="drawing-label">
            颜色:
            <input type="color" v-model="drawing.color" @input="updateBrushColor" />
          </label>
          <label class="drawing-label">
            线宽:
            <input type="range" min="1" max="50" v-model="drawing.width" @input="updateBrushWidth" />
          </label>
          <div class="drawing-buttons">
            <button @click="enableDrawingMode">启用绘图</button>
            <button @click="disableDrawingMode">停止绘图</button>
          </div>
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
// import { Canvas, FabricImage } from 'fabric';
import { fabric } from 'fabric';



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
      drawing: {
        color: '#000000',
        width: 5,
      },
      currentFilter: null,
      isCropping: false,
      isSaving: false,
      originalImage: null,
      cropData: null,
      adjustmentDefs: [
        { key: 'contrast', label: '对比度', min: -1, max: 1, step: 0.01, default: 0 },
        { key: 'brightness', label: '亮度', min: -1, max: 1, step: 0.01, default: 0 },
        { key: 'saturation', label: '饱和度', min: 0, max: 2, step: 0.01, default: 1 },
        { key: 'sharpen', label: '锐度', min: 0, max: 1, step: 0.01, default: 0 },
        { key: 'denoise', label: '去噪', min: 0, max: 1, step: 0.01, default: 0 }
      ],
      adjustments: {},
      activeAdjustment: 'brightness',
      filters: [
        { name: 'normal', label: '原图', style: 'none' },
        { name: 'grayscale', label: '黑白', style: 'grayscale(100%)' },
        { name: 'sepia', label: '复古', style: 'sepia(100%)' },
        { name: 'vintage', label: '复古', style: 'sepia(50%) contrast(120%)' },
        { name: 'warm', label: '暖色', style: 'sepia(30%) saturate(140%)' },
        { name: 'cool', label: '冷色', style: 'saturate(80%) hue-rotate(180deg)' },
        { name: 'dramatic', label: '戏剧', style: 'contrast(130%) saturate(120%)' },
        { name: 'fade', label: '褪色', style: 'contrast(90%) saturate(80%)' }
      ],
      stickers: [
        { label: '星星', src: '/stickers/star.png' },
        { label: '爱心', src: '/stickers/love.png' },
        { label: '情侣', src: '/stickers/in-love.png' },
        { label: '旅行', src: '/stickers/travel.png' },
      ]
    }
  },
  created() {
    this.adjustments = Object.fromEntries(this.adjustmentDefs.map(a => [a.key, a.default]));
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
    this.initFabricCanvas(); 
    this.loadBaseImage();

  },

  methods: {
    initFabricCanvas() {
      const canvasEl = this.$refs.canvas;
      this.canvas = new fabric.Canvas(canvasEl, {
        preserveObjectStacking: true,
        backgroundColor: null,
        selection: true
      });
    },
    async getProxiedImage(originalUrl) {
      console.log('oiginalUrl!!!', originalUrl)
      const token = localStorage.getItem('token');
      const key = decodeURIComponent(originalUrl.split('?')[0].split('.com/')[1]); // 取出 OSS key
      const encodedKey = encodeURIComponent(key);
      
      try {
        const res = await fetch(`/api/photos/proxy?key=${encodedKey}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!res.ok) throw new Error('获取签名链接失败');
        const signedUrl = await res.text();
        return signedUrl;
      } catch (e) {
        console.error('获取图片直链失败', e);
        return originalUrl; // fallback
      }
    },
    async loadBaseImage() {
      const canvasWidth = this.canvas.getWidth();
      const canvasHeight = this.canvas.getHeight();

      fabric.Image.fromURL(this.imageUrl, (img) => {
        const scaleX = canvasWidth / img.width;
        const scaleY = canvasHeight / img.height;

        const scale = Math.min(scaleX, scaleY);
        img.set({
          left: 0,
          top: 0,
          scaleX: scale,
          scaleY: scale,
          selectable: false,
          evented: false,
          name: 'baseImage'
        });

        this.canvas.clear(); // 清除所有对象
        this.canvas.add(img); // 添加主图像到对象层
        this.canvas.sendToBack(img); // 放到底层，其他贴纸/绘图在上面
        this.canvas.renderAll();
        console.log('✅ 主图已加载');
        });
    },
    getActiveDef() {
      return this.adjustmentDefs.find(d => d.key === this.activeAdjustment) || {};
    },
    getBaseAdjustments() {
      const adj = this.adjustments;
      const filters = [];

      if (adj.brightness !== 0) {
        filters.push(new fabric.Image.filters.Brightness({ brightness: adj.brightness }));
      }
      if (adj.contrast !== 0) {
        filters.push(new fabric.Image.filters.Contrast({ contrast: adj.contrast }));
      }
      if (adj.saturation !== 1) {
        filters.push(new fabric.Image.filters.Saturation({ saturation: adj.saturation - 1 }));
      }
      if (adj.sharpen > 0) {
        filters.push(new fabric.Image.filters.Convolute({ matrix: [
          0, -adj.sharpen, 0,
          -adj.sharpen, 4 * adj.sharpen + 1, -adj.sharpen,
          0, -adj.sharpen, 0
        ] }));
      }
      if (adj.denoise > 0) {
        filters.push(new fabric.Image.filters.Blur({ blur: adj.denoise }));
      }
      return filters;
    },
    onAdjustmentChange(e) {
      const val = parseFloat(e.target.value);
      this.$set(this.adjustments, this.activeAdjustment, val);
      this.applyAllFilters();
    },
    addSticker(stickerUrl) {
      fabric.Image.fromURL(stickerUrl, img => {
        img.set({
          left: 100,
          top: 100,
          scaleX: 0.5,
          scaleY: 0.5,
          selectable: true
        });
        this.canvas.add(img);
        this.canvas.setActiveObject(img);
      }, { crossOrigin: 'anonymous' });
    },
   

    // 扩展保存逻辑：将Fabric图层合并
    async mergeCanvasWithFabric(baseCanvas) {
      if (!this.canvas) return baseCanvas;

      const overlay = document.createElement('canvas');
      overlay.width = baseCanvas.width;
      overlay.height = baseCanvas.height;

      const overlayCtx = overlay.getContext('2d');

      // 渲染 fabric 到 overlay
      const dataUrl = this.canvas.toDataURL({ format: 'png' });
      const overlayImage = new Image();
      overlayImage.src = dataUrl;
      await new Promise(resolve => (overlayImage.onload = resolve));

      overlayCtx.drawImage(overlayImage, 0, 0, baseCanvas.width, baseCanvas.height);
      return overlay;
    },

    handleImageLoad(event) {
      this.originalImage = event.target;
      this.$emit('preview-updated', this.imageUrl);
    },
    /////////////////////////////////////////
    applyAllFilters() {
      const baseImage = this.canvas?.getObjects('image').find(obj => obj.name === 'baseImage');
      if (!baseImage) return;

      const filters = [];

      switch (this.currentFilter) {
        case 'grayscale':
          filters.push(new fabric.Image.filters.Grayscale());
          break;
        case 'sepia':
        case 'vintage':
          filters.push(new fabric.Image.filters.Sepia());
          break;
        case 'warm':
          filters.push(new fabric.Image.filters.Sepia());
          filters.push(new fabric.Image.filters.Saturation({ saturation: 0.4 }));
          break;
        case 'cool':
          filters.push(new fabric.Image.filters.HueRotation({ rotation: Math.PI }));
          break;
        case 'dramatic':
          filters.push(new fabric.Image.filters.Contrast({ contrast: 0.3 }));
          filters.push(new fabric.Image.filters.Saturation({ saturation: 0.2 }));
          break;
        case 'fade':
          filters.push(new fabric.Image.filters.Contrast({ contrast: -0.1 }));
          filters.push(new fabric.Image.filters.Saturation({ saturation: -0.2 }));
          break;
        default:
          break;
      }

      filters.push(...this.getBaseAdjustments());

      baseImage.filters = filters;
      baseImage.applyFilters();
      this.canvas.renderAll();
    },

    updatePreview() {
      this.applyAllFilters();
    },
    applyFilter(filterName) {
      this.currentFilter = this.currentFilter === filterName ? null : filterName;
      this.applyAllFilters();
    },
    /////////////////////////////
    rotate(degrees) {
      const baseImage = this.canvas.getObjects('image').find(obj => obj.name === 'baseImage');
      if (!baseImage) return;

      const newAngle = ((baseImage.angle || 0) + degrees) % 360;
      baseImage.rotate(newAngle);
      this.canvas.renderAll();
    },
    flip(direction) {
      const baseImage = this.canvas.getObjects('image').find(obj => obj.name === 'baseImage');
      if (!baseImage) return;

      if (direction === 'horizontal') {
        baseImage.flipX = !baseImage.flipX;
      } else {
        baseImage.flipY = !baseImage.flipY;
      }

      this.canvas.renderAll();
    },
    startCrop() {
      // 清除已有裁剪框
      const existing = this.canvas.getObjects('rect').find(obj => obj.name === 'cropRect');
      if (existing) this.canvas.remove(existing);

      // 添加一个新的裁剪框（可以拖动和缩放）
      const cropRect = new fabric.Rect({
        left: 50,
        top: 50,
        width: 200,
        height: 200,
        fill: 'rgba(0,0,0,0.1)',
        stroke: 'red',
        strokeWidth: 1,
        name: 'cropRect',
        selectable: true,
        hasBorders: true,
        hasControls: true,
      });
      this.canvas.add(cropRect);
      this.canvas.setActiveObject(cropRect);
    },
    onCropChange({ coordinates, canvas }) {
      this.cropData = { coordinates, canvas };
    },

    async applyCrop() {
      const cropRect = this.canvas.getObjects('rect').find(obj => obj.name === 'cropRect');
      const baseImage = this.canvas.getObjects('image').find(obj => obj.name === 'baseImage');
      if (!cropRect || !baseImage) return;

      // 将裁剪区域转换为 clipPath
      const clipPath = new fabric.Rect({
        left: 0,
        top: 0,
        width: cropRect.width * cropRect.scaleX,
        height: cropRect.height * cropRect.scaleY,
        originX: 'left',
        originY: 'top',
      });

      const offsetLeft = cropRect.left;
      const offsetTop = cropRect.top;

      baseImage.clipPath = clipPath;
      baseImage.left = -offsetLeft;
      baseImage.top = -offsetTop;

      // 重新设置 canvas 尺寸以适配裁剪
      this.canvas.setWidth(clipPath.width);
      this.canvas.setHeight(clipPath.height);
      this.canvas.centerObject(baseImage);

      this.canvas.remove(cropRect);
      this.canvas.renderAll();
    },

    cancelCrop() {
      this.isCropping = false;
      this.cropData = null;

      // 移除裁剪框（名称为 cropRect）
      const cropRect = this.canvas.getObjects('rect').find(obj => obj.name === 'cropRect');
      if (cropRect) {
        this.canvas.remove(cropRect);
        this.canvas.renderAll();
      }
    },
    /////////////////////////////
    updateBrushColor() {
      if (this.canvas && this.canvas.freeDrawingBrush) {
        this.canvas.freeDrawingBrush.color = this.drawing.color;
      }
    },
    updateBrushWidth() {
      if (this.canvas && this.canvas.freeDrawingBrush) {
        this.canvas.freeDrawingBrush.width = parseInt(this.drawing.width, 10);
      }
    },
    enableDrawingMode() {
      if (this.canvas) {
        this.canvas.isDrawingMode = true;
        this.canvas.freeDrawingBrush.color = this.drawing.color;
        this.canvas.freeDrawingBrush.width = parseInt(this.drawing.width, 10);
      }
    },
    disableDrawingMode() {
      if (this.canvas) {
        this.canvas.isDrawingMode = false;
      }
    },
    ////////////////////////////////
    reset() {
      const baseImage = this.canvas.getObjects('image').find(obj => obj.name === 'baseImage');
      if (baseImage) {
        baseImage.set({
          angle: 0,
          flipX: false,
          flipY: false
        });
        this.currentFilter = null;
        baseImage.filters = [];
        baseImage.applyFilters();
        this.canvas.renderAll();
      }

      this.adjustments = { ...this.initialAdjustments };
      this.cropData = null;
    },
    /////////////////////////////
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
.scroll-container {
  display: flex;
  overflow-x: auto;
  gap: 8px;
  padding: 4px 0;
  scroll-snap-type: x mandatory;
}
.scroll-container button {
  display: inline-block;
  padding: 6px 12px;
  margin-right: 8px;
  border-radius: 8px;
  border: 1px solid #ccc;
  background: #fff;
  cursor: pointer;
}
.scroll-container button.active {
  background: #333;
  color: #fff;
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
  display: flex;
  flex-direction: column;
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
.adjustment-strip {
  overflow-x: auto;
  white-space: nowrap;
  margin: 10px 0;
}

.adjustment-slider {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.adjustment-slider input[type="range"] {
  flex: 1;
}
.sticker-thumb {
  width: 50px;
  height: 50px;
  object-fit: contain; 
}


.stickers {
  max-height: 120px; /* 根据需要调整高度 */
  overflow-x: auto;
  overflow-y: hidden;
  display: flex;
  gap: 10px;
  padding: 5px;
  scroll-behavior: smooth;
}

.drawing-tools {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 10px;
  border: 1px solid #ddd;
}

.drawing-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

input[type="color"] {
  width: 40px;
  height: 30px;
  border: none;
  cursor: pointer;
}

input[type="range"] {
  width: 120px;
}

.drawing-buttons {
  display: flex;
  gap: 10px;
}

.drawing-buttons button {
  padding: 6px 14px;
  font-size: 14px;
  border-radius: 6px;
  border: none;
  background-color: #3f80ff;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.drawing-buttons button:hover {
  background-color: #2c6ee0;
}
</style> 