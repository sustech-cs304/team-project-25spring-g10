import request from '@/utils/request';

// 用户登录
export const login = async (username, password) => {
  try {
    // 创建表单数据
    const formData = new URLSearchParams();
    formData.append('username', username);
    formData.append('password', password);
    
    // 设置请求头
    const config = {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    };
    
    const response = await request.post('/users/login', formData, config);
    return response;
  } catch (error) {
    console.error("登录失败:", error);
    throw error;
  }
};

// 用户注册
export const register = async (username, password) => {
  try {
    // 创建表单数据
    const formData = new URLSearchParams();
    formData.append('username', username);
    formData.append('password', password);
    
    // 设置请求头
    const config = {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    };
    
    const response = await request.post('/users/register', formData, config);
    return response;
  } catch (error) {
    console.error("注册失败:", error);
    throw error;
  }
};

// 获取当前用户信息
export const getUserInfo = async () => {
  try {
    const response = await request.get('/users/userInfo');
    console.log("get user info response: ", response)
    return response;
  } catch (error) {
    console.error("获取用户信息失败:", error);
    throw error;
  }
};

// 用户登出
export const logout = async () => {
  try {
    const response = await request.post('/users/logout');
    
    // 清除本地存储的令牌和用户信息
    localStorage.removeItem('token');
    localStorage.removeItem('userLoggedIn');
    localStorage.removeItem('username');
    
    // 强制刷新登录状态
    window.dispatchEvent(new Event('storage'));
    
    return response;
  } catch (error) {
    console.error("登出失败:", error);
    
    // 即使API调用失败，也清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('userLoggedIn');
    localStorage.removeItem('username');
    
    throw error;
  }
};

// 更新用户信息
export const updateUserInfo = async (userData) => {
  try {
    const config = {
      headers: {'Content-Type': 'application/json'}
    };
    
    const response = await request.put('/users/update', userData, config);
    return response;
  } catch (error) {
    console.error("更新用户信息失败:", error);
    throw error;
  }
};

// 更新用户头像
export const updateAvatar = async (avatarUrl) => {
  try {
    // 创建表单数据
    const formData = new URLSearchParams();
    formData.append('avatarUrl', avatarUrl);
    
    const config = {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    };
    
    const response = await request.patch('/users/updateAvatar', formData, config);
    return response;
  } catch (error) {
    console.error("更新头像失败:", error);
    throw error;
  }
}; 