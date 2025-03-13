<template>
    <div>
      <h2>Share Photo</h2>
      <el-input v-model="shareLink" readonly />
      <el-button @click="generateShareLink">Generate Share Link</el-button>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { generatePhotoShareLink } from '@/api/photo';
  
  const shareLink = ref('');
  
  const generateShareLink = async () => {
    shareLink.value = await generatePhotoShareLink();
  };
  </script>
  