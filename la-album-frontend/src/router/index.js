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
];

const router = createRouter({
  history: createWebHistory(), // 确保使用 history 模式
  routes,
});

export default router;

