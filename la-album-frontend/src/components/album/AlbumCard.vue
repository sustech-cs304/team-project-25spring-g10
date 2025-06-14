<template>
  <div class="album-card" @click="navigateToAlbum">
    <div class="album-cover">
      <img :src="album.coverUrl" :alt="album.title + '封面'" >
      <div class="album-hover">
        <div class="album-actions">
          <button class="action-btn view" @click.stop="navigateToAlbum">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
          </button>
          <button class="action-btn edit" @click.stop="editAlbum">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
          </button>
          <button class="action-btn delete" @click.stop="deleteAlbumCard">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>
    <div class="album-info">
      <h3 class="album-title">{{ album.title }}</h3>
      <p class="album-description">{{ album.description }}</p>
      <div class="album-meta">
        <span class="album-count">{{ album.photoCount }}张照片</span>
        <span class="album-date">{{ formatDate(album.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { useRouter } from 'vue-router';
import { deleteAlbum } from '@/api/album';
import {ElMessage} from "element-plus";

// eslint-disable-next-line no-undef
const emit = defineEmits(['deleted'])

const props = defineProps({
  album: {
    type: Object,
    required: true
  }
});

const router = useRouter();

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}年${date.getMonth() + 1}月`;
};

const navigateToAlbum = () => {
  router.push({ name: 'Album', params: { id: props.album.id } });
};

const editAlbum = () => {
  // 编辑相册的逻辑
  console.log('编辑相册', props.album.id);

};

const deleteAlbumCard = async () => {
  if (!props.album || !props.album.id) {
    ElMessage.warning('无效的相册信息');
    return;
  }

  const confirmed = window.confirm(`确定要删除相册 "${props.album.name}" 吗？`);
  if (!confirmed) return;
  try {
    const response = await deleteAlbum(props.album.id);
    console.log('删除相册响应:', response);

    if (response && response.code === 0) {
      ElMessage.success(`相册 "${props.album.name}" 删除成功`);
      // 可选：刷新页面或通知父组件更新相册列表
      emit('deleted', props.album.id); //
    } else {
      console.error('删除相册返回格式异常:', response);
      ElMessage.error('删除失败，请稍后再试');
    }
  } catch (error) {
    console.error('删除相册出错:', error);
    ElMessage.error('删除失败，请稍后再试');
  }
};

</script>

<style scoped>
.album-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background-color: var(--neutral-100);
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
  cursor: pointer;
}

.album-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.album-cover {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.album-card:hover .album-cover img {
  transform: scale(1.05);
}

.album-hover {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.album-card:hover .album-hover {
  opacity: 1;
}

.album-actions {
  display: flex;
  gap: var(--space-sm);
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.action-btn.view:hover {
  background-color: var(--primary-color);
}

.action-btn.edit:hover {
  background-color: var(--warning);
}

.action-btn.delete:hover {
  background-color: var(--error);
}

.album-info {
  padding: var(--space-md);
}

.album-title {
  font-size: 1.2rem;
  margin-bottom: var(--space-xs);
  color: var(--neutral-900);
}

.album-description {
  font-size: 0.9rem;
  color: var(--neutral-700);
  margin-bottom: var(--space-sm);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.album-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--neutral-600);
}
</style> 