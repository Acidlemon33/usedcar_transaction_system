<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>我发布的</h2>
        <p>管理您发布到市场中的出售信息。</p>
      </div>
    </div>

    <div class="panel">
      <h3><i class="iconfont icon-a-217_dingdan"></i> 我的发布列表（{{ sales.length }}）</h3>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="sales.length" class="list">
        <div v-for="sale in sales" :key="sale.saleInfoId" class="sale-card" @click="goToDetail(sale)">
          <div class="sale-header">
            <strong class="sale-car">{{ brandMap[sale.car?.brand] || sale.car?.brand || '未知' }} {{ sale.car?.model || '' }}</strong>
            <span :class="['status-badge', statusClass(sale.carStatus)]">{{ sale.carStatus }}</span>
          </div>
          <div class="sale-meta">
            <span><i class="iconfont icon-a-217_xihuan-26"></i> 期望价 ¥{{ sale.expectPrice }}</span>
            <span v-if="sale.predictPrice"><i class="iconfont2 icon2-fangchaguji"></i> 预测价 ¥{{ sale.predictPrice }}</span>
          </div>
          <div v-if="sale.context" class="sale-desc">{{ sale.context }}</div>
          <div class="sale-time"><i class="iconfont2 icon2-calendar-full"></i> {{ sale.createTime }}</div>
          <div class="click-hint">点击查看详情 →</div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon"><i class="iconfont icon-a-217_dingdan" style="font-size:48px;color:#94a3b8;"></i></div>
        <p class="empty-text">暂无发布记录</p>
        <p class="empty-hint">去车辆管理中点击"出售"发布您的车辆</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { showToast } from '../toast'
import { store } from '../store'

const router = useRouter()
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
const sales = ref([])
const loading = ref(false)
const currentUserId = computed(() => store.userId)

function goToDetail(sale) {
  router.push(`/sale/${sale.saleInfoId}`)
}

function statusClass(status) {
  const map = { '在售': 'onsale', '待审核': 'pending', '已售': 'sold', '下架': 'off' }
  return map[status] || ''
}

async function loadSales() {
  if (!currentUserId.value) return
  loading.value = true
  const res = await api.getMySales(currentUserId.value)
  if (res.code === 200) {
    const salesData = res.data || []
    const carPromises = salesData.map(async (sale) => {
      try {
        const carRes = await fetch(`http://localhost:8080/carInfo/${sale.carId}`)
        const carData = await carRes.json()
        if (carData.code === 200 && carData.data) {
          sale.car = carData.data
        }
      } catch (e) { /* ignore */ }
      return sale
    })
    sales.value = await Promise.all(carPromises)
  } else {
    showToast(res.message || '加载失败', 'error')
  }
  loading.value = false
}

onMounted(loadSales)
</script>
<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.panel { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #0f172a; }
.list { display: flex; flex-direction: column; gap: 10px; }
.sale-card { padding: 16px; background: #f8fafc; border-radius: 12px; cursor: pointer; }
.sale-card:hover { background: #f1f5f9; }
.sale-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.sale-car { font-size: 15px; color: #0f172a; }
.status-badge { padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-badge.onsale { background: #d1fae5; color: #065f46; }
.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.sold { background: #dbeafe; color: #1e40af; }
.status-badge.off { background: #e2e8f0; color: #475569; }
.sale-meta { display: flex; gap: 16px; font-size: 13px; color: #64748b; flex-wrap: wrap; margin-bottom: 6px; }
.sale-desc { font-size: 13px; color: #475569; margin-bottom: 6px; }
.sale-time { font-size: 12px; color: #94a3b8; }
.click-hint { margin-top: 6px; font-size: 12px; color: #2563eb; font-weight: 500; }
.loading-state { text-align: center; padding: 40px; color: #64748b; }
.spinner { width: 24px; height: 24px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.6s linear infinite; margin: 0 auto 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: #64748b; font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.empty-hint { color: #94a3b8; font-size: 14px; }
</style>