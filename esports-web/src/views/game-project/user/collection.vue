<template>
  <div class="profile-collection">
    <h2 class="section-title">战队收藏</h2>
    <div v-loading="loading" class="collection-list">
      <el-empty description="暂无收藏的战队" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const loading = ref(false);
</script>

<style scoped>
.profile-collection {
  padding: 20px 0;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}
</style>