module.exports = {
  devServer: {
    port: 8080, // 前端自身运行端口（保持不变）
    proxy: {
      // 匹配所有以 /api 开头的请求
      '/api': {
        target: 'http://localhost:8081', // 后端实际地址
        changeOrigin: true, // 开启跨域代理（关键）
        // 可选：如果后端接口没有 /api 前缀，需要重写路径
        // pathRewrite: { '^/api': '' }
      }
    }
  }
};