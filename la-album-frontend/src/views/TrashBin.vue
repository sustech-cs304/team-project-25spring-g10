<template>
    <div>
      <h2>Trash Bin</h2>
      <el-table :data="deletedPhotos">
        <el-table-column prop="name" label="Photo Name"></el-table-column>
        <el-table-column>
          <template v-slot="{ row }">
            <el-button @click="restorePhoto(row.id)">Restore</el-button>
            <el-button @click="deletePermanently(row.id)" type="danger">Delete Permanently</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { getDeletedPhotos, restorePhoto, deletePermanently } from '@/api/photo';
  
  const deletedPhotos = ref([]);
  
  onMounted(async () => {
    deletedPhotos.value = await getDeletedPhotos();
  });
  </script>
  