<template>
    <div>
      <h2>Edit Photo</h2>
      <el-image :src="photoUrl" />
      <el-button @click="applyFilter('grayscale')">Grayscale</el-button>
      <el-button @click="applyFilter('sepia')">Sepia</el-button>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { getPhotoById, applyPhotoFilter } from '@/api/photo';
  import { useRoute } from 'vue-router';
  
  const route = useRoute();
  const photoUrl = ref('');
  
  onMounted(async () => {
    photoUrl.value = await getPhotoById(route.params.photoId);
  });
  
  const applyFilter = async (filterType) => {
    photoUrl.value = await applyPhotoFilter(route.params.photoId, filterType);
  };
  </script>
  