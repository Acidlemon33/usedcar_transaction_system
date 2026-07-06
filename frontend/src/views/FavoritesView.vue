<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>我的收藏</h2>
        <p>您收藏的所有车辆信息。</p>
      </div>
    </div>

    <div class="panel">
      <h3><i class="iconfont icon-a-217_shoucang"></i> 收藏列表（{{ favorites.length }}）</h3>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="favorites.length" class="car-grid">
        <div v-for="fav in favorites" :key="fav.favoriteId" class="car-card" @click="goToDetail(fav.carId)">
          <div class="car-image-placeholder">
            <i class="iconfont icon-a-217_shangdian" style="font-size:36px;color:#94a3b8;"></i>
            <button class="fav-btn active" @click.stop="removeFav(fav)"><i class="iconfont icon-a-217_shoucang" style="color:#f59e0b;"></i></button>
          </div>
          <div class="car-detail">
            <h3 class="car-title">{{ brandMap[fav.car?.brand] || fav.car?.brand || '未知' }} {{ fav.car?.model || '' }}</h3>
            <div class="car-price">¥{{ fav.car?.sellPrice || 0 }}</div>
            <div class="car-tags">
              <span class="tag tag-mileage"><i class="iconfont2 icon2-lichengbiao"></i> {{ fav.car?.mileage || 0 }}km</span>
              <span v-if="fav.car?.carYear" class="tag tag-year"><i class="iconfont2 icon2-calendar-full"></i> {{ fav.car?.carYear }}</span>
            </div>
            <div class="click-hint">点击查看详情 →</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon"><i class="iconfont icon-a-217_shoucang" style="font-size:48px;color:#cbd5e1;"></i></div>
        <p class="empty-text">暂无收藏车辆</p>
        <p class="empty-hint">在首页点击 <i class="iconfont icon-a-217_xihuan-26" style="color:#f59e0b;"></i> 收藏喜欢的车辆</p>
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
const favorites = ref([])
const loading = ref(false)
const currentUserId = computed(() => store.userId)

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

function goToDetail(carId) {
  router.push(`/car/${carId}`)
}

async function loadFavorites() {
  if (!currentUserId.value) return
  loading.value = true
  const res = await api.getFavorites(currentUserId.value)
  if (res.code === 200) {
    const data = res.data?.records || []
    const carPromises = data.map(async (fav) => {
      try {
        const carRes = await fetch(`http://localhost:8080/carInfo/${fav.carId}`)
        const carData = await carRes.json()
        if (carData.code === 200 && carData.data) {
          fav.car = carData.data
        }
      } catch (e) { /* ignore */ }
      return fav
    })
    favorites.value = await Promise.all(carPromises)
  }
  loading.value = false
}

async function removeFav(fav) {
  const res = await api.removeFavorite(fav.favoriteId)
  if (res.code === 200) {
    showToast('已取消收藏', 'success')
    favorites.value = favorites.value.filter(f => f.favoriteId !== fav.favoriteId)
  } else {
    showToast('操作失败', 'error')
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.panel { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #0f172a; }

.car-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
.car-card { background: #f8fafc; border-radius: 12px; overflow: hidden; cursor: pointer; transition: transform 0.15s; }
.car-card:hover { transform: translateY(-2px); }
.car-image-placeholder { height: 140px; background: linear-gradient(135deg, #e2e8f0, #f1f5f9); display: flex; align-items: center; justify-content: center; font-size: 40px; position: relative; }
.fav-btn {
  position: absolute; top: 8px; right: 8px; width: 36px; height: 36px;
  border: none; border-radius: 50%; font-size: 18px; cursor: pointer;
  background: rgba(255,255,255,.8); display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.fav-btn.active { color: #f59e0b; background: rgba(255,255,255,.95); }
.fav-btn:hover { transform: scale(1.2); }
.car-detail { padding: 12px; }
.car-title { font-size: 15px; color: #0f172a; margin: 0 0 6px; font-weight: 700; }
.car-price { font-size: 18px; font-weight: 800; color: #dc2626; margin-bottom: 6px; }
.car-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 6px; }
.tag { padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: 500; }
.tag-mileage { background: #f1f5f9; color: #475569; }
.tag-year { background: #fef3c7; color: #92400e; }
.click-hint { font-size: 11px; color: #2563eb; font-weight: 500; }

.loading-state { text-align: center; padding: 40px; }
.spinner { width: 24px; height: 24px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.6s linear infinite; margin: 0 auto 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: #64748b; font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.empty-hint { color: #94a3b8; font-size: 14px; }
</style>