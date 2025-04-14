import axios from "axios";

const BASE_URL = "http://localhost:9090/api/album"; // Spring Boot 后端地址

// 获取相册列表
export const fetchAlbumList = async () => {
  try {
    const response = await axios.get(BASE_URL);
    return response.data;  // 返回相册列表
  } catch (error) {
    console.error("Error fetching albums:", error);
    throw error;
  }
};

// 创建新相册
export const createAlbum = async (albumData) => {
  try {
    const response = await axios.post(BASE_URL, albumData);
    return response.data;  // 返回创建的相册
  } catch (error) {
    console.error("Error creating album:", error);
    throw error;
  }
};

// 更新相册
export const updateAlbum = async (albumId, albumData) => {
  try {
    const response = await axios.put(`${BASE_URL}/${albumId}`, albumData);
    return response.data;  // 返回更新后的相册
  } catch (error) {
    console.error("Error updating album:", error);
    throw error;
  }
};

// 删除相册
export const deleteAlbum = async (albumId) => {
  try {
    const response = await axios.delete(`${BASE_URL}/${albumId}`);
    return response.data;  // 返回删除的响应，通常是空
  } catch (error) {
    console.error("Error deleting album:", error);
    throw error;
  }
};


// 获取指定 ID 的相册
export const fetchAlbumById = async (albumId) => {
  try {
    const response = await axios.get(`${BASE_URL}/${albumId}`);
    return response.data;  // 返回相册数据
  } catch (error) {
    console.error("Error fetching album by ID:", error);
    throw error;  // 抛出错误，以便上层处理
  }
};

export const fetchPhotosInAlbum = async (albumId) => {
  try {
    const response = await axios.get(`${BASE_URL}/${albumId}/photos`);
    return response.data;  // 返回相册中的所有照片
  } catch (error) {
    console.error("Error fetching photos in album:", error);
    throw error;  // 抛出错误，以便上层处理
  }
};