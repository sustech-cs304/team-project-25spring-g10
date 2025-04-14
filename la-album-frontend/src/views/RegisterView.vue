<template>
  <div class="register-page">
    <div class="container">
      <div class="register-card">
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
        
        <h2 class="register-title">用户注册</h2>
        
        <!-- 注册错误提示 -->
        <div class="register-error-alert" v-if="registerError">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          {{ registerError }}
        </div>
        
        <div class="register-form">
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
                placeholder="请输入用户名 (5-16位字符)"
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
                placeholder="请输入密码 (5-16位字符)"
                @blur="validatePassword"
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
          
          <div class="form-group" :class="{ 'has-error': confirmPasswordError }">
            <label for="confirmPassword">确认密码</label>
            <div class="input-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                :type="showConfirmPassword ? 'text' : 'password'" 
                id="confirmPassword" 
                v-model="confirmPassword" 
                placeholder="请再次输入密码"
                @blur="validateConfirmPassword"
                @keyup.enter="handleRegister"
              />
              <button class="toggle-password" @click.prevent="toggleConfirmPasswordVisibility">
                <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
            </div>
            <div class="error-message" v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
          </div>
          
          <div class="terms-privacy">
            <input type="checkbox" id="agree" v-model="agreeTerms" />
            <label for="agree">我已阅读并同意 <a href="#" class="terms-link">服务条款</a> 和 <a href="#" class="privacy-link">隐私政策</a></label>
          </div>
          
          <button class="btn register-btn" :disabled="loading || !agreeTerms" @click="handleRegister">
            <span v-if="!loading">注册</span>
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
        
        <div class="register-footer">
          <p>已有账号? <router-link to="/login" class="login-link">立即登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/api/user';
import { ElMessage } from 'element-plus';

const router = useRouter();
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const usernameError = ref('');
const passwordError = ref('');
const confirmPasswordError = ref('');
const registerError = ref('');
const agreeTerms = ref(false);

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

// 验证确认密码
const validateConfirmPassword = () => {
  if (!confirmPassword.value) {
    confirmPasswordError.value = '请确认密码';
    return false;
  }
  if (confirmPassword.value !== password.value) {
    confirmPasswordError.value = '两次输入的密码不一致';
    return false;
  }
  confirmPasswordError.value = '';
  return true;
};

// 切换密码可见性
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

// 切换确认密码可见性
const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

// 处理注册
const handleRegister = async () => {
  // 验证表单
  const isUsernameValid = validateUsername();
  const isPasswordValid = validatePassword();
  const isConfirmPasswordValid = validateConfirmPassword();
  
  if (!isUsernameValid || !isPasswordValid || !isConfirmPasswordValid) {
    return;
  }
  
  if (!agreeTerms.value) {
    ElMessage.warning('请先阅读并同意服务条款和隐私政策');
    return;
  }
  
  loading.value = true;
  registerError.value = '';
  
  try {
    // 调用注册API
    const response = await register(username.value, password.value);
    console.log('注册响应:', response); // 调试输出
    
    // 检查注册是否成功
    if (response && response.code === 0) {
      ElMessage.success('注册成功，请登录');
      
      // 跳转到登录页面
      setTimeout(() => {
        router.push('/login');
      }, 1500);
    } else {
      // 注册失败，显示错误信息
      registerError.value = response && response.message ? response.message : '注册失败，请稍后再试';
    }
  } catch (error) {
    console.error('注册失败:', error);
    
    // 显示错误消息
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 400:
          registerError.value = '请求参数错误';
          break;
        case 409:
          registerError.value = '用户名已存在';
          break;
        case 500:
          registerError.value = '服务器错误，请稍后再试';
          break;
        default:
          registerError.value = '注册失败，请稍后再试';
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      registerError.value = '无法连接到服务器，请检查网络连接';
    } else {
      // 其他错误
      registerError.value = '注册过程中发生错误';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-light);
  padding: var(--space-lg) 0;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-lg);
}

.register-card {
  background-color: var(--neutral-100);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-xxl);
  max-width: 480px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background-color: var(--primary-color);
  color: white;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-weight: 600;
  font-size: 1.2rem;
  color: var(--neutral-900);
}

.register-title {
  font-size: 1.8rem;
  color: var(--neutral-900);
  margin-bottom: var(--space-lg);
  text-align: center;
}

.register-error-alert {
  background-color: var(--error-light);
  color: var(--error);
  padding: var(--space-md);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.register-form {
  margin-bottom: var(--space-xl);
}

.form-group {
  margin-bottom: var(--space-lg);
}

.form-group label {
  display: block;
  margin-bottom: var(--space-xs);
  color: var(--neutral-700);
  font-weight: 500;
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
  height: 44px;
  padding: 0 var(--space-lg) 0 calc(var(--space-lg) + 20px);
  border: 1px solid var(--neutral-300);
  border-radius: var(--radius-md);
  font-size: 1rem;
  color: var(--neutral-900);
  background-color: var(--neutral-100);
  transition: all 0.3s ease;
}

.input-wrapper input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.input-wrapper input::placeholder {
  color: var(--neutral-500);
}

.toggle-password {
  position: absolute;
  right: var(--space-sm);
  background: none;
  border: none;
  color: var(--neutral-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xs);
}

.toggle-password:hover {
  color: var(--neutral-700);
}

.form-group.has-error .input-wrapper input {
  border-color: var(--error);
  background-color: var(--error-light);
  color: var(--neutral-900);
}

.error-message {
  color: var(--error);
  font-size: 0.85rem;
  margin-top: var(--space-xs);
  font-weight: 500;
}

.terms-privacy {
  display: flex;
  align-items: flex-start;
  gap: var(--space-sm);
  margin-bottom: var(--space-lg);
}

.terms-privacy input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-top: 2px;
  accent-color: var(--primary-color);
}

.terms-privacy label {
  font-size: 0.9rem;
  color: var(--neutral-600);
  line-height: 1.4;
}

.terms-link, .privacy-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

.terms-link:hover, .privacy-link:hover {
  text-decoration: underline;
}

.btn {
  width: 100%;
  height: 48px;
  padding: var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.register-btn {
  background-color: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-btn:hover {
  background-color: var(--primary-dark);
}

.register-btn:disabled {
  background-color: var(--neutral-400);
  cursor: not-allowed;
  opacity: 0.7;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.register-footer {
  text-align: center;
  font-size: 0.9rem;
  color: var(--neutral-600);
}

.login-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 576px) {
  .register-page {
    padding: var(--space-md) 0;
  }
  
  .register-card {
    padding: var(--space-lg);
    border-radius: var(--radius-md);
  }
  
  .container {
    padding: 0 var(--space-md);
  }
  
  .register-title {
    font-size: 1.5rem;
  }
  
  .input-wrapper input {
    height: 40px;
  }
  
  .btn {
    height: 44px;
  }
}
</style> 