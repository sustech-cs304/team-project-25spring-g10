<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-logo">
          <div class="logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
          </div>
          <span class="logo-text">相册</span>
        </div>
        
        <div class="footer-links">
          <ul class="footer-nav">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/albums">相册</router-link></li>
            <li><router-link to="/search">搜索</router-link></li>
            <li><router-link to="/trash">回收站</router-link></li>
          </ul>
        </div>
        
        <div class="copyright">
          <p>&copy; {{ currentYear }} 相册应用. 保留所有权利.</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue';

const currentYear = computed(() => new Date().getFullYear());
</script>

<style scoped>
.footer {
  background-color: var(--neutral-100);
  padding: var(--space-lg) 0;
  border-top: 1px solid var(--neutral-300);
  margin-top: auto;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-weight: 700;
  font-size: 1.2rem;
}

.logo-icon {
  width: 24px;
  height: 24px;
  background-color: var(--primary-color);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-text {
  color: var(--neutral-900);
}

.footer-nav {
  display: flex;
  gap: var(--space-lg);
  list-style: none;
  margin: var(--space-sm) 0;
}

.footer-nav a {
  color: var(--neutral-700);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-nav a:hover {
  color: var(--primary-color);
}

.copyright {
  color: var(--neutral-600);
  font-size: 0.875rem;
  text-align: center;
}

@media (max-width: 768px) {
  .footer-nav {
    flex-direction: column;
    align-items: center;
    gap: var(--space-md);
  }
}
</style> 