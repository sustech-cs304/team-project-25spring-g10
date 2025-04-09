<template>
  <div class="image-editor">
    <div class="editor-container">
      <img :src="previewUrl" alt="编辑预览" class="preview-image" />
      
      <div class="editor-controls">
        <div class="control-group">
          <h3>基础调整</h3>
          <div class="slider-control">
            <label>亮度</label>
            <input type="range" v-model="brightness" min="-100" max="100" @input="updateImage">
          </div>
          <div class="slider-control">
            <label>对比度</label>
            <input type="range" v-model="contrast" min="-100" max="100" @input="updateImage">
          </div>
          <div class="slider-control">
            <label>饱和度</label>
            <input type="range" v-model="saturation" min="0" max="200" @input="updateImage">
          </div>
        </div>

        <div class="control-group">
          <h3>旋转与裁剪</h3>
          <button @click="rotateImage(-90)">向左旋转</button>
          <button @click="rotateImage(90)">向右旋转</button>
          <button @click="startCrop" v-if="!isCropping">裁剪</button>
          <button @click="applyCrop" v-if="isCropping">应用裁剪</button>
          <button @click="cancelCrop" v-if="isCropping">取消裁剪</button>
        </div>

        <div class="control-group">
          <h3>滤镜效果</h3>
          <button @click="applyFilter('grayscale')">黑白</button>
          <button @click="applyFilter('vintage')">复古</button>
          <button @click="resetFilters">重置滤镜</button>
        </div>

        <div class="action-buttons">
          <button class="save-button" @click="saveImage">保存更改</button>
          <button class="cancel-button" @click="resetAll">重置所有</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ImageEditor',
  props: {
    imageUrl: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      previewUrl: '',
      brightness: 0,
      contrast: 0,
      saturation: 100,
      rotation: 0,
      isCropping: false,
      cropData: null,
      currentFilter: null
    }
  },
  created() {
    this.previewUrl = this.imageUrl;
  },
  methods: {
    async updateImage() {
      try {
        const response = await axios.post('/api/image/edit', {
          imageUrl: this.imageUrl,
          edits: {
            brightness: this.brightness,
            contrast: this.contrast,
            saturation: this.saturation / 100,
            rotation: this.rotation,
            filter: this.currentFilter
          }
        });
        this.previewUrl = response.data.editedImageUrl;
      } catch (error) {
        console.error('图片编辑失败:', error);
      }
    },

    rotateImage(degrees) {
      this.rotation = (this.rotation + degrees) % 360;
      this.updateImage();
    },

    startCrop() {
      this.isCropping = true;
      // 这里可以初始化裁剪组件，比如使用 vue-cropper
    },

    async applyCrop() {
      if (!this.cropData) return;
      
      try {
        const response = await axios.post('/api/image/crop', {
          imageUrl: this.imageUrl,
          cropData: this.cropData
        });
        this.previewUrl = response.data.editedImageUrl;
        this.isCropping = false;
      } catch (error) {
        console.error('裁剪失败:', error);
      }
    },

    cancelCrop() {
      this.isCropping = false;
      this.cropData = null;
    },

    async applyFilter(filterType) {
      this.currentFilter = filterType;
      await this.updateImage();
    },

    resetFilters() {
      this.currentFilter = null;
      this.updateImage();
    },

    async saveImage() {
      try {
        const response = await axios.post('/api/image/save', {
          imageUrl: this.imageUrl,
          editedImageUrl: this.previewUrl
        });
        this.$emit('save-complete', response.data.savedImageUrl);
      } catch (error) {
        console.error('保存失败:', error);
      }
    },

    resetAll() {
      this.brightness = 0;
      this.contrast = 0;
      this.saturation = 100;
      this.rotation = 0;
      this.currentFilter = null;
      this.previewUrl = this.imageUrl;
    }
  }
}
</script>

<style scoped>
.image-editor {
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.editor-container {
  display: flex;
  gap: 20px;
}

.preview-image {
  max-width: 60%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.editor-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-group {
  background: white;
  padding: 15px;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.control-group h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.slider-control {
  margin-bottom: 10px;
}

.slider-control label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}

input[type="range"] {
  width: 100%;
}

button {
  padding: 8px 16px;
  margin-right: 8px;
  margin-bottom: 8px;
  border: none;
  border-radius: 4px;
  background: #4a90e2;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

button:hover {
  background: #357abd;
}

.action-buttons {
  margin-top: 20px;
}

.save-button {
  background: #4caf50;
}

.save-button:hover {
  background: #388e3c;
}

.cancel-button {
  background: #f44336;
}

.cancel-button:hover {
  background: #d32f2f;
}
</style> 