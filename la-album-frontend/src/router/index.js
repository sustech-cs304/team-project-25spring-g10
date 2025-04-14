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
  console.log('路由变化:', from.path, ' -> ', to.path);
  
  // 获取登录状态（多重检查，提高可靠性）
  const token = localStorage.getItem('token');
  const isUserLoggedIn = localStorage.getItem('userLoggedIn') === 'true';
  const isAuthenticated = token && isUserLoggedIn;
  
  // 不需要登录就可以访问的页面
  const publicPages = ['/login', '/register'];
  const authRequired = !publicPages.includes(to.path);
  
  console.log('Auth状态:', { isAuthenticated, authRequired });
  
  if (authRequired && !isAuthenticated) {
    // 如果需要登录但用户未登录，重定向到登录页面
    console.log('需要登录，重定向到登录页面');
    next('/login');
  } else {
    // 如果用户已登录且尝试访问登录页面，重定向到首页
    if (isAuthenticated && publicPages.includes(to.path)) {
      console.log('已登录用户访问登录页，重定向到首页');
      next('/');
    } else {
      // 其他情况正常导航
      console.log('正常导航');
      next();
    }
  }
});

export default router;

