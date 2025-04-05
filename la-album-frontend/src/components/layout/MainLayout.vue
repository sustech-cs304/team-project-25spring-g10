<template>
  <div class="app-wrapper">
    <TheHeader />
    <main class="main-content">
      <div class="container">
        <slot></slot>
      </div>
    </main>
    <TheFooter />
  </div>
</template>

<script setup>
import TheHeader from './TheHeader.vue';
import TheFooter from './TheFooter.vue';
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: var(--space-lg) 0;
}
</style> 