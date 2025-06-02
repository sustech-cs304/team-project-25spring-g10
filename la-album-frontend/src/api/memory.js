import request from '@/utils/request';
// 临时导入模拟音乐数据，后续可以替换为实际API
import { fetchBgMusic as mockFetchBgMusic } from './mockMemory';

const BASE_URL = '/memory';

// 获取所有记忆视频
export const fetchMemories = async () => {
  try {
    const response = await request.get(BASE_URL);
    return response.data || [];
  } catch (error) {
    console.error('获取记忆视频列表失败:', error);
    if (error.response) {
      // 服务器返回错误状态码
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      // 请求发出但没有收到响应
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      // 请求配置出错
      throw new Error('请求配置错误: ' + error.message);
    }
  }
};

// 获取记忆视频详情
export const getMemoryById = async (id) => {
  try {
    const response = await request.get(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error(`获取记忆视频 ${id} 详情失败:`, error);
    if (error.response) {
      if (error.response.status === 404) {
        throw new Error('未找到该记忆视频');
      }
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      throw new Error('请求配置错误: ' + error.message);
    }
  }
};

// 生成记忆视频
export const generateMemory = async (data) => {
  try {
    const response = await request.post(`${BASE_URL}/generate`, data);
    console.log('生成记忆视频响应:', response);
    return response.data;
  } catch (error) {
    console.error('生成记忆视频失败:', error);
    if (error.response) {
      if (error.response.status === 400) {
        throw new Error('请求数据无效: ' + (error.response.data?.message || '请检查输入数据'));
      }
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      throw new Error('请求配置错误: ' + error.message);
    }
  }
};

// 更新记忆视频
export const updateMemory = async (id, data) => {
  try {
    const response = await request.put(`${BASE_URL}/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`更新记忆视频 ${id} 失败:`, error);
    if (error.response) {
      if (error.response.status === 404) {
        throw new Error('未找到该记忆视频');
      } else if (error.response.status === 400) {
        throw new Error('请求数据无效: ' + (error.response.data?.message || '请检查输入数据'));
      }
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      throw new Error('请求配置错误: ' + error.message);
    }
  }
};

// 删除记忆视频
export const deleteMemory = async (id) => {
  try {
    const response = await request.delete(`${BASE_URL}/${id}`);
    return response;
  } catch (error) {
    console.error(`删除记忆视频 ${id} 失败:`, error);
    if (error.response) {
      if (error.response.status === 404) {
        throw new Error('未找到该记忆视频');
      }
      throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      throw new Error('请求配置错误: ' + error.message);
    }
  }
};

// 分享记忆视频
export const shareMemory = async (id) => {
  try {
    if (!window.location.origin) {
      throw new Error('无法获取当前域名');
    }
    // 目前分享功能不需要实际API调用，只是生成一个前端URL
    // 如果后续需要实现真正的分享功能，可以添加相应的API调用
    const shareLink = `${window.location.origin}/memory/share/${id}`;
    return { shareLink };
  } catch (error) {
    console.error(`分享记忆视频 ${id} 失败:`, error);
    throw new Error('生成分享链接失败: ' + error.message);
  }
};

// 获取背景音乐列表
export const fetchBgMusic = async () => {
  try {
    // 暂时使用mockMemory中的音乐数据
    // 未来可以替换为实际的API调用
    return await mockFetchBgMusic();
  } catch (error) {
    console.error('获取背景音乐失败:', error);
    throw new Error('获取背景音乐失败: ' + error.message);
  }
}; 