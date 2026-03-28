<template>
  <div class="game-project-page">
    <div class="page-header">
      <h1 class="page-title">{{ gameProjectDisplayName }}</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ gameProjectDisplayName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-box">
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="section">
            <div class="section-header">
              <h2>火热约战</h2>
              <el-button link @click="$router.push(`/${gameProject}/match`)">查看更多</el-button>
            </div>
            <div v-loading="loading" class="match-grid">
              <el-row :gutter="20">
                <el-col :span="8" v-for="match in matches" :key="match.id">
                  <el-card class="match-card" shadow="hover" @click="goToMatchDetail(match.id)">
                    <div class="match-header">
                      <el-tag :type="match.type === 1 ? 'warning' : 'success'" size="small" effect="dark">
                        {{ match.type === 1 ? '线下' : '线上' }}
                      </el-tag>
                      <span class="game-tag">{{ match.gameProject }}</span>
                    </div>
                    <h3 class="match-title">{{ match.title }}</h3>
                    
                    <div class="match-info">
                      <div class="info-item">
                        <span class="label">发起战队：</span>
                        <span class="value host-team">{{ match.hostTeamName }}</span>
                        <el-tag v-if="match.hostUniversity" size="small" effect="plain" class="university-tag">{{ match.hostUniversity }}</el-tag>
                      </div>
                      <div v-if="match.guestTeamName" class="info-item">
                        <span class="label">应战战队：</span>
                        <span class="value">{{ match.guestTeamName }}</span>
                        <el-tag v-if="match.guestUniversity" size="small" effect="plain" class="university-tag">{{ match.guestUniversity }}</el-tag>
                      </div>
                      <div class="info-item">
                        <span class="label">开赛时间：</span>
                        <span class="value">{{ formatDate(match.matchTime) }}</span>
                      </div>
                    </div>

                    <div class="match-footer">
                      <el-button 
                        v-if="match.status === 0" 
                        type="primary" 
                        class="join-btn" 
                        @click.stop="handleJoinClick(match)"
                      >立即应战</el-button>
                      <el-button 
                        v-else-if="match.status === 1" 
                        type="warning" 
                        class="join-btn" 
                        disabled
                      >等待开赛</el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              <el-empty v-if="matches.length === 0" description="暂无约战信息" />
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="section">
            <div class="section-header">
              <h2>热门战队</h2>
              <el-button link @click="$router.push(`/${gameProject}/team`)">查看更多</el-button>
            </div>
            <div v-loading="loading" class="team-list">
              <el-card v-for="team in teams" :key="team.id" class="team-card-small" shadow="hover" @click="goToTeamDetail(team.id)">
                <div class="team-info">
                  <img :src="team.logo || 'https://via.placeholder.com/60x60/eee/999?text=T'" class="team-logo">
                  <div class="team-details">
                    <h4 class="team-name">{{ team.name }}</h4>
                    <p class="team-university">{{ team.university }}</p>
                    <div class="team-meta">
                      <el-tag size="small">{{ team.gameProject }}</el-tag>
                      <span class="member-count"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
                    </div>
                  </div>
                </div>
              </el-card>
              <el-empty v-if="teams.length === 0" description="暂无战队信息" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGameProjectStore } from '../../store/gameProject';
import request from '../../utils/request';
import { User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const gameProjectStore = useGameProjectStore();

const gameProject = computed(() => route.params.gameProject?.toUpperCase());
const gameProjectDisplayName = computed(() => {
  const project = gameProjectStore.getGameProjectByName(gameProject.value);
  return project ? project.displayName : gameProject.value;
});

const loading = ref(false);
const matches = ref([]);
const teams = ref([]);

const fetchMatches = async () => {
  loading.value = true;
  try {
    const res = await request.get('/match-room/list', {
      params: {
        gameProject: gameProject.value,
        status: 0
      }
    });
    matches.value = res.data?.slice(0, 6) || [];
  } catch (err) {
    console.error('获取约战列表失败', err);
  } finally {
    loading.value = false;
  }
};

const fetchTeams = async () => {
  loading.value = true;
  try {
    const res = await request.get('/team/list', {
      params: {
        gameProject: gameProject.value
      }
    });
    teams.value = res.data?.slice(0, 5) || [];
  } catch (err) {
    console.error('获取战队列表失败', err);
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

const goToMatchDetail = (matchId) => {
  router.push(`/${gameProject.value}/match/${matchId}`);
};

const goToTeamDetail = (teamId) => {
  router.push(`/${gameProject.value}/team/${teamId}`);
};

const handleJoinClick = (match) => {
  ElMessage.info('请先登录后再进行操作');
  router.push('/login');
};

onMounted(() => {
  fetchMatches();
  fetchTeams();
});

// 监听路由变化，当游戏板块改变时重新加载数据
import { watch } from 'vue';
watch(() => route.params.gameProject, (newGameProject, oldGameProject) => {
  if (newGameProject !== oldGameProject) {
    fetchMatches();
    fetchTeams();
  }
});
</script>

<style scoped>
.game-project-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.content-box {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.match-grid {
  min-height: 200px;
}

.match-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.match-card:hover {
  transform: translateY(-4px);
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.game-tag {
  font-size: 12px;
  color: #666;
}

.match-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.match-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #666;
  margin-right: 8px;
  white-space: nowrap;
}

.info-item .value {
  color: #333;
  flex: 1;
}

.host-team {
  font-weight: bold;
  color: var(--primary);
}

.university-tag {
  margin-left: 8px;
}

.match-footer {
  display: flex;
  justify-content: center;
}

.join-btn {
  width: 100%;
}

.team-list {
  min-height: 200px;
}

.team-card-small {
  margin-bottom: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.team-card-small:hover {
  transform: translateY(-2px);
}

.team-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.team-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.team-details {
  flex: 1;
}

.team-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: #333;
}

.team-university {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px 0;
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-count {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>