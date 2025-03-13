import { createRouter, createWebHistory } from 'vue-router';

import HomeView from '../views/HomeView.vue';
import AlbumView from '../views/AlbumView.vue';
import SearchView from '../views/SearchView.vue';
import EditPhoto from '../views/EditPhoto.vue';
import SharePhoto from '../views/SharePhoto.vue';
import TrashBin from '../views/TrashBin.vue';

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/album', name: 'Album', component: AlbumView },
  { path: '/search', name: 'Search', component: SearchView },
  { path: '/edit/:id', name: 'EditPhoto', component: EditPhoto, props: true },
  { path: '/share/:id', name: 'SharePhoto', component: SharePhoto, props: true },
  { path: '/trash', name: 'TrashBin', component: TrashBin },
];

const router = createRouter({
  history: createWebHistory(), // 确保使用 history 模式
  routes,
});

export default router;

