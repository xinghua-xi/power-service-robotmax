<template>
  <div id="app">
    <!-- 电网背景效果 -->
    <div class="grid-background"></div>
    <div class="grid-overlay"></div>
    <div class="electric-line"></div>
    
    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Microsoft YaHei', sans-serif;
}

body {
  background: #0a1929;
  color: #e0e0e0;
  min-height: 100vh;
  overflow-x: hidden;
}

/* 电网背景效果 */
.grid-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(rgba(0, 150, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 150, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  z-index: -1;
}

.grid-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 50%, rgba(0, 100, 255, 0.1) 0%, transparent 50%),
             radial-gradient(circle at 80% 20%, rgba(0, 150, 255, 0.1) 0%, transparent 50%),
             radial-gradient(circle at 40% 80%, rgba(0, 200, 255, 0.1) 0%, transparent 50%);
  z-index: -1;
}

.electric-line {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(90deg, transparent 95%, rgba(0, 255, 255, 0.3) 100%),
    linear-gradient(transparent 95%, rgba(0, 255, 255, 0.3) 100%);
  background-size: 30px 30px;
  z-index: -1;
}

#app {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
}
</style>