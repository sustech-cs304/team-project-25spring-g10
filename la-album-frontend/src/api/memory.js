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
    throw error;
  }
};

// 获取记忆视频详情
export const getMemoryById = async (id) => {
  try {
    const response = await request.get(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error(`获取记忆视频 ${id} 详情失败:`, error);
    throw error;
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
    throw error;
  }
};

// 更新记忆视频
export const updateMemory = async (id, data) => {
  try {
    const response = await request.put(`${BASE_URL}/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`更新记忆视频 ${id} 失败:`, error);
    throw error;
  }
};

// 删除记忆视频
export const deleteMemory = async (id) => {
  try {
    const response = await request.delete(`${BASE_URL}/${id}`);
    return response;
  } catch (error) {
    console.error(`删除记忆视频 ${id} 失败:`, error);
    throw error;
  }
};

// 分享记忆视频
export const shareMemory = async (id) => {
  try {
    // 目前分享功能不需要实际API调用，只是生成一个前端URL
    // 如果后续需要实现真正的分享功能，可以添加相应的API调用
    const shareLink = `${window.location.origin}/memory/share/${id}`;
    return { shareLink };
  } catch (error) {
    console.error(`分享记忆视频 ${id} 失败:`, error);
    throw error;
  }
};

// 获取背景音乐列表
export const fetchBgMusic = async () => {
  // 暂时使用mockMemory中的音乐数据
  // 未来可以替换为实际的API调用
  return mockFetchBgMusic();
}; 