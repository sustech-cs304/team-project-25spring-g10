import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:9090/api', // API基础URL
  timeout: 15000,                      // 请求超时时间
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果存在token，则添加到请求头中
    if (token) {
      // 直接使用token，不添加Bearer前缀（后端会处理）
      config.headers['Authorization'] = token;
      
      // 添加调试日志
      console.log('Request Config:', {
        url: config.url,
        method: config.method,
        headers: config.headers
      });
    }
    
    return config;
  },
  error => {
    console.error('请求错误', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
// request.interceptors.response.use(
//   response => {
//     const res = response.data;
//
//     // 添加调试日志
//     console.log('Response Data:', {
//       url: response.config.url,
//       status: response.status,
//       data: res
//     });
//
//     if (!('code' in response.data)) {
//       return res;
//     }
//
//     // 判断响应状态码
//     if (res.code === 0) {
//       // 请求成功
//       console.log("success");
//       return res;
//     } else {
//       // 处理业务错误
//       if (res.code === 401) {
//         // token过期或无效
//         console.log('Token expired or invalid');
//
//         // 只清除token
//         localStorage.removeItem('token');
//
//         // 如果不是登录页面，才跳转
//         if (!window.location.pathname.includes('/login')) {
//           router.replace('/login');
//         }
//       } else {
//         // 其他业务错误
//         ElMessage.error(res.message || '请求失败');
//       }
//
//       return Promise.reject(new Error(res.message || '请求失败'));
//     }
//   },
//   error => {
//     // 处理HTTP错误
//     if (error.response) {
//       // 有响应但状态码不正确
//       switch (error.response.status) {
//         case 401:
//           ElMessage.error('登录已过期，请重新登录');
//
//           // 只清除token
//           localStorage.removeItem('token');
//
//           // 如果不是登录页面，才跳转
//           if (!window.location.pathname.includes('/login')) {
//             router.replace('/login');
//           }
//           break;
//
//         case 403:
//           ElMessage.error('没有权限访问此资源');
//           break;
//
//         case 404:
//           ElMessage.error('请求的资源不存在');
//           break;
//
//         case 500:
//           ElMessage.error('服务器错误，请稍后再试');
//           break;
//
//         default:
//           ElMessage.error('请求失败，请稍后再试');
//       }
//     } else if (error.request) {
//       // 请求已发送但没有收到响应
//       ElMessage.error('无法连接到服务器，请检查网络');
//     } else {
//       // 请求配置发生错误
//       ElMessage.error('请求错误');
//     }
//
//     return Promise.reject(error);
//   }
// );

request.interceptors.response.use(
    response => {
      const res = response.data;

      // 打印调试日志
      console.log('Response Data:', {
        url: response.config.url,
        status: response.status,
        data: res
      });

      // 如果没有 code 字段，直接返回原始数据（可能是文件下载或其他非业务接口）
      if (!res || typeof res !== 'object' || !('code' in res)) {
        return res;
      }

      if (res.code === 0) {
        return res;
      }

      // 处理业务错误
      if (res.code === 401) {
        ElMessage.error('登录状态已失效，请重新登录');
        localStorage.removeItem('token');
        if (!window.location.pathname.includes('/login')) {
          router.replace('/login');
        }
      } else {
        ElMessage.error(res.message || '请求失败');
      }

      return Promise.reject(new Error(res.message || '请求失败'));
    },

    error => {
      // HTTP 状态码错误处理
      const status = error.response?.status;

      if (status) {
        switch (status) {
          case 401:
            ElMessage.error('未授权访问，请重新登录');
            localStorage.removeItem('token');
            if (!window.location.pathname.includes('/login')) {
              router.replace('/login');
            }
            break;
          case 403:
            ElMessage.error('没有权限访问该资源');
            break;
          case 404:
            ElMessage.error('接口不存在');
            break;
          case 500:
            ElMessage.error('服务器内部错误');
            break;
          default:
            ElMessage.error(`请求失败（${status}）`);
        }
      } else if (error.request) {
        ElMessage.error('无法连接服务器，请检查网络');
      } else {
        ElMessage.error('请求配置错误');
      }

      return Promise.reject(error);
    }
);


export default request; 