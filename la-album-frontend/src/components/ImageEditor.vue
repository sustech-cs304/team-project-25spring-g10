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
      canvasEl.width = canvasEl.clientWidth;
      canvasEl.height = canvasEl.clientHeight;
      this.canvas = new fabric.Canvas(canvasEl, {
        preserveObjectStacking: true,
        backgroundColor: null,
        selection: true
      });
      this.canvas.setWidth(canvasEl.clientWidth);
      this.canvas.setHeight(canvasEl.clientHeight);
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
        this.baseScale = scale;
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
    async exportEditedImage() {
      const dataUrl = this.canvas.toDataURL({ format: 'jpeg', quality: 0.9 });
      const imageBlob = await new Promise(resolve => {
        const img = new Image();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          canvas.width = img.width;
          canvas.height = img.height;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0);
          canvas.toBlob(resolve, 'image/jpeg', 0.9);
        };
        img.src = dataUrl;
      });
      
      const originalFilename = this.imageUrl.split('/').pop().split('?')[0];
      let newFilename = originalFilename+`.jpg`;
      const file = new File([imageBlob], newFilename, { type: 'image/jpeg' });
      console.log('[DEBUG] originalFilename:', originalFilename);
      console.log('[DEBUG] newFilename:', newFilename);
      return { file, filename: newFilename };
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
      const baseImage = this.canvas.getObjects('image').find(obj => obj.name === 'baseImage');
      if (!baseImage) return;

      // 清除旧框和遮罩
      this.canvas.getObjects().forEach(obj => {
        if (['cropRect', 'cropMask'].includes(obj.name)) this.canvas.remove(obj);
      });

      // 获取缩放后的图像边界（图像已经被 scaleX/scaleY 缩放）
      const imgWidth = baseImage.width * baseImage.scaleX;
      const imgHeight = baseImage.height * baseImage.scaleY;
      const imgLeft = baseImage.left;
      const imgTop = baseImage.top;

      // 设置裁剪框为图像大小的 60%
      const cropWidth = imgWidth * 0.6;
      const cropHeight = imgHeight * 0.6;
      const cropLeft = imgLeft + (imgWidth - cropWidth) / 2;
      const cropTop = imgTop + (imgHeight - cropHeight) / 2;

      // 创建裁剪框
      const cropRect = new fabric.Rect({
        left: cropLeft,
        top: cropTop,
        width: cropWidth,
        height: cropHeight,
        fill: 'transparent',
        stroke: 'red',
        strokeWidth: 1,
        name: 'cropRect',
        selectable: true,
        hasBorders: true,
        hasControls: true,
        lockRotation: true
      });

      this.canvas.add(cropRect);
      this.canvas.setActiveObject(cropRect);

      // 添加遮罩并绑定裁剪框监听
      this.createFullScreenMask(cropRect);
    },
    createFullScreenMask(cropRect) {
      const canvasWidth = this.canvas.getWidth();
      const canvasHeight = this.canvas.getHeight();

      const mask = new fabric.Rect({
        left: 0,
        top: 0,
        width: canvasWidth,
        height: canvasHeight,
        fill: 'rgba(0, 0, 0, 0.5)',
        selectable: false,
        evented: false,
        name: 'cropMask',
      });

      const hole = new fabric.Rect({
        left: cropRect.left,
        top: cropRect.top,
        width: cropRect.width,
        height: cropRect.height,
        fill: 'white',
        selectable: false,
        evented: false,
        globalCompositeOperation: 'destination-out',
      });

      const group = new fabric.Group([mask, hole], {
        selectable: false,
        evented: false,
        name: 'cropMask',
      });

      this.canvas.add(group);
      this.canvas.sendToBack(group);

      // 监听裁剪框变化，实时更新遮罩
      cropRect.on('moving', () => this.updateCropMask(cropRect));
      cropRect.on('scaling', () => this.updateCropMask(cropRect));
    },
    updateCropMask(cropRect) {
      const group = this.canvas.getObjects().find(obj => obj.name === 'cropMask');
      if (!group || !(group instanceof fabric.Group)) return;

      const hole = group._objects[1]; // 第 2 个元素是挖空区域
      hole.set({
        left: cropRect.left,
        top: cropRect.top,
        width: cropRect.width * cropRect.scaleX,
        height: cropRect.height * cropRect.scaleY
      });
      group.dirty = true;
      this.canvas.requestRenderAll();
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
      this.canvas.getObjects().forEach(obj => {
        if (obj.name === 'cropMask') this.canvas.remove(obj);
      });
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
      this.canvas.getObjects().forEach(obj => {
        if (obj.name === 'cropMask') this.canvas.remove(obj);
      });
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
.editor-canvas {
  width: 500px;
  height: 100%;
  border: 1px solid #ccc;
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