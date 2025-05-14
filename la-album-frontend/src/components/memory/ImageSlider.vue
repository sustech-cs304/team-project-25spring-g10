<template>
  <div class="image-slider" ref="slider">
    <div class="slider-container" ref="container">
      <div v-for="(photo, index) in photos" :key="photo.id" class="slide" :class="{ active: index === currentIndex }">
        <img :src="photo.url || photo.thumbnailUrl" :alt="photo.name || `照片 ${index + 1}`" />
      </div>
    </div>
    
    <div class="slider-controls">
      <div class="slider-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${progress}%` }"></div>
        </div>
        <div class="progress-time">
          {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
        </div>
      </div>
      
      <div class="slider-buttons">
        <button class="btn-control" @click="togglePlay">
          <svg v-if="isPlaying" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="6" y="4" width="4" height="16"></rect>
            <rect x="14" y="4" width="4" height="16"></rect>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="5 3 19 12 5 21 5 3"></polygon>
          </svg>
        </button>
        
        <button class="btn-control" @click="restart">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="1 4 1 10 7 10"></polyline>
            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
/* eslint-disable no-undef */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';

const props = defineProps({
  photos: {
    type: Array,
    required: true
  },
  transition: {
    type: String,
    default: 'fade'
  },
  duration: {
    type: Number,
    default: 10 // 默认30秒
  },
  autoplay: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:currentTime', 'ended']);

const slider = ref(null);
const container = ref(null);
const isPlaying = ref(false);
const currentIndex = ref(0);
const currentTime = ref(0);
const intervalId = ref(null);

// 每张照片显示的时间（秒）
const photoDisplayTime = computed(() => {
  if (props.photos.length === 0) return 0;
  return 2;
});

// 进度百分比
const progress = computed(() => {
  return (currentTime.value / props.duration) * 100;
});

// 格式化时间为 MM:SS
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 开始播放
const startSlider = () => {
  if (intervalId.value) clearInterval(intervalId.value);
  
  isPlaying.value = true;
  
  // 每100毫秒更新一次时间和进度
  intervalId.value = setInterval(() => {
    if (currentTime.value < props.duration) {
      currentTime.value += 0.1;
      emit('update:currentTime', currentTime.value);
      
      // 检查是否需要切换照片
      const expectedIndex = Math.min(
        Math.floor(currentTime.value / photoDisplayTime.value),
        props.photos.length - 1
      );
      
      if (expectedIndex !== currentIndex.value) {
        currentIndex.value = expectedIndex;
      }
    } else {
      // 播放结束
      stopSlider();
      emit('ended');
    }
  }, 100);
};

// 停止播放
const stopSlider = () => {
  if (intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  }
  isPlaying.value = false;
};

// 切换播放/暂停
const togglePlay = () => {
  if (isPlaying.value) {
    stopSlider();
  } else {
    startSlider();
  }
};

// 重新开始播放
const restart = () => {
  currentTime.value = 0;
  currentIndex.value = 0;
  emit('update:currentTime', 0);
  
  if (isPlaying.value) {
    stopSlider();
    startSlider();
  }
};

// 设置当前时间
const setCurrentTime = (time) => {
  currentTime.value = time;
  
  // 更新当前索引
  const expectedIndex = Math.min(
    Math.floor(currentTime.value / photoDisplayTime.value),
    props.photos.length - 1
  );
  
  currentIndex.value = expectedIndex;
};

// 导出方法给父组件使用
defineExpose({
  play: startSlider,
  pause: stopSlider,
  restart,
  setCurrentTime
});

// 监听自动播放属性
watch(() => props.autoplay, (newValue) => {
  if (newValue && !isPlaying.value) {
    startSlider();
  } else if (!newValue && isPlaying.value) {
    stopSlider();
  }
}, { immediate: true });

// 组件挂载时，如果设置了自动播放，则开始播放
onMounted(() => {
  if (props.autoplay) {
    startSlider();
  }
});

// 组件卸载时，停止播放
onBeforeUnmount(() => {
  stopSlider();
});
</script>

<style scoped>
.image-slider {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #000;
  overflow: hidden;
  border-radius: var(--radius-md);
}

.slider-container {
  position: relative;
  width: 100%;
  height: calc(100% - 50px);
  overflow: hidden;
}

.slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.slide.active {
  opacity: 1;
  z-index: 10;
}

.slide img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.slider-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 10px;
  color: white;
  z-index: 20;
}

.slider-progress {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.progress-bar {
  flex-grow: 1;
  height: 4px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  margin-right: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: var(--primary-color);
  border-radius: 2px;
  transition: width 0.1s linear;
}

.progress-time {
  font-size: 0.75rem;
  min-width: 80px;
  text-align: right;
}

.slider-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-control {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.btn-control:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.btn-control:focus {
  outline: none;
}
</style> 