import { v4 as uuidv4 } from 'uuid';

// 模拟照片数据
const demoPhotos = [
  { 
    id: '1', 
    name: '照片1',
    thumbnailUrl: require('@/assets/pic/1.jpg'),
    url: require('@/assets/pic/1.jpg')
  },
  { 
    id: '2', 
    name: '照片2',
    thumbnailUrl: require('@/assets/pic/2.png'),
    url: require('@/assets/pic/2.png')
  },
  { 
    id: '3', 
    name: '照片3',
    thumbnailUrl: require('@/assets/pic/3.png'),
    url: require('@/assets/pic/3.png')
  },
  { 
    id: '4', 
    name: '照片4',
    thumbnailUrl: require('@/assets/pic/4.png'),
    url: require('@/assets/pic/4.png')
  },
  { 
    id: '5', 
    name: '照片5',
    thumbnailUrl: require('@/assets/pic/5.png'),
    url: require('@/assets/pic/5.png')
  }
];

// 模拟相册数据
const demoAlbums = [
  { 
    id: 'album1', 
    name: '游戏相册', 
    photoCount: 5, 
    coverUrl: require('@/assets/pic/1.jpg')
  }
];

// 模拟背景音乐
const demoBgMusic = [
  { id: 1, name: "轻松快乐" },
  { id: 2, name: "浪漫温馨" },
  { id: 3, name: "动感节奏" },
  { id: 4, name: "怀旧经典" },
  { id: 5, name: "自然声音" }
];

// 模拟存储
let memories = [];

// 模拟视频生成逻辑
const simulateVideoGeneration = (photos) => {
  // 模拟视频URL (实际上我们只是引用第一张图片作为封面)
  const thumbnailUrl = photos[0].thumbnailUrl;
  
  // 生成一个随机的持续时间 (30-90秒)
  const duration = Math.floor(Math.random() * 60) + 30;
  
  return {
    videoUrl: null, // 前端演示时不会有真实视频
    thumbnailUrl,
    duration
  };
};

// 获取模拟的回忆视频列表
export const fetchMemories = async () => {
  return new Promise((resolve) => {
    // 模拟网络延迟
    setTimeout(() => {
      resolve(memories);
    }, 800);
  });
};

// 获取模拟的相册列表
export const fetchAlbums = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(demoAlbums);
    }, 500);
  });
};

// 获取模拟的背景音乐列表
export const fetchBgMusic = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(demoBgMusic);
    }, 300);
  });
};

// 通过ID获取模拟的回忆视频
export const getMemoryById = async (id) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const memory = memories.find(m => m.id === id);
      
      if (memory) {
        resolve({
          ...memory,
          // 在详情页中，我们提供完整的照片数据
          photos: demoPhotos
        });
      } else {
        reject(new Error('回忆视频不存在'));
      }
    }, 800);
  });
};

// 生成模拟的回忆视频
export const generateMemory = async (data) => {
  return new Promise((resolve) => {
    // 模拟生成视频的延迟
    setTimeout(() => {
      // 生成唯一ID
      const id = uuidv4();
      
      // 使用指定的照片或默认使用所有示例照片
      const selectedPhotos = demoPhotos;
      
      // 模拟视频生成
      const { videoUrl, thumbnailUrl, duration } = simulateVideoGeneration(
        selectedPhotos
      );
      
      // 创建新的回忆视频记录
      const newMemory = {
        id,
        title: data.title,
        albumId: data.albumId || 'album1',
        albumName: '旅行相册',
        bgmId: data.bgmId || 1,
        bgmName: demoBgMusic.find(bgm => bgm.id === (data.bgmId || 1)).name,
        style: data.style || 'classic',
        transition: data.transition || 'fade',
        createdAt: new Date().toISOString(),
        photoCount: selectedPhotos.length,
        videoUrl,
        thumbnailUrl,
        duration,
        photos: selectedPhotos.map(photo => ({
          id: photo.id,
          thumbnailUrl: photo.thumbnailUrl
        }))
      };
      
      // 添加到模拟存储
      memories.unshift(newMemory);
      
      resolve(newMemory);
    }, 2000); // 模拟2秒的生成时间
  });
};

// 更新模拟的回忆视频
export const updateMemory = async (id, data) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = memories.findIndex(m => m.id === id);
      
      if (index === -1) {
        reject(new Error('回忆视频不存在'));
        return;
      }
      
      // 获取当前内存对象
      const memory = { ...memories[index] };
      
      // 更新信息
      memory.title = data.title || memory.title;
      memory.bgmId = data.bgmId || memory.bgmId;
      memory.bgmName = demoBgMusic.find(bgm => bgm.id === (data.bgmId || memory.bgmId)).name;
      memory.style = data.style || memory.style;
      memory.transition = data.transition || memory.transition;
      
      // 如果需要重新生成，则更新缩略图和时长
      if (data.regenerate) {
        // 根据photoIds过滤照片
        const selectedPhotos = data.photoIds
          ? demoPhotos.filter(photo => data.photoIds.includes(photo.id))
          : demoPhotos;
        
        // 模拟视频生成
        const { videoUrl, thumbnailUrl, duration } = simulateVideoGeneration(
          selectedPhotos
        );
        
        memory.videoUrl = videoUrl;
        memory.thumbnailUrl = thumbnailUrl;
        memory.duration = duration;
        memory.photoCount = selectedPhotos.length;
        
        // 更新照片列表
        memory.photos = selectedPhotos.map(photo => ({
          id: photo.id,
          thumbnailUrl: photo.thumbnailUrl
        }));
      }
      
      // 更新存储
      memories[index] = memory;
      
      resolve(memory);
    }, data.regenerate ? 2000 : 500); // 重新生成需要更长时间
  });
};

// 删除模拟的回忆视频
export const deleteMemory = async (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      memories = memories.filter(m => m.id !== id);
      resolve({ success: true });
    }, 500);
  });
};

// 分享模拟的回忆视频
export const shareMemory = async (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟生成分享链接
      const shareLink = `https://example.com/share/memory/${id}`;
      resolve({ shareLink });
    }, 500);
  });
}; 