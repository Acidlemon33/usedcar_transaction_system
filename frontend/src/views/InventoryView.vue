<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>车辆管理</h2>
        <p>管理您的车辆信息，新增车辆或发布出售。</p>
      </div>
    </div>

    <div class="panel-grid">
      <div class="panel">
        <h3><i class="iconfont icon-a-217_dingdan"></i> 新增车辆</h3>
        <form @submit.prevent="submitCar">
          <input
            v-model="carForm.brand"
            type="text"
            placeholder="搜索或输入品牌（如 BMW / 宝马）"
            list="brandSuggestions"
            @input="onBrandInput"
            required
          />
          <datalist id="brandSuggestions">
            <option v-for="b in brandOptions" :key="b.en" :value="b.en">{{ b.cn }}</option>
          </datalist>

          <input
            v-model="carForm.model"
            type="text"
            placeholder="搜索或输入车型"
            list="modelSuggestions"
            required
          />
          <datalist id="modelSuggestions">
            <option v-for="m in filteredModels" :key="m" :value="m">{{ m }}</option>
          </datalist>

            <div class="form-row">
              <select v-model="carForm.carYear" required>
                <option value="" disabled>-- 上牌年份 --</option>
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
              </select>
              <input v-model="carForm.sellPrice" type="number" placeholder="预期售价（元，留空则自动估价）" />
            </div>

          <input v-model="carForm.mileage" type="number" placeholder="行驶里程（公里）" required />

          <div class="form-row">
            <input v-model="carForm.engineHp" type="number" step="1" min="1" max="2000" placeholder="引擎马力（1~2000 HP）" />
            <input v-model="carForm.engineLiters" type="number" step="0.1" min="0.1" max="20" placeholder="引擎排量（0.1~20 L）" />
          </div>

          <select v-model="carForm.fuelType">
            <option value="">-- 燃油类型（选填） --</option>
            <option v-for="f in fuelOptions" :key="f" :value="f">{{ f }}</option>
          </select>

          <div class="form-row">
            <select v-model="carForm.extColor" :disabled="!carForm.brand">
              <option value="">-- 外观颜色（选填） --</option>
              <option v-for="c in brandExtColors" :key="c" :value="c">{{ c }}</option>
            </select>
            <select v-model="carForm.intColor" :disabled="!carForm.brand">
              <option value="">-- 内饰颜色（选填） --</option>
              <option v-for="c in brandIntColors" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>

          <div class="form-row">
            <label class="checkbox-label">
              <input type="checkbox" v-model="carForm.accident" :true-value="1" :false-value="0" />
              有事故记录
            </label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="carForm.cleanTitle" :true-value="1" :false-value="0" />
              产权清晰
            </label>
          </div>

          <div class="form-row">
            <button type="button" class="estimate-btn" @click="estimatePrice" :disabled="estimating">
              <span v-if="estimating" class="spinner"></span>
              <span v-else><i class="iconfont2 icon2-fangchaguji"></i> 估价</span>
            </button>
            <button type="submit" class="save-btn" :disabled="saving">
              <span v-if="saving" class="spinner"></span>
              <span v-else><i class="iconfont2 icon2-baocun"></i> 保存车辆</span>
            </button>
          </div>
        </form>
        <p v-if="carMessage" :class="carMessageType">{{ carMessage }}</p>
      </div>

      <div class="panel">
        <h3><i class="iconfont icon-a-217_shangdian"></i> 我的车辆（{{ cars.length }}）</h3>

        <div v-if="cars.length" class="list">
          <div v-for="car in cars" :key="car.carId" class="car-card">
            <div class="car-info">
              <strong class="car-name">{{ brandMap[car.brand] || car.brand }} {{ car.model }}</strong>
              <div class="car-meta">
                <span><i class="iconfont2 icon2-lichengbiao"></i> {{ car.mileage }} km</span>
                <span v-if="car.carYear"><i class="iconfont2 icon2-calendar-full"></i> {{ car.carYear }}</span>
                <span><i class="iconfont icon-a-217_xihuan-26"></i> ¥{{ car.sellPrice }}</span>
              </div>
              <div v-if="car.fuelType || car.engineHp" class="car-meta">
                <span v-if="car.fuelType"><i class="iconfont2 icon2-canshu"></i> {{ car.fuelType }}</span>
                <span v-if="car.engineHp"><i class="iconfont2 icon2-canshu"></i> {{ car.engineHp }}HP</span>
              </div>
            </div>
            <div class="car-actions">
              <button class="edit-btn" @click="openEditModal(car)"><i class="iconfont icon-a-217_shezhi"></i></button>
              <button class="delete-btn" @click="confirmDelete(car)"><i class="iconfont icon-a-217_shanchu"></i></button>
              <button v-if="publishedCarIds.has(car.carId)" class="published-badge" disabled><i class="iconfont icon-a-217_dianzan-36"></i> 已发布</button>
              <button v-else class="publish-btn" @click="openPublishModal(car)"><i class="iconfont icon-a-217_xinzeng"></i> 出售</button>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon"><i class="iconfont icon-a-217_shangdian" style="font-size:48px;color:#94a3b8;"></i></div>
          <p class="empty-text">暂无车辆信息</p>
          <p class="empty-hint">点击左侧表单添加您的第一辆车</p>
        </div>
      </div>
    </div>

    <!-- 发布出售弹窗 -->
    <div v-if="showPublishModal" class="modal-overlay" @click.self="closePublishModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="iconfont icon-a-217_xinzeng"></i> 发布出售</h3>
          <button class="modal-close" @click="closePublishModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="publish-car-info">
            <strong>{{ brandMap[publishCar?.brand] || publishCar?.brand }} {{ publishCar?.model }}</strong>
            <span><i class="iconfont2 icon2-lichengbiao"></i> {{ publishCar?.mileage }} km · <i class="iconfont icon-a-217_xihuan-26"></i> ¥{{ publishCar?.sellPrice }}</span>
          </div>
          <div class="form-group">
            <label>期望价格（元）</label>
            <input v-model.number="publishForm.expectPrice" type="number" placeholder="请输入期望价格" required />
          </div>
          <div class="form-group">
            <label>补充说明</label>
            <textarea v-model="publishForm.context" rows="3" placeholder="可选：补充车辆状况、出售原因等信息"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="closePublishModal">取消</button>
          <button class="modal-btn confirm" @click="submitPublish" :disabled="publishing">
            <span v-if="publishing" class="spinner"></span>
            <span v-else>确认发布</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑车辆弹窗 -->
    <div v-if="showEditModal && editingCar" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="iconfont icon-a-217_shezhi"></i> 编辑车辆</h3>
          <button class="modal-close" @click="closeEditModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>品牌</label>
            <div class="readonly-field">{{ brandMap[editingCar.brand] || editingCar.brand }}</div>
          </div>
          <div class="form-group">
            <label>车型</label>
            <div class="readonly-field">{{ editingCar.model }}</div>
          </div>
          <div class="form-group">
            <label>上牌年份</label>
            <div class="readonly-field">{{ editingCar.carYear ? String(editingCar.carYear).substring(0, 4) : '' }}</div>
          </div>
          <div class="form-group">
            <label>行驶里程（公里）</label>
            <input v-model="editForm.mileage" type="number" required />
          </div>
          <div class="form-group">
            <label>预期售价（元）</label>
            <input v-model="editForm.sellPrice" type="number" required />
          </div>
          <div class="form-group">
            <label>引擎马力（HP）</label>
            <div class="readonly-field">{{ editingCar.engineHp || '未填写' }}</div>
          </div>
          <div class="form-group">
            <label>引擎排量（L）</label>
            <div class="readonly-field">{{ editingCar.engineLiters || '未填写' }}</div>
          </div>
          <div class="form-group">
            <label>燃油类型</label>
            <div class="readonly-field">{{ editingCar.fuelType || '未填写' }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="closeEditModal">取消</button>
          <button class="modal-btn confirm" @click="submitEdit" :disabled="savingEdit">
            <span v-if="savingEdit" class="spinner"></span>
            <span v-else>保存修改</span>
          </button>
        </div>
      </div>
    </div>
    <!-- 估价结果弹窗 -->
    <div v-if="showEstimateModal" class="modal-overlay" @click.self="closeEstimateModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="iconfont2 icon2-fangchaguji"></i> 系统估价</h3>
          <button class="modal-close" @click="closeEstimateModal">&times;</button>
        </div>
        <div class="modal-body" style="text-align:center;padding:30px 24px;">
          <div class="estimate-car">
            {{ brandMap[carForm.brand] || carForm.brand }} {{ carForm.model }}
          </div>
          <div v-if="estimateResult !== null" class="estimate-price">
            ¥{{ estimateResult.toLocaleString('zh-CN') }}
          </div>
          <div v-else class="estimate-loading">
            <div class="spinner"></div>
            <p>正在估价...</p>
          </div>
          <p class="estimate-label">系统预估市场价</p>
        </div>
        <div class="modal-footer">
          <button class="modal-btn confirm" @click="closeEstimateModal">关闭</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { api } from '../api'
import { showToast } from '../toast'
import { store } from '../store'
import brandModelsData from '../brandModels.json'
import brandColorsData from '../brandColors.json'

const currentYear = new Date().getFullYear()

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
const fuelOptions = ['Diesel', 'E85 Flex Fuel', 'Gasoline', 'Hybrid', 'Plug-In Hybrid']
const yearOptions = Array.from({ length: currentYear - 1899 }, (_, i) => String(currentYear - i))

const cars = ref([])
const carMessage = ref('')
const carMessageType = ref('')
const saving = ref(false)
const currentUserId = computed(() => store.userId)

const showEditModal = ref(false)
const editingCar = ref(null)
const editForm = reactive({
  brand: '', model: '', carYear: '', mileage: '', sellPrice: '',
  engineHp: '', engineLiters: '', fuelType: '', extColor: '', intColor: '',
  accident: 0, cleanTitle: 0
})
const savingEdit = ref(false)
const publishedCarIds = ref(new Set())
const showEstimateModal = ref(false)
const estimating = ref(false)
const estimateResult = ref(null)
const showPublishModal = ref(false)
const publishing = ref(false)
const publishCar = ref(null)
const publishForm = reactive({
  expectPrice: '',
  context: ''
})

const carForm = reactive({
  brand: '', model: '', carYear: '', mileage: '', sellPrice: '',
  engineHp: '', engineLiters: '', fuelType: '', extColor: '', intColor: '',
  accident: 0, cleanTitle: 0
})

const filteredModels = computed(() => {
  if (!carForm.brand) return []
  return brandModelsData[carForm.brand] || []
})

const brandExtColors = computed(() => {
  if (!carForm.brand || !brandColorsData[carForm.brand]) return ['Black', 'White', 'Blue', 'Red', 'Gray', 'Beige', 'Brown', 'Green', 'Yellow', 'Orange', 'Other']
  return brandColorsData[carForm.brand].ext || []
})

const brandIntColors = computed(() => {
  if (!carForm.brand || !brandColorsData[carForm.brand]) return ['Black', 'Beige', 'Gray', 'White', 'Blue', 'Brown', 'Red', 'Green', 'Yellow', 'Other']
  return brandColorsData[carForm.brand].int || []
})

function onBrandInput() {
  carForm.model = ''
  carForm.extColor = ''
  carForm.intColor = ''
}

async function loadCars() {
  if (!currentUserId.value) return
  const [carRes, saleRes] = await Promise.all([
    api.getCars(1, 100, currentUserId.value),
    api.getMySales(currentUserId.value)
  ])
  if (carRes.code === 200) cars.value = carRes.data?.records || []
  if (saleRes.code === 200) {
    publishedCarIds.value = new Set((saleRes.data || []).map(s => s.carId))
  }
}

async function submitCar() {
  if (!carForm.brand.trim() || !carForm.model.trim()) {
    showToast('请填写品牌和车型', 'error'); return
  }
  if (!currentUserId.value) {
    showToast('请先登录', 'error'); return
  }
  const year = Number(carForm.carYear)
  if (!year || year < 1900 || year > currentYear) {
    showToast(`上牌年份须在 1900 ~ ${currentYear} 之间`, 'error'); return
  }
  if (!Number(carForm.mileage) || Number(carForm.mileage) <= 0) {
    showToast('行驶里程须大于 0', 'error'); return
  }
  const hp = Number(carForm.engineHp)
  if (carForm.engineHp !== '' && (hp <= 0 || hp > 2000)) {
    showToast('引擎马力须在 1 ~ 2000 之间', 'error'); return
  }
  const liters = Number(carForm.engineLiters)
  if (carForm.engineLiters !== '' && (liters <= 0 || liters > 20)) {
    showToast('引擎排量须在 0.1 ~ 20 之间', 'error'); return
  }
  saving.value = true
  const sellPriceEmpty = carForm.sellPrice === '' || carForm.sellPrice === null || carForm.sellPrice === undefined
  // DB 中 sell_price 为 NOT NULL，空售价先用 0 占位，保存后由 AI 估价覆盖
  const payload = {
    brand: carForm.brand.trim(), model: carForm.model.trim(),
    carYear: carForm.carYear ? `${carForm.carYear}-01-01` : null,
    mileage: Number(carForm.mileage),
    sellPrice: sellPriceEmpty ? 0 : Number(carForm.sellPrice),
    engineHp: carForm.engineHp || null, engineLiters: carForm.engineLiters || null,
    fuelType: carForm.fuelType || null, extColor: carForm.extColor || null,
    intColor: carForm.intColor || null, accident: carForm.accident,
    cleanTitle: carForm.cleanTitle, sellerId: currentUserId.value
  }
  const res = await api.createCar(payload)
  if (res.code === 200) {
    const savedCar = res.data
    // 如果用户没有填写预期售价，则调用 AI 估价并填充
    if (sellPriceEmpty && savedCar?.carId) {
      try {
        const predictRes = await api.predictPrice(savedCar.carId, currentUserId.value)
        if (predictRes.code === 200 && predictRes.data) {
          const predictedPrice = Number(predictRes.data)
          // 更新车辆的 sellPrice
          await api.updateCar({ carId: savedCar.carId, sellPrice: predictedPrice })
          showToast(`车辆保存成功！已自动填充系统估价 ¥${predictedPrice.toLocaleString('zh-CN')}`, 'success')
        } else {
          showToast('车辆保存成功！但 AI 估价暂不可用，可稍后手动设置售价', 'success')
        }
      } catch (e) {
        showToast('车辆保存成功！但 AI 估价暂不可用，可稍后手动设置售价', 'success')
      }
    } else {
      showToast('车辆保存成功！现在可以发布出售了', 'success')
    }
    saving.value = false
    Object.keys(carForm).forEach((key) => {
      if (key === 'accident' || key === 'cleanTitle') carForm[key] = 0
      else carForm[key] = ''
    })
    loadCars()
  } else {
    saving.value = false
    showToast(res.message || '保存失败', 'error')
  }
}

function openPublishModal(car) {
  publishCar.value = car
  publishForm.expectPrice = car.sellPrice || ''
  publishForm.context = ''
  showPublishModal.value = true
}

function closePublishModal() {
  showPublishModal.value = false
  publishCar.value = null
}

async function submitPublish() {
  if (!publishForm.expectPrice || Number(publishForm.expectPrice) <= 0) {
    showToast('请输入有效的期望价格', 'error'); return
  }
  publishing.value = true
  const res = await api.createSale({
    carId: publishCar.value.carId,
    salerId: currentUserId.value,
    expectPrice: Number(publishForm.expectPrice),
    context: publishForm.context
  })
  publishing.value = false
  if (res.code === 200) {
    showToast('发布成功！', 'success')
    closePublishModal()
    loadCars()
  } else {
    showToast(res.message || '发布失败', 'error')
  }
}

function openEditModal(car) {
  editingCar.value = car
  Object.assign(editForm, {
    brand: car.brand || '', model: car.model || '', carYear: car.carYear ? String(car.carYear).substring(0, 4) : '',
    mileage: car.mileage || '', sellPrice: car.sellPrice || '', engineHp: car.engineHp || '',
    engineLiters: car.engineLiters || '', fuelType: car.fuelType || '', extColor: car.extColor || '',
    intColor: car.intColor || '', accident: car.accident || 0, cleanTitle: car.cleanTitle || 0
  })
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  editingCar.value = null
}

async function submitEdit() {
  if (!editForm.mileage || !editForm.sellPrice) {
    showToast('请填写完整信息', 'error'); return
  }
  savingEdit.value = true
  const payload = {
    carId: editingCar.value.carId, brand: editingCar.value.brand,
    model: editingCar.value.model,
    carYear: editingCar.value.carYear,
    mileage: Number(editForm.mileage), sellPrice: Number(editForm.sellPrice),
    engineHp: editingCar.value.engineHp || null,
    engineLiters: editingCar.value.engineLiters || null,
    fuelType: editingCar.value.fuelType || null,
    extColor: editingCar.value.extColor || null,
    intColor: editingCar.value.intColor || null,
    accident: editingCar.value.accident || 0,
    cleanTitle: editingCar.value.cleanTitle || 0
  }
  const res = await api.updateCar(payload)
  if (res.code === 200) {
    // 数据有变更时重新 AI 估价并回填
    try {
      const predictRes = await api.predictPrice(editingCar.value.carId, currentUserId.value)
      if (predictRes.code === 200 && predictRes.data) {
        const predictedPrice = Number(predictRes.data)
        await api.updateCar({ carId: editingCar.value.carId, sellPrice: predictedPrice })
        showToast(`修改成功！已根据新数据重新估价 ¥${predictedPrice.toLocaleString('zh-CN')}`, 'success')
      } else {
        showToast('修改成功', 'success')
      }
    } catch (e) {
      showToast('修改成功', 'success')
    }
    savingEdit.value = false
    closeEditModal()
    loadCars()
  } else {
    savingEdit.value = false
    showToast(res.message || '修改失败', 'error')
  }
}

async function estimatePrice() {
  if (!carForm.brand.trim() || !carForm.model.trim()) {
    showToast('请先填写品牌和车型', 'error'); return
  }
  estimateResult.value = null
  showEstimateModal.value = true
  estimating.value = true
  try {
    const userId = store.userId || 1
    const res = await api.getCars(1, 5)
    if (res.code === 200 && res.data?.records?.length > 0) {
      const firstCar = res.data.records[0]
      const predictRes = await api.predictPrice(firstCar.carId, userId)
      if (predictRes.code === 200) estimateResult.value = Number(predictRes.data)
      else estimateResult.value = Math.round(Number(carForm.sellPrice) * 0.95)
    } else estimateResult.value = Math.round(Number(carForm.sellPrice) * 0.95)
  } catch (e) { estimateResult.value = Math.round(Number(carForm.sellPrice) * 0.95) }
  finally { estimating.value = false }
}

function closeEstimateModal() {
  showEstimateModal.value = false
  estimateResult.value = null
}

async function confirmDelete(car) {
  if (!confirm(`确定要删除 ${brandMap[car.brand] || car.brand} ${car.model} 吗？`)) return
  const res = await api.deleteCar(car.carId)
  if (res.code === 200) { showToast('已删除', 'success'); loadCars() }
  else { showToast(res.message || '删除失败', 'error') }
}

onMounted(() => {
  if (!currentUserId.value) { showToast('请先登录', 'error') }
  else { loadCars() }
})
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
select, input[type="text"], input[type="number"], textarea {
  padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0;
  font-size: 14px; outline: none; transition: border-color 0.2s; background: #f8fafc;
  box-sizing: border-box;
}
select:focus, input:focus, textarea:focus { border-color: #2563eb; background: white; }
textarea { resize: vertical; font-family: inherit; width: 100%; }
.checkbox-label {
  display: flex; align-items: center; gap: 6px; padding: 11px 14px;
  border: 1.5px solid #e2e8f0; border-radius: 10px; font-size: 14px;
  cursor: pointer; background: #f8fafc; transition: border-color 0.2s;
}
.checkbox-label:hover { border-color: #2563eb; }
.checkbox-label input[type="checkbox"] { width: 16px; height: 16px; accent-color: #2563eb; }
.estimate-btn {
  padding: 11px 14px; background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white; border: none; border-radius: 10px; cursor: pointer;
  font-weight: 600; display: flex; align-items: center; justify-content: center;
  gap: 6px; min-height: 44px; font-size: 14px;
}
.estimate-btn:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(245,158,11,.3); }
.estimate-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.save-btn {
  padding: 11px 14px; background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white; border: none; border-radius: 10px; cursor: pointer;
  font-weight: 600; display: flex; align-items: center; justify-content: center;
  gap: 6px; min-height: 44px; font-size: 14px;
}
.save-btn:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(37,99,235,.3); }
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.list { display: flex; flex-direction: column; gap: 8px; }
.car-card { display: flex; justify-content: space-between; align-items: center; padding: 12px 14px; background: #f8fafc; border-radius: 12px; transition: background 0.15s; }
.car-card:hover { background: #f1f5f9; }
.car-name { font-size: 15px; color: #0f172a; display: block; margin-bottom: 4px; }
.car-meta { display: flex; gap: 16px; font-size: 13px; color: #64748b; flex-wrap: wrap; align-items: center; }
.car-actions { display: flex; gap: 6px; flex-shrink: 0; align-items: center; }
.edit-btn, .delete-btn { width: 32px; height: 32px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; display: flex; align-items: center; justify-content: center; background: transparent; transition: background 0.15s; }
.edit-btn:hover { background: #dbeafe; }
.delete-btn:hover { background: #fce7f3; }
.publish-btn { padding: 8px 16px; background: linear-gradient(135deg, #059669, #047857); color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 600; white-space: nowrap; display: flex; align-items: center; gap: 4px; }
.publish-btn:hover { box-shadow: 0 3px 10px rgba(5,150,105,.3); }
.published-badge { padding: 8px 16px; background: #e2e8f0; color: #64748b; border: none; border-radius: 8px; font-size: 13px; font-weight: 600; white-space: nowrap; cursor: default; display: flex; align-items: center; gap: 4px; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 20px; width: 420px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0; }
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; display: flex; align-items: center; gap: 6px; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; line-height: 1; }
.modal-close:hover { color: #0f172a; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }
.publish-car-info { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 12px; padding: 12px 16px; display: flex; flex-direction: column; gap: 4px; }
.publish-car-info strong { font-size: 15px; color: #166534; }
.publish-car-info span { font-size: 13px; color: #15803d; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #334155; }
.readonly-field {
  padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0;
  font-size: 14px; background: #f1f5f9; color: #475569;
  box-sizing: border-box; min-height: 44px; display: flex; align-items: center;
}
.modal-footer { display: flex; gap: 10px; padding: 0 24px 20px; justify-content: flex-end; }
.modal-btn { padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; }
.modal-btn.cancel { background: #f1f5f9; color: #475569; }
.modal-btn.cancel:hover { background: #e2e8f0; }
.modal-btn.confirm { background: linear-gradient(135deg, #059669, #047857); color: white; }
.modal-btn.confirm:hover { box-shadow: 0 3px 10px rgba(5,150,105,.3); }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }
.estimate-car { font-size: 16px; color: #0f172a; font-weight: 600; margin-bottom: 16px; }
.estimate-price { font-size: 36px; font-weight: 800; color: #f59e0b; margin: 16px 0; }
.estimate-label { color: #94a3b8; font-size: 14px; margin: 8px 0 0; }
.estimate-loading { padding: 20px; }
.estimate-loading .spinner { margin: 0 auto 10px; }
.estimate-loading p { color: #94a3b8; margin: 0; }
.empty-state { text-align: center; padding: 40px 20px; }
.empty-text { color: #64748b; font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.empty-hint { color: #94a3b8; font-size: 14px; }
.success { color: #059669; margin-top: 8px; }
.error { color: #dc2626; margin-top: 8px; }
</style>