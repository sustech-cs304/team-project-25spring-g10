<template>
    <div>
      <h2>Search Photos</h2>
      <el-input v-model="query" placeholder="Enter keywords (time, location, tags)" />
      <el-button @click="searchPhotos">Search</el-button>
      <el-table :data="searchResults">
        <el-table-column prop="name" label="Photo Name"></el-table-column>
        <el-table-column prop="tags" label="Tags"></el-table-column>
      </el-table>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import { searchPhotoByKeyword } from '@/api/photo';
  
  const query = ref('');
  const searchResults = ref([]);
  
  const searchPhotos = async () => {
    searchResults.value = await searchPhotoByKeyword(query.value);
  };
  </script>
  