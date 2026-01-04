<template>
  <div class="power-chart-section">
    <h2>居民用电系统运行分析图</h2>
    
    <div class="chart-container">
      <!-- 图表标题+切换栏 -->
      <div class="chart-header">
        <div class="tab-group">
          <button class="tab-btn" :class="{ active: activeTab === 'voltage' }" @click="activeTab = 'voltage'">电压</button>
          <button class="tab-btn" :class="{ active: activeTab === 'current' }" @click="activeTab = 'current'">电流</button>
          <button class="tab-btn" :class="{ active: activeTab === 'power' }" @click="activeTab = 'power'">功率</button>
          <button class="tab-btn" :class="{ active: activeTab === 'energy' }" @click="activeTab = 'energy'">电能示值</button>
          <button class="tab-btn" :class="{ active: activeTab === 'factor' }" @click="activeTab = 'factor'">功率因数</button>
        </div>
        <div class="time-select">
          <select v-model="timeRange" @change="handleTimeRangeChange">
            <option value="24h">24点曲线</option>
            <option value="12h">12点曲线</option>
            <option value="real">实时曲线</option>
          </select>
        </div>
      </div>
      
      <!-- 主图表区域 -->
      <div class="main-chart-area">
        <!-- Y轴标签 -->
        <div class="y-axis">
          <div class="y-unit">{{ unitMap[activeTab] }}</div>
          <div class="y-scale">
            <div>{{ maxValue[activeTab] }}</div>
            <div>{{ (maxValue[activeTab] * 0.8).toFixed(1) }}</div>
            <div>{{ (maxValue[activeTab] * 0.6).toFixed(1) }}</div>
            <div>{{ (maxValue[activeTab] * 0.4).toFixed(1) }}</div>
            <div>{{ (maxValue[activeTab] * 0.2).toFixed(1) }}</div>
            <div>0</div>
          </div>
        </div>
        
        <!-- 图表画布 -->
        <div class="chart-canvas-container">
          <canvas ref="chartCanvas" class="chart-canvas"></canvas>
          
          <!-- X轴标签 -->
          <div class="x-axis">
            <div class="x-scale">
              <div>00:00</div>
              <div>01:00</div>
              <div>03:00</div>
              <div>04:00</div>
              <div>05:00</div>
              <div>06:00</div>
              <div>07:00</div>
              <div>08:00</div>
              <div>09:00</div>
              <div>10:00</div>
              <div>11:00</div>
              <div>12:00</div>
              <div>13:00</div>
              <div>14:00</div>
              <div>15:00</div>
              <div>16:00</div>
              <div>17:00</div>
              <div>18:00</div>
              <div>19:00</div>
              <div>20:00</div>
              <div>21:00</div>
              <div>22:00</div>
              <div>23:00</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 图例（对应三相+零线） -->
      <div class="chart-legend">
        <div class="legend-items">
          <div class="legend-item">
            <div class="legend-color" style="background-color: #f44336;"></div>
            <div class="legend-text">A相</div>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #4caf50;"></div>
            <div class="legend-text">B相</div>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #ff9800;"></div>
            <div class="legend-text">C相</div>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #2196f3;"></div>
            <div class="legend-text">零线</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'
// 移除不存在的API调用，使用模拟数据解决404错误

export default {
  name: 'PowerChart',
  setup() {
    const chartCanvas = ref(null)
    const ctx = ref(null)
    const activeTab = ref('current') // 默认选中“电流”
    const timeRange = ref('24h') // 新增：时间范围响应式变量
    
    // 各指标的单位和最大值（用于Y轴刻度）
    const unitMap = {
      voltage: '单位 (V)',
      current: '单位 (A)',
      power: '单位 (kW)',
      energy: '单位 (kWh)',
      factor: '单位 (cosφ)'
    }
    const maxValue = {
      voltage: 240,
      current: 1,
      power: 10,
      energy: 100,
      factor: 1
    }
    
    // 响应式数据：存储从后端获取的电力数据
    const chartData = ref({
      voltage: {},
      current: {},
      power: {},
      energy: {},
      factor: {}
    });

    // 模拟数据（兜底用）
    const defaultChartData = {
      // 电流数据（对应参考图）
      current: {
        A: [0.4, 0.4, 0.4, 0.38, 0.6, 0.62, 0.65, 0.7, 0.85, 0.92, 0.8, 0.75, 0.7, 0.6, 0.62, 0.7, 0.85, 0.8, 0.7, 0.65, 0.6, 0.62, 0.6],
        B: [0.38, 0.38, 0.38, 0.35, 0.58, 0.6, 0.63, 0.68, 0.82, 0.9, 0.78, 0.73, 0.68, 0.58, 0.6, 0.68, 0.82, 0.78, 0.68, 0.63, 0.58, 0.6, 0.58],
        C: [0.39, 0.39, 0.39, 0.36, 0.59, 0.61, 0.64, 0.69, 0.83, 0.91, 0.79, 0.74, 0.69, 0.59, 0.61, 0.69, 0.83, 0.79, 0.69, 0.64, 0.59, 0.61, 0.59],
        N: [0.1, 0.1, 0.1, 0.09, 0.15, 0.16, 0.17, 0.18, 0.2, 0.22, 0.2, 0.19, 0.18, 0.16, 0.17, 0.18, 0.2, 0.19, 0.18, 0.17, 0.16, 0.17, 0.16]
      },
      // 其他指标数据（示例）
      voltage: {
        A: [220, 221, 220, 219, 222, 223, 224, 222, 221, 220, 219, 220, 221, 222, 223, 222, 221, 220, 219, 220, 221, 222, 221],
        B: [219, 220, 219, 218, 221, 222, 223, 221, 220, 219, 218, 219, 220, 221, 222, 221, 220, 219, 218, 219, 220, 221, 220],
        C: [221, 222, 221, 220, 223, 224, 225, 223, 222, 221, 220, 221, 222, 223, 224, 223, 222, 221, 220, 221, 222, 223, 222],
        N: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      },
      power: {
        A: [2, 2.1, 2, 1.9, 3, 3.2, 3.5, 4, 5, 5.5, 5, 4.5, 4, 3, 3.2, 4, 5, 4.8, 4, 3.5, 3, 3.2, 3],
        B: [1.9, 2, 1.9, 1.8, 2.9, 3.1, 3.4, 3.9, 4.9, 5.4, 4.9, 4.4, 3.9, 2.9, 3.1, 3.9, 4.9, 4.7, 3.9, 3.4, 2.9, 3.1, 2.9],
        C: [2.1, 2.2, 2.1, 2, 3.1, 3.3, 3.6, 4.1, 5.1, 5.6, 5.1, 4.6, 4.1, 3.1, 3.3, 4.1, 5.1, 4.9, 4.1, 3.6, 3.1, 3.3, 3.1],
        N: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      },
      energy: {
        A: [10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54],
        B: [9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53],
        C: [11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55],
        N: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      },
      factor: {
        A: [0.9, 0.9, 0.9, 0.89, 0.92, 0.93, 0.94, 0.95, 0.96, 0.97, 0.96, 0.95, 0.94, 0.92, 0.93, 0.95, 0.96, 0.95, 0.94, 0.93, 0.92, 0.93, 0.92],
        B: [0.89, 0.89, 0.89, 0.88, 0.91, 0.92, 0.93, 0.94, 0.95, 0.96, 0.95, 0.94, 0.93, 0.91, 0.92, 0.94, 0.95, 0.94, 0.93, 0.92, 0.91, 0.92, 0.91],
        C: [0.91, 0.91, 0.91, 0.9, 0.93, 0.94, 0.95, 0.96, 0.97, 0.98, 0.97, 0.96, 0.95, 0.93, 0.94, 0.96, 0.97, 0.96, 0.95, 0.94, 0.93, 0.94, 0.93],
        N: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }
    };
    
    // 初始化电力数据（使用模拟数据避免404错误）
    const initPowerData = () => {
      try {
        // 直接使用模拟数据，避免调用不存在的后端接口
        chartData.value = defaultChartData;
        console.log("使用模拟电力数据完成：", chartData.value);
        
        // 重新渲染图表
        renderChart();
      } catch (error) {
        console.error("初始化电力数据失败：", error);
      }
    };

    // 时间范围切换处理
    const handleTimeRangeChange = () => {
      console.log('切换时间范围：', timeRange.value);
      // 可根据时间范围过滤/重新加载数据
      renderChart();
    }
    
    // 绘制网格
    const drawGrid = () => {
      const canvas = chartCanvas.value
      if (!canvas || !ctx.value) return
      
      const width = canvas.width
      const height = canvas.height
      
      // 清空画布
      ctx.value.clearRect(0, 0, width, height)
      
      // 绘制背景
      ctx.value.fillStyle = 'rgba(10, 25, 41, 0.7)'
      ctx.value.fillRect(0, 0, width, height)
      
      // 绘制网格线
      ctx.value.strokeStyle = 'rgba(0, 150, 255, 0.2)'
      ctx.value.lineWidth = 1
      
      // 垂直网格线（24小时）
      for (let i = 0; i <= 24; i++) {
        const x = (i / 24) * width
        ctx.value.beginPath()
        ctx.value.moveTo(x, 0)
        ctx.value.lineTo(x, height)
        ctx.value.stroke()
      }
      
      // 水平网格线
      for (let i = 0; i <= 5; i++) {
        const y = (i / 5) * height
        ctx.value.beginPath()
        ctx.value.moveTo(0, y)
        ctx.value.lineTo(width, y)
        ctx.value.stroke()
      }
    }
    
    // 绘制曲线
    const drawCurve = () => {
      const canvas = chartCanvas.value
      if (!canvas || !ctx.value) return
      
      const width = canvas.width
      const height = canvas.height
      // 使用响应式数据，兜底到模拟数据
      const data = chartData.value[activeTab.value] || defaultChartData[activeTab.value];
      const max = maxValue[activeTab.value]
      
      // 曲线颜色（对应参考图）
      const colors = {
        A: '#f44336',
        B: '#4caf50',
        C: '#ff9800',
        N: '#2196f3'
      }
      
      // 绘制每条曲线
      Object.keys(data).forEach(phase => {
        const points = data[phase]
        const color = colors[phase]
        
        // 绘制曲线
        ctx.value.beginPath()
        ctx.value.strokeStyle = color
        ctx.value.lineWidth = 2
        ctx.value.lineJoin = 'round'
        
        // 移动到第一个点
        const firstX = 0
        const firstY = height - (points[0] / max) * height
        ctx.value.moveTo(firstX, firstY)
        
        // 绘制曲线（修复语法错误：移除多余的括号）
        for (let i = 1; i < points.length; i++) {
          const x = (i / (points.length - 1)) * width
          const y = height - (points[i] / max) * height
          
          // 贝塞尔曲线平滑
          const prevX = ((i - 1) / (points.length - 1)) * width
          const prevY = height - (points[i - 1] / max) * height
          
          const cpX1 = prevX + (x - prevX) * 0.5
          const cpY1 = prevY
          const cpX2 = cpX1
          const cpY2 = y
          
          ctx.value.bezierCurveTo(cpX1, cpY1, cpX2, cpY2, x, y)
        }
        ctx.value.stroke()
        
        // 绘制数据点
        for (let i = 0; i < points.length; i++) {
          const x = (i / (points.length - 1)) * width
          const y = height - (points[i] / max) * height
          
          ctx.value.beginPath()
          ctx.value.arc(x, y, 3, 0, Math.PI * 2)
          ctx.value.fillStyle = color
          ctx.value.fill()
          ctx.value.strokeStyle = '#ffffff'
          ctx.value.lineWidth = 1
          ctx.value.stroke()
        }
      })
    }
    
    // 绘制图表
    const renderChart = () => {
      drawGrid()
      drawCurve()
    }
    
    // 初始化画布
    const initCanvas = () => {
      if (!chartCanvas.value) return
      
      const canvas = chartCanvas.value
      const container = canvas.parentElement
      
      // 设置画布尺寸
      canvas.width = container.clientWidth
      canvas.height = 300
      
      // 获取2D上下文
      ctx.value = canvas.getContext('2d')
      
      // 初始绘制
      renderChart()
    }

    // 窗口resize处理函数（抽离以便解绑）
    const handleResize = () => {
      initCanvas()
      renderChart()
    }
    
    onMounted(() => {
      initCanvas();
      // 初始化电力数据（使用模拟数据）
      initPowerData();
      
      // 窗口 resize 时重新绘制
      window.addEventListener('resize', handleResize)
    })

    // 组件卸载时解绑事件（防止内存泄漏）
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
    })
    
    // 监听标签切换，重新绘制图表
    watch(activeTab, () => {
      renderChart()
    })

    // 监听时间范围切换，重新绘制图表
    watch(timeRange, () => {
      renderChart()
    })
    
    return {
      chartCanvas,
      activeTab,
      timeRange, // 新增：暴露时间范围变量
      unitMap,
      maxValue,
      handleTimeRangeChange // 新增：暴露时间范围切换方法
    }
  }
}
</script>

<style scoped>
.power-chart-section {
  background: rgba(13, 43, 75, 0.8);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
}

h2 {
  color: #4fc3f7;
  margin-bottom: 20px;
  text-align: center;
}

.chart-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 标签栏样式（电压/电流/功率等） */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 150, 255, 0.3);
}
.tab-group {
  display: flex;
  gap: 5px;
}
.tab-btn {
  background: rgba(30, 70, 110, 0.6);
  color: #90caf9;
  border: 1px solid rgba(0, 150, 255, 0.2);
  border-radius: 4px 4px 0 0;
  padding: 8px 15px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}
.tab-btn.active {
  background: rgba(0, 150, 255, 0.2);
  color: #4fc3f7;
  border-bottom: 1px solid transparent;
}
.time-select select {
  background: rgba(30, 70, 110, 0.8);
  color: #e0e0e0;
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 5px;
  padding: 5px 10px;
  outline: none;
}

/* 主图表区域 */
.main-chart-area {
  display: flex;
  background: rgba(20, 50, 90, 0.5);
  border-radius: 8px;
  padding: 15px;
  border: 1px solid rgba(0, 150, 255, 0.2);
}
.y-axis {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding-right: 10px;
  width: 80px;
  flex-shrink: 0;
}
.y-unit {
  color: #4fc3f7;
  font-weight: bold;
  margin-bottom: 10px;
}
.y-scale {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  color: #90caf9;
  font-size: 0.8rem;
}
.chart-canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chart-canvas {
  width: 100%;
  height: 300px;
  background: rgba(10, 25, 41, 0.7);
  border-radius: 5px;
  border: 1px solid rgba(0, 150, 255, 0.3);
}
.x-axis {
  margin-top: 10px;
  overflow-x: auto; /* 适配小屏幕 */
}
.x-scale {
  display: flex;
  justify-content: space-between;
  width: 100%;
  color: #90caf9;
  font-size: 0.8rem;
  white-space: nowrap;
}

/* 图例样式 */
.chart-legend {
  background: rgba(20, 50, 90, 0.5);
  border-radius: 8px;
  padding: 15px;
  border: 1px solid rgba(0, 150, 255, 0.2);
}
.legend-items {
  display: flex;
  gap: 20px;
  justify-content: center;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e0e0e0;
  font-size: 0.9rem;
}
.legend-color {
  width: 15px;
  height: 15px;
  border-radius: 3px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .tab-group {
    flex-wrap: wrap;
  }
  .x-scale {
    font-size: 0.7rem;
  }
}
</style>