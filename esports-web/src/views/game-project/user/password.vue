<template>
  <div class="profile-password">
    <h2 class="section-title">修改密码</h2>
    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="120px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleUpdatePassword">确认修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '../../../store/user';
import { ElMessage } from 'element-plus';
import request from '../../../utils/request';

const userStore = useUserStore();
const pwdFormRef = ref(null);
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const handleUpdatePassword = async () => {
  if (!pwdFormRef.value) return;
  
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request.put('/user/password', {
          oldPassword: pwdForm.value.oldPassword,
          newPassword: pwdForm.value.newPassword
        }, {
          headers: {
            'Authorization': `Bearer ${userStore.token}`
          }
        });
        
        if (res.code === 200) {
          ElMessage.success('密码修改成功');
          pwdForm.value = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          };
        } else {
          ElMessage.error(res.msg || '密码修改失败');
        }
      } catch (err) {
        console.error('修改密码失败', err);
        ElMessage.error('网络错误，请稍后重试');
      }
    }
  });
};
</script>

<style scoped>
.profile-password {
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
  max-width: 400px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>