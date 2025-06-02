<template>
    <div class="style-transfer-page">
      <h2>风格迁移</h2>
  
      <div v-if="contentImageUrl">
        <p>内容图：</p>
        <img :src="contentImageUrl" alt="内容图像" />
      </div>

      <div v-if="styleImages.length">
        <p>从已有风格图选择：</p>
        <div class="style-gallery">
          <img 
            v-for="img in styleImages" 
            :key="img" 
            :src="`http://localhost:8000/data/style-images/${img}`" 
            :alt="img"
            class="style-thumb"
            :class="{ selected: selectedStyleFromServer === img }"
            @click="selectServerStyle(img)"
          />
        </div>
      </div>
      <div class="upload-wrapper">
        <label class="upload-label">
          <input type="file" @change="onStyleImageChange" hidden />
          <span class="upload-button">📁 选择风格图</span>
        </label>
        <span v-if="styleImageFile" class="upload-filename">{{ styleImageFile.name }}</span>
      </div>
  
      <div>
        <button 
        class="style-transfer-button"
        :disabled="(!styleImageFile && !selectedStyleFromServer) || isLoading" @click="submitStyleTransfer">
          {{ isLoading ? '处理中...' : '开始风格迁移' }}
        </button>
      </div>
  
      <div v-if="resultUrl">
        <p>结果图：</p>
        <img :src="resultUrl" alt="结果图像" />
      </div>
      <div class="result-actions">
        <button 
          class="save-button" 
          @click="save" 
          :disabled="!resultUrl"
        >
          💾 保存
        </button>
        <button class="back-button" @click="goBack">🔙 返回</button>
      </div>
    </div>
  </template>
  
  <script>
  import { copyPhotoToAlbum} from '@/api/photo';
  import {fetchAlbumByTitle} from '@/api/album';
  export default {
    data() {
      return {
        contentImageUrl: '',
        contentImageBlob: null,
        styleImageFile: null,
        resultUrl: '',
        isLoading: false,
        styleImages: [],          
        selectedStyleFromServer: '', 
        albums: []
      };
    },
    async created() {
      const id = this.$route.params.id;
      console.log('[StyleTransferPage] 当前 photo id:', id);
      // 拉取内容图（你已有 signed URL 逻辑）
      const token = localStorage.getItem('token');
      const res = await fetch(`/api/photos/${id}`, {
        headers: { Authorization: token }
      });
      const data = await res.json();
      const rawKey = data.url.split('.com/')[1].split('?')[0];
      const proxyRes = await fetch(`/api/photos/proxy?key=${encodeURIComponent(rawKey)}`, {
        headers: { Authorization: token }
      });
      const blob = await proxyRes.blob();
      this.contentImageBlob = blob;
      this.contentImageUrl = URL.createObjectURL(blob);
      // 拉取服务器已有风格图列表
      try {
        const res2 = await fetch('http://localhost:8000/style-images');
        const json = await res2.json();
        this.styleImages = json.files;
        console.log('[StyleTransferPage] 获取到 style-images:', this.styleImages);
      } catch (e) {
        console.error('[StyleTransferPage] 拉取 style-images 失败:', e);
      }
      const albumsRes = await fetch('/api/albums', {
        method: 'GET',
        headers: {
          'Authorization': `${token}`
        }
      });
      if (albumsRes.ok) {
        const albumsData = await albumsRes.json();
        this.albums = albumsData; // ✅ 赋值 albums
      }
    },
    methods: {
      selectServerStyle(filename) {
        this.selectedStyleFromServer = filename;
        this.styleImageFile = null;  // 清空本地选择
      },

      onStyleImageChange(e) {
        this.styleImageFile = e.target.files[0];
        this.selectedStyleFromServer = '';  // 清空服务器选择
      },

      async submitStyleTransfer() {
        this.isLoading = true;
        const formData = new FormData();
        formData.append('content_image', this.contentImageBlob, 'content.jpg');

        if (this.styleImageFile) {
          formData.append('style_image', this.styleImageFile);
          console.log('style_image:', this.styleImageFile)
        } else if (this.selectedStyleFromServer) {
          formData.append('style_filename', this.selectedStyleFromServer);
          console.log('style_filename:', this.selectedStyleFromServer)
        } else {
          alert('请上传或选择一个风格图');
          return;
        }
        const response = await fetch('http://localhost:8000/style-transfer/', {
          method: 'POST',
          body: formData
        });
        const resultBlob = await response.blob();
        this.resultUrl = URL.createObjectURL(resultBlob);
        this.isLoading = false;
      },
      async getDefaultAlbumId() {
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
      async save() {
        try {
          if (!this.resultUrl) {
            alert('没有可保存的图像');
            console.warn('[DEBUG] resultUrl is empty');
            return;
          }

          this.isSaving = true;

          console.log('[DEBUG] Fetching resultUrl blob...');
          const response = await fetch(this.resultUrl);
          const blob = await response.blob();
          console.log('[DEBUG] Blob fetched, size:', blob.size);

          const file = new File([blob], 'styled-image.jpg', { type: 'image/jpeg' });
          console.log('[DEBUG] Created File object:', file);

          const formData = new FormData();
          formData.append('file', file, file.name);

          // 日志打印字段值
          const albumId = this.$route.query.albumId || '';
          const title = this.$route.query.title || '';
          const description = this.$route.query.description || '';
          const location = this.$route.query.location || '';
          const date = this.$route.query.date || '';
          const tags = this.$route.query.tags || '[]';

          console.log('[DEBUG] Metadata:', {
            albumId, title, description, location, date, tags
          });

          formData.append('albumId', albumId);
          formData.append('title', title);
          formData.append('description', description);
          formData.append('location', location);
          formData.append('date', date);
          formData.append('tags', tags);

          const token = localStorage.getItem('token');
          console.log('[DEBUG] Sending upload request with token:', token?.slice(0, 10) + '...');

          const uploadRes = await fetch('/api/photos/upload', {
            method: 'POST',
            headers: {
              Authorization: `${token}`,
            },
            body: formData
          });

          alert('保存成功！');
          // 1. 从服务器响应中提取 photoID
          const photoID = await uploadRes.json();

          // 2. 获取默认相册ID
          const defaultAlbumId = this.getDefaultAlbumId();

          // 3. 若不是上传到默认相册，则复制
          if (albumId !== defaultAlbumId) {
            await copyPhotoToAlbum(photoID, defaultAlbumId);
          }

          // 4. 分类照片至 “编辑” 相册
          const responseType = await fetchAlbumByTitle('编辑');

          if (responseType.length === 0) {
            console.log('没有找到相关类型的相册', this.albums);
            const newAlbumID = await this.createAlbumByType('auto', '编辑');
            await copyPhotoToAlbum(photoID, newAlbumID);
          } else {
            console.log('找到相册:', responseType);
            const editAlbumId = responseType[0].id;
            await copyPhotoToAlbum(photoID, editAlbumId);
          }

          this.$emit('save-complete', photoID);
        } catch (error) {
          console.error('[DEBUG] 上传失败:', error);
          this.$emit('error', error);
        } finally {
          this.isSaving = false;
        }
      },
      goBack() {
        this.$router.back();
      }
    }
  };
  </script>

<style scoped>
.style-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.style-thumb {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 2px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.style-thumb:hover {
  border-color: #ab47bc;
}

.style-thumb.selected {
  border-color: #7e57c2;
  box-shadow: 0 0 5px rgba(126, 87, 194, 0.7);
}

.upload-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.upload-label {
  display: inline-block;
}

.upload-button {
  background: linear-gradient(135deg, #7e57c2, #ab47bc);
  color: white;
  font-size: 14px;
  font-weight: bold;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 6px rgba(171, 71, 188, 0.4);
  user-select: none;
}

.upload-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(171, 71, 188, 0.5);
}

.upload-filename {
  font-size: 13px;
  color: #555;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.style-transfer-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(171, 71, 188, 0.5);
}

.style-transfer-button:active {
  transform: scale(0.98);
}

.style-transfer-button:disabled {
  background: #ccc;
  color: #eee;
  box-shadow: none;
  cursor: not-allowed;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.save-button,
.back-button {
  padding: 10px 16px;
  font-size: 14px;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 保存按钮绿色风格 */
.save-button {
  background: linear-gradient(135deg, #43a047, #66bb6a);
  color: white;
}

.save-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(76, 175, 80, 0.5);
}

.save-button:disabled {
  background: #ccc;
  color: #eee;
  box-shadow: none;
  cursor: not-allowed;
}

/* 返回按钮灰蓝风格 */
.back-button {
  background: linear-gradient(135deg, #546e7a, #78909c);
  color: white;
}

.back-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(84, 110, 122, 0.5);
}



</style>

  