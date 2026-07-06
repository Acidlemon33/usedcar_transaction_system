<template>
  <div class="marketplace">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-bar">
        <i class="iconfont icon-a-217_sousuo search-icon"></i>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索品牌、车型..."
          @input="onSearchInput"
        />
        <button class="search-btn" @click="doSearch">搜索</button>
      </div>

      <!-- 筛选与排序 -->
      <div class="filter-bar">
        <div class="filter-group">
          <label>品牌</label>
          <select v-model="filters.brand">
            <option value="">全部品牌</option>
            <option v-for="b in brandOptions" :key="b.en" :value="b.en">{{ b.cn }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>价格</label>
          <select v-model="filters.priceRange">
            <option value="">不限</option>
            <option value="0-5">5万以下</option>
            <option value="5-10">5万 - 10万</option>
            <option value="10-20">10万 - 20万</option>
            <option value="20-50">20万 - 50万</option>
            <option value="50-999">50万以上</option>
          </select>
        </div>
        <div class="filter-group">
          <label>里程</label>
          <select v-model="filters.mileage">
            <option value="">不限</option>
            <option value="0-3">3万公里以下</option>
            <option value="3-6">3万 - 6万</option>
            <option value="6-10">6万 - 10万</option>
            <option value="10-999">10万公里以上</option>
          </select>
        </div>
        <div class="filter-group">
          <label>排序</label>
          <select v-model="sortBy">
            <option value="newest">最新发布</option>
            <option value="price_asc">价格从低到高</option>
            <option value="price_desc">价格从高到低</option>
            <option value="mileage_asc">里程最少</option>
          </select>
        </div>
        <button class="filter-reset" @click="resetFilters">重置</button>
      </div>

      <!-- Tab 导航 -->
      <div class="tab-bar">
        <button :class="['tab', activeTab === 'recommend' ? 'active' : '']" @click="activeTab = 'recommend'">
          <i class="iconfont icon-a-217_redian"></i> 推荐
        </button>
        <button :class="['tab', activeTab === 'all' ? 'active' : '']" @click="activeTab = 'all'">
          <i class="iconfont icon-a-217_dingdan"></i> 全部车辆
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在加载车辆信息...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button @click="loadCars">重新加载</button>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!cars.length" class="empty-state">
      <div class="empty-icon"><i class="iconfont icon-a-217_shangdian" style="font-size:48px;color:#cbd5e1;"></i></div>
      <h3>暂无车辆信息</h3>
      <p>当前没有符合条件的二手车，试试调整筛选条件</p>
    </div>

    <!-- 瀑布流车辆列表 -->
    <div v-else class="car-grid">
      <div v-for="car in sortedCars" :key="car.carId" class="car-card" @click="goToDetail(car.carId)">
        <div class="car-image-placeholder">
          <i class="iconfont icon-a-217_shangdian" style="font-size:36px;color:#94a3b8;"></i>
          <button v-if="isLoggedIn" class="fav-btn" :class="{ active: isFav(car.carId) }" @click.stop="toggleFav(car)">
            {{ isFav(car.carId) ? '★' : '☆' }}
          </button>
        </div>
        <div class="car-detail">
          <h3 class="car-title">{{ brandMap[car.brand] || car.brand }} {{ car.model }}</h3>
          <div class="car-price">¥{{ formatPrice(car.sellPrice) }}</div>
          <div class="car-tags">
            <span class="tag tag-mileage"><i class="iconfont2 icon2-lichengbiao"></i> {{ car.mileage }}km</span>
            <span v-if="car.carYear" class="tag tag-year"><i class="iconfont2 icon2-calendar-full"></i> {{ car.carYear }}</span>
            <span v-if="car.fuelType" class="tag tag-fuel"><i class="iconfont3 icon3-qiyou"></i> {{ car.fuelType }}</span>
          </div>
          <div v-if="car.intColor || car.extColor" class="car-colors">
            <span v-if="car.extColor"><i class="iconfont3 icon3-yanse"></i> {{ car.extColor }}</span>
          </div>
          <div class="click-hint">点击查看详情 →</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { store } from '../store'
import { showToast } from '../toast'

const cars = ref([])
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')
const activeTab = ref('recommend')
const sortBy = ref('newest')

const filters = ref({
  brand: '',
  priceRange: '',
  mileage: ''
})

const router = useRouter()

function goToDetail(carId) {
  router.push(`/car/${carId}`)
}

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

// brandOptions 数组用于下拉显示中文，value 存英文
const brandOptions = Object.entries(brandMap).map(([en, cn]) => ({ en, cn }))

let searchTimer = null

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(doSearch, 500)
}

async function doSearch() {
  await loadCars()
}

function resetFilters() {
  filters.value = { brand: '', priceRange: '', mileage: '' }
  sortBy.value = 'newest'
  searchKeyword.value = ''
  loadCars()
}

async function loadCars() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.getCars(1, 50)
    if (res.code === 200) {
      let data = res.data?.records || []

      // 搜索过滤
      if (searchKeyword.value.trim()) {
        const kw = searchKeyword.value.trim().toLowerCase()
        data = data.filter(c => {
          // 搜索匹配品牌（英文或中文）、车型
          const brandCn = (brandMap[c.brand] || '').toLowerCase()
          return c.brand?.toLowerCase().includes(kw) ||
                 brandCn.includes(kw) ||
                 c.model?.toLowerCase().includes(kw)
        })
      }

      // 品牌过滤
      if (filters.value.brand) {
        data = data.filter(c => c.brand === filters.value.brand)
      }

      // 价格过滤
      if (filters.value.priceRange) {
        const [min, max] = filters.value.priceRange.split('-').map(Number)
        data = data.filter(c => c.sellPrice >= min * 10000 && c.sellPrice <= max * 10000)
      }

      // 里程过滤
      if (filters.value.mileage) {
        const [min, max] = filters.value.mileage.split('-').map(Number)
        data = data.filter(c => c.mileage >= min * 10000 && c.mileage <= max * 10000)
      }

      cars.value = data
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    error.value = '无法连接到后端'
  } finally {
    loading.value = false
  }
}

const sortedCars = computed(() => {
  const list = [...cars.value]
  switch (sortBy.value) {
    case 'price_asc': return list.sort((a, b) => (a.sellPrice || 0) - (b.sellPrice || 0))
    case 'price_desc': return list.sort((a, b) => (b.sellPrice || 0) - (a.sellPrice || 0))
    case 'mileage_asc': return list.sort((a, b) => (a.mileage || 0) - (b.mileage || 0))
    default: return list
  }
})

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

// 收藏功能
const favoriteIds = ref(new Set())
const isLoggedIn = computed(() => !!store.userId)

function isFav(carId) {
  return favoriteIds.value.has(carId)
}

async function loadFavorites() {
  if (!store.userId) return
  const res = await api.getFavorites(store.userId)
  if (res.code === 200) {
    favoriteIds.value = new Set((res.data?.records || []).map(f => f.carId))
  }
}

async function toggleFav(car) {
  if (!store.userId) {
    showToast('请先登录', 'error')
    return
  }
  if (isFav(car.carId)) {
    // 取消收藏 - 需要找到对应的 favoriteId
    const res = await api.getFavorites(store.userId)
    if (res.code === 200) {
      const fav = (res.data?.records || []).find(f => f.carId === car.carId)
      if (fav) {
        const delRes = await api.removeFavorite(fav.favoriteId)
        if (delRes.code === 200) {
          favoriteIds.value.delete(car.carId)
          favoriteIds.value = new Set(favoriteIds.value)
          showToast('已取消收藏', 'success')
        }
      }
    }
  } else {
    const res = await api.addFavorite(store.userId, car.carId)
    if (res.code === 200) {
      favoriteIds.value.add(car.carId)
      favoriteIds.value = new Set(favoriteIds.value)
      showToast('已收藏', 'success')
    } else {
      showToast('收藏失败', 'error')
    }
  }
}

onMounted(() => {
  loadCars()
  loadFavorites()
})
</script>

<style scoped>
.marketplace {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 搜索区域 */
.search-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 0 16px;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.search-bar:focus-within {
  border-color: #2563eb;
  background: white;
}

.search-icon { font-size: 18px; }

.search-bar input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 0;
  font-size: 15px;
  outline: none;
}

.search-btn {
  padding: 10px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 120px;
}

.filter-group label {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.filter-group select {
  padding: 8px 10px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  background: #f8fafc;
  outline: none;
}

.filter-reset {
  padding: 8px 16px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #64748b;
  cursor: pointer;
  font-size: 13px;
}

/* Tab 导航 */
.tab-bar {
  display: flex;
  gap: 4px;
  margin-top: 16px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 10px;
}

.tab {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e2e8f0;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.error-state {
  text-align: center;
  padding: 40px;
  color: #dc2626;
}

.error-state button {
  margin-top: 12px;
  padding: 10px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon { font-size: 56px; margin-bottom: 12px; }
.empty-state h3 { color: #0f172a; margin: 0 0 8px; }
.empty-state p { color: #64748b; }

/* 车辆网格 */
.car-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.car-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.car-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0,0,0,.1);
}

.car-image-placeholder {
  height: 180px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  position: relative;
}
.fav-btn {
  position: absolute; top: 10px; right: 10px;
  width: 40px; height: 40px; border: none;
  border-radius: 50%; font-size: 22px;
  cursor: pointer; background: rgba(255,255,255,.85);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; color: #94a3b8;
}
.fav-btn.active { color: #f59e0b; }
.fav-btn:hover { transform: scale(1.2); }

.car-detail {
  padding: 16px;
}

.car-title {
  font-size: 16px;
  color: #0f172a;
  margin: 0 0 8px;
  font-weight: 700;
}

.car-price {
  font-size: 22px;
  font-weight: 800;
  color: #dc2626;
  margin-bottom: 10px;
}

.car-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.tag-mileage { background: #f1f5f9; color: #475569; }
.tag-year { background: #fef3c7; color: #92400e; }
.tag-fuel { background: #dbeafe; color: #1e40af; }

.car-colors {
  font-size: 12px;
  color: #94a3b8;
}

.click-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #2563eb;
  font-weight: 500;
}
</style>
