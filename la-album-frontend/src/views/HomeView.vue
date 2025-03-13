<template>
  <div class="container">
    <h1>My Smart Album</h1>
    <div class="button-group">
      <el-button type="primary" class="blue-btn" @click="navigateTo('/album')">📁 Manage Albums</el-button>
      <el-button type="success" class="green-btn" @click="navigateTo('/search')">🔍 Search Photos</el-button>
      <el-button type="warning" class="yellow-btn" @click="navigateTo('/edit/1')">🖼 Edit Photo</el-button>
      <el-button type="danger" class="red-btn" @click="navigateTo('/trash')">🗑 View Trash Bin</el-button>
      <el-button type="info" class="gray-btn" @click="navigateTo('/share/1')">📤 Share Photo</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const navigateTo = (path) => {
  router.push(path);
};
</script>


<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f5f5;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
}

.button-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.el-button {
  width: 250px;
  height: 50px;
  font-size: 16px;
}
</style>
