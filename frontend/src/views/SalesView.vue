<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>销售发布</h2>
        <p>从你已录入的车辆中选择一辆发布为可售信息。</p>
      </div>
    </div>

    <div class="panel-grid">
      <div class="panel">
        <h3>发布出售信息</h3>
        <form @submit.prevent="submitSale">
          <label>
            选择车辆
            <select v-model="saleForm.carId" required>
              <option value="">请选择一辆您的车辆</option>
              <option v-for="car in myCars" :key="car.carId" :value="car.carId">
                {{ car.brand }} {{ car.model }} · {{ car.mileage }}km · ¥{{ car.sellPrice }}
              </option>
            </select>
          </label>
          <label>
            期望价格
            <input v-model.number="saleForm.expectPrice" type="number" step="0.01" placeholder="请输入期望价格" required />
          </label>
          <label>
            说明
            <textarea v-model="saleForm.context" rows="4" placeholder="补充车辆说明"></textarea>
          </label>
          <button type="submit">发布</button>
        </form>
        <p v-if="saleMessage" :class="saleMessageType">{{ saleMessage }}</p>
      </div>

      <div class="panel">
        <h3>当前销售列表</h3>
        <div v-if="sales.length" class="list">
          <div v-for="sale in sales" :key="sale.saleInfoId" class="item">
            <strong>车辆 {{ sale.carId }}</strong>
            <span>{{ sale.carStatus }} · ¥{{ sale.expectPrice }}</span>
          </div>
        </div>
        <div v-else>暂无销售数据</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const sales = ref([])
const myCars = ref([])
const saleMessage = ref('')
const saleMessageType = ref('')

const saleForm = reactive({
  carId: '',
  expectPrice: '',
  context: ''
})

async function loadSales() {
  const res = await api.getSales()
  if (res.code === 200) sales.value = res.data?.records || []
}

async function loadMyCars() {
  const res = await api.getCars()
  if (res.code === 200) myCars.value = res.data?.records || []
}

async function submitSale() {
  const res = await api.createSale({
    ...saleForm,
    expectPrice: Number(saleForm.expectPrice)
  })
  if (res.code === 200) {
    saleMessage.value = '发布成功'
    saleMessageType.value = 'success'
    saleForm.carId = ''
    saleForm.expectPrice = ''
    saleForm.context = ''
    loadSales()
  } else {
    saleMessage.value = res.message || '发布失败'
    saleMessageType.value = 'error'
  }
}

onMounted(() => {
  loadSales()
  loadMyCars()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.panel-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.panel { background: white; border-radius: 14px; padding: 16px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
form { display: flex; flex-direction: column; gap: 10px; }
label { display: flex; flex-direction: column; gap: 6px; font-weight: 600; }
input, select, textarea, button { padding: 10px 12px; border-radius: 8px; border: 1px solid #d1d5db; }
button { background: #2563eb; color: white; border: none; cursor: pointer; }
.list { display: flex; flex-direction: column; gap: 8px; }
.item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
.success { color: #059669; }
.error { color: #dc2626; }
</style>
