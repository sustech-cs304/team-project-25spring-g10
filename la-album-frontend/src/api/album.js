import request from '@/utils/request';



// 获取所有相册
export const fetchAlbumList = async () => {
  try {
    const response = await request.get('/albums');
    return response.data;
  } catch (error) {
    console.error("获取相册列表失败:", error);
    throw error;
  }
};

// 获取最近的相册（限制数量）
export const fetchRecentAlbums = async (limit = 4) => {
  try {
    const response = await request.get('/albums');
    // 获取所有相册后，按创建时间排序并限制数量
    if (response && response.data && Array.isArray(response.data)) {
      // 按创建时间降序排序
      const sortedAlbums = response.data.sort((a, b) => 
        new Date(b.createTime) - new Date(a.createTime)
      );
      // 返回指定数量的相册
      return sortedAlbums.slice(0, limit);
    }
    return [];
  } catch (error) {
    console.error("获取最近相册失败:", error);
    throw error;
  }
};

// 获取单个相册详情
export const fetchAlbumById = async (albumId) => {
  try {
    const response = await request.get(`/albums/${albumId}`);
    return response.data;
  } catch (error) {
    console.error(`获取相册 ${albumId} 详情失败:`, error);
    throw error;
  }
};

// 创建新相册
export const createAlbum = async (albumData) => {
  try {
    const response = await request.post('/albums', albumData);
    return response.data;
  } catch (error) {
    console.error("创建相册失败:", error);
    throw error;
  }
};

// 更新相册
export const updateAlbum = async (albumId, albumData) => {
  try {
    const response = await request.put(`/albums/${albumId}`, albumData);
    return response.data;
  } catch (error) {
    console.error(`更新相册 ${albumId} 失败:`, error);
    throw error;
  }
};

// 删除相册
export const deleteAlbum = async (albumId) => {
  try {
    const response = await request.delete(`/albums/${albumId}`);
    return response.data;
  } catch (error) {
    console.error(`删除相册 ${albumId} 失败:`, error);
    throw error;
  }
};

// 获取相册中的所有照片
export const fetchPhotosInAlbum = async (albumId) => {
  try {
    const response = await request.get(`/albums/${albumId}/photos`);
    return response.data;
  } catch (error) {
    console.error(`获取相册 ${albumId} 中的照片失败:`, error);
    throw error;
  }
};
