import axios from "axios";
import request from '@/utils/request';
// import {getUserInfo} from "@/api/user";

const BASE_URL = "http://localhost:9090/api/photos"; 

export const getPhotoById = async (id) => {
  try {
    const response = await request.get(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching photo:", error);
    throw error;
  }
};

// export const getDeletedPhotos = async () => {
//   try {
//       const response = await request.get(`${BASE_URL}/deleted`);
//       return response.data;
//   } catch (error) {
//       console.error("Error fetching deleted photos:", error);
//       return [];
//   }
// };
// 上传照片到指定相册
export const uploadPhoto = async (file, albumId, options = {}) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('albumId', albumId);
      
      // 添加额外选项
      if (options.autoRename !== undefined) {
        formData.append('autoRename', options.autoRename);
      }
      
      if (options.preserveExif !== undefined) {
        formData.append('preserveExif', options.preserveExif);
      }
      
      const config = {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': localStorage.getItem('token')
        },
        timeout: 30000 // 增加超时时间到30秒
      };
      
      // 只有在提供了onProgress回调时才添加onUploadProgress
      if (typeof options.onProgress === 'function') {
        config.onUploadProgress = (progressEvent) => {
          // 确保progressEvent包含必要信息
          const total = progressEvent.total || 0;
          const loaded = progressEvent.loaded || 0;
          
          // 避免除以零错误
          if (total > 0) {
            const percentCompleted = Math.round((loaded * 100) / total);
            options.onProgress({
              total,
              loaded,
              percent: percentCompleted
            });
          }
        };
      }
      
      const response = await axios.post(`${BASE_URL}/upload`, formData, config);
      return response.data;
    } catch (error) {
      console.error("上传照片失败:", error);
      
      // 返回标准化的错误响应，而不是抛出异常
      return {
        code: -1,
        message: error.response ? 
          `服务器错误: ${error.response.status}` : 
          (error.request ? "无法连接到服务器" : "请求发送失败"),
        data: null
      };
    }
  };
  
  // 模拟上传照片（用于测试进度条和API失败时的备选方案）
  export const mockUploadPhoto = async (file, albumId, options = {}) => {
    return new Promise((resolve) => {
      // 模拟网络延迟（随机1-2秒）
      const networkDelay = Math.floor(Math.random() * 1000) + 1000;
      setTimeout(() => {
        // 模拟上传总时间（3-5秒）
        const totalTime = Math.floor(Math.random() * 2000) + 3000;
        // 更新间隔（100毫秒）
        const interval = 100;
        // 总步数
        const steps = totalTime / interval;
        // 当前步数
        let currentStep = 0;
        
        console.log(`模拟上传开始: 文件=${file.name}, 大小=${file.size}字节, 相册ID=${albumId}`);
        
        // 模拟进度更新
        const progressInterval = setInterval(() => {
          currentStep++;
          
          // 进度计算：前90%缓慢增加，后10%快速增加
          let progressPercent;
          if (currentStep <= steps * 0.9) {
            // 前90%的时间，完成90%的进度
            progressPercent = Math.min(90, Math.floor((currentStep / (steps * 0.9)) * 90));
          } else {
            // 后10%的时间，完成剩余10%的进度
            const remainingSteps = steps - (steps * 0.9);
            const remainingProgress = currentStep - (steps * 0.9);
            progressPercent = Math.min(100, 90 + Math.floor((remainingProgress / remainingSteps) * 10));
          }
          
          // 调用进度回调
          if (typeof options.onProgress === 'function') {
            options.onProgress({
              loaded: progressPercent,
              total: 100,
              percent: progressPercent
            });
          }
          
          // 完成上传
          if (currentStep >= steps || progressPercent >= 100) {
            clearInterval(progressInterval);
            console.log(`模拟上传完成: 文件=${file.name}`);
            
            // 延迟返回成功响应
            setTimeout(() => {
              // 模拟文件URL
              const objectUrl = URL.createObjectURL(file);
              
              resolve({
                code: 0,
                message: '上传成功',
                data: {
                  id: Math.floor(Math.random() * 10000) + 1,
                  albumId: albumId,
                  filename: file.name,
                  url: objectUrl,
                  thumbnailUrl: objectUrl,
                  size: file.size,
                  type: file.type,
                  createTime: new Date().toISOString()
                }
              });
            }, 500);
          }
        }, interval);
      }, networkDelay);
    });
  };


//copy照片到其他相册
export const copyPhotoToAlbum = async (photoId, newAlbumId) => {
    try {
      const config = {
        headers: {
          'Authorization': localStorage.getItem('token'),
           'Content-Type': 'application/x-www-form-urlencoded'
        },
        timeout: 15000
      };
  
      const params = new URLSearchParams();
      params.append('photoId', photoId);
      params.append('newAlbumId', newAlbumId);
  
      const response = await axios.post(`${BASE_URL}/copy`, params, config);
      return response.data;
    } catch (error) {
      console.error("复制照片失败:", error);
      return {
        code: -1,
        message: error.response ?
          `服务器错误: ${error.response.status}` :
          (error.request ? "无法连接到服务器" : "请求发送失败"),
        data: null
      };
    }
  };

// 更新照片信息
export const updatePhoto = async (id, photoData) => {
    try {
      const config = {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': localStorage.getItem('token')
        },
        timeout: 15000
      };
  
      const response = await axios.put(`${BASE_URL}/${id}`, photoData, config);
      return response.data;
    } catch (error) {
      console.error("更新照片失败:", error);
      return {
        code: -1,
        message: error.response ? 
          `服务器错误: ${error.response.status}` : 
          (error.request ? "无法连接到服务器" : "请求发送失败"),
        data: null
      };
    }
  };
  
  export const updatePhotoDescriptionAndTags = async (id, description, tags) => {
    try {
      const config = {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': localStorage.getItem('token')
        }
      };
  
      const data = {};
      if (description !== undefined) data.description = description;
      if (tags !== undefined) data.tags = tags;
  
      const response = await axios.put(`${BASE_URL}/${id}`, data, config);
      return response.data;
    } catch (error) {
      console.error("更新照片失败:", error);
      return {
        code: -1,
        message: error.response ? `服务器错误: ${error.response.status}` : "请求发送失败",
        data: null
      };
    }
  };

// 删除指定ID的照片
export const deletePhoto = async (id) => {
  try {
    const response = await request.delete(`${BASE_URL}/${id}`);
    return {
      code: 0,
      message: '删除成功',
      data: response.data
    };
  } catch (error) {
    console.error("删除照片失败:", error);
    return {
      code: -1,
      message: error.response ? `服务器错误: ${error.response.status}` : '请求失败',
      data: null
    };
  }
};


export const movePhoto = async (photoId, destAlbumId) => {
  try {
    const response = await request.put(`${BASE_URL}/${photoId}/move`, {
      id: destAlbumId  // 后端只需要相册ID
    });

    return {
      code: 0,
      message: '移动成功',
      data: response.data
    };
  } catch (error) {
    console.error("移动照片失败:", error);
    return {
      code: -1,
      message: error.response ? `服务器错误: ${error.response.status}` : '请求失败',
      data: null
    };
  }
};

