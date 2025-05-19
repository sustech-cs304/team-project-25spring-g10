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
          {{ formatTime(currentTime) }} / {{ formatTime(totalDuration) }}
        </div>
      </div>
      
      <div class="slider-info">
        <div class="photo-info">
          照片 {{ currentIndex + 1 }}/{{ photos.length }}
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
    
    <!-- 添加背景音乐播放器 -->
    <audio ref="backgroundMusic" loop></audio>
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
    default: 10
  },
  autoplay: {
    type: Boolean,
    default: false
  },
  bgmUrl: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:currentTime', 'ended']);

const slider = ref(null);
const container = ref(null);
const backgroundMusic = ref(null);
const isPlaying = ref(false);
const currentIndex = ref(0);
const currentTime = ref(0);
const intervalId = ref(null);

// 照片的累计显示时间（用于计算当前应该显示哪张照片）
const cumulativePhotoTimes = computed(() => {
  const times = [];
  let cumulativeTime = 0;
  
  // 如果所有照片都没有displayDuration，使用均分总时长的方式计算
  const hasDisplayDurations = props.photos.some(photo => parseInt(photo.displayDuration) > 0);
  
  if (!hasDisplayDurations && props.photos.length > 0) {
    // 每张照片均分总时长
    const perPhotoTime = Math.floor(props.duration / props.photos.length);
    
    props.photos.forEach(() => {
      cumulativeTime += perPhotoTime;
      times.push(cumulativeTime);
    });
  } else {
    // 使用每张照片的displayDuration，如果没有则默认为0
    props.photos.forEach((photo) => {
      const displayTime = parseInt(photo.displayDuration) || 0;
      cumulativeTime += displayTime;
      times.push(cumulativeTime);
    });
  }
  
  return times;
});

// 总时长现在是所有照片显示时长的总和
const totalDuration = computed(() => {
  if (props.photos.length === 0) return 0;
  
  // 使用照片时长总和
  const calculatedDuration = props.photos.reduce((total, photo) => {
    return total + (parseInt(photo.displayDuration) || 0); // 如果照片没有时长，不提供默认值
  }, 0);
  
  // 如果计算出来的总时长为0，使用传入的props.duration
  return calculatedDuration > 0 ? calculatedDuration : props.duration;
});

// 进度百分比
const progress = computed(() => {
  return (currentTime.value / totalDuration.value) * 100;
});

// 格式化时间为 MM:SS
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 获取当前应该显示的照片索引
const getCurrentPhotoIndex = (currentTime) => {
  // 找到第一个累计时间大于当前时间的索引
  const index = cumulativePhotoTimes.value.findIndex(time => time > currentTime);
  
  // 如果找不到（比如已经到最后），则显示最后一张
  return index === -1 ? props.photos.length - 1 : index;
};

// 开始播放
const startSlider = () => {
  if (intervalId.value) clearInterval(intervalId.value);
  
  isPlaying.value = true;
  
  // 播放背景音乐
  if (backgroundMusic.value && props.bgmUrl) {
    console.log('尝试播放背景音乐:', props.bgmUrl);
    try {
      // 为了确保音频能正确加载，使用字符串形式的URL
      let audioSrc = props.bgmUrl;
      if (typeof audioSrc === 'object' && audioSrc.default) {
        audioSrc = audioSrc.default; // 处理webpack require加载的资源
      }
      
      backgroundMusic.value.src = audioSrc;
      backgroundMusic.value.volume = 0.5; // 设置适当的音量
      
      // 添加canplaythrough事件监听，确保音频加载完成后再播放
      backgroundMusic.value.oncanplaythrough = () => {
        console.log('背景音乐已加载完成，准备播放');
        backgroundMusic.value.play()
          .then(() => {
            console.log('背景音乐播放成功');
          })
          .catch(error => {
            console.error('背景音乐播放失败:', error);
          });
      };
      
      // 添加错误处理
      backgroundMusic.value.onerror = (e) => {
        console.error('背景音乐加载失败:', e);
      };
      
      // 如果已经缓存，可能不会触发canplaythrough事件，所以尝试直接播放
      if (backgroundMusic.value.readyState >= 3) { // HAVE_FUTURE_DATA
        backgroundMusic.value.play()
          .then(() => console.log('背景音乐直接播放成功'))
          .catch(error => console.error('直接播放失败:', error));
      }
    } catch (error) {
      console.error('设置背景音乐失败:', error);
    }
  } else {
    console.log('无法播放背景音乐', {
      audioElement: !!backgroundMusic.value,
      bgmUrl: props.bgmUrl
    });
  }
  
  // 每100毫秒更新一次时间和进度
  intervalId.value = setInterval(() => {
    if (currentTime.value < totalDuration.value) {
      currentTime.value += 0.1;
      emit('update:currentTime', currentTime.value);
      
      // 根据当前时间确定应该显示哪张照片
      const expectedIndex = getCurrentPhotoIndex(currentTime.value);
      
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
  
  // 暂停背景音乐
  if (backgroundMusic.value) {
    backgroundMusic.value.pause();
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
  
  // 重置背景音乐
  if (backgroundMusic.value) {
    backgroundMusic.value.currentTime = 0;
  }
  
  if (isPlaying.value) {
    stopSlider();
    startSlider();
  }
};

// 设置当前时间
const setCurrentTime = (time) => {
  currentTime.value = time;
  
  // 更新当前索引
  const expectedIndex = getCurrentPhotoIndex(currentTime.value);
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

// 监听bgmUrl的变化
watch(() => props.bgmUrl, (newValue) => {
  if (backgroundMusic.value && newValue) {
    backgroundMusic.value.src = newValue;
    if (isPlaying.value) {
      backgroundMusic.value.play().catch(error => {
        console.error('背景音乐播放失败:', error);
      });
    }
  }
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

.slider-info {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
}

.photo-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.duration-badge {
  display: none !important;
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