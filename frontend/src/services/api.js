import axios from 'axios';

// 调试：打印环境变量
console.log('Environment Variables:', process.env);
console.log('VUE_APP_API_BASE_URL:', process.env.VUE_APP_API_BASE_URL);

// 核心：端口改为后端实际的8081（与后端application.yml一致）
const BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081';

const api = axios.create({
    baseURL: BASE_URL,
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 调试：打印axios配置
console.log('[API CONFIG] BASE_URL:', BASE_URL);
console.log('[API CONFIG] Axios baseURL:', api.defaults.baseURL);
console.log('[API CONFIG] Axios timeout:', api.defaults.timeout);
console.log('[API CONFIG] Full config:', api.defaults);

// 请求拦截器：统一添加token、打印请求信息
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // 调试：打印最终请求配置
        console.log('[REQUEST] URL:', config.url);
        console.log('[REQUEST] Method:', config.method);
        console.log('[REQUEST] Data:', config.data);
        return config;
    },
    error => {
        console.error('[REQUEST ERROR] 请求拦截器异常:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器：统一处理返回格式、错误码、401跳转
api.interceptors.response.use(
    response => {
        // 后端返回格式：{success: boolean, message: string, data: any}
        return response.data;
    },
    error => {
        if (error.response) {
            // 401未授权：清空token并跳转登录页
            if (error.response.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('username');
                localStorage.removeItem('userRole');
                window.location.href = '/login';
                return Promise.reject(new Error('登录状态失效，请重新登录'));
            }

            // 其他错误：提取详细信息
            const errorMsg = error.response.data?.message || `接口请求失败（${error.response.status}）`;
            console.error('API错误:', errorMsg);
            console.error('错误状态码:', error.response.status);
            console.error('错误响应:', error.response.data);
            return Promise.reject(new Error(errorMsg));
        } else if (error.request) {
            // 无响应：网络/后端服务问题
            const netErrMsg = '网络错误，请检查后端服务是否启动（端口8081）';
            console.error(netErrMsg, error.message);
            return Promise.reject(new Error(netErrMsg));
        } else {
            // 请求配置错误
            console.error('请求配置错误:', error.message);
            return Promise.reject(new Error(`请求配置异常：${error.message}`));
        }
    }
);

// ==================== 认证相关 API ====================
// 账号密码登录
export const login = (credentials) => {
    return api.post('/api/auth/login', credentials);
};

// 兼容旧版登录调用
export const traditionalLogin = login;

// 面容登录
export const faceLogin = (faceData) => {
    return api.post('/api/auth/face-login', faceData);
};

// 注册面容（核心优化：移除硬编码ID，通过用户名查询真实ID）
export const registerFace = async (param1, param2) => {
    // 兼容两种调用方式：对象参数 {username, faceFeatures} 或 (userId, faceFeatures)
    let username, faceFeatures, userId;
    if (typeof param1 === 'object') {
        username = param1.username;
        faceFeatures = param1.faceFeatures;
    } else {
        userId = param1;
        faceFeatures = param2;
    }

    console.log('=== 开始面容注册 ===');
    try {
        // 步骤1：如果传了用户名，先查询用户ID（优先通过用户名匹配）
        if (username) {
            console.log('用户名:', username);
            const userRes = await api.get(`/api/users/username/${username}`);
            // 检查后端返回的成功状态
            if (!userRes.success) {
                throw new Error(userRes.message || `未找到用户: ${username}`);
            }
            if (!userRes.data) {
                throw new Error(`用户 ${username} 不存在`);
            }
            userId = userRes.data.id;
            console.log('查询到用户ID:', userId);
        }

        // 步骤2：校验用户ID是否有效
        if (!userId || isNaN(Number(userId))) {
            throw new Error('用户ID无效，请传入正确的用户名或ID');
        }

        // 步骤3：调用面容注册接口
        console.log('原始面容特征数据:', faceFeatures);
        // 处理faceData格式：转换为JSON字符串，与faceLogin函数保持一致
        let faceDataToSend;
        if (faceFeatures && Array.isArray(faceFeatures)) {
            // 从Login.vue传递的faceDots格式（包含active和style属性的点阵数据）
            const facePoints = faceFeatures.filter(dot => dot.active).map(dot => ({
                x: parseFloat(dot.style.left),
                y: parseFloat(dot.style.top)
            }));
            faceDataToSend = JSON.stringify({ facePoints });
        } else if (faceFeatures && typeof faceFeatures === 'object') {
            // 直接传递的faceData对象
            faceDataToSend = JSON.stringify(faceFeatures);
        } else {
            // 原始格式（字符串或其他）
            faceDataToSend = faceFeatures;
        }
        console.log('处理后发送的faceData:', faceDataToSend);
        
        const registerRes = await api.post(`/api/auth/register-face/${userId}`, {
            faceData: faceDataToSend
        });

        console.log('面容注册成功:', registerRes.message);
        return registerRes;

    } catch (error) {
        console.error('面容注册过程中发生错误:', error);
        console.error('错误类型:', error.constructor.name);
        console.error('错误信息:', error.message);
        if (error.response) {
            console.error('响应状态:', error.response.status);
            console.error('响应数据:', error.response.data);
        }
        throw error; // 向上抛出，让业务层处理
    }
};

// 检查面容注册状态
export const checkFaceRegistered = (userId) => {
    // 校验ID有效性
    if (!userId || isNaN(Number(userId))) {
        return Promise.reject(new Error('用户ID必须为数字'));
    }
    return api.get(`/api/auth/check-face-registered/${userId}`);
};

// ==================== 聊天相关 API ====================
/**
 * 发送流式聊天请求（适配DeepSeek原生格式）
 * @param {string} prompt - 用户输入的问题
 * @param {Function} onMessage - 接收单字/短句的回调
 * @param {Function} onError - 错误回调
 * @param {Function} onComplete - 完成回调
 * @returns {Function} 关闭连接的方法
 */
export function sendChatStream(prompt, onMessage, onError, onComplete) {
  // 1. 拼接正确的接口路径（确保和后端一致：/api/chat/stream）
  const encodedPrompt = encodeURIComponent(prompt);
  const url = `${BASE_URL}/api/chat/stream?prompt=${encodedPrompt}`;

  // 2. 创建SSE连接
  const eventSource = new EventSource(url, { withCredentials: true });
  eventSource.reconnectInterval = 0;
  
  // 用于标记是否已经正常完成
  let isCompleted = false;
  // 用于记录是否已经接收到有效数据
  let hasReceivedData = false;

  // 3. 直接处理纯文本数据（过滤data:前缀）
  eventSource.onmessage = (e) => {
    hasReceivedData = true; // 标记已经收到数据
    let content = e.data.trim();
    
    // 移除多余的data:前缀
    if (content.startsWith("data: ")) {
      content = content.substring("data: ".length);
    }
    
    // 处理结束标记
    if (content === "[DONE]") {
      isCompleted = true;
      eventSource.close();
      onComplete?.();
      return;
    }
    
    // 直接传递处理后的纯文本内容
    if (content) {
      console.log('收到处理后的纯文本流式数据:', content);
      onMessage?.(content);
    }
  };

  // 4. 处理连接关闭（正常结束）
  eventSource.onclose = () => {
    console.log('SSE连接正常关闭');
    if (!isCompleted) {
      isCompleted = true;
      onComplete?.();
    }
  };

  // 5. 处理连接错误（避免正常关闭被误判为错误）
  eventSource.onerror = (err) => {
    console.log('SSE error event, readyState:', eventSource.readyState, 'hasReceivedData:', hasReceivedData);
    
    // 检查是否真的是错误：
    // 1. 不是已完成状态
    // 2. 连接状态不是CLOSED（正常关闭）
    // 3. 如果已经收到过数据，即使有error事件也不认为是错误（很多浏览器在正常结束时也会触发error）
    if (!isCompleted && 
        eventSource.readyState !== EventSource.CLOSED &&
        !hasReceivedData) {
      console.error("SSE连接真正错误：", err);
      onError?.(new Error("连接异常，请稍后重试"));
      isCompleted = true;
    }
    // 即使有错误，也调用完成回调以确保状态正确更新
    if (!isCompleted) {
      isCompleted = true;
      onComplete?.();
    }
    eventSource.close();
  };

  // 6. 返回关闭连接的方法（供外部调用）
  return () => {
    if (eventSource.readyState !== EventSource.CLOSED) {
      eventSource.close();
      if (!isCompleted) {
        isCompleted = true;
        onComplete?.();
      }
    }
  };
}

// 发送聊天消息（支持流式响应）
export const sendChatMessage = (messageData, onStreamData, onError, onComplete) => {
    // 校验消息内容
    // 根据后端接口文档，AI对话接口使用prompt字段
    const messageContent = messageData?.prompt || messageData?.message;
    if (!messageData || !messageContent || messageContent.trim() === '') {
        return Promise.reject(new Error('消息内容不能为空'));
    }
    
    // 检查是否需要流式响应
    if (onStreamData) {
        // 使用新的sendChatStream函数
        return sendChatStream(messageContent, onStreamData, onError, onComplete);
    } else {
        // 普通请求方式
        return api.post('/api/chat', {
            prompt: messageContent
        });
    }
};

export const getChatHistory = (sessionId) => {
    if (!sessionId) {
        return Promise.reject(new Error('会话ID不能为空'));
    }
    return api.get(`/api/chat/history/${sessionId}`);
};

// 聊天服务健康检查
export const checkChatHealth = () => {
    return api.get('/api/chat/health');
};

// ==================== 服务相关 API ====================
export const getServices = () => {
    return api.get('/api/services');
};

// 根据ID获取服务
export const getServiceById = (id) => {
    if (!id || isNaN(Number(id))) {
        return Promise.reject(new Error('服务ID必须为数字'));
    }
    return api.get(`/api/services/${id}`);
};

export const getMonitorData = () => {
    return api.get('/api/monitor/electricity');
};

// ==================== 电费监控相关 API ====================
export const getElectricityData = () => {
    return api.get('/api/monitor/electricity');
};

export const updateElectricityData = (data) => {
    // 校验必要参数
    const requiredFields = ['dataType', 'period', 'periodDate', 'amount', 'count'];
    const missingFields = requiredFields.filter(field => !data[field]);
    if (missingFields.length > 0) {
        return Promise.reject(new Error(`缺少必要参数：${missingFields.join(', ')}`));
    }
    return api.post('/api/monitor/update-electricity', data);
};

// 获取系统状态
export const getSystemStatus = () => {
    return api.get('/api/monitor/system-status');
};

// ==================== 用户相关 API ====================
// 获取所有用户
export const getUsers = () => {
    return api.get('/api/users');
};

// 根据ID查询用户
export const getUserById = (id) => {
    if (!id || isNaN(Number(id))) {
        return Promise.reject(new Error('用户ID必须为数字'));
    }
    return api.get(`/api/users/${id}`);
};

// 更新用户信息
export const updateUser = (id, userData) => {
    if (!id || isNaN(Number(id))) {
        return Promise.reject(new Error('用户ID必须为数字'));
    }
    return api.put(`/api/users/${id}`, userData);
};

// 删除用户
export const deleteUser = (id) => {
    if (!id || isNaN(Number(id))) {
        return Promise.reject(new Error('用户ID必须为数字'));
    }
    return api.delete(`/api/users/${id}`);
};

// 根据用户名查询用户（新增：面容注册依赖）
export const getUserByUsername = (username) => {
    if (!username) {
        return Promise.reject(new Error('用户名不能为空'));
    }
    return api.get(`/api/users/username/${username}`);
};

// ==================== 知识库相关 API ====================
// 获取知识库列表
export const getKnowledgeBase = () => {
    return api.get('/api/knowledge-base');
};

// 获取服务类型列表
export const getServiceTypes = () => {
    return api.get('/api/service-types');
};

// 获取热门问题列表
export const getPopularQuestions = () => {
    return api.get('/api/knowledge-base/popular');
};

// 根据服务类型获取知识库
export const getKnowledgeBaseByServiceType = (serviceTypeId) => {
    if (!serviceTypeId || isNaN(Number(serviceTypeId))) {
        return Promise.reject(new Error('服务类型ID必须为数字'));
    }
    return api.get(`/api/knowledge-base/service-type/${serviceTypeId}`);
};

export default api;