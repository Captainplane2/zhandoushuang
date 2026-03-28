<template>
  <div class="wzry-home-page">
    <div class="hero-section">
      <div class="hero-content">
        <h1>王者荣耀板块</h1>
        <p>欢迎来到王者荣耀电竞专区，这里是高校王者荣耀战队的集结地</p>
        <div class="hero-buttons">
          <el-button type="primary" size="large" @click="$router.push('/wzry/team')">浏览战队</el-button>
          <el-button size="large" @click="$router.push('/wzry/match')">查看约战</el-button>
        </div>
      </div>
    </div>

    <div class="section-wrapper">
      <div class="section-header">
        <h2 class="section-title">热门战队</h2>
        <el-button link @click="$router.push('/wzry/team')">全部战队 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6" v-for="team in hotTeams" :key="team.id">
          <el-card class="team-item-card" :body-style="{ padding: '0px' }" shadow="hover" @click="$router.push(`/wzry/team/${team.id}`)">
            <div class="team-cover">
              <img :src="team.logo || 'https://via.placeholder.com/300x200/eee/999?text=TEAM'" class="cover-img">
            </div>
            <div class="team-info">
              <h3 class="team-name">{{ team.name }}</h3>
              <p class="team-desc">{{ team.description || '该战队很懒，暂无简介...' }}</p>
              <div class="team-footer">
                <span class="stars"><el-icon><User /></el-icon> {{ team.memberCount }}人</span>
                <el-button type="primary" link @click.stop="$router.push(`/wzry/team/${team.id}`)">详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="section-wrapper" style="margin-top: 40px;">
      <div class="section-header">
        <h2 class="section-title">最新约战</h2>
        <el-button link @click="$router.push('/wzry/match')">全部约战 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6" v-for="room in latestMatches" :key="room.id">
          <el-card class="match-room-card" shadow="hover" @click="$router.push(`/wzry/match/${room.id}`)">
            <div class="room-header">
              <el-tag :type="room.type === 1 ? 'warning' : 'success'" size="small" effect="dark">
                {{ room.type === 1 ? '线下' : '线上' }}
              </el-tag>
            </div>
            <h3 class="room-title">{{ room.title }}</h3>
            <div class="room-info">
              <div class="info-item">
                <span class="label">发起战队：</span>
                <span class="value host-team">{{ room.hostTeamName }}</span>
                <el-tag v-if="room.hostUniversity" size="small" effect="plain" class="university-tag">{{ room.hostUniversity }}</el-tag>
              </div>
              <div v-if="room.guestTeamName" class="info-item">
                <span class="label">应战战队：</span>
                <span class="value">{{ room.guestTeamName }}</span>
                <el-tag v-if="room.guestUniversity" size="small" effect="plain" class="university-tag">{{ room.guestUniversity }}</el-tag>
              </div>
              <div class="info-item">
                <span class="label">开赛时间：</span>
                <span class="value">{{ formatDate(room.matchTime) }}</span>
              </div>
              <div v-if="room.type === 1" class="info-item">
                <span class="label">地点：</span>
                <span class="value">{{ room.location }}</span>
              </div>
            </div>
            <div class="room-footer">
              <el-button 
                v-if="room.status === 0" 
                type="primary" 
                class="join-btn" 
                @click.stop="$router.push(`/wzry/match/${room.id}`)"
              >立即应战</el-button>
              <el-button 
                v-else-if="room.status === 1" 
                type="warning" 
                class="join-btn"
                disabled
              >等待开赛</el-button>
              <el-button 
                v-else 
                type="info" 
                class="join-btn"
                disabled
              >已结束</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../../utils/request';

const router = useRouter();
const loading = ref(false);
const hotTeams = ref([]);
const latestMatches = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    // 获取热门战队
    const teamsRes = await request.get('/team/list', { params: { gameProject: 'WZRY', page: 1, pageSize: 4 } });
    hotTeams.value = teamsRes.data || [];

    // 获取最新约战
    const matchesRes = await request.get('/match-room/list', { params: { gameProject: 'WZRY', page: 1, pageSize: 4 } });
    latestMatches.value = matchesRes.data || [];
  } catch (err) {
    console.error('获取WZRY板块数据失败', err);
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth()+1}月${date.getDate()}日 ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.wzry-home-page {
  padding: 20px 0;
}

.hero-section {
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
  margin-bottom: 40px;
}

.hero-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
  font-weight: bold;
}

.hero-content p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.section-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.team-item-card {
  transition: transform 0.3s;
  cursor: pointer;
}

.team-item-card:hover {
  transform: translateY(-5px);
}

.team-cover {
  height: 160px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.team-item-card:hover .cover-img {
  transform: scale(1.05);
}

.team-info {
  padding: 15px;
}

.team-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.team-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stars {
  font-size: 14px;
  color: #999;
}

.match-room-card {
  transition: transform 0.3s;
  cursor: pointer;
}

.match-room-card:hover {
  transform: translateY(-5px);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.room-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 15px 0;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.room-info {
  margin-bottom: 15px;
}

.info-item {
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.label {
  color: #999;
  min-width: 80px;
}

.value {
  color: #333;
  font-weight: 500;
}

.host-team {
  color: #ff4d4f;
  font-weight: bold;
}

.university-tag {
  margin-left: 5px;
}

.room-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.join-btn {
  width: 100%;
}
</style>