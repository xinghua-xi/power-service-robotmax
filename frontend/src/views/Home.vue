<template>
  <div class="container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-left">
        <div class="user-info">
          <div class="user-avatar">👤</div>
          <div class="user-details">
            <div class="user-name">{{ username }}</div>
            <div class="user-role">{{ userRole }}</div>
          </div>
        </div>
      </div>
      
      <div class="nav-center">
        <h1>电力系统实时运行监控</h1>
        <p class="nav-subtitle">西把栅供电所 - 专业电力服务</p>
      </div>
      
      <div class="nav-right">
        <button class="logout-btn" @click="handleLogout">
          <span class="logout-icon">🚪</span>
          退出登录
        </button>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 电力曲线图 -->
      <PowerChart />
      
      <!-- 服务面板和聊天面板 -->
      <div class="content-row">
        <ServicesPanel @service-selected="handleServiceSelected" />
        <ChatPanel 
          :messages="messages" 
          :is-sending="isSending"
          :is-camera-active="isCameraActive"
          :camera-placeholder-text="cameraPlaceholderText"
          @send-message="handleSendMessage"
          @camera-status-change="handleCameraStatusChange"
          ref="chatPanel"
        />
      </div>
      
      <!-- 紧急联系 -->
      <div class="content-row">
        <EmergencyContact />
      </div>
    </div>
    
    <!-- 底部信息 -->
    <div class="bottom-info">
      <p>© 2025 西把栅供电所 | 服务热线: 95598 | 当前时间: {{ currentTime }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
// 导入组件
import ServicesPanel from '@/components/ServicesPanel.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import EmergencyContact from '@/components/EmergencyContact.vue'
import PowerChart from '@/components/PowerChart.vue'
// 导入API接口
import { sendChatMessage, sendChatStream, getChatHistory } from '@/services/api.js'

export default {
  name: 'Home',
  components: {
    ServicesPanel,
    ChatPanel,
    EmergencyContact,
    PowerChart
  },
  setup() {
    const router = useRouter()
    // 聊天消息列表（响应式）
    const messages = ref([
      { id: 1, sender: 'bot', text: '您好！欢迎使用电力服务智能助手。我是您的电力服务助手，随时为您提供专业的电力服务咨询。' },
      { id: 2, sender: 'bot', text: '请问您需要什么帮助？您可以选择下方的服务分类，或直接输入您的问题。' }
    ])
    
    // 用户信息
    const username = ref('')
    const userRole = ref('')
    const currentTime = ref('')
    const chatPanel = ref(null)
    // 加载状态（避免重复发送消息）
    const isSending = ref(false)
    // 时间定时器
    let timeInterval = null
    // 摄像头状态
    const isCameraActive = ref(false)
    // 摄像头占位文本
    const cameraPlaceholderText = ref('摄像头未开启')
    
    // 更新时间（优化格式）
    const updateTime = () => {
      try {
        const now = new Date()
        currentTime.value = now.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        })
      } catch (error) {
        console.error('更新时间失败:', error)
        currentTime.value = new Date().toLocaleString()
      }
    }
    
    // 处理服务选择（增加空值校验）
    const handleServiceSelected = (service) => {
      if (!service || !chatPanel.value) return
      
      // 设置摄像头占位文本
      if (service === '文件读取') {
        cameraPlaceholderText.value = '准备进行文件读取，点击发送按钮开始'
      } else {
        cameraPlaceholderText.value = '摄像头未开启'
      }
      
      chatPanel.value.setUserInput?.(service) // 可选链操作符防止方法不存在
    }
    
    // 保存关闭连接的方法（避免重复关闭）
    let closeStreamFn = null;
    
    // 处理消息发送（调用后端API，支持流式响应和打字效果）
    const handleSendMessage = (message) => {
      // 二次兜底校验（防止子组件校验失效）
      const validMsg = message?.trim();
      if (!validMsg || validMsg.length === 0 || isSending.value) {
        // 提示用户输入内容
        alert('请输入您的问题后再发送');
        return;
      }
      
      isSending.value = true;
      // 添加用户消息到列表
      const userMsgId = Date.now();
      messages.value.push({
        id: userMsgId,
        sender: 'user',
        text: validMsg
      });
      
      // 创建机器人消息占位（用于逐字更新）
      const botMsgId = Date.now() + 1;
      const assistantMsgIndex = messages.value.push({
        id: botMsgId,
        sender: 'bot',
        text: '', // 初始为空，不设置任何兜底内容
        isStreaming: true
      }) - 1;
      
      // 检查是否为文件读取请求
      if (validMsg === '文件读取') {
        // 直接返回固定回答，不调用后端API
        messages.value[assistantMsgIndex].text = '文件由下方读取';
        messages.value[assistantMsgIndex].isStreaming = false;
        isSending.value = false;
        // 发送消息后开启摄像头
        isCameraActive.value = true;
        return;
      }
      
      // 检查是否为电力报修问题
      const lowerMsg = validMsg.toLowerCase();
      if (lowerMsg.includes('电力报修') || lowerMsg.includes('我现在需要解决电力保修问题，告诉我需要提供的文件') || lowerMsg.includes('故障报修')) {
        // 返回电力报修的默认回答
        messages.value[assistantMsgIndex].text = '您需要提供已填好的电力报修表，点击文件读取，扫描文件即可完成，静等系统回复。';
        messages.value[assistantMsgIndex].isStreaming = false;
        isSending.value = false;
        return;
      }
      
      // 调用后端流式API
      console.log('准备发送流式API请求:', {
        prompt: validMsg
      });
      
      // 使用新的sendChatStream函数
      closeStreamFn = sendChatStream(
        validMsg,
        // 接收单字/短句的回调：逐字更新聊天框
        (content) => {
          console.log('收到流式响应数据:', content);
          messages.value[assistantMsgIndex].text += content;
        },
        // 错误回调
        (err) => {
          console.error('流式请求失败:', err);
          // 只有当机器人消息为空时才显示兜底回复
          // 如果已经收到部分数据，就保留已有的数据
          if (!messages.value[assistantMsgIndex].text) {
            messages.value[assistantMsgIndex].text = '抱歉，我暂时无法回答这个问题，请稍后再试。';
          }
          messages.value[assistantMsgIndex].isStreaming = false;
          isSending.value = false;
        },
        // 完成回调
      () => {
        console.log('流式响应完成');
        messages.value[assistantMsgIndex].isStreaming = false;
        isSending.value = false;
        closeStreamFn = null; // 清空关闭方法
      }
      );
    }
    
    // 加载历史聊天记录（改进错误处理和超时配置）
    const loadChatHistory = async () => {
      try {
        // 获取或生成sessionId
        let sessionId = localStorage.getItem('chatSessionId')
        if (!sessionId) {
          sessionId = 'SESS_' + Date.now()
          localStorage.setItem('chatSessionId', sessionId)
          return; // 新会话，没有历史记录
        }
        
        // 使用Axios的超时配置（30秒），不再额外设置5秒超时
        const response = await getChatHistory(sessionId)
        
        // 处理响应格式，确保与后端返回一致
        // 后端返回格式：ApiResponse.success("获取聊天记录成功", history)
        // 响应拦截器已经返回response.data，所以直接使用response
        const history = response || []
        
        // 清空默认消息，加载历史记录（数据校验）
        if (Array.isArray(history) && history.length > 0) {
          const formattedMessages = []
          let messageId = 1
          
          // 将ChatRecord转换为前端需要的格式
          history.forEach(record => {
            if (record && record.userMessage) {
              // 添加用户消息
              formattedMessages.push({
                id: messageId++,
                sender: 'user',
                text: record.userMessage
              })
            }
            
            if (record && record.botResponse) {
              // 添加机器人回复
              formattedMessages.push({
                id: messageId++,
                sender: 'bot',
                text: record.botResponse
              })
            }
          })
          
          // 只有在有格式化消息时才替换默认消息
          if (formattedMessages.length > 0) {
            messages.value = formattedMessages
          }
        }
      } catch (error) {
        console.error('加载聊天记录失败：', error.response || error.message || error)
        // 失败时保留默认欢迎语，并显示错误提示
        messages.value = [
          { id: 1, sender: 'bot', text: '您好！欢迎使用电力服务智能助手。' },
          { id: 2, sender: 'bot', text: '请问您需要什么帮助？' }
        ]
      }
    }
    
    // 模拟机器人回复（兜底用，优化匹配逻辑）
    const getBotResponse = (userInput) => {
      if (!userInput) return '请问您有什么电力相关的问题需要咨询？'
      
      const responses = {
        '故障报修': '您好，故障报修服务已受理。请提供您的详细地址、故障现象和联系方式，我们将尽快安排维修人员处理。',
        '电力业务': '电力业务办理包括：1.新装电表 2.增容申请 3.变更用电 4.电费查询。请问您需要办理哪项具体业务？',
        '用电咨询': '用电咨询请详细描述您遇到的问题，我们会为您提供专业的解答。您可以咨询电费、用电政策、安全用电等问题。',
        '安全宣传': '安全用电提醒：请勿私拉乱接电线；定期检查家用电器；雷雨天气减少用电；远离高压线路；发现电力设施异常请及时报告。',
        '政策解读': '现行电价政策包括阶梯电价、峰谷电价等。具体标准因地区而异，请提供您所在地区或用户编号，我将为您查询详细政策。',
        '电表问题': '电表问题请提供：1.用户编号 2.电表异常现象 3.联系电话。我们将安排专业人员核查。',
        '访民问需': '感谢您的反馈！为了提供更好的服务，请告诉我们您的需求或建议，我们会认真研究并改进服务质量。',
        '上门服务': '上门服务需要预约，请提供：1.姓名 2.联系电话 3.详细地址 4.服务内容。我们将安排工作人员与您联系。'
      }
      
      // 不区分大小写匹配关键词
      const lowerInput = userInput.toLowerCase()
      for (const [service, response] of Object.entries(responses)) {
        if (lowerInput.includes(service.toLowerCase())) {
          return response
        }
      }
      
      if (lowerInput.includes('电话') || lowerInput.includes('联系')) {
        return '我们的24小时客服电话是95598，紧急情况请直接拨打。您也可以通过在线客服进行咨询。'
      }
      
      return '我已收到您的咨询。为了更好地为您服务，请选择下方的服务分类，或描述更具体的问题，我会尽力为您解答。'
    }
    
    // 处理摄像头状态变化
    const handleCameraStatusChange = (status) => {
      isCameraActive.value = status;
    }
    
    // 处理退出登录（优化资源清理）
    const handleLogout = () => {
      try {
        // 清除所有登录状态
        localStorage.removeItem('isLoggedIn')
        localStorage.removeItem('username')
        localStorage.removeItem('userRole')
        // 保留非登录相关的本地存储（可选）
        
        // 清除定时器
        if (timeInterval) clearInterval(timeInterval)
        timeInterval = null
        
        // 跳转到登录页面
        router.push('/login')
      } catch (error) {
        console.error('退出登录失败：', error)
        // 强制跳转
        router.replace('/login')
      }
    }
    
    // 检查登录状态（独立方法）
    const checkLoginStatus = () => {
      const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
      if (!isLoggedIn) {
        router.push('/login')
        return false
      }
      return true
    }
    
    onMounted(() => {
      // 先检查登录状态
      if (!checkLoginStatus()) return
      
      // 获取用户信息（兜底值）
      username.value = localStorage.getItem('username') || '电力用户'
      userRole.value = localStorage.getItem('userRole') || '普通用户'
      
      // 初始化时间
      updateTime()
      timeInterval = setInterval(updateTime, 1000)
      
      // 加载聊天历史
      loadChatHistory()
    })
    
    // 组件卸载时清理资源（强化）
    onUnmounted(() => {
      if (timeInterval) clearInterval(timeInterval)
      timeInterval = null
      // 清空引用，避免内存泄漏
      chatPanel.value = null
      // 关闭流式连接，避免内存泄漏
      if (closeStreamFn) {
        closeStreamFn();
      }
    })
    
    // 监听路由变化，提前清理资源
    watch(
      () => router.currentRoute.path,
      (newPath) => {
        if (newPath !== '/home') {
          if (timeInterval) clearInterval(timeInterval)
          timeInterval = null
        }
      }
    )
    
    return {
      messages,
      username,
      userRole,
      currentTime,
      chatPanel,
      handleServiceSelected,
      handleSendMessage,
      handleLogout,
      isSending,
      isCameraActive,
      cameraPlaceholderText,
      handleCameraStatusChange
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  background-color: #0a1929; /* 全局背景色，提升整体视觉效果 */
  box-sizing: border-box; /* 修复padding导致的溢出问题 */
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  background: rgba(13, 43, 75, 0.9);
  border-radius: 15px;
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.nav-left, .nav-center, .nav-right {
  flex: 1;
}

.nav-center {
  text-align: center;
}

.nav-center h1 {
  color: #4fc3f7;
  margin: 0 0 5px 0;
  font-size: 1.8rem;
  text-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
}

.nav-subtitle {
  color: #90caf9;
  font-size: 0.9rem;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #1a2a6c, #4fc3f7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  box-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  color: #ffffff;
  font-weight: bold;
  font-size: 1rem;
}

.user-role {
  color: #90caf9;
  font-size: 0.85rem;
}

.nav-right {
  display: flex;
  justify-content: flex-end;
}

.logout-btn {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 8px;
  padding: 10px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background: rgba(244, 67, 54, 0.3);
  border-color: #f44336;
  box-shadow: 0 0 15px rgba(244, 67, 54, 0.3);
}

.logout-icon {
  font-size: 1.1rem;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.content-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.content-row > * {
  flex: 1;
  min-width: 300px;
}

.bottom-info {
  text-align: center;
  padding: 15px;
  background: rgba(13, 43, 75, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(0, 150, 255, 0.2);
  color: #90caf9;
  font-size: 0.9rem;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .top-nav {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .nav-left, .nav-center, .nav-right {
    width: 100%;
    display: flex;
    justify-content: center;
  }
  
  .nav-center {
    order: 1;
  }
  
  .nav-left {
    order: 2;
  }
  
  .nav-right {
    order: 3;
  }
  
  .content-row {
    flex-direction: column;
  }
  
  .container {
    padding: 10px;
    gap: 15px;
  }
  
  .nav-center h1 {
    font-size: 1.5rem; /* 移动端标题字号优化 */
  }
}

/* 全局滚动条优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(13, 43, 75, 0.5);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 150, 255, 0.5);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 150, 255, 0.7);
}
</style>