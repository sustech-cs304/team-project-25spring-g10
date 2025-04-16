import { createRouter, createWebHistory } from 'vue-router';

import HomeView from '../views/HomeView.vue';
import AlbumListView from '../views/AlbumListView.vue';
import AlbumView from '../views/AlbumView.vue';
import PhotoView from '../views/PhotoView.vue';
import SearchView from '../views/SearchView.vue';
import EditPhoto from '../views/EditPhoto.vue';
import SharePhoto from '../views/SharePhoto.vue';
import TrashBin from '../views/TrashBin.vue';
import PhotoUploadEdit from '../views/PhotoUploadEdit.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/albums', name: 'AlbumList', component: AlbumListView },
  { path: '/album/:id', name: 'Album', component: AlbumView, props: true },
  { path: '/photo/:id', name: 'Photo', component: PhotoView, props: true },
  { path: '/search', name: 'Search', component: SearchView },
  { path: '/edit/:id', name: 'EditPhoto', component: EditPhoto, props: true },
  { path: '/share/:id', name: 'SharePhoto', component: SharePhoto, props: true },
  { path: '/trash', name: 'TrashBin', component: TrashBin },
  { path: '/upload', name: 'PhotoUpload', component: PhotoUploadEdit },
  { path: '/login', name: 'Login', component: LoginView },
  { path: '/register', name: 'Register', component: RegisterView },
];

const router = createRouter({
  history: createWebHistory(), // 确保使用 history 模式
  routes,
});

// 全局导航守卫，检查用户是否登录
router.beforeEach((to, from, next) => {
  console.log('Navigation:', {
    from: from.path,
    to: to.path,
    timestamp: new Date().toISOString()
  });
  
  // 获取token
  const token = localStorage.getItem('token');
  
  // 验证token基本格式
  const isValidToken = token && token.length > 20;
  
  // 不需要登录就可以访问的页面
  const publicPages = ['/login', '/register'];
  const authRequired = !publicPages.includes(to.path);
  
  console.log('Auth Check:', {
    token: token ? '存在' : '不存在',
    isValidToken,
    authRequired,
    path: to.path
  });
  
  if (authRequired && !isValidToken) {
    // 如果需要登录但没有有效token，重定向到登录页
    next('/login');
  } else {
    next();
  }
});

export default router;

