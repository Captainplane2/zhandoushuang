<template>
  <div class="lol-match-manage-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/lol' }">LOL首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/lol/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>{{ isEdit ? '编辑约战' : '创建约战' }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>{{ isEdit ? '编辑约战' : '创建约战' }}</h1>
    </div>

    <el-card shadow="never" class="match-form-card">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="约战标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入约战标题" />
        </el-form-item>
        
        <el-form-item label="约战类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="0">线上</el-radio>
            <el-radio label="1">线下</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="form.type === 1" label="约战地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入约战地点" />
        </el-form-item>
        
        <el-form-item label="开赛时间" prop="matchTime">
          <el-date-picker
            v-model="form.matchTime"
            type="datetime"
            placeholder="选择开赛时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :min-date="new Date()"
          />
        </el-form-item>
        
        <el-form-item label="约战说明" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入约战说明" :rows="4" />
        </el-form-item>
        
        <el-form-item label="选择战队" prop="hostTeamId">
          <el-select v-model="form.hostTeamId" placeholder="请选择您的战队">
            <el-option v-for="team in myTeams" :key="team.id" :label="team.name" :value="team.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="$router.push('/lol/match')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../../../utils/request';
import { useUserStore } from '../../../store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const myTeams = ref([]);

const matchId = computed(() => route.params.id);
const isEdit = computed(() => !!matchId.value);

const form = ref({
  title: '',
  type: 0,
  location: '',
  matchTime: '',
  description: '',
  hostTeamId: ''
});

const rules = {
  title: [{ required: true, message: '请输入约战标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择约战类型', trigger: 'change' }],
  location: [{ required: form.value.type === 1, message: '请输入约战地点', trigger: 'blur' }],
  matchTime: [{ required: true, message: '请选择开赛时间', trigger: 'change' }],
  hostTeamId: [{ required: true, message: '请选择战队', trigger: 'change' }]
};

const fetchMyTeams = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const res = await request.get(`/team/leader/${userStore.userInfo.id}`, {
      params: { gameProject: 'LOL' }
    });
    if (res.data) {
      myTeams.value = [res.data];
      if (!isEdit.value) {
        form.value.hostTeamId = res.data.id;
      }
    } else {
      ElMessage.warning('您还没有创建战队，请先创建战队');
      router.push('/lol/team/manage');
    }
  } catch (err) {
    console.error('获取我的战队失败', err);
  }
};

const fetchMatchDetail = async () => {
  if (!matchId.value) return;
  try {
    const res = await request.get(`/match-room/${matchId.value}`);
    const data = res.data;
    form.value = {
      title: data.title,
      type: data.type,
      location: data.location,
      matchTime: data.matchTime,
      description: data.description,
      hostTeamId: data.hostTeamId
    };
  } catch (err) {
    console.error('获取约战详情失败', err);
  }
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const requestData = {
          ...form.value,
          hostId: userStore.userInfo.id,
          gameProject: 'LOL'
        };
        if (isEdit.value) {
          await request.put('/match-room', requestData);
          ElMessage.success('约战编辑成功');
        } else {
          await request.post('/match-room', requestData);
          ElMessage.success('约战创建成功');
        }
        router.push('/lol/match');
      } catch (err) {
        console.error('提交失败', err);
      } finally {
        loading.value = false;
      }
    }
  });
};

onMounted(() => {
  fetchMyTeams();
  if (isEdit.value) {
    fetchMatchDetail();
  }
});
</script>

<style scoped>
.lol-match-manage-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 10px 0 0 0;
}

.match-form-card {
  max-width: 800px;
  margin: 0 auto;
}
</style>