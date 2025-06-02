import { v4 as uuidv4 } from 'uuid';
import { fetchAlbumById, fetchAlbumList } from './album';  // 导入实际的相册API

// 模拟照片数据（仅在未找到实际照片时使用）
const demoPhotos = [
  { 
    id: '1', 
    name: '照片1',
    thumbnailUrl: require('@/assets/pic/1.jpg'),
    url: require('@/assets/pic/1.jpg'),
    displayDuration: 5 // 默认显示5秒
  },
  { 
    id: '2', 
    name: '照片2',
    thumbnailUrl: require('@/assets/pic/2.png'),
    url: require('@/assets/pic/2.png'),
    displayDuration: 5
  },
  { 
    id: '3', 
    name: '照片3',
    thumbnailUrl: require('@/assets/pic/3.png'),
    url: require('@/assets/pic/3.png'),
    displayDuration: 5
  },
  { 
    id: '4', 
    name: '照片4',
    thumbnailUrl: require('@/assets/pic/4.png'),
    url: require('@/assets/pic/4.png'),
    displayDuration: 5
  },
  { 
    id: '5', 
    name: '照片5',
    thumbnailUrl: require('@/assets/pic/5.png'),
    url: require('@/assets/pic/5.png'),
    displayDuration: 5
  }
];

// 模拟背景音乐
const demoBgMusic = [
  { 
    id: 1, 
    name: "轻松快乐", 
    style: "欢快",
    description: "适合记录生日、旅行等欢乐场景",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  },
  { 
    id: 2, 
    name: "八方来财", 
    style: "财富",
    description: "适合没钱的一些温馨瞬间",
    previewUrl: require('@/assets/audio/八方来财.mp3') 
  },
  { 
    id: 3, 
    name: "动感节奏", 
    style: "动感",
    description: "适合软工写不出来的场景",
    previewUrl: require('@/assets/audio/The Fox.mp3') 
  },
  { 
    id: 4, 
    name: "怀旧经典", 
    style: "怀旧",
    description: "适合回忆往事、纪念日等场景",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  },
  { 
    id: 5, 
    name: "自然声音", 
    style: "放松",
    description: "适合自然风景、冥想放松等场景",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  },
  { 
    id: 6, 
    name: "梦幻旋律", 
    style: "梦幻",
    description: "适合童话般的美好记忆",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  },
  { 
    id: 7, 
    name: "激情摇滚", 
    style: "摇滚",
    description: "适合激情澎湃的精彩瞬间",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  },
  { 
    id: 8, 
    name: "舒缓钢琴", 
    style: "古典",
    description: "适合优雅、庄重的场合",
    previewUrl: require('@/assets/audio/startmenu0.mp3') 
  }
];

// 模拟存储
let memories = [];

// 模拟视频生成逻辑
const simulateVideoGeneration = (photos) => {
  // 确保至少有一张照片
  if (!photos || photos.length === 0) {
    return {
      videoUrl: null,
      thumbnailUrl: null,
      duration: 0
    };
  }
  
  // 模拟视频URL (实际上我们只是引用第一张图片作为封面)
  const thumbnailUrl = photos[0].thumbnailUrl || photos[0].url;
  
  // 计算总持续时间为所有照片的显示时间总和
  const duration = photos.reduce((total, photo) => {
    const photoTime = parseInt(photo.displayDuration) || 5; // 如果没有设置，默认5秒
    return total + photoTime;
  }, 0);
  
  // 确保总时长不为0
  const finalDuration = duration > 0 ? duration : (photos.length * 5);
  
  console.log('生成视频:', {
    photosCount: photos.length,
    photoTimes: photos.map(p => ({ id: p.id, time: p.displayDuration })),
    calculatedDuration: duration,
    finalDuration
  });
  
  return {
    videoUrl: null, // 前端演示时不会有真实视频
    thumbnailUrl,
    duration: finalDuration
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

// 获取模拟的相册列表（使用实际API）
export const fetchAlbums = async () => {
  try {
    const albums = await fetchAlbumList();
    // 将实际相册转换为需要的格式
    return albums.map(album => ({
      id: album.id.toString(),
      name: album.title,
      photoCount: album.photos ? album.photos.length : 0,
      coverUrl: album.photos && album.photos.length > 0 ? album.photos[0].url : null
    }));
  } catch (error) {
    console.error('获取相册列表失败:', error);
    // 如果实际API调用失败，返回模拟数据
    return [
      { 
        id: 'album1', 
        name: '游戏相册', 
        photoCount: 5, 
        coverUrl: require('@/assets/pic/1.jpg')
      }
    ];
  }
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
        // 如果是从实际相册创建的，尝试加载实际照片
        const loadPhotos = async () => {
          try {
            // 如果有有效的相册ID，尝试从实际相册加载照片
            if (memory.albumId) {
              console.log(`尝试为回忆视频${id}加载相册${memory.albumId}的照片`);
              
              const albumResponse = await fetchAlbumById(memory.albumId);
              console.log('获取到的相册响应:', JSON.stringify(albumResponse));
              
              if (albumResponse && albumResponse.photos && albumResponse.photos.length > 0) {
                console.log(`为回忆视频找到${albumResponse.photos.length}张照片`);
                
                // 从相册获取所有照片信息
                const albumPhotos = albumResponse.photos.map(photo => ({
                  id: photo.id.toString(),
                  name: photo.title || '未命名照片',
                  thumbnailUrl: photo.url,
                  url: photo.url
                }));
                
                // 使用memory.photos中的信息来确定正确的顺序和显示时长
                if (memory.photos && memory.photos.length > 0) {
                  console.log('使用内存中保存的照片顺序和显示时长');
                  
                  // 创建完整的照片信息数组，保持memory.photos中的顺序
                  const completePhotos = memory.photos.map(memoryPhoto => {
                    // 查找对应的相册照片以获取完整信息
                    const albumPhoto = albumPhotos.find(p => p.id === memoryPhoto.id);
                    
                    // 合并信息，优先使用memory中的信息
                    return {
                      id: memoryPhoto.id,
                      name: albumPhoto ? albumPhoto.name : '未命名照片',
                      thumbnailUrl: memoryPhoto.thumbnailUrl || (albumPhoto ? albumPhoto.thumbnailUrl : null),
                      url: albumPhoto ? albumPhoto.url : memoryPhoto.thumbnailUrl,
                      displayDuration: memoryPhoto.displayDuration // 使用原始保存的时长，不提供默认值
                    };
                  });
                  
                  console.log('排序后的照片顺序:', completePhotos.map(p => p.id));
                  return completePhotos;
                }
                
                return albumPhotos;
              } else {
                console.log('相册中没有照片，使用演示照片');
              }
            }
            console.log('使用演示照片');
            return demoPhotos; // 如果无法加载实际照片，返回演示照片
          } catch (error) {
            console.error('加载相册照片失败:', error);
            return demoPhotos; // 出错时返回演示照片
          }
        };
        
        loadPhotos().then(photos => {
          // 查找背景音乐
          const bgm = demoBgMusic.find(b => b.id === memory.bgmId);
          
          // 确保总是有bgmUrl，如果找不到对应的音乐，使用默认的
          let bgmUrl = null;
          if (bgm && bgm.previewUrl) {
            bgmUrl = bgm.previewUrl;
          } else {
            // 如果找不到对应的音乐，使用第一个音乐作为默认
            bgmUrl = demoBgMusic[0].previewUrl;
          }
          
          // 记录背景音乐信息
          console.log('返回的背景音乐信息:', {
            bgmId: memory.bgmId,
            bgmFound: !!bgm,
            bgmName: bgm ? bgm.name : '未找到',
            bgmUrl: bgmUrl
          });
          
          resolve({
            ...memory,
            photos,
            // 添加背景音乐URL
            bgmUrl: bgmUrl
          });
        });
      } else {
        reject(new Error('回忆视频不存在'));
      }
    }, 800);
  });
};

// 获取相册中的照片
const getPhotosFromAlbum = async (albumId) => {
  try {
    if (!albumId) {
      console.log('没有提供相册ID，使用演示照片');
      return demoPhotos;
    }
    
    console.log(`尝试从相册ID ${albumId} 获取照片`);
    const albumResponse = await fetchAlbumById(albumId);
    console.log('获取到的相册响应:', JSON.stringify(albumResponse));
    
    // 检查相册数据
    if (albumResponse && albumResponse.photos && albumResponse.photos.length > 0) {
      console.log(`找到 ${albumResponse.photos.length} 张照片`);
      return albumResponse.photos.map(photo => ({
        id: photo.id.toString(),
        name: photo.title || '未命名照片',
        thumbnailUrl: photo.url,
        url: photo.url,
        displayDuration: photo.displayDuration || 5 // 默认每张照片显示5秒
      }));
    } else {
      console.log('相册中没有照片或响应格式不正确，使用演示照片');
    }
    
    console.log('返回演示照片');
    return demoPhotos; // 如果无法获取相册照片，返回演示照片
  } catch (error) {
    console.error('获取相册照片失败:', error);
    console.log('由于错误返回演示照片');
    return demoPhotos; // 出错时返回演示照片
  }
};

// 生成模拟的回忆视频
export const generateMemory = async (data) => {
  // 获取相册中的照片
  const allPhotos = await getPhotosFromAlbum(data.albumId);
  
  // 如果指定了照片ID列表，则只使用这些照片
  let selectedPhotos = allPhotos;
  if (data.photoIds && Array.isArray(data.photoIds) && data.photoIds.length > 0) {
    console.log('使用指定的照片ID列表:', data.photoIds);
    selectedPhotos = allPhotos.filter(photo => 
      data.photoIds.includes(photo.id.toString())
    );
    
    // 如果过滤后没有照片，则使用全部照片
    if (selectedPhotos.length === 0) {
      console.log('过滤后没有匹配的照片，使用全部照片');
      selectedPhotos = allPhotos;
    }
  }
  
  // 如果提供了照片时长设置，更新每张照片的显示时长
  if (data.photoDisplayDurations && typeof data.photoDisplayDurations === 'object') {
    console.log('应用照片时长设置', data.photoDisplayDurations);
    Object.keys(data.photoDisplayDurations).forEach(photoId => {
      const photo = selectedPhotos.find(p => p.id.toString() === photoId.toString());
      if (photo) {
        photo.displayDuration = data.photoDisplayDurations[photoId];
        console.log(`设置照片 ${photoId} 时长为 ${photo.displayDuration}秒`);
      }
    });
  }
  
  // 获取相册信息
  let albumName = '默认相册';
  try {
    if (data.albumId) {
      const albumResponse = await fetchAlbumById(data.albumId);
      console.log('获取相册信息，相册响应数据:', albumResponse);
      
      // 直接从响应中获取标题
      if (albumResponse && albumResponse.title) {
        albumName = albumResponse.title;
      }
    }
  } catch (error) {
    console.error('获取相册名称失败:', error);
  }
  
  // 获取背景音乐
  const bgm = demoBgMusic.find(item => item.id === (data.bgmId || 1));
  
  // 确保bgmUrl不为null
  let bgmUrl = bgm && bgm.previewUrl ? bgm.previewUrl : demoBgMusic[0].previewUrl;
  
  // 打印选中照片的时长信息，方便调试
  console.log('选中照片的时长信息:', selectedPhotos.map(p => ({
    id: p.id,
    duration: p.displayDuration || 5
  })));
  
  return new Promise((resolve) => {
    // 模拟生成视频的延迟
    setTimeout(() => {
      // 生成唯一ID
      const id = uuidv4();
      
      // 模拟视频生成
      const { videoUrl, thumbnailUrl, duration } = simulateVideoGeneration(
        selectedPhotos
      );
      
      console.log(`计算的视频总时长: ${duration}秒`);
      
      // 创建新的回忆视频记录
      const newMemory = {
        id,
        title: data.title,
        albumId: data.albumId || 'album1',
        albumName,
        bgmId: data.bgmId || 1,
        bgmName: bgm ? bgm.name : '默认音乐',
        bgmUrl: bgmUrl,
        transition: data.transition || 'fade',
        createdAt: new Date().toISOString(),
        photoCount: selectedPhotos.length,
        videoUrl,
        thumbnailUrl,
        duration,
        photos: selectedPhotos.map(photo => ({
          id: photo.id,
          thumbnailUrl: photo.thumbnailUrl || photo.url,
          displayDuration: photo.displayDuration // 包含每张照片的显示时长，不提供默认值
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
  // 先获取当前存储的内存对象
  const index = memories.findIndex(m => m.id === id);
  
  if (index === -1) {
    throw new Error('回忆视频不存在');
  }
  
  // 获取当前内存对象
  const memory = { ...memories[index] };
  
  // 更新基本信息
  memory.title = data.title || memory.title;
  memory.bgmId = data.bgmId || memory.bgmId;
  
  // 获取背景音乐信息
  const bgm = demoBgMusic.find(item => item.id === (data.bgmId || memory.bgmId));
  memory.bgmName = bgm ? bgm.name : '默认音乐';
  memory.bgmUrl = bgm && bgm.previewUrl ? bgm.previewUrl : demoBgMusic[0].previewUrl;
  
  memory.transition = data.transition || memory.transition;
  
  // 更新照片显示时长
  if (data.photoDisplayDurations && typeof data.photoDisplayDurations === 'object') {
    memory.photos.forEach(photo => {
      if (data.photoDisplayDurations[photo.id]) {
        photo.displayDuration = parseInt(data.photoDisplayDurations[photo.id]) || 5;
      }
    });
  }
  
  // 如果需要重新生成，预先获取照片
  let selectedPhotos = null;
  if (data.regenerate) {
    try {
      // 获取照片数据
      if (data.photoIds && data.photoIds.length > 0) {
        // 如果指定了照片ID列表，从内存中获取
        const allPhotos = await getPhotosFromAlbum(memory.albumId);
        
        // 首先过滤出指定ID的照片
        selectedPhotos = allPhotos.filter(photo => 
          data.photoIds.includes(photo.id.toString())
        );
        
        // 应用自定义显示时长
        if (data.photoDisplayDurations && typeof data.photoDisplayDurations === 'object') {
          selectedPhotos.forEach(photo => {
            if (data.photoDisplayDurations[photo.id]) {
              photo.displayDuration = data.photoDisplayDurations[photo.id];
            }
          });
        }
      } else {
        // 否则使用相册中的所有照片
        selectedPhotos = await getPhotosFromAlbum(memory.albumId);
        
        // 应用自定义显示时长
        if (data.photoDisplayDurations && typeof data.photoDisplayDurations === 'object') {
          selectedPhotos.forEach(photo => {
            if (data.photoDisplayDurations[photo.id]) {
              photo.displayDuration = parseInt(data.photoDisplayDurations[photo.id]) || 5;
            }
          });
        }
      }
    } catch (error) {
      console.error('重新生成视频时获取照片失败:', error);
    }
  }
  
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (index === -1) {
        reject(new Error('回忆视频不存在'));
        return;
      }
      
      // 如果需要重新生成，则更新缩略图和时长
      if (data.regenerate && selectedPhotos) {
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
          thumbnailUrl: photo.thumbnailUrl || photo.url,
          displayDuration: photo.displayDuration // 包含每张照片的显示时长，不提供默认值
        }));
        
        // 记录照片顺序信息，方便调试
        console.log('更新后的记忆照片顺序:', memory.photos.map(p => p.id));
      } else {
        // 即使不重新生成视频，也要重新计算总时长
        memory.duration = memory.photos.reduce(
          (total, photo) => total + (photo.displayDuration || 5), 
          0
        );
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