<template>
    <div>
      <h2>Albums</h2>
      <el-button @click="fetchAlbums">Load Albums</el-button>
      <el-table :data="albums" style="width: 100%">
        <el-table-column prop="name" label="Album Name"></el-table-column>
        <el-table-column prop="photoCount" label="Photo Count"></el-table-column>
      </el-table>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { fetchAlbumList } from '@/api/album';
  
  const albums = ref([]);
  
  const fetchAlbums = async () => {
    albums.value = await fetchAlbumList();
  };
  
  onMounted(fetchAlbums);
  </script>
  