<template>
  <header class="header">
    <div class="container">
      <div class="navbar">
        <div class="logo">
          <div class="logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <router-link to="/" class="logo-text">我的相册</router-link>
        </div>
        
        <nav class="nav">
          <ul class="nav-links">
            <li><router-link to="/" class="nav-link" :class="{ 'active': currentRoute === '/' }">首页</router-link></li>
            <li><router-link to="/albums" class="nav-link" :class="{ 'active': currentRoute.includes('/album') }">相册管理</router-link></li>
            <li><router-link to="/memories" class="nav-link" :class="{ 'active': currentRoute.includes('/memor') }">回忆视频</router-link></li>
            <li><router-link to="/search" class="nav-link" :class="{ 'active': currentRoute === '/search' }">搜索照片</router-link></li>
            <li><router-link to="/people" class="nav-link" :class="{ 'active': currentRoute === '/people' }">人脸识别</router-link></li>
            <li><router-link to="/catagory" class="nav-link" :class="{ 'active': currentRoute === '/catagory' }">自动分类</router-link></li>
            <li><router-link to="/trash" class="nav-link" :class="{ 'active': currentRoute === '/trash' }">回收站</router-link></li>
          </ul>
        </nav>
        
        <div class="user-actions">
          <button class="btn btn-icon" @click="toggleDarkMode">
            <svg v-if="isDarkMode" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="5"></circle>
              <line x1="12" y1="1" x2="12" y2="3"></line>
              <line x1="12" y1="21" x2="12" y2="23"></line>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
              <line x1="1" y1="12" x2="3" y2="12"></line>
              <line x1="21" y1="12" x2="23" y2="12"></line>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
          </button>
          
          <!-- 用户未登录显示登录按钮 -->
          <router-link to="/login" class="btn btn-secondary" v-if="!isLoggedIn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
              <polyline points="10 17 15 12 10 7"></polyline>
              <line x1="15" y1="12" x2="3" y2="12"></line>
            </svg>
            登录
          </router-link>
          
          <!-- 用户已登录显示用户菜单 -->
          <div class="user-dropdown" v-if="isLoggedIn">
            <button class="user-dropdown-btn" @click="toggleUserDropdown">
              <div class="user-avatar">
                {{ usernameInitial }}
              </div>
              <span class="username">{{ username }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </button>
            <div class="user-dropdown-content" v-show="userDropdownOpen">
              <a href="#" @click.prevent="navigateToProfile">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                个人信息
              </a>
              <a href="#" @click.prevent="handleLogout">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                  <polyline points="16 17 21 12 16 7"></polyline>
                  <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                退出登录
              </a>
            </div>
          </div>
          
          <div class="dropdown" v-if="isLoggedIn">
            <button class="btn btn-primary" @click="toggleDropdown">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              新建
            </button>
            <div class="dropdown-content" v-show="dropdownOpen">
              <a @click="createNewAlbum">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <circle cx="8.5" cy="8.5" r="1.5"></circle>
                  <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
                新建相册
              </a>
              <a @click="uploadPhotos">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                上传照片
              </a>
              <a @click="createNewMemory">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polygon points="23 7 16 12 23 17 23 7"></polygon>
                  <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
                </svg>
                创建回忆视频
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 移动端菜单按钮 -->
    <div class="mobile-menu-toggle" @click="toggleMobileMenu">
      <svg v-if="!mobileMenuOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="3" y1="12" x2="21" y2="12"></line>
        <line x1="3" y1="6" x2="21" y2="6"></line>
        <line x1="3" y1="18" x2="21" y2="18"></line>
      </svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </div>
    
    <!-- 移动端菜单 -->
    <div class="mobile-menu" :class="{ 'open': mobileMenuOpen }">
      <nav class="mobile-nav">
        <ul class="mobile-nav-links">
          <li><router-link to="/" class="mobile-nav-link" @click="closeMobileMenu">首页</router-link></li>
          <li><router-link to="/albums" class="mobile-nav-link" @click="closeMobileMenu">相册管理</router-link></li>
          <li><router-link to="/memories" class="mobile-nav-link" @click="closeMobileMenu">回忆视频</router-link></li>
          <li><router-link to="/search" class="mobile-nav-link" @click="closeMobileMenu">搜索照片</router-link></li>
          <li><router-link to="/people" class="mobile-nav-link" @click="closeMobileMenu">人脸识别</router-link></li>
          <li><router-link to="/catagory" class="mobile-nav-link" @click="closeMobileMenu">自动分类</router-link></li>
          <li><router-link to="/trash" class="mobile-nav-link" @click="closeMobileMenu">回收站</router-link></li>
          <li class="mobile-nav-action" @click="createNewAlbum">新建相册</li>
          <li class="mobile-nav-action" @click="uploadPhotos">上传照片</li>
          <li class="mobile-nav-action" @click="createNewMemory">创建回忆</li>
        </ul>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { logout } from '@/api/user';

const route = useRoute();
const router = useRouter();
const currentRoute = computed(() => route.path);
const mobileMenuOpen = ref(false);
const dropdownOpen = ref(false);
const userDropdownOpen = ref(false);

// 获取用户登录状态和信息
const isLoggedIn = ref(localStorage.getItem('userLoggedIn') === 'true');
const username = ref(localStorage.getItem('username') || '');
const usernameInitial = computed(() => {
  return username.value ? username.value.charAt(0).toUpperCase() : '';
});

// 监听 localStorage 变化
const checkLoginStatus = () => {
  isLoggedIn.value = localStorage.getItem('userLoggedIn') === 'true';
  username.value = localStorage.getItem('username') || '';
};

// 深色模式状态
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true');

// 切换深色模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value;
  document.body.classList.toggle('dark-mode', isDarkMode.value);
  localStorage.setItem('darkMode', isDarkMode.value);
};

// 切换下拉菜单显示状态
const toggleDropdown = (event) => {
  event.stopPropagation();
  dropdownOpen.value = !dropdownOpen.value;
  if (dropdownOpen.value) userDropdownOpen.value = false;
};

// 切换用户菜单显示状态
const toggleUserDropdown = (event) => {
  event.stopPropagation();
  userDropdownOpen.value = !userDropdownOpen.value;
  if (userDropdownOpen.value) dropdownOpen.value = false;
};

// 点击页面其他区域关闭下拉菜单
const closeDropdowns = (event) => {
  const dropdown = document.querySelector('.dropdown');
  const userDropdown = document.querySelector('.user-dropdown');
  
  if (dropdown && !dropdown.contains(event.target)) {
    dropdownOpen.value = false;
  }
  
  if (userDropdown && !userDropdown.contains(event.target)) {
    userDropdownOpen.value = false;
  }
};

// 退出登录
const handleLogout = async () => {
  try {
    // 调用登出接口
    await logout();
    // 清除本地存储并重定向到登录页
    localStorage.removeItem('token');
    localStorage.removeItem('userLoggedIn');
    localStorage.removeItem('username');
    
    // 刷新登录状态
    checkLoginStatus();
    
    // 重定向到登录页
    router.push('/login');
  } catch (error) {
    console.error('登出失败:', error);
    // 即使API调用失败，也清除本地登录状态
    localStorage.removeItem('token');
    localStorage.removeItem('userLoggedIn');
    localStorage.removeItem('username');
    
    // 刷新登录状态
    checkLoginStatus();
    
    // 重定向到登录页
    router.push('/login');
  }
};

// 导航到个人信息页面
const navigateToProfile = () => {
  // 关闭用户菜单
  userDropdownOpen.value = false;
  // 导航到个人信息页面，暂时未实现，可以根据需要添加
  console.log('导航到个人信息页面');
};

// 监听全局点击事件
onMounted(() => {
  document.addEventListener('click', closeDropdowns);
  // 页面加载时检查登录状态
  checkLoginStatus();
  
  // 创建 storage 事件监听器
  window.addEventListener('storage', checkLoginStatus);
});

// 组件销毁前移除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropdowns);
  window.removeEventListener('storage', checkLoginStatus);
});

// 切换移动端菜单
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value;
  if (mobileMenuOpen.value) {
    document.body.classList.add('menu-open');
  } else {
    document.body.classList.remove('menu-open');
  }
};

// 关闭移动端菜单
const closeMobileMenu = () => {
  mobileMenuOpen.value = false;
  document.body.classList.remove('menu-open');
};

// 创建新相册
const createNewAlbum = () => {
  closeMobileMenu();
  dropdownOpen.value = false;
  // 导航到相册列表页并打开创建模式
  router.push({ 
    path: '/albums',
    query: { createNew: 'true' }
  });
};

// 上传照片
const uploadPhotos = () => {
  closeMobileMenu();
  dropdownOpen.value = false;
  // 导航到上传页面
  router.push({ name: 'PhotoUpload' });
};

// 创建新回忆视频
const createNewMemory = () => {
  closeMobileMenu();
  dropdownOpen.value = false;
  // 导航到回忆列表页面并触发创建模式
  router.push({ 
    path: '/memories',
    query: { createNew: 'true' }
  });
};

// 初始应用深色模式
if (isDarkMode.value) {
  document.body.classList.add('dark-mode');
}
</script>

<style scoped>
.header {
  background-color: var(--neutral-100);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md) 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-weight: 700;
  font-size: 1.5rem;
  text-decoration: none;
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
  color: var(--neutral-900);
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: var(--space-lg);
  list-style: none;
}

.nav-link {
  font-weight: 500;
  color: var(--neutral-700);
  transition: color 0.2s;
  position: relative;
  text-decoration: none;
}

.nav-link::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: var(--primary-color);
  transition: width 0.3s;
}

.nav-link:hover,
.nav-link.active {
  color: var(--primary-color);
}

.nav-link:hover::after,
.nav-link.active::after {
  width: 100%;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.btn-icon {
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  position: absolute;
  right: 0;
  min-width: 160px;
  background-color: white;
  box-shadow: var(--shadow-md);
  border-radius: var(--radius-md);
  z-index: 10;
  margin-top: var(--space-xs);
  padding: var(--space-xs) 0;
}

.dropdown-content a {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  color: var(--neutral-800);
  cursor: pointer;
  text-decoration: none;
}

/* 移动端菜单样式 */
.mobile-menu-toggle {
  display: none;
  position: fixed;
  top: 15px;
  right: 15px;
  z-index: 1001;
  background-color: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
  cursor: pointer;
}

.mobile-menu {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background-color: white;
  z-index: 1000;
  transform: translateX(100%);
  transition: transform 0.3s ease;
}

.mobile-menu.open {
  transform: translateX(0);
}

.mobile-nav {
  padding: var(--space-xxl) var(--space-lg);
}

.mobile-nav-links {
  list-style: none;
}

.mobile-nav-link {
  display: block;
  padding: var(--space-md) 0;
  font-size: 1.2rem;
  font-weight: 500;
  color: var(--neutral-800);
  text-decoration: none;
  border-bottom: 1px solid var(--neutral-200);
}

.mobile-nav-action {
  display: block;
  padding: var(--space-md) 0;
  font-size: 1.2rem;
  font-weight: 500;
  color: var(--primary-color);
  cursor: pointer;
  border-bottom: 1px solid var(--neutral-200);
}

@media (max-width: 768px) {
  .navbar {
    padding: var(--space-md) 0;
  }
  
  .nav {
    display: none;
  }
  
  .user-actions {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: flex;
  }
  
  .mobile-menu {
    display: block;
  }
  
  body.menu-open {
    overflow: hidden;
  }
}

/* 用户下拉菜单 */
.user-dropdown {
  position: relative;
  margin-right: var(--space-md);
}

.user-dropdown-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  background: none;
  border: none;
  color: var(--neutral-800);
  font-weight: 500;
  cursor: pointer;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.user-dropdown-btn:hover {
  background-color: var(--neutral-200);
}

.user-avatar {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  font-weight: 600;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown-content {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: var(--neutral-100);
  box-shadow: var(--shadow-md);
  border-radius: var(--radius-md);
  min-width: 150px;
  z-index: 1;
  margin-top: var(--space-xs);
  overflow: hidden;
}

.user-dropdown-content a {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  color: var(--neutral-800);
  text-decoration: none;
  transition: all 0.2s;
}

.user-dropdown-content a:hover {
  background-color: var(--neutral-200);
}

/* 深色模式适配 */
.dark-mode .user-dropdown-btn {
  color: var(--neutral-700);
}

.dark-mode .user-dropdown-btn:hover {
  background-color: var(--neutral-200);
}

.dark-mode .user-dropdown-content {
  background-color: var(--neutral-100);
}

.dark-mode .user-dropdown-content a {
  color: var(--neutral-700);
}

.dark-mode .user-dropdown-content a:hover {
  background-color: var(--neutral-200);
}
</style> 