<template>
  <div class="photo-thumbnail" :class="{ 'selected': isSelected }" @click="handleClick">
    <div class="thumbnail-inner">
      <img :src="photo.url" :alt="photo.title || '照片'" loading="lazy">
      <div class="photo-overlay">
        <div class="photo-actions">
          <button class="action-btn view" @click.stop="viewPhoto">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
          </button>
          <button class="action-btn edit" @click.stop="editPhoto">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
            </svg>
          </button>
          <button class="action-btn share" @click.stop="sharePhoto">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="18" cy="5" r="3"></circle>
              <circle cx="6" cy="12" r="3"></circle>
              <circle cx="18" cy="19" r="3"></circle>
              <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
              <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
            </svg>
          </button>
        </div>
      </div>
      <div class="selection-indicator" v-if="selectable">
        <input 
          type="checkbox" 
          :checked="isSelected" 
          @click.stop="toggleSelection"
        >
      </div>
    </div>
    <div class="photo-info" v-if="showInfo">
      <div class="photo-title" :title="photo.title">{{ photo.title || '未命名照片' }}</div>
      <div class="photo-date">{{ formatDate(photo.createdAt) }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  photo: {
    type: Object,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  },
  selectable: {
    type: Boolean,
    default: false
  },
  showInfo: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['select', 'view', 'edit', 'share']);
const router = useRouter();

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
};

const toggleSelection = () => {
  emit('select', props.photo.id);
};

const handleClick = () => {
  if (props.selectable) {
    toggleSelection();
  } else {
    viewPhoto();
  }
};

const viewPhoto = () => {
  emit('view', props.photo.id);
  router.push({ name: 'ViewPhoto', params: { id: props.photo.id } });
};

const editPhoto = () => {
  emit('edit', props.photo.id);
  router.push({ name: 'EditPhoto', params: { id: props.photo.id } });
};

const sharePhoto = () => {
  emit('share', props.photo.id);
  router.push({ name: 'SharePhoto', params: { id: props.photo.id } });
};
</script>

<style scoped>
.photo-thumbnail {
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--neutral-100);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
  cursor: pointer;
  position: relative;
}

.photo-thumbnail:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.thumbnail-inner {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.photo-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.photo-thumbnail:hover img {
  transform: scale(1.05);
}

.photo-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.photo-thumbnail:hover .photo-overlay {
  opacity: 1;
}

.photo-actions {
  display: flex;
  gap: var(--space-xs);
}

.action-btn {
  width: 32px;
  height: 32px;
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

.action-btn.share:hover {
  background-color: var(--info);
}

.selection-indicator {
  position: absolute;
  top: var(--space-xs);
  right: var(--space-xs);
  z-index: 2;
}

.selection-indicator input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.photo-info {
  padding: var(--space-xs) var(--space-sm);
}

.photo-title {
  font-size: 0.9rem;
  color: var(--neutral-800);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.photo-date {
  font-size: 0.75rem;
  color: var(--neutral-600);
}

.selected {
  outline: 3px solid var(--primary-color);
  position: relative;
  z-index: 1;
}

.selected::after {
  content: "";
  position: absolute;
  inset: 0;
  background-color: var(--primary-light);
  opacity: 0.3;
  pointer-events: none;
}
</style> 