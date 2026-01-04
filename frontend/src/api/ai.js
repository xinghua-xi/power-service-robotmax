import request from '@/utils/request';

/**
 * 调用DeepSeek Chat模型进行对话
 * @param {string} prompt - 用户输入的问题或提示
 * @returns {Promise} - 包含AI回复的Promise对象
 */
export function chatWithAI(prompt) {
  // 验证输入参数
  if (!prompt || typeof prompt !== 'string' || prompt.trim() === '') {
    return Promise.reject(new Error('请提供有效的对话内容'));
  }
  
  return request({
    url: '/api/ai/chat',
    method: 'post',
    data: {
      prompt: prompt.trim() // deepseek-chat模型需要prompt参数
    }
  });
}
   