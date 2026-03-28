<template>
  <div class="lol-team-detail-page">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/lol' }">LOL首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/lol/team' }">战队列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ team.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-card v-loading="loading" shadow="never" class="team-detail-card">
      <div class="team-header">
        <el-avatar :size="100" :src="team.logo || 'https://via.placeholder.com/150/eee/999?text=TEAM'" />
        <div class="team-info">
          <h1 class="team-name">{{ team.name }}</h1>
          <p class="team-uni"><el-icon><School /></el-icon> {{ team.university }}</p>
          <p class="team-desc">{{ team.description || '该战队很懒，暂无简介...' }}</p>
          <div class="team-stats">
            <span class="stat-item"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
            <span class="stat-item"><el-icon><Timer /></el-icon> {{ formatDate(team.createTime) }}创建</span>
          </div>
        </div>
        <div class="team-actions">
          <el-button type="primary" v-if="isLeader" @click="$router.push('/lol/team/manage')">管理战队</el-button>
          <el-button v-else-if="isMember" type="info" disabled>已加入</el-button>
          <el-button v-else type="primary" @click="joinTeam">申请加入</el-button>
        </div>
      </div>

      <el-divider content-position="left">战队成员</el-divider>
      <div class="team-members">
        <div class="member-item" v-for="member in members" :key="member.userId">
          <el-avatar :size="60" :src="member.avatar || 'https://via.placeholder.com/150/eee/999?text=USER'" />
          <div class="member-info">
            <h3 class="member-name">{{ member.nickname }}</h3>
            <p class="member-role">{{ member.role === 'ROLE_LEADER' ? '队长' : '队员' }}</p>
            <p class="member-uni">{{ member.university }}</p>
          </div>
          <div class="member-actions">
            <el-button link @click="goToUserInfo(member.userId)">查看资料</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="recentMatches.length > 0" shadow="never" class="recent-matches-card" style="margin-top: 30px;">
      <template #header>
        <div class="card-header">
          <h3>近期约战</h3>
          <el-button link @click="$router.push('/lol/match')">查看全部</el-button>
        </div>
      </template>
      <div class="match-list">
        <div class="match-item" v-for="match in recentMatches" :key="match.id">
          <div class="match-info">
            <h4 class="match-title">{{ match.title }}</h4>
            <p class="match-time"><el-icon><Calendar /></el-icon> {{ formatDate(match.matchTime) }}</p>
            <p class="match-status">{{ getStatusText(match.status) }}</p>
          </div>
          <el-button type="primary" link @click="goToMatchDetail(match.id)">查看详情</el-button>
        </div>
      </div>
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
const loading = ref(false);
const team = ref({});
const members = ref([]);
const recentMatches = ref([]);

const teamId = computed(() => route.params.id);

const isLeader = computed(() => {
  return userStore.token && team.value.leaderId === userStore.userInfo.id;
});

const isMember = computed(() => {
  return userStore.token && members.value.some(member => member.userId === userStore.userInfo.id);
});

const fetchTeamDetail = async () => {
  loading.value = true;
  try {
    // 获取战队详情
    const teamRes = await request.get(`/team/${teamId.value}`);
    team.value = teamRes.data;

    // 获取战队成员
    const membersRes = await request.get(`/team/members/${teamId.value}`);
    members.value = membersRes.data || [];

    // 获取近期约战
    const matchesRes = await request.get('/match-room/list', {
      params: {
        gameProject: 'LOL',
        hostTeamId: teamId.value,
        page: 1,
        pageSize: 3
      }
    });
    recentMatches.value = matchesRes.data || [];
  } catch (err) {
    console.error('获取战队详情失败', err);
  } finally {
    loading.value = false;
  }
};

const joinTeam = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    await request.post('/team/apply', {
      teamId: teamId.value,
      userId: userStore.userInfo.id
    });
    ElMessage.success('申请已提交，请等待队长审核');
  } catch (err) {
    console.error('申请加入战队失败', err);
  }
};

const goToUserInfo = (userId) => {
  router.push(`/user/info/${userId}`);
};

const goToMatchDetail = (matchId) => {
  router.push(`/lol/match/${matchId}`);
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
};

const getStatusText = (status) => {
  const statusMap = {
    0: '招募中',
    1: '已应战',
    2: '已结束',
    3: '已取消'
  };
  return statusMap[status] || '未知状态';
};

onMounted(() => {
  fetchTeamDetail();
});
</script>

<style scoped>
.lol-team-detail-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 30px;
}

.team-detail-card {
  margin-bottom: 30px;
}

.team-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.team-info {
  flex: 1;
}

.team-name {
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.team-uni {
  font-size: 16px;
  color: #999;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.team-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  font-size: 14px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
}

.team-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.team-members {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: transform 0.3s;
}

.member-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: #333;
}

.member-role {
  font-size: 14px;
  color: #1890ff;
  margin: 0 0 5px 0;
}

.member-uni {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.recent-matches-card {
  margin-top: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.match-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: transform 0.3s;
}

.match-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.match-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.match-time {
  font-size: 14px;
  color: #999;
  margin: 0 0 5px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.match-status {
  font-size: 14px;
  color: #1890ff;
  margin: 0;
}
</style>