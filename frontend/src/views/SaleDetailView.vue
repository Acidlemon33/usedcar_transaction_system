<template>
  <section class="detail-page">
    <button class="back-btn" @click="$router.push('/orders')">← 返回我发布的</button>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button @click="loadData">重新加载</button>
    </div>

    <div v-else-if="sale" class="detail-container">
      <!-- 头部：车辆名称 + 状态 -->
      <div class="detail-header">
        <div class="header-left">
          <h1>{{ brandMap[sale.car?.brand] || sale.car?.brand || '未知' }} {{ sale.car?.model || '' }}</h1>
          <span :class="['status-badge', statusClass(sale.carStatus)]">{{ sale.carStatus }}</span>
        </div>
        <div class="header-price">
          <span class="price-label">期望价格</span>
          <span class="price-value">¥{{ sale.expectPrice }}</span>
        </div>
      </div>

      <!-- 核心信息卡片 -->
      <div class="section">
        <h3><i class="iconfont2 icon2-canshu"></i> 车辆参数</h3>
        <div class="params-grid">
          <div class="param-item">
            <span class="param-label">品牌</span>
            <span class="param-value">{{ brandMap[sale.car?.brand] || sale.car?.brand || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">车型</span>
            <span class="param-value">{{ sale.car?.model || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">上牌年份</span>
            <span class="param-value">{{ sale.car?.carYear || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">行驶里程</span>
            <span class="param-value">{{ sale.car?.mileage }} 公里</span>
          </div>
          <div class="param-item">
            <span class="param-label">燃油类型</span>
            <span class="param-value">{{ sale.car?.fuelType || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">引擎马力</span>
            <span class="param-value">{{ sale.car?.engineHp || '-' }} HP</span>
          </div>
          <div class="param-item" v-if="sale.car?.engineLiters">
            <span class="param-label">引擎排量</span>
            <span class="param-value">{{ sale.car?.engineLiters }} L</span>
          </div>
          <div class="param-item">
            <span class="param-label">预测价格</span>
            <span class="param-value predict-price">¥{{ sale.predictPrice || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 卖家说明 -->
      <div v-if="sale.context" class="section">
        <h3><i class="iconfont4 icon4-shuoming"></i> 卖家说明</h3>
        <p class="context-text">{{ sale.context }}</p>
      </div>

      <!-- 时间信息 -->
      <div class="time-info">
        <span v-if="sale.updateTime && sale.updateTime !== sale.createTime">
          <i class="iconfont2 icon2-calendar-full"></i> 更新时间：{{ formatTime(sale.updateTime) }}
        </span>
        <span v-else>
          <i class="iconfont2 icon2-calendar-full"></i> 发布时间：{{ formatTime(sale.createTime) }}
        </span>
      </div>

    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'

const route = useRoute()
const router = useRouter()
const sale = ref(null)
const loading = ref(true)
const error = ref('')

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

function statusClass(status) {
  const map = { '在售': 'onsale', '待审核': 'pending', '已售': 'sold', '下架': 'off' }
  return map[status] || ''
}

function formatTime(timeStr) {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

async function loadData() {
  const saleInfoId = route.params.id
  if (!saleInfoId) {
    error.value = '参数错误'
    loading.value = false
    return
  }
  loading.value = true
  error.value = ''
  try {
    // 获取出售信息
    const saleRes = await fetch(`http://localhost:8080/saleInfo/${saleInfoId}`)
    const saleData = await saleRes.json()
    if (saleData.code === 200 && saleData.data) {
      sale.value = saleData.data
      // 获取关联车辆信息
      if (sale.value.carId) {
        const carRes = await fetch(`http://localhost:8080/carInfo/${sale.value.carId}`)
        const carData = await carRes.json()
        if (carData.code === 200 && carData.data) {
          sale.value.car = carData.data
        }
      }
    } else {
      error.value = '出售信息不存在'
    }
  } catch (e) {
    error.value = '无法加载数据'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.detail-page {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.back-btn {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 18px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  align-self: flex-start;
}
.back-btn:hover { border-color: #2563eb; color: #2563eb; }

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 头部 */
.detail-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.header-left { flex: 1; }
.header-left h1 { margin: 0 0 10px; font-size: 24px; color: #0f172a; }
.status-badge {
  padding: 4px 14px; border-radius: 999px; font-size: 13px; font-weight: 600;
  display: inline-block;
}
.status-badge.onsale { background: #d1fae5; color: #065f46; }
.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.sold { background: #dbeafe; color: #1e40af; }
.status-badge.off { background: #e2e8f0; color: #475569; }

.header-price { text-align: right; }
.price-label { display: block; font-size: 12px; color: #94a3b8; margin-bottom: 4px; }
.price-value { font-size: 28px; font-weight: 800; color: #dc2626; }

/* 通用卡片 */
.section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}
.section h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #0f172a;
}

.params-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.param-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.param-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}
.param-value {
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}
.predict-price { color: #2563eb; }

.context-text {
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

/* 时间信息 */
.time-info {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  padding: 8px 0;
}

/* 加载/错误状态 */
.loading-state { text-align: center; padding: 80px 20px; color: #64748b; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid #e2e8f0; border-top-color: #2563eb;
  border-radius: 50%; animation: spin 0.7s linear infinite;
  margin: 0 auto 12px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.error-state { text-align: center; padding: 40px; color: #dc2626; }
.error-state button { margin-top: 12px; padding: 10px 20px; background: #2563eb; color: white; border: none; border-radius: 8px; cursor: pointer; }
</style>