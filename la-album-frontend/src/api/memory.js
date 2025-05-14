import axios from "axios";

const BASE_URL = "http://localhost:9090/api/memory"; 

// 根据相册ID生成回忆视频
export const generateMemory = async (data) => {
  try {
    const response = await axios.post(`${BASE_URL}/generate`, data);
    return response.data;
  } catch (error) {
    console.error("Error generating memory:", error);
    throw error;
  }
};

// 获取用户所有回忆视频
export const fetchMemories = async () => {
  try {
    const response = await axios.get(`${BASE_URL}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching memories:", error);
    throw error;
  }
};

// 获取单个回忆视频详情
export const getMemoryById = async (id) => {
  try {
    const response = await axios.get(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching memory:", error);
    throw error;
  }
};

// 分享回忆视频
export const shareMemory = async (id, shareData) => {
  try {
    const response = await axios.post(`${BASE_URL}/${id}/share`, shareData);
    return response.data;
  } catch (error) {
    console.error("Error sharing memory:", error);
    throw error;
  }
};

// 删除回忆视频
export const deleteMemory = async (id) => {
  try {
    const response = await axios.delete(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting memory:", error);
    throw error;
  }
};

// 更新回忆视频信息
export const updateMemory = async (id, data) => {
  try {
    const response = await axios.put(`${BASE_URL}/${id}`, data);
    return response.data;
  } catch (error) {
    console.error("Error updating memory:", error);
    throw error;
  }
}; 