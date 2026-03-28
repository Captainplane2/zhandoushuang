<template>
  <div class="match-manage-page full-container">
    <div class="page-header">
      <h1 class="page-title">王者荣耀 约战管理</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/wzry' }">王者荣耀首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/wzry/match' }">约战大厅</el-breadcrumb-item>
        <el-breadcrumb-item>约战管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box">
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="约战状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable @change="fetchMyMatches" style="width: 120px">
              <el-option label="招募中" value="0" />
              <el-option label="已应战" value="1" />
              <el-option label="已结束" value="2" />
              <el-option label="已取消" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchMyMatches">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="match-list">
        <el-table :data="myMatches" style="width: 100%">
          <el-table-column prop="title" label="约战标题" width="250" />
          <el-table-column prop="gameProject" label="竞技项目" width="120" />
          <el-table-column label="约战类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.type === 1 ? 'warning' : 'success'">
                {{ scope.row.type === 1 ? '线下' : '线上' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="约战状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="matchTime" label="开赛时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.matchTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="hostTeamName" label="发起方" width="150" />
          <el-table-column prop="guestTeamName" label="应战方" width="150">
            <template #default="scope">
              {{ scope.row.guestTeamName || '等待应战' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="goToMatchDetail(scope.row.id)">查看详情</el-button>
              <el-button 
                v-if="scope.row.status === 0 && scope.row.hostId === userStore.userInfo.id" 
                size="small" 
                type="danger" 
                @click="cancelMatch(scope.row.id)"
              >取消约战</el-button>
              <el-button 
                v-if="scope.row.status === 1 && scope.row.hostId === userStore.userInfo.id" 
                size="small" 
                type="info" 
                @click="finishMatch(scope.row.id)"
              >结束约战</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="myMatches.length === 0" description="暂无约战记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../../store/user';
import { useRouter } from 'vue-router';
import request from '../../../utils/request';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const router = useRouter();
const myMatches = ref([]);
const loading = ref(false);

const queryForm = ref({
  status: ''
});

const fetchMyMatches = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    return;
  }
  
  loading.value = true;
  try {
    // 这里应该调用获取我的约战的接口，暂时使用list接口模拟
    const res = await request.get('/match-room/list', { params: { gameProject: 'WZRY' } });
    const allMatches = res.data || [];
    // 过滤出我参与的约战（发起方或应战方）
    myMatches.value = allMatches.filter(match => 
      match.hostId === userStore.userInfo.id || match.guestId === userStore.userInfo.id
    );
    // 按状态过滤
    if (queryForm.value.status !== '') {
      myMatches.value = myMatches.value.filter(match => match.status == queryForm.value.status);
    }
  } catch (err) {
    console.error('获取约战记录失败', err);
  } finally {
    loading.value = false;
  }
};

const cancelMatch = async (roomId) => {
  try {
    await request.post(`/match-room/cancel/${roomId}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已取消');
    fetchMyMatches();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '取消失败');
  }
};

const finishMatch = async (roomId) => {
  try {
    await request.post(`/match-room/finish/${roomId}`, null, {
      params: { userId: userStore.userInfo.id }
    });
    ElMessage.success('约战已结束');
    fetchMyMatches();
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '结束失败');
  }
};

const goToMatchDetail = (roomId) => {
  router.push(`/wzry/match/${roomId}`);
};

const getStatusText = (status) => {
  const map = { 0: '招募中', 1: '已应战', 2: '已结束', 3: '已取消' };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'info', 3: 'danger' };
  return map[status] || 'info';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(fetchMyMatches);
</script>

<style scoped>
.match-manage-page { padding: 20px 0; }

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.content-box {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.filter-bar {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.match-list {
  margin-top: 20px;
}
</style>