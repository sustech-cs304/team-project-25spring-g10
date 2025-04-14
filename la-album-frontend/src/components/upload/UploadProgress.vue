<template>
  <div class="upload-progress" :class="{ 'is-uploading': file.status === 'uploading' }">
    <div class="upload-file-info">
      <div class="file-icon" v-if="!file.thumbnail">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
          <circle cx="8.5" cy="8.5" r="1.5"></circle>
          <polyline points="21 15 16 10 5 21"></polyline>
        </svg>
      </div>
      <div class="file-thumbnail" v-else>
        <img :src="file.thumbnail" :alt="file.name">
      </div>
      <div class="file-details">
        <div class="file-name">{{ truncatedFileName }}</div>
        <div class="file-meta">
          <span class="file-size">{{ formatFileSize }}</span>
          <span class="upload-status" :class="statusClass">{{ statusText }}</span>
        </div>
      </div>
      <div class="file-actions">
        <button 
          class="action-btn cancel" 
          v-if="file.status === 'uploading'" 
          @click="cancelUpload"
          title="取消上传"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <button 
          class="action-btn retry" 
          v-else-if="file.status === 'error'" 
          @click="retryUpload"
          title="重试"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="1 4 1 10 7 10"></polyline>
            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
          </svg>
        </button>
        <button 
          class="action-btn remove" 
          v-if="file.status !== 'uploading'"
          @click="removeFile"
          title="移除"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </button>
      </div>
    </div>
    
    <div class="progress-bar-container" v-if="file.status === 'uploading'">
      <div class="progress-bar" :style="{ width: `${file.progress}%` }"></div>
      <div class="progress-text">{{ file.progress }}%</div>
    </div>
    
    <div class="upload-error" v-if="file.status === 'error' && file.errorMessage">
      {{ file.errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

/* eslint-disable-next-line no-undef */
const props = defineProps({
  file: {
    type: Object,
    required: true,
    validator: (file) => {
      return file && 
        typeof file.name === 'string' && 
        typeof file.size === 'number' && 
        ['uploading', 'success', 'error'].includes(file.status);
    }
  }
});

/* eslint-disable-next-line no-undef */
const emit = defineEmits(['cancel', 'retry', 'remove']);

// 计算状态文本
const statusText = computed(() => {
  switch (props.file.status) {
    case 'uploading':
      return `${props.file.progress}%`;
    case 'success':
      return '上传成功';
    case 'error':
      return '上传失败';
    default:
      return '';
  }
});

// 计算状态样式类
const statusClass = computed(() => {
  return props.file.status;
});

// 计算截断后的文件名
const truncatedFileName = computed(() => {
  const name = props.file.name;
  if (name.length > 25) {
    const ext = name.lastIndexOf('.');
    if (ext !== -1) {
      const fileName = name.substring(0, ext);
      const extension = name.substring(ext);
      return fileName.substring(0, 20) + '...' + extension;
    }
    return name.substring(0, 22) + '...';
  }
  return name;
});

// 格式化文件大小
const formatFileSize = computed(() => {
  const { size } = props.file;
  if (size < 1024) {
    return `${size} B`;
  } else if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  } else if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  } else {
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
  }
});

// 取消上传
const cancelUpload = () => {
  emit('cancel', props.file);
};

// 重试上传
const retryUpload = () => {
  emit('retry', props.file);
};

// 移除文件
const removeFile = () => {
  emit('remove', props.file);
};
</script>

<style scoped>
.upload-progress {
  background-color: var(--neutral-50);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  margin-bottom: var(--space-sm);
  border: 1px solid var(--neutral-200);
  transition: all 0.3s ease;
}

.upload-progress:hover {
  border-color: var(--neutral-300);
  box-shadow: var(--shadow-sm);
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--neutral-600);
  background-color: var(--neutral-200);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.file-thumbnail {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}

.file-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: var(--neutral-800);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: var(--neutral-600);
}

.file-size {
  margin-right: var(--space-sm);
}

.upload-status {
  font-weight: 500;
}

.upload-status.uploading {
  color: var(--primary-color);
}

.upload-status.success {
  color: var(--success);
}

.upload-status.error {
  color: var(--error);
}

.file-actions {
  display: flex;
  gap: var(--space-xs);
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: var(--neutral-200);
  color: var(--neutral-700);
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: var(--neutral-300);
  transform: scale(1.05);
}

.action-btn.cancel:hover,
.action-btn.remove:hover {
  background-color: var(--error-light);
  color: var(--error);
}

.action-btn.retry:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.upload-progress.is-uploading {
  border-left: 3px solid var(--primary-color);
}

.progress-bar-container {
  margin-top: var(--space-sm);
  height: 8px;
  background-color: var(--neutral-200);
  border-radius: var(--radius-pill);
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background-color: var(--primary-color);
  border-radius: var(--radius-pill);
  transition: width 0.3s ease-in-out;
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.5);
}

.progress-text {
  position: absolute;
  top: 8px;
  right: 0;
  font-size: 12px;
  color: var(--neutral-600);
  font-weight: 500;
}

.upload-error {
  margin-top: var(--space-sm);
  font-size: 0.85rem;
  color: var(--error);
}

@media (max-width: 576px) {
  .upload-file-info {
    flex-wrap: wrap;
  }
  
  .file-details {
    flex-basis: calc(100% - 80px);
  }
  
  .file-actions {
    margin-left: auto;
  }
}
</style> 