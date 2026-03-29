<template>
  <div class="match-status-panel">
    <!-- 等待开赛倒计时 -->
    <div v-if="!isMatchStarted" class="waiting-start-section">
      <div class="countdown-box">
        <div class="countdown-label">等待开赛</div>
        <div class="countdown-time" :class="{ 'urgent': countdownSeconds < 60 }">
          {{ formatCountdown(countdownSeconds) }}
        </div>
      </div>
    </div>
    
    <!-- 比赛已开始，显示准备按钮和开始比赛按钮 -->
    <div v-else class="match-started-section">
      <!-- 战队准备区域 -->
      <div class="teams-ready-section">
        <!-- 发起方准备 -->
        <div class="team-ready-box">
          <div class="team-name">{{ matchData?.hostTeamName }}</div>
          <el-button
            v-if="isHostLeader"
            :type="hostReady ? 'success' : 'info'"
            size="large"
            class="ready-btn"
            :disabled="!canPrepare"
            @click="toggleHostReady"
          >
            <el-icon v-if="hostReady"><Check /></el-icon>
            {{ hostReady ? '已准备' : '未准备' }}
          </el-button>
          <el-tag 
            v-else 
            :type="hostReady ? 'success' : 'info'" 
            size="large" 
            class="ready-status"
            :class="{ 'status-changed': hostReadyChanged }"
          >
            {{ hostReady ? '已准备' : '未准备' }}
          </el-tag>
        </div>
        
        <!-- VS -->
        <div class="vs-divider">VS</div>
        
        <!-- 应战方准备 -->
        <div class="team-ready-box">
          <div class="team-name">{{ matchData?.guestTeamName }}</div>
          <el-button
            v-if="isGuestLeader"
            :type="guestReady ? 'success' : 'info'"
            size="large"
            class="ready-btn"
            :disabled="!canPrepare"
            @click="toggleGuestReady"
          >
            <el-icon v-if="guestReady"><Check /></el-icon>
            {{ guestReady ? '已准备' : '未准备' }}
          </el-button>
          <el-tag 
            v-else 
            :type="guestReady ? 'success' : 'info'" 
            size="large" 
            class="ready-status"
            :class="{ 'status-changed': guestReadyChanged }"
          >
            {{ guestReady ? '已准备' : '未准备' }}
          </el-tag>
        </div>
      </div>
      
      <!-- 开始比赛按钮 -->
      <div class="start-match-section">
        <el-button
          type="danger"
          size="large"
          class="start-match-btn"
          :disabled="!canStartMatch"
          @click="startMatch"
        >
          <div class="btn-content">
            <div class="btn-main">开始比赛 {{ readyCount }}/2</div>
            <div v-if="showStartCountdown" class="btn-sub">
              准备开赛 倒计时{{ startCountdown }}s
            </div>
          </div>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../store/user';
import request from '../utils/request';

const props = defineProps({
  matchId: Number,
  matchData: Object
});

const userStore = useUserStore();

// 倒计时相关
const countdownSeconds = ref(0);
const isMatchStarted = ref(false);
let countdownTimer = null;

// 准备状态
const hostReady = ref(false);
const guestReady = ref(false);

// 状态变化动画
const hostReadyChanged = ref(false);
const guestReadyChanged = ref(false);

// 开始比赛倒计时
const showStartCountdown = ref(false);
const startCountdown = ref(15);
let startCountdownTimer = null;

// 轮询定时器
let pollingTimer = null;

// 计算属性：当前用户是否是发起方队长
const isHostLeader = computed(() => {
  return userStore.token && props.matchData && 
    String(userStore.userInfo.id) === String(props.matchData.hostId);
});

// 计算属性：当前用户是否是应战方队长
const isGuestLeader = computed(() => {
  return userStore.token && props.matchData && 
    String(userStore.userInfo.id) === String(props.matchData.guestId);
});

// 计算属性：已准备的队伍数量
const readyCount = computed(() => {
  let count = 0;
  if (hostReady.value) count++;
  if (guestReady.value) count++;
  return count;
});

// 计算属性：是否可以开始比赛
const canStartMatch = computed(() => {
  return readyCount.value === 2;
});

// 计算属性：是否在允许准备的时间范围内
const canPrepare = computed(() => {
  if (!props.matchData || !props.matchData.matchTime) return false;
  
  // 添加调试日志
  console.log('MatchStatusPanel canPrepare检查 - props.matchData:', props.matchData);
  console.log('MatchStatusPanel canPrepare检查 - status:', props.matchData.status, 'matchStatus:', props.matchData.matchStatus);
  
  // 检查状态：必须是已应战状态
  // status 是 Integer 类型，1 表示已应战
  // matchStatus 可能是枚举字符串 "READY" 或数字 1
  const isReadyStatus = props.matchData.status === 1 || 
                       props.matchData.matchStatus === 'READY' || 
                       props.matchData.matchStatus === 1;
  
  console.log('MatchStatusPanel canPrepare检查 - isReadyStatus:', isReadyStatus);
  
  if (!isReadyStatus) return false;
  
  const matchTime = new Date(props.matchData.matchTime);
  const now = new Date();
  const thirtyMinutes = 30 * 60 * 1000;
  
  // 开始前30分钟内或开始后30分钟内
  const timeDiff = Math.abs(matchTime - now);
  const result = timeDiff <= thirtyMinutes;
  
  console.log('MatchStatusPanel canPrepare检查 - timeDiff:', timeDiff, 'result:', result);
  
  return result;
});

// 计算属性：是否超过开赛时间30分钟
const isOverdue = computed(() => {
  if (!props.matchData || !props.matchData.matchTime) return false;
  
  const matchTime = new Date(props.matchData.matchTime);
  const now = new Date();
  const thirtyMinutes = 30 * 60 * 1000;
  
  // 超过开赛时间30分钟
  return now - matchTime > thirtyMinutes;
});

// 检查比赛是否已开始（开赛时间已过）
const checkMatchStart = () => {
  if (!props.matchData || !props.matchData.matchTime) return;
  
  const matchTime = new Date(props.matchData.matchTime);
  const now = new Date();
  
  if (now >= matchTime) {
    isMatchStarted.value = true;
  } else {
    // 计算剩余秒数
    const diff = Math.floor((matchTime - now) / 1000);
    countdownSeconds.value = Math.max(0, diff);
    startWaitingCountdown();
  }
};

// 获取准备状态
const fetchReadyStatus = async () => {
  if (!props.matchId) return;
  
  try {
    const res = await request.get(`/match-status/ready/${props.matchId}`);
    if (res.code === 200) {
      // 检查状态变化，触发动画
      if (hostReady.value !== res.data.hostReady) {
        hostReady.value = res.data.hostReady;
        hostReadyChanged.value = true;
        setTimeout(() => {
          hostReadyChanged.value = false;
        }, 1000);
      }
      if (guestReady.value !== res.data.guestReady) {
        guestReady.value = res.data.guestReady;
        guestReadyChanged.value = true;
        setTimeout(() => {
          guestReadyChanged.value = false;
        }, 1000);
      }
    }
  } catch (err) {
    console.error('获取准备状态失败:', err);
  }
};

// 启动轮询
const startPolling = () => {
  if (pollingTimer) clearInterval(pollingTimer);
  
  // 每1秒轮询一次
  pollingTimer = setInterval(() => {
    if (isMatchStarted.value) {
      fetchReadyStatus();
    }
  }, 1000);
};

// 启动等待开赛倒计时
const startWaitingCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer);
  
  countdownTimer = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value--;
    } else {
      isMatchStarted.value = true;
      clearInterval(countdownTimer);
    }
  }, 1000);
};

// 格式化倒计时显示
const formatCountdown = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  if (hours > 0) {
    return `${hours}时${String(minutes).padStart(2, '0')}分${String(secs).padStart(2, '0')}秒`;
  } else if (minutes > 0) {
    return `${minutes}分${String(secs).padStart(2, '0')}秒`;
  } else {
    return `${secs}秒`;
  }
};

// 切换发起方准备状态
const toggleHostReady = async () => {
  if (!canPrepare.value) {
    if (isOverdue.value) {
      ElMessage.warning('超时未开赛，比赛已取消');
    } else {
      ElMessage.warning('当前状态不允许准备操作');
    }
    return;
  }
  
  try {
    const response = await request.post(`/match-status/ready/${props.matchId}`, null, {
      params: {
        userId: Number(userStore.userInfo.id),
        teamType: 'host'
      }
    });
    
    if (response.code === 200) {
      hostReady.value = !hostReady.value;
      checkBothReady();
      ElMessage.success(hostReady.value ? '发起方已准备' : '发起方取消准备');
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};

// 切换应战方准备状态
const toggleGuestReady = async () => {
  if (!canPrepare.value) {
    if (isOverdue.value) {
      ElMessage.warning('超时未开赛，比赛已取消');
    } else {
      ElMessage.warning('当前状态不允许准备操作');
    }
    return;
  }
  
  try {
    const response = await request.post(`/match-status/ready/${props.matchId}`, null, {
      params: {
        userId: Number(userStore.userInfo.id),
        teamType: 'guest'
      }
    });
    
    if (response.code === 200) {
      guestReady.value = !guestReady.value;
      checkBothReady();
      ElMessage.success(guestReady.value ? '应战方已准备' : '应战方取消准备');
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};

// 检查双方是否都准备
const checkBothReady = () => {
  if (hostReady.value && guestReady.value) {
    // 双方都已准备，启动开始比赛倒计时
    if (!showStartCountdown.value) {
      showStartCountdown.value = true;
      startCountdown.value = 15;
      startStartCountdown();
    }
  } else {
    // 任何一方取消准备，重置倒计时
    if (showStartCountdown.value) {
      showStartCountdown.value = false;
      startCountdown.value = 15;
      if (startCountdownTimer) {
        clearInterval(startCountdownTimer);
        startCountdownTimer = null;
      }
    }
  }
};

// 启动开始比赛倒计时
const startStartCountdown = () => {
  if (startCountdownTimer) clearInterval(startCountdownTimer);
  
  startCountdownTimer = setInterval(() => {
    if (startCountdown.value > 0) {
      startCountdown.value--;
    } else {
      // 倒计时结束，自动开始比赛
      clearInterval(startCountdownTimer);
      startMatch();
    }
  }, 1000);
};

// 开始比赛
const startMatch = async () => {
  if (!canStartMatch.value) {
    ElMessage.warning('双方战队都需要准备后才能开始比赛');
    return;
  }
  
  try {
    const response = await request.post(`/match-status/start/${props.matchId}`);
    if (response.code === 200) {
      ElMessage.success('比赛开始！');
      // 刷新页面或更新状态
      window.location.reload();
    }
  } catch (error) {
    ElMessage.error(error.message || '开始比赛失败');
  }
};

onMounted(() => {
  checkMatchStart();
  startPolling();
  // 初始获取准备状态
  if (isMatchStarted.value) {
    fetchReadyStatus();
  }
});

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (startCountdownTimer) clearInterval(startCountdownTimer);
  if (pollingTimer) clearInterval(pollingTimer);
});
</script>

<style scoped>
.match-status-panel {
  margin: 20px 0;
}

/* 等待开赛区域 */
.waiting-start-section {
  display: flex;
  justify-content: center;
  padding: 30px;
}

.countdown-box {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 60px;
  text-align: center;
  color: white;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.countdown-label {
  font-size: 18px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.countdown-time {
  font-size: 48px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.countdown-time.urgent {
  color: #ff6b6b;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 比赛已开始区域 */
.match-started-section {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: 20px;
}

/* 战队准备区域 */
.teams-ready-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
}

.team-ready-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  min-width: 180px;
}

.team-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.ready-btn {
  width: 120px;
  font-weight: bold;
}

.ready-btn .el-icon {
  margin-right: 5px;
}

.ready-status {
  font-weight: bold;
  padding: 10px 20px;
  width: 120px;
  text-align: center;
}

/* 状态变化动画 */
.status-changed {
  animation: statusChange 1s ease-in-out;
}

@keyframes statusChange {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.vs-divider {
  font-size: 24px;
  font-weight: bold;
  color: #999;
  padding: 0 20px;
}

/* 开始比赛按钮区域 */
.start-match-section {
  display: flex;
  justify-content: center;
}

.start-match-btn {
  min-width: 280px;
  min-height: 80px;
  font-size: 20px;
  font-weight: bold;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

.start-match-btn:not(:disabled):hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(245, 108, 108, 0.4);
}

.start-match-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.btn-main {
  font-size: 22px;
}

.btn-sub {
  font-size: 14px;
  font-weight: normal;
  opacity: 0.9;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 0.9;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .countdown-box {
    padding: 20px 40px;
  }
  
  .countdown-time {
    font-size: 36px;
  }
  
  .teams-ready-section {
    flex-direction: column;
    gap: 20px;
  }
  
  .vs-divider {
    transform: rotate(90deg);
    padding: 10px 0;
  }
  
  .start-match-btn {
    min-width: 240px;
    min-height: 70px;
    font-size: 18px;
  }
  
  .btn-main {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .countdown-time {
    font-size: 28px;
  }
  
  .start-match-btn {
    min-width: 200px;
    min-height: 60px;
  }
  
  .btn-main {
    font-size: 18px;
  }
  
  .btn-sub {
    font-size: 12px;
  }
  
  .team-ready-box {
    min-width: 140px;
    padding: 15px;
  }
}
</style>
