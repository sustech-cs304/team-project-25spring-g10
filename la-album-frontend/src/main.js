import { createApp } from 'vue';
import router from './router';
import App from './App.vue'; 
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

// 导入全局样式
import './assets/css/global.css';
import axios from 'axios';
// 配置 axios 默认设置
axios.defaults.baseURL = 'http://localhost:8080'; // 替换成你的 API 基本地址
axios.defaults.headers['Content-Type'] = 'application/json';  // 如果你的后端需要 json 格式

// 添加请求拦截器，检查 token 并添加到请求头中
axios.interceptors.request.use(config => {
  // 获取本地存储中的 token
  const token = localStorage.getItem('token');
  
  // 如果 token 存在，添加到请求头
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
}, error => {
  return Promise.reject(error);
});







const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
