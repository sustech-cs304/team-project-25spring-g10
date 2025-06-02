<template>
  <div class="edit-photo">
    <div class="header">
      <h1>编辑照片</h1>
      <div class="actions">
        <button class="cancel-button" @click="cancel">取消</button>
        <button 
          class="save-button" 
          @click="save"
          :disabled="isSaving"
        >
          <span v-if="isSaving">保存中...</span>
          <span v-else>保存</span>
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="retry">重试</button>
    </div>

    <div v-else class="content">
      <div class="editor-section">
        <image-editor v-if="signedBlobUrl"
          ref="editor"
          :image-url="signedBlobUrl"
          :alt="photo.title"
          :initial-adjustments="initialAdjustments"
          @preview-updated="handlePreviewUpdate"
          @save-complete="handleSaveComplete"
          @error="handleError"
        />
      </div>

      <div class="info-section">
        <div class="form-group">
          <label>标题</label>
          <input 
            type="text"
            v-model="photo.title"
            placeholder="输入照片标题"
          >
        </div>

        <div class="form-group">
          <label>描述</label>
          <textarea
            v-model="photo.description"
            placeholder="输入照片描述"
            rows="4"
          ></textarea>
        </div>

        <div class="form-group">
          <label>日期</label>
          <input 
            type="datetime-local"
            v-model="photo.date"
          >
        </div>

        <div class="form-group">
          <label>位置</label>
          <input 
            type="text"
            v-model="photo.location"
            placeholder="输入照片位置"
          >
        </div>

        <div class="form-group">
          <label>相册</label>
          <select v-model="photo.albumId">
            <option value="">选择相册</option>
            <option 
              v-for="album in albums"
              :key="album.id"
              :value="album.id"
            >
              {{ album.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>标签</label>
          <div class="tags-input">
            <input 
              type="text"
              v-model="newTag"
              @keydown.enter="addTag"
              placeholder="输入标签并按回车"
            >
            <div class="tags">
              <span 
                v-for="tag in photo.tags"
                :key="tag"
                class="tag"
              >
                {{ tag }}
                <button @click="removeTag(tag)">×</button>
              </span>
            </div>
          </div>
        </div>
        <div class="advanced-actions">
          <button class="style-transfer-button" @click="openStyleTransfer">
            ✨ 风格迁移
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ImageEditor from '@/components/ImageEditor.vue';
import { copyPhotoToAlbum} from '@/api/photo';
import {createAlbumByType as createAlbumByTypeApi,fetchAlbumByTitle} from '@/api/album';
export default {
  name: 'EditPhoto',
  components: {
    ImageEditor
  },
  data() {
    return {
      loading: true,
      error: null,
      isSaving: false,
      newTag: '',
      photo: {
        id: null,
        title: '',
        description: '',
        url: '',
        previewUrl: '',
        date: '',
        location: '',
        albumId: '',
        tags: [],
        exif: null
      },
      albums: [],
      initialAdjustments: {
        brightness: 0,
        contrast: 0,
        saturation: 100,
        rotation: 0,
        flipH: false,
        flipV: false
      },
      editedFile: null,
      editedFilename: '',
      signedBlobUrl: ''
    }
  },
  created() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.$router.push('/login');
      return;
    }
    this.loadData();
  },
  methods: {
    async getProxiedBlobUrl(key) {
      const token = localStorage.getItem('token');
      const encodedKey = encodeURIComponent(key);
      const res = await fetch(`/api/photos/proxy?key=${encodedKey}`, {
        headers: {
          'Authorization': `${token}`
        }
      });
      if (!res.ok) throw new Error('图片代理失败');
      const blob = await res.blob();
      // console.log('👉 Blob 类型:', blob.type);
      return URL.createObjectURL(blob); // ✔️ 本地可用 URL
    },
    async loadData() {
      try {
        this.loading = true;
        this.error = null;

        // 从 localStorage 中获取 JWT Token
        const token = localStorage.getItem('token'); 
        console.log(token);

        if (!token) {
          throw new Error('未找到有效的认证 token');
        }
        // 加载照片数据
        const photoId = this.$route.params.id;
        console.log('加载照片ID:', photoId);
        
        const photoResponse = await fetch(`/api/photos/${photoId}`, {
          method: 'GET',
          headers: {
            'Authorization': `${token}` // 添加 Bearer Token
          }
        });

        if (!photoResponse.ok) throw new Error('加载照片失败');
        const photoData = await photoResponse.json();
        console.log('照片数据:', photoData);
        // console.log(photoData.url);
        const rawKey = photoData.url.split('.com/')[1].split('?')[0];
        // console.log(rawKey);
        this.signedBlobUrl = await this.getProxiedBlobUrl(rawKey);
        console.log(this.signedBlobUrl);
        
        // 加载相册列表
        const albumsResponse = await fetch('/api/albums', {
          method: 'GET',
          headers: {
            'Authorization': `${token}` // 添加 Bearer Token
          }
        });

        if (!albumsResponse.ok) {
          const errorData = await albumsResponse.json().catch(() => ({}));
          throw new Error(errorData.message || '加载相册失败');
        }
        
        const albumsData = await albumsResponse.json();
        console.log('相册数据:', albumsData);

        this.photo = {
          ...photoData,
          date: this.formatDate(photoData.date),
          tags: photoData.tags || [],
        };
        
        // 确保 URL 是完整的
        if (this.photo.url && !this.photo.url.startsWith('http')) {
          this.photo.url = `/api${this.photo.url}`;
        }
        this.albums = albumsData;
      } catch (error) {
        console.error('加载数据失败:', error);
        this.error = error.message;
        if (error.response?.status === 401) {
        // token 过期，跳转到登录页
          this.$router.push('/login');
        }
      } finally {
        this.loading = false;
      }
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return d.toISOString().slice(0, 16);
    },
    handlePreviewUpdate(url) {
      console.log('收到预览图 URL:', url);
      this.previewUrl = url;
    },
    handleSaveComplete(data) {
      if (typeof data === 'string') {
        // 旧版本兼容，直接是URL
        this.photo.url = data;
      } else {
        // 新版本，包含URL和文件对象
        this.photo.url = data.url;
        this.editedFile = data.file;
        this.editedFilename = data.filename;
      }
    },
    handleError(error) {
      console.error('编辑失败:', error);
      this.$emit('error', error);
    },
    addTag() {
      const tag = this.newTag.trim();
      if (tag && !this.photo.tags.includes(tag)) {
        this.photo.tags.push(tag);
      }
      this.newTag = '';
    },
    removeTag(tag) {
      const index = this.photo.tags.indexOf(tag);
      if (index > -1) {
        this.photo.tags.splice(index, 1);
      }
    },
      //获取全部照片的id
      getDefaultAlbumId() {
    try {
        // 将类似对象的 JSON 转换为真正的数组
        const albumsArray = Object.values(this.albums.data);
        console.log('获取的相册数据:',albumsArray);
        // 在数组中查找 title 为"全部照片"的相册
        const defaultAlbum = albumsArray.find(album => album.title === '全部照片');
        
        // 如果找到则返回 ID，否则返回 null
        return defaultAlbum ? defaultAlbum.id : null;
    } catch (error) {
        console.error('获取默认相册ID失败:', error);
        return null;
    }
    },
    async createAlbumByType(type,name){

    try {
        // 调用实际API创建相册
        const response = await createAlbumByTypeApi({
        title: name,
        description: '',
        type: type
        });

        if (response && response.code === 0 && response.data) {
        const newAlbumData = response.data;

        return newAlbumData.id; // 返回新相册ID
        } else {
        return null;
        }
    } catch (error) {
        console.error('创建相册失败:', error);
        return null;
    } 
    },
  
    async save() {
      try {
        this.isSaving = true;
        const formData = new FormData();
        const editor = this.$refs.editor;
        const { file, filename } = await editor.exportEditedImage();
        if (!file) {
          alert('未检测到编辑后的图像');
          console.warn('[DEBUG] exportEditedImage 返回空 file');
          return;
        }
        console.log('[DEBUG] file:', file);
        console.log('[DEBUG] filename:', filename);
        formData.append('file', file, filename); 
        formData.append('albumId', this.photo.albumId);
        formData.append('title', this.photo.title);
        formData.append('description', this.photo.description);
        formData.append('location', this.photo.location);
        formData.append('date', this.photo.date);
        formData.append('tags', JSON.stringify(this.photo.tags));

        const token = localStorage.getItem('token');
        console.log('[DEBUG] 正在上传...');
        const response = await fetch('/api/photos/upload', {
          method: 'POST',
          headers: {
            'Authorization': `${token}`,
          },
          body: formData
        });
        
        // if (!response.ok) throw new Error('上传失败');
        const photoID = await response.json();
        alert('保存成功！');
        const defaultAlbumId = this.getDefaultAlbumId();
       //若不是给默认相册上传，则自动上传到默认相册
       if(this.photo.albumId!=defaultAlbumId){
        await copyPhotoToAlbum(photoID,defaultAlbumId)
       }
       //把照片分类为编辑相册
       const responseType = await fetchAlbumByTitle('编辑');
        // 判断返回结果是否为空数组
        if (responseType.length === 0) {
        console.log('没有找到相关类型的相册',this.albums);
        // 处理空数组情况
        // 例如：创建新相册
        const newAlbumID = await this.createAlbumByType('auto','编辑');
        await copyPhotoToAlbum(photoID,newAlbumID)
        } else {
        console.log('找到相册:', responseType);
        // 处理非空情况
        // 例如：使用第一个相册
        const albumId = responseType[0].id;
        await copyPhotoToAlbum(photoID,albumId)
        }
       this.$emit('save-complete', photoID);


      } catch (error) {
        console.error('[DEBUG] 上传失败:', error);
        this.$emit('error', error);
      } finally {
        this.isSaving = false;
      }
    },
    cancel() {
      this.$router.back();
    },
    retry() {
      this.loadData();
    },
    openStyleTransfer() {
    console.log('[点击风格迁移]', this.photo.id);
    this.$router.push(`/style-transfer/${this.photo.id}`)
    console.log('[点击风格迁移]', this.photo.id);
    }
  },
  
}
</script>

<style scoped>
.edit-photo {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.actions {
  display: flex;
  gap: 10px;
}

.actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.cancel-button {
  background: #f5f5f5;
  color: #666;
}

.cancel-button:hover {
  background: #e0e0e0;
}

.save-button {
  background: #4caf50;
  color: white;
}

.save-button:hover:not(:disabled) {
  background: #388e3c;
}

.save-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f5f5f5;
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #f44336;
}

.error button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  background: #f44336;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.error button:hover {
  background: #d32f2f;
}

.content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  min-height: 0;
}

.editor-section {
  width:900px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  flex: 1;
  display: flex;
}

.info-section {
  width:300px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-group label {
  font-size: 14px;
  color: #666;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #4caf50;
}

.tags-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #e8f5e9;
  color: #4caf50;
  border-radius: 4px;
  font-size: 12px;
}

.tag button {
  padding: 0;
  width: 16px;
  height: 16px;
  border: none;
  background: none;
  color: inherit;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.2s;
}

.tag button:hover {
  opacity: 1;
}

.exif-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.exif-info h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.exif-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.exif-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.exif-item .label {
  font-size: 12px;
  color: #666;
}

.exif-item .value {
  font-size: 14px;
  color: #333;
}

@media (max-width: 992px) {
  .content {
    grid-template-columns: 1fr;
  }
  
  .info-section {
    order: -1;
  }
}

@media (max-width: 768px) {
  .edit-photo {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .actions {
    justify-content: flex-end;
  }
  
  .exif-grid {
    grid-template-columns: 1fr;
  }
}

.advanced-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.style-transfer-button {
  padding: 10px 16px;
  background: linear-gradient(135deg, #7e57c2, #ab47bc);
  color: white;
  font-size: 14px;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(171, 71, 188, 0.4);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.style-transfer-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(171, 71, 188, 0.5);
}

.style-transfer-button:active {
  transform: scale(0.98);
}


</style>
  