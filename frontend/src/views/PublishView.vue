<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>发布出售信息</h2>
        <p>从已有车辆中选择一辆发布为可售信息。</p>
      </div>
    </div>

    <div class="form-panel">
      <form @submit.prevent="submitSale">
        <div class="form-group">
          <label>选择要出售的车辆</label>
          <div v-if="cars.length" class="select-wrap">
            <select v-model="form.carId" required>
              <option value="" disabled>-- 请选择一辆您的车辆 --</option>
              <option v-for="car in cars" :key="car.carId" :value="car.carId">
                {{ brandMap[car.brand] || car.brand }} {{ car.model }} · {{ car.mileage }}km · ¥{{ car.sellPrice }}
              </option>
            </select>
          </div>
          <div v-else class="no-cars">
            <p>暂无可用车辆</p>
            <router-link to="/inventory" class="link">去车辆管理添加车辆 →</router-link>
          </div>
        </div>

        <div class="form-group">
          <label>期望价格（元）</label>
          <input v-model.number="form.expectPrice" type="number" step="0.01" placeholder="请输入期望价格" required />
        </div>

        <div class="form-group">
          <label>补充说明</label>
          <textarea v-model="form.context" rows="4" placeholder="可选：补充车辆状况、出售原因等信息"></textarea>
        </div>

        <button type="submit" class="submit-btn" :disabled="saving || !form.carId">
          <span v-if="saving" class="spinner"></span>
          <span v-else>发布出售</span>
        </button>
      </form>

      <p v-if="message" :class="messageType" class="form-msg">{{ message }}</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { showToast } from '../toast'
import { store } from '../store'

const brandMap = {
  'Acura': '讴歌',
  'Alfa Romeo': '阿尔法·罗密欧',
  'Aston Martin': '阿斯顿·马丁',
  'Audi': '奥迪',
  'Bentley': '宾利',
  'Bmw': '宝马',
  'Bugatti': '布加迪',
  'Buick': '别克',
  'Cadillac': '凯迪拉克',
  'Chevrolet': '雪佛兰',
  'Chrysler': '克莱斯勒',
  'Dodge': '道奇',
  'Ferrari': '法拉利',
  'Fiat': '菲亚特',
  'Ford': '福特',
  'Genesis': '捷尼赛思',
  'Gmc': 'GMC',
  'Honda': '本田',
  'Hummer': '悍马',
  'Hyundai': '现代',
  'Infiniti': '英菲尼迪',
  'Jaguar': '捷豹',
  'Jeep': '吉普',
  'Karma': '卡玛',
  'Kia': '起亚',
  'Lamborghini': '兰博基尼',
  'Land Rover': '路虎',
  'Lexus': '雷克萨斯',
  'Lincoln': '林肯',
  'Lotus': '路特斯',
  'Lucid': 'Lucid',
  'Maserati': '玛莎拉蒂',
  'Maybach': '迈巴赫',
  'Mazda': '马自达',
  'Mclaren': '迈凯伦',
  'Mercedes-Benz': '奔驰',
  'Mercury': '水星',
  'Mini': 'MINI',
  'Mitsubishi': '三菱',
  'Nissan': '日产',
  'Plymouth': '普利茅斯',
  'Polestar': '极星',
  'Pontiac': '庞蒂克',
  'Porsche': '保时捷',
  'Ram': 'Ram',
  'Rivian': 'Rivian',
  'Rolls-Royce': '劳斯莱斯',
  'Saab': '萨博',
  'Saturn': '土星',
  'Scion': '赛恩',
  'Smart': 'Smart',
  'Subaru': '斯巴鲁',
  'Suzuki': '铃木',
  'Tesla': '特斯拉',
  'Toyota': '丰田',
  'Volkswagen': '大众',
  'Volvo': '沃尔沃'
}

const router = useRouter()
const cars = ref([])
const saving = ref(false)
const message = ref('')
const messageType = ref('')

const form = reactive({
  carId: '',
  expectPrice: '',
  context: ''
})

const currentUserId = computed(() => store.userId)

async function loadCars() {
  const res = await api.getCars(1, 100, currentUserId.value)
  if (res.code === 200) cars.value = res.data?.records || []
}

async function submitSale() {
  if (!form.carId) {
    showToast('请选择一辆车辆', 'error')
    return
  }
  if (!form.expectPrice) {
    showToast('请输入期望价格', 'error')
    return
  }

  saving.value = true
  const res = await api.createSale({
    carId: form.carId,
    salerId: currentUserId.value,
    expectPrice: Number(form.expectPrice),
    context: form.context
  })
  saving.value = false

  if (res.code === 200) {
    showToast('发布成功！', 'success')
    form.carId = ''
    form.expectPrice = ''
    form.context = ''
    setTimeout(() => router.push('/'), 800)
  } else {
    showToast(res.message || '发布失败', 'error')
  }
}

onMounted(() => {
  if (!currentUserId.value) {
    showToast('请先登录', 'error')
    router.push('/login')
    return
  }
  loadCars()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { margin-bottom: 4px; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }

.form-panel {
  max-width: 560px;
  background: white;
  padding: 28px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

form { display: flex; flex-direction: column; gap: 20px; }

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.select-wrap select,
input,
textarea {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  background: #f8fafc;
  box-sizing: border-box;
}

.select-wrap select:focus,
input:focus,
textarea:focus {
  border-color: #2563eb;
  background: white;
}

textarea { resize: vertical; min-height: 80px; font-family: inherit; }

.no-cars {
  background: #fffbeb;
  border: 1.5px dashed #fbbf24;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
}

.no-cars p { color: #92400e; margin: 0 0 8px; font-weight: 500; }

.link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
}
.link:hover { text-decoration: underline; }

.submit-btn {
  padding: 13px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 48px;
  transition: transform 0.1s, box-shadow 0.2s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(37,99,235,.3);
}

.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.form-msg { margin-top: 12px; }
.success { color: #059669; }
.error { color: #dc2626; }
</style>