import axios from "axios";

const BASE_URL = "http://localhost:9090/api/photo"; 

export const getPhotoById = async (id) => {
  try {
    const response = await axios.get(`${BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching photo:", error);
    throw error;
  }
};
export const getDeletedPhotos = async () => {
  try {
      const response = await axios.get(`${BASE_URL}/deleted`);
      return response.data;
  } catch (error) {
      console.error("Error fetching deleted photos:", error);
      return [];
  }
};
