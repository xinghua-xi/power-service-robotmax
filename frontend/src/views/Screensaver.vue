<template>
  <div class="screensaver" @click.self="enterSystem">
    <!-- 动态电网背景（保留原科技感动效） -->
    <div class="dynamic-grid">
      <div 
        v-for="(line, index) in dynamicLines" 
        :key="index"
        class="grid-line"
        :style="line.style"
      ></div>
    </div>
    
    <!-- 电力脉冲效果（保留原动效） -->
    <div class="power-pulse" :style="pulseStyle"></div>
    
    <!-- 电费监控大屏内容 - 修改为垂直居中 -->
    <div class="monitor-content">
      <!-- 大屏标题+归属 -->
      <div class="monitor-header">
        <h1 class="monitor-title">电费运营监控大屏</h1>
        <div class="monitor-belong">西把栅供电所</div>
      </div>

      <!-- 时间显示 - 调整为居中显示 -->
      <div class="time-area">
        <div class="current-week">{{ currentWeek }}</div>
        <div class="current-date">{{ currentDate }}</div>
        <div class="current-time">{{ currentTime }}</div>
      </div>

      <!-- 核心数据区域：居民+非居民用电交费 -->
      <div class="data-container">
        <!-- 居民用电交费区域 -->
        <div class="data-card resident-card">
          <div class="card-title">当月居民用电交费（万元）</div>
          <div class="main-number">
            <span class="num-part">0</span>
            <span class="num-part">.</span>
            <span class="num-part">1</span>
            <span class="num-part">7</span>
          </div>
          <div class="sub-data-group">
            <div class="sub-data">
              <div class="sub-label">当日缴费金额</div>
              <div class="sub-value">0.17<span class="unit">万元</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">当日缴费笔数</div>
              <div class="sub-value">6<span class="unit">笔</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">当月缴费笔数</div>
              <div class="sub-value">6<span class="unit">笔</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">本年缴费金额</div>
              <div class="sub-value">0.17<span class="unit">万元</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">本年缴费笔数</div>
              <div class="sub-value">6<span class="unit">笔</span></div>
            </div>
          </div>
        </div>

        <!-- 非居民用电交费区域 -->
        <div class="data-card non-resident-card">
          <div class="card-title">当月非居民用电交费（万元）</div>
          <div class="main-number">
            <span class="num-part">1</span>
            <span class="num-part">.</span>
            <span class="num-part">1</span>
            <span class="num-part">4</span>
          </div>
          <div class="sub-data-group">
            <div class="sub-data">
              <div class="sub-label">当日缴费金额</div>
              <div class="sub-value">1.14<span class="unit">万元</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">当日缴费笔数</div>
              <div class="sub-value">1<span class="unit">笔</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">当月缴费笔数</div>
              <div class="sub-value">1<span class="unit">笔</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">本年缴费金额</div>
              <div class="sub-value">1.14<span class="unit">万元</span></div>
            </div>
            <div class="sub-data">
              <div class="sub-label">本年缴费笔数</div>
              <div class="sub-value">1<span class="unit">笔</span></div>
            </div>
          </div>
        </div>
      </div>



      <!-- 进入系统提示 -->
      <div class="login-prompt">
        <div class="prompt-text">点击任意位置进入系统</div>
        <div class="prompt-arrow">👇</div>
      </div>
      <button class="enter-btn" @click="enterSystem">进入系统</button>
    </div>

    <!-- 右下角系统状态 -->
    <div class="corner-info">
      <div class="power-status">
        <div class="status-dot active"></div>
        <span>电费系统在线</span>
      </div>
      <div class="version">V1.0.0</div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'ElectricityMonitorScreensaver',
  setup() {
    const router = useRouter()
    const currentTime = ref('')
    const currentDate = ref('')
    const currentWeek = ref('')
    const pulseSize = ref(0)
    const isPulsing = ref(false)
    const dynamicLines = ref([])

    // 更新时间/日期/星期
    const updateTime = () => {
      const now = new Date()
      // 时间：HH:MM:SS
      currentTime.value = now.toLocaleTimeString('zh-CN', { 
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
      // 日期：YYYY-MM-DD
      currentDate.value = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
      // 星期
      const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      currentWeek.value = weeks[now.getDay()]
    }

    // 电力脉冲动画（保留原逻辑）
    const startPulseAnimation = () => {
      if (isPulsing.value) return
      isPulsing.value = true
      pulseSize.value = 0
      const animate = () => {
        if (pulseSize.value < 100) {
          pulseSize.value += 2
          requestAnimationFrame(animate)
        } else {
          isPulsing.value = false
          setTimeout(() => Math.random() > 0.3 && startPulseAnimation(), Math.random() * 3000 + 1000)
        }
      }
      animate()
    }

    // 创建动态网格线（保留原逻辑）
    const createDynamicLines = () => {
      const lines = []
      const lineCount = 25
      for (let i = 0; i < lineCount; i++) {
        lines.push({
          style: {
            top: `${Math.random() * 100}%`,
            left: `${Math.random() * 100}%`,
            opacity: Math.random() * 0.4 + 0.1,
            animationDelay: `${Math.random() * 5}s`,
            animationDuration: `${Math.random() * 4 + 3}s`
          }
        })
      }
      dynamicLines.value = lines
    }

    // 进入系统 - 修复：跳转到登录页面
    const enterSystem = () => {
      router.push('/login') // 修改为正确的登录页面路由
    }

    onMounted(() => {
      updateTime()
      createDynamicLines()
      startPulseAnimation()
      // 定时更新时间
      const timeInterval = setInterval(updateTime, 1000)
      // 定时刷新网格线
      const lineInterval = setInterval(createDynamicLines, 30000)
      // 清理定时器
      onUnmounted(() => {
        clearInterval(timeInterval)
        clearInterval(lineInterval)
      })
    })

    // 脉冲样式计算
    const pulseStyle = {
      width: `${pulseSize.value * 3}px`,
      height: `${pulseSize.value * 3}px`,
      opacity: (100 - pulseSize.value) / 100
    }

    return {
      currentTime,
      currentDate,
      currentWeek,
      dynamicLines,
      pulseStyle,
      enterSystem
    }
  }
}
</script>

<style scoped>
/* 基础屏保容器 - 修改为垂直居中 */
.screensaver {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #0a1929; /* 监控大屏深色背景 */
  display: flex;
  flex-direction: column; /* 改为垂直排列 */
  justify-content: center; /* 垂直居中 */
  align-items: center; /* 水平居中 */
  cursor: pointer;
  z-index: 1000;
  overflow: auto; /* 允许滚动，以防内容过长 */
  padding: 20px; /* 添加内边距，防止内容太靠边 */
}

/* 动态电网背景 */
.dynamic-grid {
  position: fixed; /* 改为fixed，使其固定在背景上 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1; /* 确保在内容下方 */
}
.grid-line {
  position: absolute;
  width: 2px;
  height: 120px;
  background: linear-gradient(to bottom, transparent, #00e5ff, transparent);
  animation: gridLineFloat linear infinite;
}
@keyframes gridLineFloat {
  0% { transform: translateY(-120px) rotate(45deg); }
  100% { transform: translateY(calc(100vh + 120px)) rotate(45deg); }
}

/* 电力脉冲效果 */
.power-pulse {
  position: fixed; /* 改为fixed */
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 229, 255, 0.2), transparent 70%);
  pointer-events: none;
  transform: translate(-50%, -50%);
  top: 50%;
  left: 50%;
  z-index: -1; /* 确保在内容下方 */
}

/* 监控大屏内容容器 - 修改为垂直居中布局 */
.monitor-content {
  width: 90%;
  max-width: 1200px; /* 适当减小最大宽度，使内容更集中 */
  padding: 30px;
  background: rgba(10, 25, 41, 0.85);
  border: 2px solid rgba(0, 229, 255, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(8px);
  box-shadow: 0 0 40px rgba(0, 229, 255, 0.15);
  animation: contentFloat 8s ease-in-out infinite;
  z-index: 2;
  margin: auto; /* 自动外边距，辅助居中 */
  display: flex;
  flex-direction: column;
  justify-content: center; /* 内容垂直居中 */
  cursor: default; /* 防止内容区域也触发点击 */
  min-height: auto; /* 不设置最小高度，让内容自然高度 */
}

@keyframes contentFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* 大屏标题+归属 - 调整位置和样式 */
.monitor-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.2);
  position: relative;
}
.monitor-title {
  color: #00e5ff;
  font-size: 2.2rem;
  font-weight: bold;
  text-shadow: 0 0 15px rgba(0, 229, 255, 0.5);
  margin: 0 auto; /* 标题自动居中 */
  text-align: center;
  flex: 1;
}
.monitor-belong {
  color: #90caf9;
  font-size: 1rem;
  background: rgba(0, 229, 255, 0.1);
  padding: 6px 12px;
  border-radius: 6px;
  white-space: nowrap; /* 防止文字换行 */
  position: absolute;
  right: 0;
}

/* 时间区域 - 调整为居中显示 */
.time-area {
  display: flex;
  justify-content: center; /* 改为居中 */
  gap: 20px;
  margin-bottom: 25px;
  color: #90caf9;
  font-size: 1.1rem;
  flex-wrap: wrap; /* 允许在小屏幕上换行 */
}
.current-week, .current-date, .current-time {
  padding: 4px 12px; /* 增加水平内边距 */
  background: rgba(16, 40, 68, 0.7);
  border-radius: 6px;
  text-align: center;
}
.current-time {
  color: #00e5ff;
  font-weight: bold;
  background: rgba(0, 229, 255, 0.2); /* 突出显示当前时间 */
}

/* 核心数据容器 */
.data-container {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  justify-content: center; /* 使两个卡片居中 */
}
/* 数据卡片 */
.data-card {
  flex: 1;
  min-width: 500px;
  max-width: 650px; /* 增大最大宽度 */
  background: rgba(16, 40, 68, 0.8);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 0 30px rgba(0, 0, 255, 0.15);
}
.card-title {
  color: #90caf9;
  font-size: 1.6rem;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
  text-align: center; /* 标题居中 */
}
/* 主数字（如0.17） */
.main-number {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 25px;
  justify-content: center; /* 数字居中 */
}
.num-part {
  color: #00e5ff;
  font-size: 4.5rem;
  font-weight: bold;
  text-shadow: 0 0 15px rgba(0, 229, 255, 0.6);
}
/* 子数据组 */
.sub-data-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.sub-data {
  flex: 1;
  min-width: 150px;
  background: rgba(20, 50, 80, 0.7);
  padding: 15px;
  border-radius: 10px;
  text-align: center; /* 子数据内容居中 */
}
.sub-label {
  color: #90caf9;
  font-size: 1.1rem;
  margin-bottom: 6px;
}
.sub-value {
  color: #4fc3f7;
  font-size: 1.4rem;
  font-weight: bold;
}
.unit {
  font-size: 1rem;
  color: #90caf9;
  margin-left: 3px;
}

/* 指标卡片区域 */
.indicator-group {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 35px;
  flex-wrap: wrap;
}
.indicator-card {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(20, 50, 80, 0.8);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 12px;
  color: #00e5ff;
  text-align: center;
  box-shadow: 0 0 15px rgba(0, 229, 255, 0.1);
  animation: indicatorPulse 3s infinite;
}
@keyframes indicatorPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
.indicator-icon {
  font-size: 2.5rem;
  margin-bottom: 8px;
}
.indicator-label {
  font-size: 0.95rem;
}

/* 进入系统提示 */
.login-prompt {
  text-align: center;
  margin-bottom: 20px;
  animation: promptBlink 2s infinite;
}
@keyframes promptBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
.prompt-text {
  color: #90caf9;
  font-size: 1.1rem;
}
.prompt-arrow {
  color: #00e5ff;
  font-size: 1.8rem;
  margin-top: 8px;
}
.enter-btn {
  display: block;
  margin: 0 auto;
  padding: 12px 35px;
  background: linear-gradient(135deg, #0a1929, #00e5ff);
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 0 15px rgba(0, 229, 255, 0.3);
  transition: all 0.3s;
}
.enter-btn:hover {
  background: linear-gradient(135deg, #00e5ff, #0a1929);
  transform: translateY(-3px);
}

/* 右下角状态 */
.corner-info {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  gap: 15px;
  color: #90caf9;
  font-size: 0.9rem;
  z-index: 3;
}
.power-status {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(16, 40, 68, 0.8);
  padding: 6px 10px;
  border-radius: 4px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4caf50;
  box-shadow: 0 0 8px #4caf50;
  animation: statusPulse 2s infinite;
}
@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
.version {
  opacity: 0.7;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .monitor-content {
    width: 95%;
    padding: 20px;
  }
  
  .monitor-title { 
    font-size: 1.8rem; 
    margin-right: 10px; /* 在大标题和归属之间增加间距 */
  }
  
  .monitor-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .monitor-belong {
    margin-top: 5px;
  }
  
  .main-number .num-part { 
    font-size: 2.5rem; 
  }
  
  .data-card { 
    min-width: 300px; 
    max-width: 100%;
  }
  
  .time-area {
    gap: 10px;
  }
  
  .current-week, .current-date, .current-time {
    padding: 4px 8px;
    font-size: 1rem;
  }
  
  .indicator-card {
    width: 100px;
    height: 100px;
  }
  
  .indicator-icon {
    font-size: 2rem;
  }
  
  .indicator-label {
    font-size: 0.85rem;
  }
}

@media (max-width: 768px) {
  .monitor-title {
    font-size: 1.5rem;
  }
  
  .data-container {
    flex-direction: column;
    align-items: center;
  }
  
  .data-card {
    width: 100%;
    min-width: unset;
  }
  
  .sub-data-group {
    justify-content: center;
  }
  
  .sub-data {
    min-width: 100px;
  }
  
  .indicator-group {
    gap: 10px;
  }
  
  .indicator-card {
    width: 80px;
    height: 80px;
  }
  
  .indicator-icon {
    font-size: 1.5rem;
    margin-bottom: 5px;
  }
  
  .indicator-label {
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .monitor-content {
    padding: 15px;
  }
  
  .monitor-title {
    font-size: 1.3rem;
  }
  
  .time-area {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  
  .enter-btn {
    padding: 10px 25px;
    font-size: 1rem;
  }
  
  .corner-info {
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }
}
</style>