<template>
  <div class="profile-info">
    <h2 class="section-title">个人资料</h2>
    <el-form :model="form" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="高校">
        <el-input v-model="form.university" placeholder="请输入高校" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input
          v-model="form.bio"
          type="textarea"
          rows="3"
          placeholder="请输入个人简介"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../../store/user';
import { ElMessage } from 'element-plus';
import request from '../../../utils/request';

const userStore = useUserStore();
const form = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  university: '',
  bio: ''
});

onMounted(() => {
  if (userStore.userInfo) {
    form.value = {
      username: userStore.userInfo.username || '',
      nickname: userStore.userInfo.nickname || '',
      email: userStore.userInfo.email || '',
      phone: userStore.userInfo.phone || '',
      university: userStore.userInfo.university || '',
      bio: userStore.userInfo.bio || ''
    };
  }
});

const saveProfile = async () => {
  try {
    const res = await request.put('/user/profile', form.value, {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    });
    if (res.code === 200) {
      userStore.updateUserInfo(form.value);
      ElMessage.success('资料更新成功');
    } else {
      ElMessage.error(res.msg || '资料更新失败');
    }
  } catch (err) {
    console.error('更新资料失败', err);
    ElMessage.error('网络错误，请稍后重试');
  }
};
</script>

<style scoped>
.profile-info {
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

.el-form {
  max-width: 600px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>