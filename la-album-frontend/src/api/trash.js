import request from '@/utils/request';

const BASE_URL = "http://localhost:9090/api/trash";

// 获取用户回收站中的所有照片
export const getTrashPhotos = async () => {
  try {
    const response = await request.get(`${BASE_URL}`);
    return {
      code: 0,
      message: '获取成功',
      data: response.data
    };
  } catch (error) {
    console.error("获取回收站照片失败:", error);
    return {
      code: -1,
      message: error.response ? `服务器错误: ${error.response.status}` : '请求失败',
      data: []
    };
  }
};

// 恢复一张照片
export const restorePhoto = async (photoId) => {
  try {
    const response = await request.post(`${BASE_URL}/restore/${photoId}`);
    return {
      code: 0,
      message: '恢复成功',
      data: response.data
    };
  } catch (error) {
    console.error("恢复照片失败:", error);
    return {
      code: -1,
      message: error.response ? `服务器错误: ${error.response.status}` : '请求失败',
      data: null
    };
  }
};

// 永久删除一张照片
export const deleteFromTrash = async (photoId) => {
  try {
    const response = await request.delete(`${BASE_URL}/${photoId}`);
    return {
      code: 0,
      message: '删除成功',
      data: response.data
    };
  } catch (error) {
    console.error("永久删除照片失败:", error);
    return {
      code: -1,
      message: error.response ? `服务器错误: ${error.response.status}` : '请求失败',
      data: null
    };
  }
};
