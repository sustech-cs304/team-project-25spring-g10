<template>
  <div class="photo-view">
    <div v-if="loading" class="loading-state">
      <el-skeleton animated />
    </div>

    <template v-else-if="photo">
      <div class="photo-header">
        <div class="container">
          <div class="header-content">
            <div class="breadcrumb">
              <router-link to="/">首页</router-link> /
              <router-link :to="`/album/${photo.albumId}`">{{ album?.title || '相册' }}</router-link> /
              <span>{{ photo.title }}</span>
            </div>

            <div class="photo-actions">
              <button class="btn btn-icon" @click="previousPhoto" v-if="hasPrevious">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="15 18 9 12 15 6"></polyline>
                </svg>
              </button>

              <button class="btn" @click="editPhoto">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"></path>
                  <path
                      d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z">
                  </path>
                </svg>
                编辑
              </button>

              <button class="btn btn-danger" @click="confirmDelete">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path
                      d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2">
                  </path>
                </svg>
                删除
              </button>

              <button class="btn btn-primary" @click="sharePhoto">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="18" cy="5" r="3"></circle>
                  <circle cx="6" cy="12" r="3"></circle>
                  <circle cx="18" cy="19" r="3"></circle>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                </svg>
                分享
              </button>

              <button class="btn" @click="openMoveDialog">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M5 12h14"></path>
                  <path d="M12 5l7 7-7 7"></path>
                </svg>
                移动
              </button>

              <button class="btn btn-icon" @click="nextPhoto" v-if="hasNext">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="photo-content">
        <div class="container">
          <div class="photo-display">
            <img :src="photo.url" :alt="photo.title">
          </div>

          <div class="photo-info">
            <h1 class="photo-title">{{ photo.title }}</h1>
            <p class="photo-description">{{ photo.description }}</p>

            <div class="photo-metadata">
              <div class="metadata-item">
                <span class="label">拍摄日期</span>
                <span class="value">{{ formatDate(photo.createdAt) }}</span>
              </div>

              <div class="metadata-item" v-if="photo.location">
                <span class="label">位置</span>
                <span class="value">{{ photo.location }}</span>
              </div>

              <div class="metadata-item">
                <span class="label">相册</span>
                <router-link :to="`/album/${photo.albumId}`" class="value link">
                  {{ album?.title || '未知相册' }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="delete-modal" v-if="showDeleteModal">
        <div class="modal-content">
          <h3>确认删除</h3>
          <p>您确定要删除照片"{{ photo.title }}"吗？此操作不可撤销。</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="cancelDelete">取消</button>
            <button class="btn btn-danger" @click="deletePhoto">删除</button>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="error-state">
      <div class="container">
        <h2>照片未找到</h2>
        <p>抱歉，找不到此照片或您没有权限访问</p>
        <button class="btn" @click="goBack">返回</button>
      </div>
    </div>
  </div>

  <el-dialog v-model="moveDialogVisible" title="移动到其他相册" width="400px">
    <el-radio-group v-model="selectedAlbumId">
      <el-radio
          v-for="album in albumList"
          :key="album.id"
          :label="album.id">
        {{ album.title }}
      </el-radio>
    </el-radio-group>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>


<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { fetchAlbumById, fetchPhotosInAlbum, fetchAlbumList } from "@/api/album";
import {movePhoto} from "@/api/photo";

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const photo = ref(null);
const album = ref(null);
const showDeleteModal = ref(false);
const albumPhotos = ref([]);
const moveDialogVisible = ref(false);
const albumList = ref([]);
const selectedAlbumId = ref(null);

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

    const albumId = route.query.albumId;
    if (albumId) {
      album.value = await fetchAlbumById(albumId);
      const photosResponse = await fetchPhotosInAlbum(albumId);
      albumPhotos.value = photosResponse.data;
    }
  } catch (error) {
    console.error('获取照片数据失败:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};

const currentIndex = computed(() => {
  if (!photo.value || albumPhotos.value.length === 0) return -1;
  return albumPhotos.value.findIndex(p => p.id === photo.value.id);
});

const hasPrevious = computed(() => currentIndex.value > 0);
const hasNext = computed(() => currentIndex.value < albumPhotos.value.length - 1 && currentIndex.value !== -1);

const previousPhoto = () => {
  if (!hasPrevious.value) return;
  router.push({name: 'Photo', params: {id: albumPhotos.value[currentIndex.value - 1].id}});
};

const nextPhoto = () => {
  if (!hasNext.value) return;
  router.push({name: 'Photo', params: {id: albumPhotos.value[currentIndex.value + 1].id}});
};

const editPhoto = () => {
  router.push({name: 'EditPhoto', params: {id: photo.value.id}});
};

const sharePhoto = () => {
  router.push({name: 'SharePhoto', params: {id: photo.value.id}});
};

const confirmDelete = () => {
  ElMessageBox.confirm(`您确定要删除照片"${photo.value.title}"吗？此操作不可撤销。`, '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => deletePhoto());
};

const cancelDelete = () => showDeleteModal.value = false;

const deletePhoto = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`/api/photos/${photo.value.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.ok) {
      const from = route.query.from;
      if (from === 'search') {
        // 回跳到搜索页面并保留搜索参数
        router.push({
          name: 'Search',
          query: {
            q: route.query.q || '',
            startDate: route.query.startDate || '',
            endDate: route.query.endDate || '',
            albumId: route.query.albumId || ''
          }
        });
      } else {
        // 默认跳转到所属相册
        // const albumId = route.query.albumId;
        // router.push({ name: 'Album', params: { id: albumId } });
        router.back();
      }
    } else {
      console.error('删除照片失败');
    }
  } catch (error) {
    console.error('删除照片时出错:', error);
  }
};


// 打开对话框并加载相册列表
const openMoveDialog = async () => {
  try {
    const res = await fetchAlbumList(); // 你需要根据用户获取所有相册
    console.log("album list in move: ", res);
    albumList.value = res;
    moveDialogVisible.value = true;
  } catch (e) {
    console.error('获取相册失败', e);
    ElMessage.error('加载相册列表失败');
  }
};

// 确认移动
const confirmMove = async () => {
  if (!selectedAlbumId.value) {
    ElMessage.warning('请选择一个相册');
    return;
  }
  try {
    await movePhoto(photo.value.id, selectedAlbumId.value);
    console.log("selected album id:", selectedAlbumId.value);
    ElMessage.success('移动成功');
    moveDialogVisible.value = false;
    router.push({ name: 'Album', params: { id: selectedAlbumId.value } });
  } catch (err) {
    console.error('移动失败', err);
    ElMessage.error('移动失败');
  }
};

const goBack = () => router.back();
</script>

<style scoped>
.photo-view {
  min-height: 100vh;
  position: relative;
}

.photo-header {
  padding: 1.5rem 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 2rem;
}

.breadcrumb {
  font-size: 0.9rem;
  color: #888;
}

.breadcrumb a {
  color: #409EFF;
  text-decoration: none;
}

.breadcrumb span {
  font-weight: bold;
  color: #333;
}

.photo-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.photo-actions .btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.85rem;
  transition: all 0.2s ease-in-out;
}

.photo-actions .btn:hover {
  background-color: #f0f0f0;
}

.photo-actions .btn-danger {
  background: #ff4d4f;
  color: white;
}

.photo-actions .btn-danger:hover {
  background: #d9363e;
}

.photo-actions .btn-primary {
  background: #409EFF;
  color: white;
}

.photo-actions .btn-primary:hover {
  background: #347ccd;
}

.photo-actions .btn-icon {
  background: transparent;
  padding: 0.4rem;
  border-radius: 50%;
}

.photo-content .photo-display img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  display: block;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.photo-info {
  margin-top: 2rem;
  background: #fafafa;
  padding: 1.5rem 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.photo-metadata {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem 2rem;
  margin-top: 1rem;
}

.metadata-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #666;
}

.value {
  font-size: 1rem;
  color: #333;
  margin-top: 0.2rem;
}

.link {
  color: #409EFF;
  text-decoration: none;
}

.delete-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.delete-modal .modal-content {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}
</style>
