<template>
  <div class="chat-panel">
    <div class="chat-header">
      <div class="bot-avatar">🤖</div>
      <div>
        <h3>电力服务助手</h3>
        <p>随时为您提供专业的电力服务</p>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="message in messages" 
        :key="message.id" 
        :class="['message', message.sender === 'bot' ? 'bot-message' : 'user-message']"
      >
        <div v-if="message.sender === 'bot'" v-html="parseMarkdown(message.text)"></div>
        <div v-else>{{ message.text }}</div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        type="text" 
        v-model="userInput" 
        placeholder="请输入您的问题..." 
        @keyup.enter="sendMessage"
        ref="inputField"
        :disabled="isSending"
      >
      <button @click="sendMessage" :disabled="isSending">发送</button>
    </div>
    
    <!-- 使用新的摄像头模块 -->
    <CameraModule 
      :is-active="isCameraActive"
      :placeholder-text="cameraPlaceholderText"
      @camera-status-change="handleCameraStatusChange"
    />
    
    <!-- 电力信息面板（仅保留一份，无需重复） -->
    <div v-if="showElectricInfo" class="electric-info-panel">
      <div class="info-title">电力账户信息</div>
      <div class="info-content">
        <div class="info-item">
          <div class="info-label">剩余电量:</div>
          <div class="info-value">{{ electricInfo.remainingPower }} kWh</div>
        </div>
        <div class="info-item">
          <div class="info-label">剩余余额:</div>
          <div class="info-value">¥{{ electricInfo.remainingBalance }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, nextTick } from 'vue'
import CameraModule from './CameraModule.vue'

export default {
  name: 'ChatPanel',
  props: {
    messages: {
      type: Array,
      required: true
    },
    isSending: {
      type: Boolean,
      default: false
    },
    isCameraActive: {
      type: Boolean,
      default: false
    },
    cameraPlaceholderText: {
      type: String,
      default: '摄像头未开启'
    }
  },
  emits: ['send-message', 'camera-status-change'],
  components: {
    CameraModule
  },
  setup(props, { emit }) {
    const userInput = ref('')
    const messagesContainer = ref(null)
    const inputField = ref(null)
    
    // 电力信息面板相关数据
    const showElectricInfo = ref(false)
    const electricInfo = ref({
      remainingPower: 125.5,  // 默认剩余电量
      remainingBalance: 86.2  // 默认剩余余额
    })
    
    // Markdown解析函数，将Markdown格式转换为HTML
    const parseMarkdown = (text) => {
      if (!text) return ''
      
      let html = text
      
      // 转换 **bold** 为 <strong>bold</strong>
      html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      
      // 转换 *italic* 为 <em>italic</em>
      html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
      
      // 转换 ### heading 为 <h3>heading</h3>
      html = html.replace(/### (.*?)(\n|$)/g, '<h3>$1</h3>\n')
      
      // 转换 ## heading 为 <h2>heading</h2>
      html = html.replace(/## (.*?)(\n|$)/g, '<h2>$1</h2>\n')
      
      // 转换 # heading 为 <h1>heading</h1>
      html = html.replace(/# (.*?)(\n|$)/g, '<h1>$1</h1>\n')
      
      // 转换 \n 为 <br>
      html = html.replace(/\n/g, '<br>')
      
      // 去除多余的空行（两个或更多<br>转换为一个<br>）
      html = html.replace(/<br>\s*<br>/g, '<br>')
      
      return html
    }
    
    const sendMessage = () => {
      const validMsg = userInput.value?.trim();
      // 1. 空值直接拦截，不发起任何请求
      if (!validMsg) {
        alert('请输入您的问题后再发送！');
        return;
      }
      
      emit('send-message', validMsg)
      userInput.value = ''
    }
    
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }
    
    const setUserInput = (text) => {
      userInput.value = text
      
      // 当选择电费查询时，显示电力信息面板
      if (text === '电费查询') {
        showElectricInfo.value = true
      } else {
        showElectricInfo.value = false
      }
      
      if (inputField.value) {
        inputField.value.focus()
      }
    }
    
    watch(() => props.messages, () => {
      nextTick(() => {
        scrollToBottom()
      })
    })
    
    // 处理摄像头状态变化
    const handleCameraStatusChange = (status) => {
      emit('camera-status-change', status);
    }
    
    return {
      userInput,
      messagesContainer,
      inputField,
      sendMessage,
      setUserInput,
      showElectricInfo,
      electricInfo,
      parseMarkdown,
      handleCameraStatusChange
    }
  }
}
</script>

<style scoped>
.chat-panel {
  flex: 2;
  min-width: 400px;
  background: rgba(13, 43, 75, 0.8);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
  min-height: 700px;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 150, 255, 0.3);
}

.bot-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #1a2a6c, #4fc3f7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 15px;
  box-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid rgba(0, 150, 255, 0.3);
  border-radius: 10px;
  margin-bottom: 15px;
  background: rgba(20, 50, 90, 0.5);
  min-height: 300px;
}

.message {
  margin-bottom: 15px;
  padding: 10px 15px;
  border-radius: 18px;
  max-width: 80%;
  line-height: 1.5;
}

.bot-message {
  background: rgba(30, 70, 130, 0.7);
  border-bottom-left-radius: 5px;
  align-self: flex-start;
  border: 1px solid rgba(0, 150, 255, 0.3);
}

.user-message {
  background: linear-gradient(135deg, #1a2a6c, #4fc3f7);
  color: white;
  border-bottom-right-radius: 5px;
  align-self: flex-end;
  margin-left: auto;
}

.chat-input {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

input {
  flex: 1;
  padding: 18px 20px;
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 25px;
  outline: none;
  font-size: 18px;
  background: rgba(20, 50, 90, 0.7);
  color: #e0e0e0;
  min-height: 60px;
}

input::placeholder {
  color: #90a4ae;
}

button {
  background: linear-gradient(135deg, #1a2a6c, #4fc3f7);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 12px 25px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

button:hover {
  background: linear-gradient(135deg, #4fc3f7, #1a2a6c);
  box-shadow: 0 0 15px rgba(79, 195, 247, 0.5);
}

/* 电力信息面板样式 */
.electric-info-panel {
  background: rgba(30, 70, 110, 0.7);
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 12px;
  padding: 30px 25px;
  margin-top: 15px;
  margin-bottom: 0;
  box-shadow: 0 0 15px rgba(0, 150, 255, 0.3);
  width: 100%;
  box-sizing: border-box;
  min-height: 200px;
  position: relative;
  display: flex;
  flex-direction: column;
}

.info-title {
  color: #4fc3f7;
  font-size: 1.4rem;
  font-weight: bold;
  margin-bottom: 30px;
  text-align: center;
  text-shadow: 0 0 8px rgba(79, 195, 247, 0.7);
}

.info-content {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 40px;
  flex: 1;
  align-items: center;
  min-height: 120px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 180px;
  flex: 1;
  max-width: 400px;
  padding: 20px 15px;
}

.info-label {
  color: #90caf9;
  font-size: 1.1rem;
  margin-bottom: 15px;
  font-weight: 500;
}

.info-value {
  color: #ffffff;
  font-size: 2.5rem;
  font-weight: bold;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.7);
  text-align: center;
  word-break: break-all;
}
</style>