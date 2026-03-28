<template>
  <div class="profile-post">
    <h2 class="section-title">我的帖子</h2>
    <div v-loading="loading" class="post-list">
      <el-empty description="暂无帖子" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const loading = ref(false);
</script>

<style scoped>
.profile-post {
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