<template>
  <div class="emergency-contact">
    <h2>紧急联系电话</h2>
    <p>如遇紧急用电问题，请直接拨打</p>
    <div class="phone-number">95598</div>
    <p>24小时电力服务热线</p>
  </div>
</template>

<script>
export default {
  name: 'EmergencyContact'
}
</script>

<style scoped>
.emergency-contact {
  background: rgba(13, 43, 75, 0.8);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  text-align: center;
  border: 1px solid rgba(0, 150, 255, 0.3);
  backdrop-filter: blur(10px);
}

h2 {
  color: #4fc3f7;
  margin-bottom: 10px;
}

.phone-number {
  font-size: 1.8rem;
  color: #f44336;
  font-weight: bold;
  margin: 10px 0;
  text-shadow: 0 0 10px rgba(244, 67, 54, 0.5);
}
</style>