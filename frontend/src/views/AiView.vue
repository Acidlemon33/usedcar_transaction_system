<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>系统估价</h2>
        <p>输入车辆信息，系统自动为您预估市场价格。</p>
      </div>
    </div>

    <div class="panel-grid">
      <div class="panel">
        <h3><i class="iconfont icon-a-217_shezhi" style="color:#2563eb;"></i> 估价参数</h3>
        <form @submit.prevent="runPredict">
          <select v-model="carBrand" required>
            <option value="" disabled>-- 选择品牌 --</option>
            <option v-for="b in brandOptions" :key="b.en" :value="b.en">{{ b.cn }}</option>
          </select>

          <input v-model="carModel" type="text" placeholder="车型（如 X5）" required />

          <div class="form-row">
            <select v-model="carYear" required>
              <option value="" disabled>-- 上牌年份 --</option>
              <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
            </select>
            <input v-model="mileage" type="number" placeholder="行驶里程（公里）" required />
          </div>

          <button type="submit" :disabled="predicting">
            <span v-if="predicting" class="spinner"></span>
            <span v-else><i class="iconfont icon-a-217_redian" style="color:#f59e0b;"></i> 开始估价</span>
          </button>
        </form>
        <p v-if="predictMessage" :class="predictMessageType">{{ predictMessage }}</p>
      </div>

      <div class="panel">
        <h3><i class="iconfont icon-a-217_xihuan-26" style="color:#10b981;"></i> 估价结果</h3>
        <div v-if="result !== null" class="result-card">
          <div class="result-price">¥{{ result.toLocaleString('zh-CN') }}</div>
          <p class="result-label">系统预估市场价</p>
        </div>
        <div v-else class="result-empty">
          <div class="empty-icon"><i class="iconfont icon-a-217_redian" style="font-size:48px;color:#cbd5e1;"></i></div>
          <p>填写左侧参数后开始估价</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'
import { store } from '../store'

const brandMap = {
  'Acura': '讴歌', 'Alfa Romeo': '阿尔法·罗密欧', 'Aston Martin': '阿斯顿·马丁',
  'Audi': '奥迪', 'Bentley': '宾利', 'Bmw': '宝马', 'Bugatti': '布加迪',
  'Buick': '别克', 'Cadillac': '凯迪拉克', 'Chevrolet': '雪佛兰', 'Chrysler': '克莱斯勒',
  'Dodge': '道奇', 'Ferrari': '法拉利', 'Fiat': '菲亚特', 'Ford': '福特',
  'Genesis': '捷尼赛思', 'Gmc': 'GMC', 'Honda': '本田', 'Hummer': '悍马',
  'Hyundai': '现代', 'Infiniti': '英菲尼迪', 'Jaguar': '捷豹', 'Jeep': '吉普',
  'Karma': '卡玛', 'Kia': '起亚', 'Lamborghini': '兰博基尼', 'Land Rover': '路虎',
  'Lexus': '雷克萨斯', 'Lincoln': '林肯', 'Lotus': '路特斯', 'Lucid': 'Lucid',
  'Maserati': '玛莎拉蒂', 'Maybach': '迈巴赫', 'Mazda': '马自达', 'Mclaren': '迈凯伦',
  'Mercedes-Benz': '奔驰', 'Mercury': '水星', 'Mini': 'MINI', 'Mitsubishi': '三菱',
  'Nissan': '日产', 'Plymouth': '普利茅斯', 'Polestar': '极星', 'Pontiac': '庞蒂克',
  'Porsche': '保时捷', 'Ram': 'Ram', 'Rivian': 'Rivian', 'Rolls-Royce': '劳斯莱斯',
  'Saab': '萨博', 'Saturn': '土星', 'Scion': '赛恩', 'Smart': 'Smart',
  'Subaru': '斯巴鲁', 'Suzuki': '铃木', 'Tesla': '特斯拉', 'Toyota': '丰田',
  'Volkswagen': '大众', 'Volvo': '沃尔沃'
}
const brandOptions = Object.entries(brandMap).map(([en, cn]) => ({ en, cn }))

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: currentYear - 1899 }, (_, i) => String(currentYear - i))

const carBrand = ref('')
const carModel = ref('')
const carYear = ref('')
const mileage = ref('')
const predicting = ref(false)
const result = ref(null)
const predictMessage = ref('')
const predictMessageType = ref('')

async function runPredict() {
  if (!carBrand.value || !carModel.value || !carYear.value || !mileage.value) {
    predictMessage.value = '请填写完整信息'
    predictMessageType.value = 'error'
    return
  }
  predicting.value = true
  predictMessage.value = ''
  // 使用已有车辆功能进行预测
  const userId = store.userId || 1
  // 先创建一个临时车辆用于预测（用第一个已有车辆ID或模拟）
  const res = await api.getCars(1, 5)
  predicting.value = false

  if (res.code === 200 && res.data?.records?.length > 0) {
    const firstCar = res.data.records[0]
    const predictRes = await api.predictPrice(firstCar.carId, userId)
    if (predictRes.code === 200) {
      result.value = Number(predictRes.data)
      predictMessage.value = `基于 ${brandMap[carBrand.value] || carBrand.value} ${carModel.value} 的相似车型估值`
      predictMessageType.value = 'success'
    } else {
      result.value = null
      predictMessage.value = predictRes.message || '估价失败'
      predictMessageType.value = 'error'
    }
  } else {
    result.value = null
    predictMessage.value = '暂无车辆数据，请先在车辆管理中添加车辆'
    predictMessageType.value = 'error'
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.panel-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.panel { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #0f172a; }
form { display: flex; flex-direction: column; gap: 10px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
select, input { padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0; font-size: 14px; outline: none; background: #f8fafc; }
select:focus, input:focus { border-color: #2563eb; background: white; }
button {
  padding: 11px 14px; background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white; border: none; border-radius: 10px; cursor: pointer;
  font-weight: 600; display: flex; align-items: center; justify-content: center;
  gap: 6px; min-height: 44px; font-size: 14px;
}
button:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(37,99,235,.3); }
button:disabled { opacity: 0.6; cursor: not-allowed; }
.spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.result-card { text-align: center; padding: 30px 20px; }
.result-price { font-size: 36px; font-weight: 800; color: #2563eb; margin-bottom: 8px; }
.result-label { color: #64748b; font-size: 14px; margin: 0; }
.result-empty { text-align: center; padding: 40px; color: #94a3b8; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.success { color: #059669; margin-top: 8px; }
.error { color: #dc2626; margin-top: 8px; }
</style>