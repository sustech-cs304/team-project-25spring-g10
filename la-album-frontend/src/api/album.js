import axios from "axios";

const BASE_URL = "http://localhost:9090/api/album"; // Spring Boot 后端地址

// 获取相册列表
export const fetchAlbumList = async () => {
  try {
    const response = await axios.get(BASE_URL);
    return response.data;
  } catch (error) {
    console.error("Error fetching albums:", error);
    throw error;
  }
};
