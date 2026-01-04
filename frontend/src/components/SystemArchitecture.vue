<template>
  <div class="system-architecture">
    <h2>电力智能服务系统架构</h2>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载架构数据中...</p>
    </div>
    
    <!-- 架构图（兼容兜底数据） -->
    <div v-else class="architecture-diagram">
      <!-- 先判断数据是否有效 -->
      <template v-if="Array.isArray(architectureData) && architectureData.length > 0">
        <div 
          v-for="(component, index) in architectureData" 
          :key="component.id || index"  <!-- 兼容无id的情况 -->
          class="arch-component-wrapper"  <!-- 拆分结构，避免箭头和组件冲突 -->
        >
          <div class="component-card">
            <div class="component-icon">{{ component.icon || '🔹' }}</div>  <!-- 图标兜底 -->
            <div class="component-name">{{ component.name || '未命名模块' }}</div>  <!-- 名称兜底 -->
            <div class="component-desc">{{ component.description || '暂无描述' }}</div>  <!-- 描述兜底 -->
          </div>
          
          <!-- 箭头：最后一个组件不显示，且仅当有下一个组件时显示 -->
          <div v-if="index < architectureData.length - 1" class="arch-arrow">→</div>
        </div>
      </template>
      
      <!-- 空数据提示（兼容各种空数据场景） -->
      <div v-else class="empty-tip">
        暂无系统架构数据
      </div>
    </div>
  </div>
</template>

<script>
// 1. 确保正确导入Vue3的组合式API
import { ref, onMounted } from 'vue'
// 2. 导入api（如果接口未定义，先注释掉，用本地兜底数据）
// 注意：如果没有api文件，先注释这行，避免报错
// import api from '@/services/api.js';

export default {
  name: 'SystemArchitecture',
  setup() {
    // 响应式数据：存储架构数据（初始化为空数组）
    const architectureData = ref([]);
    // 加载状态
    const loading = ref(true);
    
    // 3. 定义接口方法（如果api.js中没有，先临时定义，避免报错）
    const getSystemArchitecture = async () => {
      try {
        // 先注释掉真实接口，用模拟数据测试（解决接口未定义报错）
        // const res = await api.get('/system/architecture');
        // return res;
        
        // 模拟后端返回数据（测试用，后续替换为真实接口）
        return [
          { id: 1, name: '感知层', icon: '📡', description: '电力设备数据采集' },
          { id: 2, name: '网络层', icon: '🌐', description: '数据传输与通信' },
          { id: 3, name: '平台层', icon: '🖥️', description: '数据处理与存储' },
          { id: 4, name: '应用层', icon: '📱', description: '智能服务与展示' }
        ];
      } catch (err) {
        console.error("接口调用失败，使用兜底数据：", err);
        // 接口失败时的兜底数据
        return [
          { id: 1, name: '感知层', icon: '📡', description: '电力设备数据采集' },
          { id: 2, name: '网络层', icon: '🌐', description: '数据传输与通信' },
          { id: 3, name: '平台层', icon: '🖥️', description: '数据处理与存储' },
          { id: 4, name: '应用层', icon: '📱', description: '智能服务与展示' }
        ];
      }
    };
    
    // 加载架构数据（增加错误捕获，避免组件崩溃）
    const loadArchitectureData = async () => {
      try {
        loading.value = true;
        // 调用接口获取数据
        const data = await getSystemArchitecture();
        // 校验数据格式，确保是数组
        if (Array.isArray(data)) {
          architectureData.value = data;
        } else {
          // 数据格式错误时用兜底数据
          architectureData.value = [
            { id: 1, name: '感知层', icon: '📡', description: '电力设备数据采集' },
            { id: 2, name: '网络层', icon: '🌐', description: '数据传输与通信' }
          ];
        }
        console.log("系统架构数据加载完成：", architectureData.value);
      } catch (error) {
        console.error("加载架构数据失败：", error);
        // 任何报错都用兜底数据，避免页面空白
        architectureData.value = [
          { id: 1, name: '感知层', icon: '📡', description: '电力设备数据采集' },
          { id: 2, name: '网络层', icon: '🌐', description: '数据传输与通信' },
          { id: 3, name: '平台层', icon: '🖥️', description: '数据处理与存储' },
          { id: 4, name: '应用层', icon: '📱', description: '智能服务与展示' }
        ];
      } finally {
        // 无论成功失败，都结束加载状态
        loading.value = false;
      }
    };
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadArchitectureData();
    });
    
    return {
      architectureData,
      loading
    }
  }
}
</script>

<!-- 关键修复：补全style标签的开头，且保留scoped属性（样式仅作用于当前组件） -->
<style scoped>
.system-architecture {
  background: rgba(13, 43, 75, 0.8);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
  min-height: 300px; /* 避免高度塌陷 */
}

h2 {
  color: #4fc3f7;
  margin-bottom: 20px;
  text-align: center;
  text-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #4fc3f7;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(79, 195, 247, 0.3);
  border-radius: 50%;
  border-top-color: #4fc3f7;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 架构图容器 - 修复布局问题 */
.architecture-diagram {
  display: flex;
  justify-content: center;  /* 居中对齐 */
  align-items: center;
  margin-top: 15px;
  flex-wrap: wrap;
  gap: 5px; /* 减小间距，避免箭头错位 */
}

/* 架构组件容器 - 拆分箭头和组件 */
.arch-component-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-width: 150px;
  padding: 0 5px;
}

/* 组件卡片样式 */
.component-card {
  background: rgba(30, 70, 110, 0.6);
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 10px;
  padding: 20px 10px;
  width: 100%;
  text-align: center;
  transition: all 0.3s ease;
}

.component-card:hover {
  background: rgba(0, 100, 255, 0.7);
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 150, 255, 0.4);
}

.component-icon {
  font-size: 32px;
  color: #ffffff;
  margin-bottom: 10px;
}

.component-name {
  color: #4fc3f7;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 5px;
}

.component-desc {
  color: #e0f7ff;
  font-size: 0.85rem;
  line-height: 1.4;
}

/* 箭头样式 - 修复显示问题 */
.arch-arrow {
  font-size: 28px;
  color: #4fc3f7;
  margin: 0 10px;
  text-shadow: 0 0 8px rgba(79, 195, 247, 0.5);
  flex-shrink: 0; /* 防止箭头被压缩 */
}

/* 空数据提示 */
.empty-tip {
  width: 100%;
  text-align: center;
  padding: 40px 0;
  color: #90caf9;
  font-size: 1rem;
}

/* 响应式适配 - 修复小屏布局 */
@media (max-width: 768px) {
  .architecture-diagram {
    flex-direction: column;
  }
  
  .arch-component-wrapper {
    flex-direction: column;
    width: 100%;
    min-width: unset;
    margin-bottom: 10px;
  }
  
  .arch-arrow {
    transform: rotate(90deg);
    margin: 15px 0;
  }
}
</style>