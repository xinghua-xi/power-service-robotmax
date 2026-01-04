import axios from 'axios';

// 创建axios实例，支持deepseek-chat模型
const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081',  // 后端服务端口
  timeout: 60000,  // deepseek-chat响应可能较慢，设置60秒超时
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
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

// 响应拦截器 - 统一处理deepseek-chat模型的响应
request.interceptors.response.use(
  response => {
    // 后端返回格式：{success: boolean, message: string, data: any}
    console.log('[RESPONSE]', response.data);
    if (response.data.success) {
      return response.data;
    } else {
      console.error(response.data.message || '请求失败');
      return Promise.reject(response.data);
    }
  },
  error => {
    console.error('网络异常，请稍后重试');
    return Promise.reject(error);
  }
);

export default request;