<template>
  <section class="detail-page">
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button @click="$router.back()">返回首页</button>
    </div>

    <div v-else-if="car" class="detail-card">
      <button class="back-btn" @click="$router.push('/')">← 返回首页</button>

      <div class="car-image">
        <i class="iconfont icon-a-217_shangdian" style="font-size:36px;color:#94a3b8;"></i>
      </div>

      <div class="section info-section">
        <div class="title-row">
          <h2>{{ car.brand }} {{ car.model }}</h2>
          <span class="price">¥{{ formatPrice(car.sellPrice) }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-item"><i class="iconfont2 icon2-lichengbiao"></i> {{ car.mileage }} 公里</span>
          <span v-if="car.carYear" class="meta-item"><i class="iconfont2 icon2-calendar-full"></i> {{ car.carYear }}</span>
          <span v-if="car.fuelType" class="meta-item"><i class="iconfont2 icon2-canshu"></i> {{ car.fuelType }}</span>
        </div>
      </div>

      <div class="section">
        <h3><i class="iconfont2 icon2-canshu"></i> 车辆参数</h3>
        <div class="params-grid">
          <div class="param-item">
            <span class="param-label">品牌</span>
            <span class="param-value">{{ car.brand || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">车型</span>
            <span class="param-value">{{ car.model || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">上牌年份</span>
            <span class="param-value">{{ car.carYear || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">行驶里程</span>
            <span class="param-value">{{ car.mileage }} 公里</span>
          </div>
          <div class="param-item">
            <span class="param-label">售价</span>
            <span class="param-value price-value">¥{{ formatPrice(car.sellPrice) }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">内饰颜色</span>
            <span class="param-value">{{ car.intColor || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">外观颜色</span>
            <span class="param-value">{{ car.extColor || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">燃油类型</span>
            <span class="param-value">{{ car.fuelType || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">引擎马力</span>
            <span class="param-value">{{ car.engineHp || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">引擎排量</span>
            <span class="param-value">{{ car.engineLiters || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3><i class="iconfont2 icon2-canshu"></i> 车况信息</h3>
        <div class="params-grid">
          <div class="param-item">
            <span class="param-label">事故记录</span>
            <span :class="['param-value', car.accident === 1 ? 'bad' : 'good']">
              {{ car.accident === 1 ? '有事故' : '无事故' }}
            </span>
          </div>
          <div class="param-item">
            <span class="param-label">产权情况</span>
            <span :class="['param-value', car.cleanTitle === 1 ? 'good' : 'bad']">
              {{ car.cleanTitle === 1 ? '产权清晰' : '产权异常' }}
            </span>
          </div>
          <div class="param-item">
            <span class="param-label">卖家</span>
            <span class="param-value">{{ sellerName || '-' }}</span>
          </div>
          <div class="param-item">
            <span class="param-label">发布时间</span>
            <span class="param-value">{{ car.createTime || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 状态信息 -->
      <div v-if="saleInfo" class="section">
        <div class="sale-status-bar">
          <span :class="['status-badge-lg', saleStatusClass]">{{ saleInfo.carStatus }}</span>
          <span class="sale-price-label">售价：¥{{ formatPrice(saleInfo.expectPrice) }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="contact-btn" @click="openContactModal">
          <i class="iconfont icon-a-217_xiaoxi"></i> 联系卖家
        </button>
        <button
          v-if="canBuy"
          class="buy-btn"
          @click="openBuyConfirm"
          :disabled="buying"
        >
          <span v-if="buying" class="btn-spinner"></span>
          <span v-else><i class="iconfont icon-a-217_gouwuche"></i> 立即购买</span>
        </button>
      </div>

      <!-- 购买支付弹窗 -->
      <div v-if="showBuyConfirm" class="modal-overlay" @click.self="closeBuyConfirm">
        <div class="modal-content" style="width:420px;">
          <div class="modal-header">
            <h3><i class="iconfont icon-a-217_gouwuche"></i> 确认购买</h3>
            <button class="modal-close" @click="closeBuyConfirm">&times;</button>
          </div>
          <div class="modal-body">
            <div class="confirm-car-info">
              <strong>{{ car.brand }} {{ car.model }}</strong>
              <span><i class="iconfont2 icon2-lichengbiao"></i> {{ car.mileage }} km</span>
            </div>
            <div class="pay-detail-row">
              <span>售价</span>
              <span class="pay-amount">¥{{ formatPrice(car.sellPrice) }}</span>
            </div>
            <div class="pay-detail-row">
              <span>账户余额</span>
              <span :class="['pay-amount', buyBalance >= car.sellPrice ? 'enough' : 'insufficient']">¥{{ formatPrice(buyBalance) }}</span>
            </div>
            <div v-if="buyBalance < car.sellPrice" class="balance-warning">
              <i class="iconfont icon-a-217_redian"></i> 余额不足，请先充值
            </div>
            <div v-else class="balance-ok">
              <i class="iconfont icon-a-217_dianzan-36"></i> 余额充足，将支付 ¥{{ formatPrice(car.sellPrice) }}
            </div>
          </div>
          <div class="modal-footer" style="display:flex;gap:10px;padding:16px 24px 20px;justify-content:flex-end;">
            <button class="modal-btn cancel" @click="cancelBuy" :disabled="buying">暂不支付</button>
            <button class="modal-btn confirm" @click="buyAndPay" :disabled="buying || buyBalance < car.sellPrice">
              <span v-if="buying" class="btn-spinner"></span>
              <span v-else>确认支付</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 联系卖家弹窗 -->
    <div v-if="showContactModal" class="modal-overlay" @click.self="closeContactModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="iconfont icon-a-217_xiaoxi"></i> 联系卖家</h3>
          <button class="modal-close" @click="closeContactModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="contact-row">
            <span class="contact-label">卖家</span>
            <span class="contact-value">{{ sellerName || '未知' }}</span>
          </div>
          <div class="contact-row">
            <span class="contact-label">手机号</span>
            <span class="contact-value phone">{{ sellerPhone || '未公开' }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn" @click="closeContactModal">关闭</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from '../toast'
import { store } from '../store'

const route = useRoute()
const router = useRouter()
const car = ref(null)
const loading = ref(true)
const error = ref('')
const showContactModal = ref(false)
const sellerName = ref('')
const sellerPhone = ref('')
const saleInfo = ref(null)
const currentUserId = computed(() => store.userId)

// 购买
const showBuyConfirm = ref(false)
const buying = ref(false)
const buyBalance = ref(0)
const saleStatus = ref('在售')

const canBuy = computed(() => {
  return currentUserId.value &&
         car.value &&
         car.value.sellerId &&
         car.value.sellerId !== currentUserId.value
})

const saleStatusClass = computed(() => {
  const map = { '在售': 'onsale', '交易中': 'pending', '已售': 'sold', '下架': 'off' }
  return map[saleInfo.value?.carStatus] || ''
})

const BACKEND_BASE_URL = 'http://localhost:8080'

async function loadCar() {
  loading.value = true
  error.value = ''
  try {
    const carId = route.params.id
    const res = await fetch(`${BACKEND_BASE_URL}/carInfo/${carId}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      car.value = data.data
      await loadSellerInfo(car.value.sellerId)
      await loadSaleInfo(carId)
    } else {
      error.value = '车辆信息不存在'
    }
  } catch (e) {
    error.value = '无法加载车辆信息'
  } finally {
    loading.value = false
  }
}

async function loadSellerInfo(sellerId) {
  if (!sellerId) {
    sellerName.value = '未知'
    sellerPhone.value = '未公开'
    return
  }
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/user/${sellerId}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      sellerName.value = data.data.username || '未知'
      sellerPhone.value = data.data.phone || '未公开'
    }
  } catch (e) { /* ignore */ }
}

async function loadSaleInfo(carId) {
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/saleInfo/list`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      // 匹配任意状态的出售记录，用于显示状态标签
      saleInfo.value = data.data.find(s => s.carId === Number(carId)) || null
    }
  } catch (e) { /* ignore */ }
}

// 联系卖家
function openContactModal() {
  showContactModal.value = true
}

function closeContactModal() {
  showContactModal.value = false
}

// 购买
async function openBuyConfirm() {
  // 重新检查车辆出售状态
  await loadSaleInfo(route.params.id)
  if (saleInfo.value && (saleInfo.value.carStatus === '已售' || saleInfo.value.carStatus === '交易中')) {
    showToast('该车辆当前不可购买', 'error')
    return
  }
  // 查询余额
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      buyBalance.value = data.data.balance || 0
    }
  } catch (e) {
    buyBalance.value = 0
  }
  showBuyConfirm.value = true
}

function closeBuyConfirm() {
  showBuyConfirm.value = false
}

// 暂不支付（创建待支付订单）
async function cancelBuy() {
  if (!currentUserId.value) { showToast('请先登录', 'error'); return }
  buying.value = true
  try {
    const orderRef = 'ORD' + Date.now()
    
    // 锁定车辆
    if (saleInfo.value) {
      await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: saleInfo.value.saleInfoId, carStatus: '交易中' })
      })
    }

    // 创建待支付订单
    const orderRes = await fetch(`${BACKEND_BASE_URL}/transaction_order`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderReference: orderRef, buyerId: currentUserId.value, sellerId: car.value.sellerId,
        carId: car.value.carId, transactionPrice: car.value.sellPrice,
        saleInfoId: saleInfo.value?.saleInfoId
      })
    })
    if ((await orderRes.json()).code !== 200) throw new Error('订单创建失败')

    if (saleInfo.value) saleInfo.value.carStatus = '交易中'
    
    closeBuyConfirm()
    showToast('订单已创建，请尽快支付', 'success')
    setTimeout(() => router.push('/myorders'), 500)
  } catch (e) {
    showToast('下单失败: ' + e.message, 'error')
  } finally {
    buying.value = false
  }
}

// 确认支付（创建已支付订单，直接扣款）
async function buyAndPay() {
  if (!currentUserId.value) { showToast('请先登录', 'error'); return }
  buying.value = true
  try {
    if (buyBalance.value < car.value.sellPrice) { throw new Error('余额不足') }

    const orderRef = 'ORD' + Date.now()
    
    // 1. 锁定车辆
    if (saleInfo.value) {
      const lockRes = await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: saleInfo.value.saleInfoId, carStatus: '交易中' })
      })
      if ((await lockRes.json()).code !== 200) throw new Error('车辆已被锁定')
    }

    // 2. 扣买家余额
    const deductRes = await fetch(`${BACKEND_BASE_URL}/user`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUserId.value, balance: buyBalance.value - car.value.sellPrice })
    })
    if ((await deductRes.json()).code !== 200) throw new Error('扣款失败')

    // 3. 加卖家余额
    const sellerRes = await fetch(`${BACKEND_BASE_URL}/user/${car.value.sellerId}`)
    const sellerData = await sellerRes.json()
    if (sellerData.code === 200 && sellerData.data) {
      await fetch(`${BACKEND_BASE_URL}/user`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: car.value.sellerId, balance: (sellerData.data.balance || 0) + car.value.sellPrice })
      })
    }

    // 4. 创建订单（状态为1:已支付）
    const orderRes = await fetch(`${BACKEND_BASE_URL}/transaction_order`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderReference: orderRef, buyerId: currentUserId.value, sellerId: car.value.sellerId,
        carId: car.value.carId, transactionPrice: car.value.sellPrice,
        transactionState: 1, paymentTime: new Date().toISOString(),
        saleInfoId: saleInfo.value?.saleInfoId
      })
    })
    if ((await orderRes.json()).code !== 200) throw new Error('订单创建失败')

    // 5. 更新出售信息为已售
    if (saleInfo.value) {
      await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: saleInfo.value.saleInfoId, carStatus: '已售' })
      })
    }

    closeBuyConfirm()
    showToast('购买成功！', 'success')
    setTimeout(() => router.push('/myorders'), 500)

  } catch (e) {
    showToast('购买失败: ' + e.message, 'error')
  } finally {
    buying.value = false
  }
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

onMounted(loadCar)
</script>

<style scoped>
.detail-page {
  max-width: 720px;
  margin: 0 auto;
}

.back-btn {
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 18px;
  font-size: 14px;
  cursor: pointer;
  margin-bottom: 16px;
  font-weight: 500;
}

.car-image {
  height: 280px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.car-emojis { font-size: 80px; }

.detail-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

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

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.title-row h2 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.price {
  font-size: 26px;
  font-weight: 800;
  color: #dc2626;
  white-space: nowrap;
}

.meta-row {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.meta-item {
  padding: 6px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 14px;
  color: #475569;
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

.price-value { color: #dc2626; font-size: 16px; }

.param-value.good { color: #059669; }
.param-value.bad { color: #dc2626; }

/* 状态栏 */
.sale-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-badge-lg {
  padding: 6px 16px; border-radius: 999px; font-size: 14px; font-weight: 600;
}
.status-badge-lg.onsale { background: #d1fae5; color: #065f46; }
.status-badge-lg.pending { background: #fef3c7; color: #92400e; }
.status-badge-lg.sold { background: #dbeafe; color: #1e40af; }
.status-badge-lg.off { background: #e2e8f0; color: #475569; }
.sale-price-label { font-size: 16px; font-weight: 700; color: #dc2626; }

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
}
.action-buttons button {
  flex: 1;
}
.contact-btn {
  padding: 14px;
  background: linear-gradient(135deg, #059669, #047857);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}
.contact-btn:hover { box-shadow: 0 4px 14px rgba(5,150,105,.3); }

.buy-btn {
  padding: 14px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}
.buy-btn:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(37,99,235,.3); }
.buy-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-spinner {
  display: inline-block;
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,.3); border-top-color: white;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}

/* 购买确认 */
.confirm-car-info {
  background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 12px;
  padding: 12px 16px; display: flex; flex-direction: column; gap: 4px;
}
.confirm-car-info strong { font-size: 15px; color: #166534; }
.pay-detail-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f1f5f9; font-size: 15px; color: #334155; }
.pay-amount { font-size: 18px; font-weight: 700; color: #0f172a; }
.pay-amount.enough { color: #059669; }
.pay-amount.insufficient { color: #dc2626; }
.balance-warning { background: #fef3c7; border: 1px solid #fde68a; border-radius: 10px; padding: 12px 16px; font-size: 13px; color: #92400e; text-align: center; }
.balance-ok { background: #d1fae5; border: 1px solid #a7f3d0; border-radius: 10px; padding: 12px 16px; font-size: 13px; color: #065f46; text-align: center; }

/* 弹窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,.4); display: flex; align-items: center;
  justify-content: center; z-index: 1000;
}
.modal-content {
  background: white; border-radius: 20px; width: 360px;
  max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px 0;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; line-height: 1; }
.modal-close:hover { color: #0f172a; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }
.contact-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #f8fafc; border-radius: 12px;
}
.contact-label { font-size: 14px; color: #64748b; font-weight: 500; }
.contact-value { font-size: 15px; color: #0f172a; font-weight: 600; }
.contact-value.phone { font-size: 16px; color: #2563eb; letter-spacing: 1px; }
.modal-footer { padding: 0 24px 20px; }
.modal-btn {
  width: 100%; padding: 12px; background: #f1f5f9; color: #475569;
  border: none; border-radius: 12px; font-size: 15px; font-weight: 600; cursor: pointer;
}
.modal-btn:hover { background: #e2e8f0; }
.modal-btn.cancel {
  width: auto; padding: 10px 20px; background: #f1f5f9; color: #475569;
}
.modal-btn.cancel:hover { background: #e2e8f0; }
.modal-btn.confirm {
  width: auto; padding: 10px 20px; background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
}
.modal-btn.confirm:hover:not(:disabled) { box-shadow: 0 3px 10px rgba(37,99,235,.3); }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }

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