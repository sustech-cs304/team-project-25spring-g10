<template>
    <div class="upload-view">
      <div class="upload-header">
        <div class="container">
          <div class="header-content">
            <h1>上传照片</h1>
            <div class="header-actions">
              <button class="btn" @click="navigateToAlbum" v-if="selectedAlbumId">返回相册</button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="upload-content">
        <div class="container">
          <div class="upload-section">
            <div class="upload-area" 
              :class="{ 'dragging': isDragging }"
              @dragover.prevent="handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @drop.prevent="handleDrop"
              @click="triggerFileInput"
            >
              <div class="upload-area-content">
                <div class="upload-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                </div>
                <h2>拖放照片到这里</h2>
                <p>或点击选择文件</p>
                <p class="upload-formats">支持 JPG, PNG, GIF, WEBP 格式</p>
                <input 
                  type="file" 
                  ref="fileInput" 
                  multiple 
                  accept="image/*" 
                  class="file-input"
                  @change="handleFileSelect"
                >
              </div>
            </div>
            
            <div class="upload-options">
              <div class="form-group">
                <label for="upload-album">选择相册</label>
                <select id="upload-album" v-model="selectedAlbumId">
                  <option value="">选择一个相册</option>
                  <option 
                    v-for="album in albums" 
                    :key="album.id" 
                    :value="album.id"
                  >
                    {{ album.title }}
                  </option>
                </select>
                <button class="btn btn-sm create-album-btn" @click="showCreateAlbumModal = true">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"></line>
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                  </svg>
                  新建相册
                </button>
              </div>
              
              <div class="form-group">
                <label>上传选项</label>
                <div class="checkbox-group">
                  <label class="checkbox-option">
                    <input type="checkbox" v-model="autoRename">
                    <span>自动重命名文件</span>
                  </label>
                  <label class="checkbox-option">
                    <input type="checkbox" v-model="preserveExif">
                    <span>保留EXIF信息（拍摄日期、位置等）</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          
          <div class="upload-progress-section" v-if="uploadFiles.length > 0">
            <div class="section-header">
              <h2>上传列表 ({{ uploadFiles.length }})</h2>
              <div class="section-actions">
                <button 
                  class="btn btn-sm btn-secondary" 
                  @click="clearCompletedUploads" 
                  v-if="hasCompletedUploads"
                >
                  清除已完成
                </button>
                <button 
                  class="btn btn-sm btn-danger" 
                  @click="cancelAllUploads" 
                  v-if="hasActiveUploads"
                >
                  取消所有上传
                </button>
              </div>
            </div>
            
            <div class="upload-list">
              <UploadProgress 
                v-for="file in uploadFiles" 
                :key="file.id" 
                :file="file"
                @cancel="cancelUpload"
                @retry="retryUpload"
                @remove="removeFile"
              />
            </div>
            
            <div class="upload-actions">
              <button 
                class="btn btn-primary" 
                @click="uploadAllFiles"
                :disabled="isUploadingAll || uploadFiles.length === 0 || !selectedAlbumId"
              >
                <svg v-if="isUploadingAll" class="spin-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                上传所有照片
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 创建相册对话框 -->
      <div class="modal" v-if="showCreateAlbumModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>创建新相册</h3>
            <button class="close-btn" @click="showCreateAlbumModal = false">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="album-title">相册标题</label>
              <input 
                type="text" 
                id="album-title" 
                v-model="newAlbum.title" 
                placeholder="输入相册标题"
              >
            </div>
            
            <div class="form-group">
              <label for="album-description">相册描述</label>
              <textarea 
                id="album-description" 
                v-model="newAlbum.description" 
                placeholder="添加相册描述"
                rows="3"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showCreateAlbumModal = false">取消</button>
            <button 
              class="btn btn-primary" 
              @click="createAlbum"
              :disabled="!newAlbum.title || isCreatingAlbum"
            >
              <svg v-if="isCreatingAlbum" class="spin-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="2" x2="12" y2="6"></line>
                <line x1="12" y1="18" x2="12" y2="22"></line>
                <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                <line x1="2" y1="12" x2="6" y2="12"></line>
                <line x1="18" y1="12" x2="22" y2="12"></line>
                <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
              </svg>
              创建相册
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import UploadProgress from '@/components/upload/UploadProgress.vue';
  import { fetchAlbumList, createAlbum as createAlbumApi,createAlbumByType as createAlbumByTypeApi,fetchAlbumByTitle} from '@/api/album';
  import { uploadPhoto, mockUploadPhoto,copyPhotoToAlbum,updatePhotoDescriptionAndTags} from '@/api/photo';
  import { ElMessage } from 'element-plus';
  
  // 调试模式开关 - 设为true使用模拟上传，false使用真实API
  const DEBUG_MODE = false;
  // 失败自动回退开关 - 设为true时，如果真实API失败则自动使用模拟上传
  const AUTO_FALLBACK = true;
  
  const route = useRoute();
  const router = useRouter();
  const fileInput = ref(null);
  const isDragging = ref(false);
  const uploadFiles = ref([]);
  const albums = ref([]);
  const selectedAlbumId = ref('');
  const autoRename = ref(true);
  const preserveExif = ref(true);
  const isUploadingAll = ref(false);
  const showCreateAlbumModal = ref(false);
  const isCreatingAlbum = ref(false);
  const newAlbum = ref({
    title: '',
    description: ''
  });
  
  // 获取相册列表
  const fetchAlbums = async () => {
    try {
      const albumList = await fetchAlbumList();
      
      if (Array.isArray(albumList) && albumList.length > 0) {
        // 处理相册数据，只需要id和title用于下拉选择
        albums.value = albumList.map(album => ({
          id: album.id,
          title: album.title
        }));
      } else {
        albums.value = [];
        ElMessage.warning('暂无相册，请先创建一个相册');
      }
    } catch (error) {
      console.error('获取相册列表失败:', error);
      ElMessage.error('获取相册列表失败，请稍后再试');
      albums.value = [];
    }
  };
  
  onMounted(async () => {
    await fetchAlbums(); // 确保在组件挂载时获取相册列表
    
    const albumId = route.query.albumId;
    if (albumId) {
      selectedAlbumId.value = albumId;
    }
  });
  
  // 计算是否有已完成的上传
  const hasCompletedUploads = computed(() => {
    return uploadFiles.value.some(file => file.status === 'success' || file.status === 'error');
  });
  
  // 计算是否有正在上传的文件
  const hasActiveUploads = computed(() => {
    return uploadFiles.value.some(file => file.status === 'uploading');
  });
  
  // 触发文件选择对话框
  const triggerFileInput = () => {
    fileInput.value.click();
  };
  
  // 处理拖拽事件
  const handleDragOver = () => {
    isDragging.value = true;
  };
  
  const handleDragLeave = () => {
    isDragging.value = false;
  };
  
  const handleDrop = (event) => {
    isDragging.value = false;
    const files = event.dataTransfer.files;
    if (files.length > 0) {
      addFiles(files);
    }
  };
  
  // 处理文件选择
  const handleFileSelect = (event) => {
    const files = event.target.files;
    if (files.length > 0) {
      addFiles(files);
      // 重置文件输入，以便再次选择相同文件
      event.target.value = '';
    }
  };
  
  // 添加文件到上传列表
  const addFiles = (fileList) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    
    Array.from(fileList).forEach(file => {
      // 验证文件类型
      if (!allowedTypes.includes(file.type)) {
        console.warn(`不支持的文件类型: ${file.type}`);
        return;
      }
      
      // 创建文件预览
      const reader = new FileReader();
      reader.onload = (e) => {
        const newFile = {
          id: Date.now() + Math.random().toString(36).substr(2, 9),
          file: file,
          name: file.name,
          size: file.size,
          type: file.type,
          thumbnail: e.target.result,
          progress: 0,
          status: 'uploading',
          errorMessage: null
        };
        
        uploadFiles.value.push(newFile);
        
        // 自动开始上传（可选）
        // uploadFile(newFile);
      };
      reader.readAsDataURL(file);
    });
  };
  
  //获取全部照片的id
  function getDefaultAlbumId(albums) {
    const album = albums.value.find(a => a.title === '全部照片');
    return album ? album.id : null;
  }
  
  
  // 上传单个文件
  const uploadFile = async (file) => {
    // 更新文件状态
    const fileIndex = uploadFiles.value.findIndex(f => f.id === file.id);
    if (fileIndex === -1) return;
    
    uploadFiles.value[fileIndex].status = 'uploading';
    uploadFiles.value[fileIndex].progress = 0;
    uploadFiles.value[fileIndex].errorMessage = null;
    
    // 强制更新UI
    uploadFiles.value = [...uploadFiles.value];
    
    // 尝试使用实际API上传
    let useRealAPI = !DEBUG_MODE;
    let uploadSuccess = false;
    
    while (!uploadSuccess) {
      try {
        // 选择上传方法：实际API或模拟API
        const uploadFunction = useRealAPI ? uploadPhoto : mockUploadPhoto;
        console.log(`使用${useRealAPI ? '实际' : '模拟'}上传方法`);
        
        // 准备上传配置
        const uploadOptions = {
          autoRename: autoRename.value,
          preserveExif: preserveExif.value,
          onProgress: (progressInfo) => {
            // 输出进度信息用于调试
            console.log('Upload progress:', progressInfo);
            
            // 查找最新的文件索引
            const currentIndex = uploadFiles.value.findIndex(f => f.id === file.id);
            if (currentIndex !== -1) {
              // 使用传入的百分比或计算百分比
              const percentCompleted = progressInfo.percent !== undefined 
                ? progressInfo.percent 
                : (progressInfo.total > 0 
                    ? Math.round((progressInfo.loaded * 100) / progressInfo.total) 
                    : 0);
              
              // 更新进度条
              uploadFiles.value[currentIndex].progress = percentCompleted;
              
              // 强制Vue更新UI
              uploadFiles.value = [...uploadFiles.value];
            }
          }
        };
        // 执行上传
      console.log("selectedAlbumId.value",selectedAlbumId.value);
      const response= await uploadFunction(file.file, selectedAlbumId.value, uploadOptions);
      
     setTimeout(async () => {
      const photoId = response
        //先给图形生成描述
       const response2 = await autoProcess(photoId);
       const description = response2.description;
       const tag = response2.classification;
       console.log("description",description);
        console.log("tag",tag);
       await updatePhotoDescriptionAndTags(
        photoId, 
        description,  
        tag        
        );
       const defaultAlbumId = getDefaultAlbumId(albums);
       //若不是给默认相册上传，则自动上传到默认相册
       if(selectedAlbumId.value!=defaultAlbumId){
       const copyResponse = copyPhotoToAlbum(photoId,defaultAlbumId)
       console.log("copy info", copyResponse );
  
     }
   // 进行自动分类（人脸/其他）
    if(tag == '人物') {
    //进行人脸识别
    const album_id = await autoClassifyPhotos(photoId);

    if (album_id > 0) {
        console.log('自动分类到相册:', album_id);
        await copyPhotoToAlbum(photoId,album_id)
        // await uploadFunction(file.file, album_id, uploadOptions);
    } else if(album_id == 0) {
        const newAlbumID = await createAlbumByType('face','人物');
        console.log('新建的人脸相册', newAlbumID);
        await copyPhotoToAlbum(photoId,newAlbumID)
        // await uploadFunction(file.file, newAlbumID, uploadOptions);
    }
    else if(album_id == -2){
        const responseType = await fetchAlbumByTitle('合照');
        if (responseType.length === 0) {
        const newAlbumID = await createAlbumByType('face','合照');
        await copyPhotoToAlbum(photoId,newAlbumID)
        } else {
        console.log('找到相册:', responseType);
        const albumId = responseType[0].id;
        await copyPhotoToAlbum(photoId,albumId)
        }
    }
    else{
        console.warn('未检测到人脸');
    }
    }
    //进行其他分类
    else{
        const responseType = await fetchAlbumByTitle(tag);
        // 判断返回结果是否为空数组
        if (responseType.length === 0) {
        console.log('没有找到相关类型的相册');
        // 处理空数组情况
        // 例如：创建新相册
        const newAlbumID = await createAlbumByType('auto',tag);
        await copyPhotoToAlbum(photoId,newAlbumID)
        } else {
        console.log('找到相册:', responseType);
        // 处理非空情况
        // 例如：使用第一个相册
        const albumId = responseType[0].id;
        await copyPhotoToAlbum(photoId,albumId)
        }
    }
    }, 0);
  
    //     // 如果是实际API上传，则使用实际API
    //     if (useRealAPI) {
    //       console.log('使用实际API上传');
    //     } else {
    //       console.log('使用模拟API上传');
    //     }
  
    //     // 执行上传
    //   await uploadFunction(file.file, album_id, 200, uploadOptions);
      
        
  
        // 处理响应
        if (response) {
          // 上传成功
          const currentIndex = uploadFiles.value.findIndex(f => f.id === file.id);
          if (currentIndex !== -1) {
            uploadFiles.value[currentIndex].status = 'success';
            uploadFiles.value[currentIndex].progress = 100;
            uploadFiles.value = [...uploadFiles.value]; // 强制更新UI
          }
          
          uploadSuccess = true;
        } else {
          // 业务错误
          console.error('上传失败:', response?.message || '未知错误');
          
          // 如果使用了实际API但失败了，且允许回退，则尝试使用模拟API
          if (useRealAPI && AUTO_FALLBACK) {
            useRealAPI = false;
            console.log('尝试使用模拟上传作为备选方案');
            continue; // 继续循环，尝试模拟上传
          }
          
          // 如果没有回退或回退也失败了，则显示错误
          const currentIndex = uploadFiles.value.findIndex(f => f.id === file.id);
          if (currentIndex !== -1) {
            uploadFiles.value[currentIndex].status = 'error';
            uploadFiles.value[currentIndex].errorMessage = response?.message || '上传失败';
            uploadFiles.value = [...uploadFiles.value]; // 强制更新UI
          }
          
          uploadSuccess = true; // 结束循环，即使是失败状态
        }
      } catch (error) {
        console.error('上传过程中出现异常:', error);
        
        // 如果使用了实际API但失败了，且允许回退，则尝试使用模拟API
        if (useRealAPI && AUTO_FALLBACK) {
          useRealAPI = false;
          console.log('出现异常，尝试使用模拟上传作为备选方案');
          continue; // 继续循环，尝试模拟上传
        }
        
        // 如果没有回退或回退也失败了，则显示错误
        const currentIndex = uploadFiles.value.findIndex(f => f.id === file.id);
        if (currentIndex !== -1) {
          uploadFiles.value[currentIndex].status = 'error';
          uploadFiles.value[currentIndex].errorMessage = '上传失败，请稍后重试';
          uploadFiles.value = [...uploadFiles.value]; // 强制更新UI
        }
        
        uploadSuccess = true; // 结束循环，即使是失败状态
      }
    }
  };
  
  import axios from 'axios';
  const autoClassifyPhotos = async (id) => {
    try {
      // 调用后端的 /auto_class API
      const response = await axios.post('http://localhost:7777/auto_class', {
        photoId: id, // 将 URL 作为请求体的一部分传递
        token:localStorage.getItem('token')
      });
  
      if (response.status === 200 ) {
        return response.data.albumId;
      } else {
        console.error('人脸识别失败:', response.data.message);
      }
    } catch (error) {
      console.error('调用 auto_class API 失败:', error);
    }
  };

  //和flask连，获取description和tag
  const autoProcess = async (id) => {
    try {
      // 调用后端的 /auto_class API
      const response = await axios.post('http://localhost:7777/description', {
        photoId: id, // 将 URL 作为请求体的一部分传递
        token:localStorage.getItem('token')
      });
  
      if (response.status === 200 ) {
        return response.data;
      } else {
        console.error('自动分类失败:', response.data.message);
      }
    } catch (error) {
      console.error('调用 auto_class API 失败:', error);
    }
  };

  const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
  // 上传所有文件
  const uploadAllFiles = async() => {
    if (!selectedAlbumId.value) {
      ElMessage.warning('请先选择相册');
      return;
    }
    
    if (uploadFiles.value.length === 0) return;
    
    isUploadingAll.value = true;
    
    // 筛选出待上传和失败的文件
    const filesToUpload = uploadFiles.value.filter(
      file => file.status !== 'success'
    );
    
    if (filesToUpload.length === 0) {
      isUploadingAll.value = false;
      ElMessage.info('没有需要上传的文件');
      return;
    }
    
    // 显示开始上传的提示
    ElMessage.info(`开始上传 ${filesToUpload.length} 张照片...`);
    
    // 开始上传所有文件
    for (const file of filesToUpload) {
       uploadFile(file);
       await delay(1000); // 上传完一张等待 1 秒
   }
    
    // 等待所有上传完成并显示结果
    const checkInterval = 500; // 检查间隔，毫秒
    const maxWaitTime = 300000; // 最长等待时间，5分钟
    let elapsedTime = 0;
    
    const checkAllComplete = setInterval(() => {
      const pendingFiles = uploadFiles.value.filter(
        file => file.status === 'uploading'
      );
      
      // 所有文件上传完成或超时
      if (pendingFiles.length === 0 || elapsedTime >= maxWaitTime) {
        isUploadingAll.value = false;
        clearInterval(checkAllComplete);
        
        // 统计成功和失败的数量
    const successCount = uploadFiles.value.filter(file => file.status === 'success').length;
    const errorCount = uploadFiles.value.filter(file => file.status === 'error').length;
        const pendingCount = uploadFiles.value.filter(file => file.status === 'uploading').length;
        
        if (pendingCount > 0) {
          // 超时但仍有文件在上传中
          ElMessage.warning(`上传超时: ${successCount} 张成功, ${errorCount} 张失败, ${pendingCount} 张尚未完成`);
        } else if (errorCount === 0 && successCount > 0) {
          // 全部成功
      ElMessage.success(`成功上传 ${successCount} 张照片`);
          // 如果需要自动跳转到相册
          if (successCount === uploadFiles.value.length) {
            setTimeout(() => {
              if (confirm('是否查看已上传的相册?')) {
                navigateToAlbum();
              }
            }, 1000);
          }
    } else if (successCount > 0 && errorCount > 0) {
          // 部分成功
      ElMessage.warning(`上传完成: ${successCount} 张成功, ${errorCount} 张失败`);
        } else if (errorCount > 0 && successCount === 0) {
          // 全部失败
      ElMessage.error(`上传失败: ${errorCount} 张照片上传失败`);
    }
      }
      
      // 增加已经过时间
      elapsedTime += checkInterval;
    }, checkInterval);
  };
  
  // 取消上传
  const cancelUpload = (file) => {
    const fileIndex = uploadFiles.value.findIndex(f => f.id === file.id);
    if (fileIndex !== -1) {
      uploadFiles.value[fileIndex].status = 'error';
      uploadFiles.value[fileIndex].progress = 0;
      uploadFiles.value[fileIndex].errorMessage = '上传已取消';
      ElMessage.info('已取消上传');
  }
};
  
  // 重试上传
  const retryUpload = (file) => {
    // 重置文件状态后重新上传
    const fileIndex = uploadFiles.value.findIndex(f => f.id === file.id);
    if (fileIndex !== -1) {
      // 确保重置状态
      uploadFiles.value[fileIndex].status = 'uploading';
      uploadFiles.value[fileIndex].progress = 0;
      uploadFiles.value[fileIndex].errorMessage = null;
      
      // 调用上传函数
      uploadFile(uploadFiles.value[fileIndex]);
    }
  };
  
  // 移除文件
  const removeFile = (file) => {
    uploadFiles.value = uploadFiles.value.filter(f => f.id !== file.id);
    ElMessage.info('已移除文件');
  };
  
  // 清除已完成的上传
  const clearCompletedUploads = () => {
    uploadFiles.value = uploadFiles.value.filter(
      file => file.status === 'uploading'
    );
  };
  
  // 取消所有上传
  const cancelAllUploads = () => {
    uploadFiles.value.forEach(file => {
      if (file.status === 'uploading') {
        file.status = 'error';
        file.errorMessage = '上传已取消';
      }
    });
  };
  
  // 创建新相册
  const createAlbum = async () => {
    if (!newAlbum.value.title) return;
    
    isCreatingAlbum.value = true;
    
    try {
      // 调用实际API创建相册
      const response = await createAlbumApi({
        title: newAlbum.value.title,
        description: newAlbum.value.description || ''
      });
      
      if (response && response.code === 0 && response.data) {
        const newAlbumData = response.data;
        
        // 添加到相册列表
        albums.value.push({
          id: newAlbumData.id,
          title: newAlbumData.title
        });
        
        // 选择新创建的相册
        selectedAlbumId.value = newAlbumData.id;
        
        // 关闭对话框并重置表单
        newAlbum.value = { title: '', description: '' };
        showCreateAlbumModal.value = false;
        
        ElMessage.success('相册创建成功');
      } else {
        ElMessage.error('创建相册失败: ' + (response?.message || '未知错误'));
      }
    } catch (error) {
      console.error('创建相册失败:', error);
      ElMessage.error('创建相册失败，请稍后重试');
    } finally {
      isCreatingAlbum.value = false;
    }
  };

  //创建系统相册
  const createAlbumByType = async (type,name) => {
  isCreatingAlbum.value = true;

  try {
    // 调用实际API创建相册
    const response = await createAlbumByTypeApi({
      title: name,
      description: '',
      type: type
    });

    if (response && response.code === 0 && response.data) {
      const newAlbumData = response.data;

      // 添加到相册列表
      albums.value.push({
        id: newAlbumData.id,
        title: newAlbumData.title
      });

      // 关闭对话框并重置表单
      newAlbum.value = { title: '', description: '' };
      showCreateAlbumModal.value = false;

      return newAlbumData.id; // 返回新相册ID
    } else {
      ElMessage.error('创建相册失败: ' + (response?.message || '未知错误'));
      return null;
    }
  } catch (error) {
    console.error('创建相册失败:', error);
    ElMessage.error('创建相册失败，请稍后重试');
    return null;
  } finally {
    isCreatingAlbum.value = false;
  }
};
  
  // 导航到相册页面
  const navigateToAlbum = () => {
    if (selectedAlbumId.value) {
      router.push({ name: 'Album', params: { id: selectedAlbumId.value } });
    }
  };
  </script>
  
  <style scoped>
  .upload-view {
    min-height: 100vh;
  }
  
  .upload-header {
    background-color: var(--primary-light);
    padding: var(--space-lg) 0;
    margin-bottom: var(--space-xl);
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .upload-content {
    padding-bottom: var(--space-xxl);
  }
  
  .upload-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: var(--space-xl);
    margin-bottom: var(--space-xxl);
  }
  
  .upload-area {
    height: 300px;
    border: 2px dashed var(--neutral-300);
    border-radius: var(--radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: var(--neutral-50);
  }
  
  .upload-area:hover {
    border-color: var(--primary-color);
    background-color: var(--primary-light);
  }
  
  .upload-area.dragging {
    border-color: var(--primary-color);
    background-color: var(--primary-light);
    transform: scale(1.01);
  }
  
  .upload-area-content {
    text-align: center;
    padding: var(--space-xl);
  }
  
  .upload-icon {
    margin-bottom: var(--space-md);
    color: var(--neutral-500);
  }
  
  .upload-area h2 {
    margin-bottom: var(--space-sm);
    color: var(--neutral-800);
  }
  
  .upload-area p {
    color: var(--neutral-600);
  }
  
  .upload-formats {
    font-size: 0.85rem;
    margin-top: var(--space-md);
    color: var(--neutral-500);
  }
  
  .file-input {
    display: none;
  }
  
  .upload-options {
    padding: var(--space-lg);
    background-color: var(--neutral-100);
    border-radius: var(--radius-lg);
    height: fit-content;
  }
  
  .form-group {
    margin-bottom: var(--space-lg);
  }
  
  .form-group:last-child {
    margin-bottom: 0;
  }
  
  .form-group label {
    display: block;
    margin-bottom: var(--space-sm);
    color: var(--neutral-700);
    font-weight: 500;
  }
  
  .form-group select,
  .form-group input[type="text"],
  .form-group textarea {
    width: 100%;
    padding: var(--space-sm);
    border: 1px solid var(--neutral-300);
    border-radius: var(--radius-sm);
    background-color: white;
  }
  
  .create-album-btn {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    margin-top: var(--space-sm);
    padding: var(--space-xs) var(--space-sm);
  }
  
  .checkbox-group {
    display: flex;
    flex-direction: column;
    gap: var(--space-sm);
  }
  
  .checkbox-option {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    cursor: pointer;
  }
  
  .upload-progress-section {
    background-color: var(--neutral-100);
    border-radius: var(--radius-lg);
    padding: var(--space-lg);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--space-md);
  }
  
  .section-header h2 {
    font-size: 1.2rem;
    color: var(--neutral-800);
  }
  
  .section-actions {
    display: flex;
    gap: var(--space-sm);
  }
  
  .upload-list {
    margin-bottom: var(--space-lg);
    max-height: 400px;
    overflow-y: auto;
  }
  
  .upload-actions {
    display: flex;
    justify-content: flex-end;
  }
  
  .btn-primary {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    padding: var(--space-sm) var(--space-lg);
    background-color: var(--primary-color);
    color: white;
    border: none;
    border-radius: var(--radius-md);
    font-weight: 500;
    cursor: pointer;
  }
  
  .btn-primary:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  .btn-secondary {
    background-color: var(--neutral-200);
    color: var(--neutral-700);
  }
  
  .btn-danger {
    background-color: var(--error-light);
    color: var(--error);
  }
  
  .btn-sm {
    padding: var(--space-xs) var(--space-sm);
    font-size: 0.9rem;
  }
  
  .spin-icon {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  /* 模态框样式 */
  .modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
  }
  
  .modal-content {
    background-color: white;
    border-radius: var(--radius-lg);
    width: 100%;
    max-width: 500px;
    box-shadow: var(--shadow-lg);
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--space-lg);
    border-bottom: 1px solid var(--neutral-200);
  }
  
  .modal-header h3 {
    margin: 0;
    color: var(--neutral-900);
  }
  
  .close-btn {
    background: none;
    border: none;
    cursor: pointer;
    color: var(--neutral-500);
  }
  
  .close-btn:hover {
    color: var(--neutral-700);
  }
  
  .modal-body {
    padding: var(--space-lg);
  }
  
  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: var(--space-md);
    padding: var(--space-lg);
    border-top: 1px solid var(--neutral-200);
  }
  
  /* 响应式设计 */
  @media (max-width: 992px) {
    .upload-section {
      grid-template-columns: 1fr;
    }
    
    .upload-area {
      height: 200px;
    }
  }
  
  @media (max-width: 768px) {
    .header-content {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-md);
    }
    
    .section-header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--space-sm);
    }
    
    .section-actions {
      width: 100%;
    }
    
    .upload-actions {
      justify-content: center;
    }
    
    .btn-primary {
      width: 100%;
      justify-content: center;
    }
  }
  </style> 