<template>
  <div class="services-panel">
    <h2>电力服务内容</h2>
    <div class="service-list">
      <!-- 加载中占位 -->
      <div v-if="loading" class="loading-placeholder">
        <div class="loading-spinner"></div>
        <p>加载服务列表中...</p>
      </div>
      
      <!-- 服务列表（后端数据） -->
      <div 
        v-else
        v-for="service in filteredServices" 
        :key="service.id"
        class="service-item" 
        @click="selectService(service.name)"
      >
        <div class="service-icon">{{ getServiceIcon(service.icon, service.name) }}</div>
        <div class="service-name">{{ service.name }}</div>
      </div>
      
    <div v-if="!loading && filteredServices.length === 0" class="empty-placeholder">
      <p>暂无服务数据</p>
    </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
// 导入后端API接口
import { getServices } from '@/services/api.js';

export default {
  name: 'ServicesPanel',
  emits: ['service-selected'],
  setup(props, { emit }) {
    // 响应式数据：存储后端返回的服务列表
    const services = ref([]);
    // 加载状态：用于显示加载动画
    const loading = ref(true);
    
    // 图标映射：将占位符文本转换为实际emoji图标
    const iconMap = {
      'icon-electric': '💸',
      'icon-repair': '🔧',
      'icon-consult': '📞',
      'icon-payment': '💳',
      '文件读取': '📷' // 文件读取使用摄像头图标
    };

    // 获取服务图标（处理占位符文本）
    const getServiceIcon = (icon, serviceName) => {
      // 优先检查服务名称对应的图标映射
      if (iconMap[serviceName]) {
        return iconMap[serviceName];
      }
      // 如果是占位符文本，转换为实际图标
      if (iconMap[icon]) {
        return iconMap[icon];
      }
      // 如果图标已经是有效的emoji或字符，直接返回
      if (icon && icon.length <= 2 && icon !== serviceName) {
        return icon;
      }
      // 根据服务名称提供默认图标
      if (serviceName === '电力业务') return '⚡';
      if (serviceName === '用电咨询') return '💡';
      if (serviceName === '文件读取') return '📷';
      if (serviceName === '故障报修') return '🔧';
      if (serviceName === '电表问题') return '📊';
      if (serviceName === '电费查询') return '💸';
      // 默认返回空（过滤掉无效图标）
      return '';
    };

    // 过滤掉没有有效图标的服务项
    const filteredServices = ref([]);

    // 从后端加载服务列表
    const loadServices = async () => {
      try {
        loading.value = true;
        // 调用后端接口
        const response = await getServices();
        // 处理响应格式，确保与后端返回一致
        const serviceData = response?.data || response || [];
        // 赋值给响应式数据
        services.value = Array.isArray(serviceData) ? serviceData : [];
        
        // 过滤出有有效图标的服务项，并排除不需要的服务
        filteredServices.value = services.value.filter(service => {
          const icon = getServiceIcon(service.icon, service.name);
          // 排除的服务列表
          const excludedServices = ['访民问需', '上门服务', '缴费方式', '政策解读', '业务咨询', '安全宣传'];
          return icon !== '' && !excludedServices.includes(service.name);
        });
        
        // 添加文件读取服务（如果还没有的话）
        const hasFileReadService = filteredServices.value.some(service => service.name === '文件读取');
        if (!hasFileReadService) {
          filteredServices.value.push({
            id: 999, // 使用一个较大的ID，避免与后端数据冲突
            name: '文件读取',
            icon: '📷'
          });
        }
        
        // 自定义服务顺序（按照用户要求）
        const customOrder = ['电费查询', '电力业务', '用电咨询', '文件读取', '故障报修', '电表问题'];
        filteredServices.value.sort((a, b) => {
          const indexA = customOrder.indexOf(a.name);
          const indexB = customOrder.indexOf(b.name);
          // 有顺序的服务按顺序排列，没有顺序的服务放在后面
          if (indexA !== -1 && indexB !== -1) {
            return indexA - indexB;
          } else if (indexA !== -1) {
            return -1;
          } else if (indexB !== -1) {
            return 1;
          } else {
            return 0;
          }
        });
        
        console.log("后端服务列表加载完成：", services.value);
        console.log("过滤后的服务列表：", filteredServices.value);
      } catch (error) {
        console.error("加载服务列表失败：", error);
        // 加载失败时使用兜底数据，保证页面不空白
        services.value = [
          { id: 1, name: '电费查询', icon: '💸' },
          { id: 2, name: '电力业务', icon: '⚡' },
          { id: 3, name: '用电咨询', icon: '💡' },
          { id: 4, name: '文件读取', icon: '📷' },
          { id: 5, name: '故障报修', icon: '🔧' },
          { id: 6, name: '电表问题', icon: '📊' }
        ];
        filteredServices.value = services.value;
      } finally {
        loading.value = false; // 无论成功失败，结束加载状态
      }
    };
    
    const selectService = (serviceName) => {
      emit('service-selected', serviceName)
    }
    
    // 组件挂载时加载后端数据
    onMounted(() => {
      loadServices();
    });
    
    return {
      services,
      filteredServices,
      loading,
      selectService,
      getServiceIcon
    }
  }
}
</script>

<style scoped>
/* 服务面板容器：占满父容器空间 */
.services-panel {
  flex: 1; /* 关键：占满父容器分配的所有空间 */
  min-width: 300px;
  max-width: 100%;
  background: rgba(13, 43, 75, 0.8);
  border-radius: 15px;
  padding: 25px; /* 增加内边距，让内容更舒展 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column; /* 垂直布局，让列表占满剩余空间 */
  height: 100%; /* 确保高度占满 */
  box-sizing: border-box; /* 防止padding导致溢出 */
}

/* 标题样式优化 */
h2 {
  color: #4fc3f7;
  margin: 0 0 30px 0; /* 调整间距 */
  font-size: 2rem; /* 进一步放大标题 */
  text-align: center;
  text-shadow: 0 0 15px rgba(79, 195, 247, 0.6);
}

/* 服务列表：占满剩余空间，均匀分布 */
.service-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 固定2列布局，更规整 */
  gap: 30px; /* 增加间距，避免拥挤 */
  flex: 1; /* 关键：占满面板剩余空间 */
  align-content: center; /* 垂直居中分布 */
}

/* 服务项：进一步放大尺寸，增强视觉效果 */
.service-item {
  background: rgba(30, 70, 110, 0.6);
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 20px;
  padding: 50px 25px; /* 大幅增加内边距，放大点击区域 */
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%; /* 确保每个服务项高度一致 */
}

/* hover效果增强 */
.service-item:hover {
  background: rgba(0, 100, 255, 0.7);
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 150, 255, 0.5);
  border-color: #4fc3f7;
}

/* 服务图标：大幅放大尺寸 */
.service-icon {
  font-size: 70px; /* 大幅放大图标 */
  margin-bottom: 20px;
  color: #ffffff;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.7);
}

/* 服务名称：大幅放大字体 */
.service-name {
  font-size: 1.8rem; /* 大幅放大文字 */
  color: #e0f7ff;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(224, 247, 255, 0.5);
}

/* 加载中占位样式 */
.loading-placeholder {
  grid-column: 1 / -1; /* 占满所有列 */
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

/* 空数据占位样式 */
.empty-placeholder {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 0;
  color: #90caf9;
  font-size: 1rem;
}

/* 响应式适配：小屏幕保持美观 */
@media (max-width: 768px) {
  .services-panel {
    padding: 20px;
    min-width: unset;
  }
  
  .service-list {
    grid-template-columns: repeat(2, 1fr); /* 小屏幕仍保持2列 */
    gap: 15px;
  }
  
  .service-icon {
    font-size: 32px;
  }
  
  .service-name {
    font-size: 1rem;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .service-list {
    grid-template-columns: 1fr; /* 超小屏幕1列布局 */
  }
  
  .service-item {
    padding: 20px 15px;
  }
}
</style>