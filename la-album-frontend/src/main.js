import { createApp } from 'vue';
import router from './router';
import App from './App.vue'; 
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

// 导入全局样式
import './assets/css/global.css';

const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
