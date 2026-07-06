<template>
  <section>
    <div class="page-header">
      <div>
        <h2>车辆列表</h2>
        <p>从后端接口读取当前可售车辆信息。</p>
      </div>
      <router-link class="btn" to="/publish">立即发布</router-link>
    </div>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>
    <div v-else class="card-grid">
      <article v-for="item in sales" :key="item.saleInfoId" class="card">
        <div class="car-image-placeholder">
          <i class="iconfont icon-a-217_shangdian" style="font-size:36px;color:#94a3b8;"></i>
        </div>
        <h3>{{ item.carId || '车辆' }}</h3>
        <p><strong>状态：</strong>{{ item.carStatus }}</p>
        <p><strong>期望价格：</strong>{{ item.expectPrice }}</p>
        <p><strong>预测价格：</strong>{{ item.predictPrice }}</p>
        <p><strong>说明：</strong>{{ item.context || '暂无说明' }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const sales = ref([])
const loading = ref(true)
const error = ref('')

async function loadSales() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/saleInfo/list')
    const result = await res.json()
    if (result.code === 200) {
      sales.value = result.data || []
    } else {
      error.value = result.message || '加载失败'
    }
  } catch (e) {
    error.value = '无法连接后端，请确认 Spring Boot 已启动。'
  } finally {
    loading.value = false
  }
}

onMounted(loadSales)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.btn {
  display: inline-block;
  padding: 10px 14px;
  background: #2563eb;
  color: #fff;
  text-decoration: none;
  border-radius: 8px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}
.card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,.06);
}
.card h3 {
  padding: 0 16px;
  margin-top: 12px;
}
.card p {
  padding: 0 16px;
}
.card p:last-child {
  padding-bottom: 16px;
}
.car-image-placeholder {
  height: 140px;
  background: linear-gradient(135deg, #e2e8f0, #f1f5f9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}
.state {
  padding: 16px;
  background: #fff;
  border-radius: 10px;
}
.error {
  color: #dc2626;
}
</style>
