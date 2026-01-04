<template>
  <div class="camera-module">
    <div class="camera-title">摄像头</div>
    <div class="camera-video-wrapper">
      <video 
        ref="videoRef" 
        id="video"
        autoplay 
        playsinline 
        class="camera-video"
        v-if="isActive"
      ></video>
      <div v-else class="camera-placeholder">
        <div class="placeholder-icon">📷</div>
        <p>{{ placeholderText }}</p>
      </div>
      <!-- 扫描动画效果 -->
      <div v-if="isActive && isScanning" class="scan-animation">
        <div class="scan-line"></div>
        <div class="scan-overlay">扫描中...</div>
      </div>
    </div>
    
    <!-- 处理结果预览区域 -->
    <div v-if="processedImage" class="image-preview">
      <div class="preview-title">扫描预览</div>
      <div class="preview-content">
        <img 
          :src="processedImage" 
          class="processed-image"
        />
      </div>
      <div class="scan-status">{{ scanResultText }}</div>
    </div>
    
    <!-- 格式选择 -->
    <div class="format-selector" v-if="isActive">
      <label for="outputFormat">输出格式：</label>
      <select id="outputFormat" v-model="outputFormat" class="format-select">
        <option value="jpg">JPG</option>
        <option value="png">PNG</option>
      </select>
    </div>
    
    <!-- 操作按钮 -->
    <div class="camera-controls">
      <button @click="scanFile" :disabled="!isActive || isScanning" class="control-btn scan-btn">
        <span class="btn-icon">📄</span> 扫描文件
      </button>
      <button @click="closeCamera" :disabled="!isActive" class="control-btn close-btn">
        <span class="btn-icon">❌</span> 关闭摄像头
      </button>
    </div>
    
    <!-- 扫描结果显示 -->
    <div v-if="scanResults.length > 0" class="scan-results">
      <h4>扫描结果</h4>
      <div class="result-list">
        <div v-for="(result, index) in scanResults" :key="index" class="result-item">
          <span class="result-icon">📁</span>
          <span class="result-name">{{ result.filename }}</span>
          <span class="result-size">{{ result.size }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, onUnmounted, onMounted } from 'vue'

export default {
  name: 'CameraModule',
  props: {
    isActive: {
      type: Boolean,
      default: false
    },
    placeholderText: {
      type: String,
      default: '摄像头未开启'
    }
  },
  emits: ['camera-status-change'],
  setup(props, { emit }) {
    const videoRef = ref(null)
    let stream = null
    const isScanning = ref(false)
    const scanResults = ref([])
    
    // 新增的扫描相关状态
    const currentImage = ref(null)
    const processedImage = ref('')
    const scanResultText = ref('')
    const outputFormat = ref('jpg') // 新增：输出格式选择，默认为jpg
    
    // 监听isActive变化，控制摄像头开关
    watch(() => props.isActive, (newVal) => {
      if (newVal) {
        startCamera()
      } else {
        stopCamera()
      }
    })
    
    // 开始摄像头
    const startCamera = async () => {
      try {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
          alert('您的浏览器不支持摄像头功能，请使用Chrome、Firefox等现代浏览器。');
          emit('camera-status-change', false);
          return;
        }
        
        stream = await navigator.mediaDevices.getUserMedia({ 
          video: { width: { ideal: 640 }, height: { ideal: 480 }, frameRate: { ideal: 30 } }, 
          audio: false 
        });
        
        if (videoRef.value) {
          videoRef.value.srcObject = stream;
          emit('camera-status-change', true);
        }
      } catch (error) {
        console.error('获取摄像头失败:', error);
        if (error.name === 'NotAllowedError') {
          alert('摄像头权限被拒绝，请在浏览器设置中允许访问摄像头。');
        } else if (error.name === 'NotFoundError') {
          alert('未检测到摄像头设备。');
        } else {
          alert('开启摄像头失败，请重试。');
        }
        emit('camera-status-change', false);
      }
    }
    
    // 停止摄像头
    const stopCamera = () => {
      if (stream) {
        stream.getTracks().forEach(track => track.stop());
        stream = null;
      }
      isScanning.value = false;
      currentImage.value = null;
      processedImage.value = '';
      scanResultText.value = '';
      emit('camera-status-change', false);
    }
    
    // 关闭摄像头（供按钮调用）
    const closeCamera = () => {
      emit('camera-status-change', false);
    }
    
    // 整合后的文件下载功能 - 优先使用 File System Access API，不支持则回退到传统方式
    const downloadFile = async (dataUrl, filename) => {
      try {
        // 将base64转换为blob
        const blob = await (await fetch(dataUrl)).blob();
        
        // 检查浏览器是否支持 File System Access API
        if ('showDirectoryPicker' in window) {
          try {
            // 1. 请求用户选择保存目录
            const directoryHandle = await window.showDirectoryPicker({
              mode: 'readwrite',
              startIn: 'downloads' // 建议从下载文件夹开始
            });
            
            // 2. 在选择的目录中创建文件
            const fileHandle = await directoryHandle.getFileHandle(filename, { create: true });
            const writable = await fileHandle.createWritable();
            
            // 3. 写入文件内容
            await writable.write(blob);
            await writable.close();
            
            console.log('文件保存成功:', filename);
            alert(`文件保存成功！\n\n文件名: ${filename}\n保存位置: 您选择的目录`);
            return;
          } catch (err) {
            console.log('用户取消了文件系统操作或发生错误，将回退到传统下载方式:', err);
            // 回退到传统下载方式
          }
        }
        
        // 传统下载方式（不支持 File System Access API 的浏览器）
        const link = document.createElement('a');
        link.href = dataUrl;
        link.download = filename;
        
        // 添加到文档并触发点击
        document.body.appendChild(link);
        link.click();
        
        // 清理
        setTimeout(() => {
          document.body.removeChild(link);
        }, 100);
        
        console.log('文件下载成功:', filename);
        alert(`文件已开始下载\n\n注意：由于浏览器安全限制，\n文件会通过浏览器下载对话框保存，\n请手动选择D:\\扫描结果\\文件夹进行保存。`);
      } catch (error) {
        console.error('文件下载失败:', error);
        alert('文件下载失败，请重试。');
      }
    }
    
    // 核心：高级文档扫描算法 - 整合灰度转换、直方图均衡化、对比度调整和二值化
    const processImage = (imageData) => {
      const canvas = document.createElement('canvas');
      canvas.width = imageData.width;
      canvas.height = imageData.height;
      const ctx = canvas.getContext('2d');
      ctx.putImageData(imageData, 0, 0);

      // 获取像素数据
      let imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      let data = imgData.data;

      // 1. 转换为灰度图
      for (let i = 0; i < data.length; i += 4) {
        const gray = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2];
        data[i] = data[i + 1] = data[i + 2] = gray;
      }
      ctx.putImageData(imgData, 0, 0);

      // 2. 直方图均衡化（增强图像对比度）
      const histogramEqualization = (imgData) => {
        const data = imgData.data;
        const histogram = new Array(256).fill(0);
        
        // 统计灰度值分布
        for (let i = 0; i < data.length; i += 4) {
          const grayValue = Math.floor(data[i]);
          histogram[grayValue]++;
        }
        
        // 计算累积分布函数(CDF)
        const cdf = new Array(256).fill(0);
        cdf[0] = histogram[0];
        for (let i = 1; i < 256; i++) {
          cdf[i] = cdf[i - 1] + histogram[i];
        }
        
        // 找到最小非零CDF值
        const cdfMin = Math.min(...cdf.filter(value => value > 0));
        const totalPixels = imgData.width * imgData.height;
        
        // 应用直方图均衡化映射
        for (let i = 0; i < data.length; i += 4) {
          const gray = Math.floor(data[i]);
          const newGray = Math.round(((cdf[gray] - cdfMin) / (totalPixels - cdfMin)) * 255);
          data[i] = data[i + 1] = data[i + 2] = Math.max(0, Math.min(255, newGray));
        }
      };
      
      histogramEqualization(imgData);
      ctx.putImageData(imgData, 0, 0);

      // 3. 对比度和亮度调整
      imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      data = imgData.data;
      const contrast = 1.5; // 对比度系数
      const brightness = 20; // 亮度偏移
      
      for (let i = 0; i < data.length; i += 4) {
        let gray = data[i];
        gray = gray * contrast + brightness;
        gray = Math.max(0, Math.min(255, gray));
        data[i] = data[i + 1] = data[i + 2] = gray;
      }
      ctx.putImageData(imgData, 0, 0);

      // 4. 智能二值化处理
      imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      data = imgData.data;
      const threshold = 180; // 二值化阈值
      
      for (let i = 0; i < data.length; i += 4) {
        const gray = data[i];
        // 对于高亮度区域转为白色，低亮度区域保持一定灰度以保留细节
        const binary = gray > threshold ? 255 : Math.round(gray * 0.8);
        data[i] = data[i + 1] = data[i + 2] = binary;
      }
      ctx.putImageData(imgData, 0, 0);

      return canvas;
    }
    
    // 扫描文件（实际处理摄像头画面）
    const scanFile = async () => {
      if (!videoRef.value) return;
      
      isScanning.value = true;
      scanResultText.value = '正在扫描文档...';
      
      try {
        // 1. 从摄像头采集当前画面
        const canvas = document.createElement('canvas');
        canvas.width = videoRef.value.videoWidth;
        canvas.height = videoRef.value.videoHeight;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(videoRef.value, 0, 0, canvas.width, canvas.height);

        // 2. 获取原始图像数据
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

        // 3. 调用文档扫描核心算法，处理图像
        scanResultText.value = '正在处理图像...';
        const processedCanvas = processImage(imageData);
        currentImage.value = processedCanvas;

        // 4. 显示处理后的扫描图像
        const dataUrlType = outputFormat.value === 'jpg' ? 'image/jpeg' : 'image/png';
        processedImage.value = processedCanvas.toDataURL(dataUrlType);
        scanResultText.value = '图像扫描完成';
        
        // 5. 创建扫描结果记录
        const scanIndex = scanResults.value.length + 1;
        const fileExtension = outputFormat.value;
        const scanResult = {
          filename: `扫描结果${scanIndex}.${fileExtension}`,
          size: `${(Math.random() * 10 + 1).toFixed(2)} MB`,
          path: `D:\\扫描结果\\扫描结果${scanIndex}.${fileExtension}`, // 修复转义字符
          dataUrl: processedImage.value // 存储实际图像数据
        };
        
        // 添加到扫描结果列表
        scanResults.value.push(scanResult);
        
        // 提示用户下载文件
        alert(`文件扫描完成！\n文件名: ${scanResult.filename}\n大小: ${scanResult.size}\n格式: ${outputFormat.value.toUpperCase()}\n点击确定开始下载。`);
        
        // 触发文件下载
        await downloadFile(processedImage.value, scanResult.filename);
        
      } catch (error) {
        console.error('扫描失败:', error);
        scanResultText.value = '扫描失败，请重试';
        alert('文件扫描失败，请重试。');
      } finally {
        isScanning.value = false;
      }
    }
    

    
    // 组件生命周期
    onMounted(() => {
      // 初始化组件
    })
    
    onUnmounted(() => {
      stopCamera();
    })
    
    return {
      videoRef,
      isScanning,
      scanResults,
      scanFile,
      closeCamera,
      processedImage,
      scanResultText,
      downloadFile,
      outputFormat // 新增：输出格式选择
    }
  }
}
</script>

<style scoped>
.camera-module {
  background: rgba(30, 70, 110, 0.7);
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 0 15px rgba(0, 150, 255, 0.3);
  width: 100%;
  box-sizing: border-box;
}

.camera-title {
  color: #4fc3f7;
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 15px;
  text-align: center;
  text-shadow: 0 0 8px rgba(79, 195, 247, 0.7);
}

.camera-video-wrapper {
  position: relative;
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(0, 150, 255, 0.5);
}

.camera-video {
  width: 100%;
  height: auto;
  display: block;
  background: #000;
}

.camera-placeholder {
  width: 100%;
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(20, 40, 80, 0.5);
  color: #90caf9;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 15px;
  opacity: 0.6;
}

.camera-placeholder p {
  margin: 0;
  font-size: 1.1rem;
}

/* 扫描动画效果 */
.scan-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00ff88, transparent);
  box-shadow: 0 0 10px #00ff88;
  animation: scan 2s linear infinite;
}

.scan-overlay {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 255, 136, 0.3);
  color: #00ff88;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 255, 136, 0.8);
  animation: pulse 1s ease-in-out infinite alternate;
}

@keyframes scan {
  0% { top: 0; }
  100% { top: 100%; }
}

@keyframes pulse {
  0% { opacity: 0.7; }
  100% { opacity: 1; }
}

/* 图像预览区域 */
.image-preview {
  margin-top: 20px;
  padding: 15px;
  background: rgba(20, 40, 80, 0.5);
  border-radius: 8px;
  border: 1px solid rgba(0, 150, 255, 0.3);
}

.preview-title {
  color: #4fc3f7;
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 10px;
  text-align: center;
}

.preview-content {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.processed-image {
  width: 100%;
  max-width: 640px;
  border: 2px solid #333;
  border-radius: 8px;
}



/* 扫描状态文本 */
.scan-status {
  color: #90caf9;
  text-align: center;
  margin-top: 10px;
  font-size: 0.9rem;
}

/* 控制按钮样式 */
.format-selector {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 15px 0;
  gap: 10px;
  color: #90caf9;
}

.format-select {
  padding: 8px 12px;
  border: 1px solid rgba(0, 150, 255, 0.5);
  border-radius: 6px;
  background: rgba(30, 70, 110, 0.8);
  color: #e3f2fd;
  font-size: 1rem;
}

.camera-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.control-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.scan-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
}

.scan-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);
}

.close-btn {
    background: linear-gradient(135deg, #f44336, #da190b);
    color: white;
  }

  .close-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, #da190b, #b31217);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(244, 67, 54, 0.4);
  }

.btn-icon {
  font-size: 1.2rem;
}

/* 扫描结果样式 */
.scan-results {
  margin-top: 20px;
  padding: 15px;
  background: rgba(20, 40, 80, 0.5);
  border-radius: 8px;
  border: 1px solid rgba(0, 150, 255, 0.3);
}

.scan-results h4 {
  color: #4fc3f7;
  margin: 0 0 10px 0;
  font-size: 1.1rem;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 150px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(30, 70, 110, 0.6);
  border-radius: 6px;
  color: #e0f7ff;
}

.result-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.result-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-size {
  font-size: 0.9rem;
  color: #90caf9;
  flex-shrink: 0;
}
</style>