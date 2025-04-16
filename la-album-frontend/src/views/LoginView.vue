<template>
  <div class="login-page">
    <div class="container">
      <div class="login-card">
        <div class="logo">
          <div class="logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <span class="logo-text">我的相册</span>
        </div>
        
        <h2 class="login-title">用户登录</h2>
        
        <!-- 登录错误提示 -->
        <div class="login-error-alert" v-if="loginError">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          {{ loginError }}
        </div>
        
        <div class="login-form">
          <div class="form-group" :class="{ 'has-error': usernameError }">
            <label for="username">用户名</label>
            <div class="input-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                type="text" 
                id="username" 
                v-model="username" 
                placeholder="请输入用户名"
                @blur="validateUsername"
              />
            </div>
            <div class="error-message" v-if="usernameError">{{ usernameError }}</div>
          </div>
          
          <div class="form-group" :class="{ 'has-error': passwordError }">
            <label for="password">密码</label>
            <div class="input-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                :type="showPassword ? 'text' : 'password'" 
                id="password" 
                v-model="password" 
                placeholder="请输入密码"
                @blur="validatePassword"
                @keyup.enter="handleLogin"
              />
              <button class="toggle-password" @click.prevent="togglePasswordVisibility">
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
            </div>
            <div class="error-message" v-if="passwordError">{{ passwordError }}</div>
          </div>
          
          <div class="remember-forgot">
            <div class="remember-me">
              <input type="checkbox" id="remember" v-model="rememberMe" />
              <label for="remember">记住我</label>
            </div>
            <a href="#" class="forgot-password">忘记密码?</a>
          </div>
          
          <button class="btn login-btn" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登录</span>
            <span v-else class="loading-spinner">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="spin">
                <line x1="12" y1="2" x2="12" y2="6"></line>
                <line x1="12" y1="18" x2="12" y2="22"></line>
                <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                <line x1="2" y1="12" x2="6" y2="12"></line>
                <line x1="18" y1="12" x2="22" y2="12"></line>
                <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
              </svg>
            </span>
          </button>
        </div>
        
        <div class="login-footer">
          <p>还没有账号? <router-link to="/register" class="register-link">立即注册</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/user';

const router = useRouter();
const username = ref('');
const password = ref('');
const rememberMe = ref(false);
const showPassword = ref(false);
const loading = ref(false);
const usernameError = ref('');
const passwordError = ref('');
const loginError = ref('');

// 验证用户名
const validateUsername = () => {
  if (!username.value) {
    usernameError.value = '请输入用户名';
    return false;
  }
  if (username.value.length < 5 || username.value.length > 16) {
    usernameError.value = '用户名必须是5-16位非空字符';
    return false;
  }
  usernameError.value = '';
  return true;
};

// 验证密码
const validatePassword = () => {
  if (!password.value) {
    passwordError.value = '请输入密码';
    return false;
  }
  if (password.value.length < 5 || password.value.length > 16) {
    passwordError.value = '密码必须是5-16位非空字符';
    return false;
  }
  passwordError.value = '';
  return true;
};

// 切换密码可见性
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

// 处理登录
const handleLogin = async () => {
  // 验证表单
  const isUsernameValid = validateUsername();
  const isPasswordValid = validatePassword();
  
  if (!isUsernameValid || !isPasswordValid) {
    return;
  }
  
  loading.value = true;
  loginError.value = '';
  
  try {
    // 调用登录API
    const response = await login(username.value, password.value);
    console.log('登录响应:', response);
    
    // 检查登录是否成功
    if (response && response.code === 0) {
      // 确保token正确格式化并存储
      const token = response.data;
      
      // 存储用户令牌到本地存储
      localStorage.setItem('token', token);
      
      // 如果勾选了"记住我"，可以设置更长的过期时间或其他标记
      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true');
      }
      
      console.log('登录成功，准备跳转到首页');
      
      // 使用 router.push 进行导航
      try {
        await router.push({ path: '/' });
        console.log('导航到首页成功');
      } catch (navigationError) {
        console.error('导航失败:', navigationError);
        // 如果导航失败，使用 window.location
        window.location.href = '/';
      }
    } else {
      // 登录失败，显示错误信息
      loginError.value = response && response.message ? response.message : '登录失败，请检查用户名和密码';
    }
  } catch (error) {
    console.error('登录失败:', error);
    
    // 显示错误消息
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 400:
          loginError.value = '请求参数错误';
          break;
        case 401:
          loginError.value = '用户名或密码错误';
          break;
        case 500:
          loginError.value = '服务器错误，请稍后再试';
          break;
        default:
          loginError.value = '登录失败，请稍后再试';
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      loginError.value = '无法连接到服务器，请检查网络连接';
    } else {
      // 其他错误
      loginError.value = '登录过程中发生错误';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--neutral-200);
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: var(--space-xl);
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background-color: var(--primary-color);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--neutral-900);
}

.login-title {
  text-align: center;
  margin-bottom: var(--space-xl);
  color: var(--neutral-900);
}

.login-form {
  margin-bottom: var(--space-lg);
}

.form-group {
  margin-bottom: var(--space-md);
}

.form-group.has-error input {
  border-color: var(--error);
}

.form-group label {
  display: block;
  margin-bottom: var(--space-xs);
  font-weight: 500;
  color: var(--neutral-800);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper svg {
  position: absolute;
  left: var(--space-sm);
  color: var(--neutral-500);
}

.input-wrapper input {
  width: 100%;
  padding: var(--space-md);
  padding-left: calc(var(--space-sm) * 4);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md);
  font-size: 1rem;
  background-color: var(--neutral-100);
  color: var(--neutral-900);
  transition: all 0.2s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.toggle-password {
  position: absolute;
  right: var(--space-sm);
  background: transparent;
  border: none;
  color: var(--neutral-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.toggle-password:hover {
  color: var(--neutral-700);
}

.error-message {
  color: var(--error);
  font-size: 0.85rem;
  margin-top: var(--space-xs);
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.remember-me input[type="checkbox"] {
  accent-color: var(--primary-color);
}

.forgot-password {
  color: var(--primary-color);
  font-size: 0.9rem;
}

.login-btn {
  width: 100%;
  padding: var(--space-md);
  font-size: 1rem;
  background-color: var(--primary-color);
  border: none;
}

.login-btn:disabled {
  background-color: var(--neutral-400);
  cursor: not-allowed;
}

.loading-spinner svg {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-footer {
  text-align: center;
  color: var(--neutral-700);
}

.register-link {
  color: var(--primary-color);
  font-weight: 500;
}

/* 深色模式适配 */
.dark-mode .login-card {
  background-color: var(--neutral-100);
}

.dark-mode .input-wrapper input {
  background-color: var(--neutral-200);
  border-color: var(--neutral-400);
  color: var(--neutral-800);
}

.dark-mode .login-title,
.dark-mode .logo-text,
.dark-mode .form-group label {
  color: var(--neutral-800);
}

/* 添加登录错误提示样式 */
.login-error-alert {
  background-color: rgba(248, 113, 113, 0.1);
  border-left: 3px solid var(--error);
  padding: var(--space-md);
  margin-bottom: var(--space-lg);
  color: var(--error);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 0.9rem;
}

.login-error-alert svg {
  flex-shrink: 0;
}

/* 深色模式适配 */
.dark-mode .login-error-alert {
  background-color: rgba(248, 113, 113, 0.05);
}
</style> 