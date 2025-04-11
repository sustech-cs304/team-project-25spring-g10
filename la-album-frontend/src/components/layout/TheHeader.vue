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
            <li><router-link to="/search" class="nav-link" :class="{ 'active': currentRoute === '/search' }">搜索照片</router-link></li>
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
          
          <div class="dropdown">
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
          <li><router-link to="/search" class="mobile-nav-link" @click="closeMobileMenu">搜索照片</router-link></li>
          <li><router-link to="/trash" class="mobile-nav-link" @click="closeMobileMenu">回收站</router-link></li>
          <li class="mobile-nav-action" @click="createNewAlbum">新建相册</li>
          <li class="mobile-nav-action" @click="uploadPhotos">上传照片</li>
        </ul>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const currentRoute = computed(() => route.path);
const mobileMenuOpen = ref(false);
const dropdownOpen = ref(false);

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
};

// 点击页面其他区域关闭下拉菜单
const closeDropdown = (event) => {
  const dropdown = document.querySelector('.dropdown');
  if (dropdown && !dropdown.contains(event.target)) {
    dropdownOpen.value = false;
  }
};

// 监听全局点击事件
onMounted(() => {
  document.addEventListener('click', closeDropdown);
});

// 组件销毁前移除事件监听
onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropdown);
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
</style> 