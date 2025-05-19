<template>
  <div class="music-selector">
    <label class="form-label">背景音乐</label>
    
    <!-- 音乐风格分类 -->
    <div class="style-filter">
      <button 
        v-for="style in uniqueStyles" 
        :key="style" 
        class="style-btn"
        :class="{ active: selectedStyle === style }"
        @click="filterByStyle(style)"
      >
        {{ style }}
      </button>
      <button 
        class="style-btn"
        :class="{ active: selectedStyle === 'all' }"
        @click="filterByStyle('all')"
      >
        全部
      </button>
    </div>
    
    <!-- 音乐列表 -->
    <div class="music-list">
      <div 
        v-for="music in filteredMusic" 
        :key="music.id" 
        class="music-item"
        :class="{ selected: modelValue === music.id }"
        @click="selectMusic(music.id)"
      >
        <div class="music-info">
          <div class="music-name">{{ music.name }}</div>
          <div class="music-description">{{ music.description }}</div>
        </div>
        
        <div class="music-controls">
          <button class="preview-btn" @click.stop="previewMusic(music)">
            <svg v-if="currentlyPlaying === music.id" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="6" y="4" width="4" height="16"></rect>
              <rect x="14" y="4" width="4" height="16"></rect>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="5 3 19 12 5 21 5 3"></polygon>
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 音频播放元素 (隐藏) -->
    <audio ref="audioPlayer" @ended="stopPreview"></audio>
  </div>
</template>

<script setup>
/* eslint-disable no-undef */
import { ref, computed, onBeforeUnmount, watch } from 'vue';

const props = defineProps({
  musicList: {
    type: Array,
    required: true
  },
  modelValue: {
    type: [Number, String],
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

const audioPlayer = ref(null);
const currentlyPlaying = ref(null);
const selectedStyle = ref('all');

// 获取所有唯一的音乐风格
const uniqueStyles = computed(() => {
  const styles = new Set(props.musicList.map(music => music.style));
  return Array.from(styles);
});

// 根据选择的风格过滤音乐
const filteredMusic = computed(() => {
  if (selectedStyle.value === 'all') {
    return props.musicList;
  }
  return props.musicList.filter(music => music.style === selectedStyle.value);
});

// 根据风格过滤音乐
const filterByStyle = (style) => {
  selectedStyle.value = style;
};

// 选择音乐
const selectMusic = (musicId) => {
  emit('update:modelValue', musicId);
};

// 预览音乐
const previewMusic = (music) => {
  // 如果正在播放同一首音乐，停止播放
  if (currentlyPlaying.value === music.id) {
    stopPreview();
    return;
  }
  
  // 停止当前播放
  stopPreview();
  
  // 开始播放新音乐
  if (audioPlayer.value && music.previewUrl) {
    try {
      audioPlayer.value.src = music.previewUrl;
      audioPlayer.value.play()
        .then(() => {
          currentlyPlaying.value = music.id;
        })
        .catch(error => {
          console.error('播放失败:', error);
          // 显示提示
          alert(`音乐 "${music.name}" 预览不可用，但您仍可以选择它作为背景音乐。`);
          
          // 如果没有真实音频，模拟播放状态
          currentlyPlaying.value = music.id;
          setTimeout(() => {
            if (currentlyPlaying.value === music.id) {
              stopPreview();
            }
          }, 5000); // 5秒后自动停止
        });
    } catch (error) {
      console.error('加载音频失败:', error);
      // 显示提示
      alert(`音乐 "${music.name}" 预览不可用，但您仍可以选择它作为背景音乐。`);
      
      // 模拟播放状态
      currentlyPlaying.value = music.id;
      setTimeout(() => {
        if (currentlyPlaying.value === music.id) {
          stopPreview();
        }
      }, 5000); // 5秒后自动停止
    }
  } else {
    // 如果没有音频元素或预览URL，模拟播放状态
    alert(`音乐 "${music.name}" 预览不可用，但您仍可以选择它作为背景音乐。`);
    
    currentlyPlaying.value = music.id;
    setTimeout(() => {
      if (currentlyPlaying.value === music.id) {
        stopPreview();
      }
    }, 5000); // 5秒后自动停止
  }
};

// 停止预览
const stopPreview = () => {
  if (audioPlayer.value) {
    audioPlayer.value.pause();
    audioPlayer.value.currentTime = 0;
  }
  currentlyPlaying.value = null;
};

// 监听modelValue的变化，确保组件内部状态与外部保持一致
watch(() => props.modelValue, (newValue) => {
  // 如果外部更改了选中的音乐，更新内部状态
  if (newValue && props.musicList.some(music => music.id === Number(newValue))) {
    // 可以在这里处理其他需要随选择变化的逻辑
  }
});

// 暴露方法给父组件使用
defineExpose({
  stopPreview
});

// 组件卸载前停止播放
onBeforeUnmount(() => {
  stopPreview();
});
</script>

<style scoped>
.music-selector {
  margin-bottom: var(--space-md);
}

.form-label {
  display: block;
  margin-bottom: var(--space-xs);
  font-weight: 500;
}

.style-filter {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
  margin-bottom: var(--space-md);
}

.style-btn {
  background-color: var(--neutral-200);
  border: none;
  border-radius: var(--radius-sm);
  padding: 6px 12px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.style-btn:hover {
  background-color: var(--neutral-300);
}

.style-btn.active {
  background-color: var(--primary-color);
  color: white;
}

.music-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--space-md);
  max-height: 300px;
  overflow-y: auto;
  padding-right: var(--space-sm);
}

.music-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-sm);
  background-color: white;
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.music-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.music-item.selected {
  border: 2px solid var(--primary-color);
  background-color: rgba(var(--primary-rgb), 0.05);
}

.music-info {
  flex-grow: 1;
}

.music-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.music-description {
  font-size: 0.8rem;
  color: var(--neutral-600);
}

.music-controls {
  margin-left: var(--space-sm);
}

.preview-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--neutral-100);
  border: 1px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.preview-btn:hover {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}
</style> 