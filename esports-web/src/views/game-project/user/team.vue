<template>
  <div class="profile-team">
    <h2 class="section-title">我的战队</h2>
    <div v-loading="loading" class="team-list">
      <el-card v-for="team in myTeams" :key="team.id" class="team-card" shadow="hover">
        <div class="team-info">
          <img :src="team.logo || 'https://via.placeholder.com/80x80/ccc/999?text=战队'" class="team-logo">
          <div class="team-details">
            <h3 class="team-name">{{ team.name }}</h3>
            <p class="team-meta">
              <el-tag size="small">{{ team.gameProject }}</el-tag>
              <span class="university"><el-icon><School /></el-icon> {{ team.university }}</span>
            </p>
            <p class="team-desc">{{ team.description || '暂无战队简介' }}</p>
            <div class="team-actions">
              <el-button type="primary" link @click="goToTeamDetail(team.id)">战队详情</el-button>
              <el-button v-if="team.leaderId === userStore.userInfo.id" type="success" link @click="goToTeamManage(team.id)">管理战队</el-button>
            </div>
          </div>
        </div>
      </el-card>
      <el-empty v-if="!loading && myTeams.length === 0" description="暂未加入任何战队" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '../../../store/user';
import { School } from '@element-plus/icons-vue';
import request from '../../../utils/request';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const gameProject = computed(() => route.params.gameProject?.toUpperCase() || null);
const loading = ref(false);
const myTeams = ref([]);

const fetchMyTeams = async () => {
  if (!userStore.token || !userStore.userInfo.id) return;
  
  loading.value = true;
  try {
    const res = await request.get(`/team/my/${userStore.userInfo.id}`, {
      params: {
        gameProject: gameProject.value
      }
    });
    myTeams.value = res.data || [];
  } catch (err) {
    console.error('获取我的战队失败', err);
  } finally {
    loading.value = false;
  }
};

const goToTeamDetail = (teamId) => {
  router.push(`/${gameProject.value}/team/${teamId}`);
};

const goToTeamManage = (teamId) => {
  router.push(`/${gameProject.value}/team/manage`);
};

onMounted(() => {
  fetchMyTeams();
});
</script>

<style scoped>
.profile-team {
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

.team-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.team-card {
  border-radius: 8px;
  transition: transform 0.3s;
}

.team-card:hover {
  transform: translateY(-2px);
}

.team-info {
  display: flex;
  gap: 15px;
}

.team-logo {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.team-details {
  flex: 1;
}

.team-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
}

.university {
  display: flex;
  align-items: center;
  gap: 4px;
}

.team-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.team-actions {
  display: flex;
  gap: 10px;
}
</style>