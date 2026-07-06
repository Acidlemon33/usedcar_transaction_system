<template>
  <section class="detail-page">
    <button class="back-btn" @click="$router.push('/myorders')">← 返回我的订单</button>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="order" class="detail-card">
      <div class="section">
        <div class="title-row">
          <h2><i class="iconfont icon-a-217_dingdan"></i> 订单详情</h2>
          <span :class="['status-badge-lg', statusClass]">{{ statusText }}</span>
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">订单编号</span>
            <span class="info-value">{{ order.orderReference }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><i class="iconfont icon-a-217_xihuan-26"></i> 订单金额</span>
            <span class="info-value price">¥{{ formatPrice(order.transactionPrice) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><i class="iconfont2 icon2-calendar-full"></i> 创建时间</span>
            <span class="info-value">{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><i class="iconfont2 icon2-calendar-full"></i> 支付时间</span>
            <span class="info-value">{{ order.paymentTime ? formatTime(order.paymentTime) : '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><i class="iconfont icon-a-217_kehu"></i> 我的角色</span>
            <span class="info-value">{{ String(order.buyerId) === currentUserIdStr ? '买家' : '卖家' }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3><i class="iconfont icon-a-217_shangdian"></i> 车辆信息</h3>
        <div class="car-summary" v-if="carInfo">
          <strong>{{ carInfo.brand }} {{ carInfo.model }}</strong>
          <span><i class="iconfont2 icon2-lichengbiao"></i> {{ carInfo.mileage }} 公里</span>
          <span><i class="iconfont2 icon2-calendar-full"></i> {{ carInfo.carYear }}</span>
        </div>
        <div v-else class="car-summary">
          <span>车辆ID: {{ order.carId }}</span>
        </div>
        <button class="link-btn" @click="$router.push(`/car/${order.carId}`)">查看车辆详情 →</button>
      </div>

      <!-- 详细状态描述 -->
      <div class="section status-detail-section">
        <h3><i class="iconfont icon-a-217_redian"></i> 订单状态</h3>
        <div class="status-detail">
          <template v-if="order.transactionState === 0">
            <p>待支付 — 请在 <strong>15分钟</strong> 内完成支付</p>
          </template>
          <template v-else-if="order.transactionState === 1">
            <p>买家已付款，等待确认成交</p>
          </template>
          <template v-else-if="order.transactionState === 2">
            <p>订单已完成</p>
          </template>
          <template v-else-if="order.transactionState === 3">
            <p>售后中</p>
          </template>
          <template v-else-if="order.transactionState === 4">
            <p>未支付，超时自动取消</p>
          </template>
        </div>
      </div>

      <!-- 倒计时（待支付） -->
      <div v-if="order.transactionState === 0" class="section countdown-section">
        <div class="countdown-box">
          <span class="countdown-label"><i class="iconfont icon-a-217_redian"></i> 支付剩余时间</span>
          <span :class="['countdown-value', countdown <= 60 ? 'urgent' : '']">
            {{ formatCountdown(countdown) }}
          </span>
        </div>
      </div>

      <!-- 支付确认弹窗 -->
      <div v-if="showPayConfirm" class="modal-overlay" @click.self="closePayConfirm">
        <div class="modal-content" style="width:420px;">
          <div class="modal-header">
            <h3><i class="iconfont icon-a-217_gouwuche"></i> 确认支付</h3>
            <button class="modal-close" @click="closePayConfirm">&times;</button>
          </div>
          <div class="modal-body">
            <div class="pay-detail-row">
              <span>订单金额</span>
              <span class="pay-amount">¥{{ formatPrice(order?.transactionPrice) }}</span>
            </div>
            <div class="pay-detail-row">
              <span>账户余额</span>
              <span :class="['pay-amount', balance >= (order?.transactionPrice || 0) ? 'enough' : 'insufficient']">¥{{ formatPrice(balance) }}</span>
            </div>
            <div v-if="balance < (order?.transactionPrice || 0)" class="balance-warning">
              <i class="iconfont icon-a-217_redian"></i> 余额不足，无法支付
            </div>
            <div v-else class="balance-ok">
              <i class="iconfont icon-a-217_dianzan-36"></i> 余额充足，支付 ¥{{ formatPrice(order?.transactionPrice) }}
            </div>
          </div>
          <div class="modal-footer" style="display:flex;gap:10px;padding:0 24px 20px;justify-content:flex-end;">
            <button class="modal-btn cancel" @click="closePayConfirm">取消</button>
            <button class="modal-btn confirm" @click="doPay" :disabled="paying || balance < (order?.transactionPrice || 0)">
              <span v-if="paying" class="btn-spinner"></span>
              <span v-else>确认付款</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <template v-if="order.transactionState === 0 && String(order.buyerId) === currentUserIdStr">
          <button class="action-btn pay" @click="openPayConfirm"><i class="iconfont icon-a-217_gouwuche"></i> 立即支付</button>
          <button class="action-btn cancel" @click="cancelOrder"><i class="iconfont icon-a-217_shanchu"></i> 取消订单</button>
        </template>
        <template v-if="order.transactionState === 1">
          <button class="action-btn confirm" @click="confirmOrder"><i class="iconfont icon-a-217_dianzan-36"></i> 确认成交</button>
        </template>
        <template v-if="order.transactionState === 2 && String(order.buyerId) === currentUserIdStr">
          <button class="action-btn aftersale" @click="startAfterSale"><i class="iconfont icon-a-217_shezhi"></i> 售后</button>
          <button class="action-btn review" @click="openReview"><i class="iconfont icon-a-217_xihuan-26"></i> 评价</button>
        </template>
        <template v-if="order.transactionState === 3">
          <button class="action-btn done" @click="finishAfterSale"><i class="iconfont icon-a-217_dianzan-36"></i> 完成售后</button>
        </template>
      </div>

      <!-- 已有评论显示 -->
      <div v-if="existingReview" class="section">
        <h3><i class="iconfont icon-a-217_xihuan-26"></i> 我的评价</h3>
        <div class="review-display">
          <div class="review-stars">
            <span v-for="s in 5" :key="s" class="star-display" :class="{ active: s <= Number(existingReview.score) }">★</span>
            <span class="review-score-text">{{ existingReview.score }}/5</span>
          </div>
          <p class="review-content-text">{{ existingReview.content }}</p>
          <p class="review-time-text"><i class="iconfont2 icon2-calendar-full"></i> {{ formatTime(existingReview.createTime) }}</p>
        </div>
      </div>

      <!-- 评价弹窗 -->
      <div v-if="showReviewModal" class="modal-overlay" @click.self="closeReview">
        <div class="modal-content">
          <div class="modal-header">
            <h3><i class="iconfont icon-a-217_xihuan-26"></i> 评价订单</h3>
            <button class="modal-close" @click="closeReview">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>评分</label>
              <div class="star-rating">
                <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= reviewScore }" @click="reviewScore = s">★</span>
                <span class="score-text">{{ reviewScore }}/5</span>
              </div>
            </div>
            <div class="form-group">
              <label>评价内容</label>
              <textarea v-model="reviewContent" placeholder="说说您的购车体验..." rows="4"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="closeReview">取消</button>
            <button class="modal-btn confirm" @click="submitReview" :disabled="submittingReview">提交评价</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from '../toast'
import { store } from '../store'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const carInfo = ref(null)
const loading = ref(true)
const error = ref('')
const currentUserId = computed(() => store.userId)
const currentUserIdStr = computed(() => String(currentUserId.value || ''))
const BACKEND_BASE_URL = 'http://localhost:8080'

const countdown = ref(0)
let countdownTimer = null

const paying = ref(false)
const showPayConfirm = ref(false)
const balance = ref(0)
const existingReview = ref(null)
const showReviewModal = ref(false)
const submittingReview = ref(false)
const reviewScore = ref(5)
const reviewContent = ref('')

const statusText = computed(() => {
  const map = { 0: '待支付', 1: '已支付', 2: '已成交', 3: '售后中', 4: '已取消' }
  return map[order.value?.transactionState] || '未知'
})

const statusClass = computed(() => {
  const map = { 0: 'pending', 1: 'paid', 2: 'done', 3: 'after-sale', 4: 'cancelled' }
  return map[order.value?.transactionState] || ''
})

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function formatCountdown(seconds) {
  if (seconds <= 0) return '已超时'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

async function loadOrder() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/transaction_order/${route.params.id}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      order.value = data.data
      if (order.value.carId) {
        try {
          const carRes = await fetch(`${BACKEND_BASE_URL}/carInfo/${order.value.carId}`)
          const carData = await carRes.json()
          if (carData.code === 200) carInfo.value = carData.data
        } catch (e) { /* ignore */ }
      }
      // 加载已有评价
      try {
        const reviewRes = await fetch(`${BACKEND_BASE_URL}/comment/page?pageSize=1&orderId=${route.params.id}`)
        const reviewData = await reviewRes.json()
        if (reviewData.code === 200 && reviewData.data?.records?.length > 0) {
          existingReview.value = reviewData.data.records[0]
        }
      } catch (e) { /* ignore */ }
      startCountdown()
    } else {
      error.value = '订单不存在'
    }
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

function startCountdown() {
  if (!order.value || order.value.transactionState !== 0) return
  const PAYMENT_TIMEOUT_MINUTES = 15
  const createTime = new Date(order.value.createTime).getTime()
  const deadline = createTime + PAYMENT_TIMEOUT_MINUTES * 60 * 1000

  function update() {
    const remaining = Math.max(0, Math.floor((deadline - Date.now()) / 1000))
    countdown.value = remaining
    if (remaining <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }
  update()
  countdownTimer = setInterval(update, 1000)
}

async function payOrder() {
  if (!order.value) return
  paying.value = true
  try {
    if (countdown.value <= 0) {
      showToast('订单已超时', 'error')
      await cancelOrder()
      return
    }
    const userRes = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`)
    const userData = await userRes.json()
    if (userData.code !== 200) { showToast('获取用户信息失败', 'error'); return }
    const balance = userData.data?.balance || 0
    if (balance < order.value.transactionPrice) { showToast('余额不足', 'error'); return }

    await fetch(`${BACKEND_BASE_URL}/user`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: currentUserId.value, balance: balance - order.value.transactionPrice }) })
    const sellerRes = await fetch(`${BACKEND_BASE_URL}/user/${order.value.sellerId}`)
    const sellerData = await sellerRes.json()
    if (sellerData.code === 200 && sellerData.data) {
      await fetch(`${BACKEND_BASE_URL}/user`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: order.value.sellerId, balance: (sellerData.data.balance || 0) + order.value.transactionPrice }) })
    }
    await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.value.orderId, transactionState: 1, paymentTime: new Date().toISOString() }) })
    showToast('支付成功！', 'success')
    loadOrder()
  } catch (e) { showToast('支付失败', 'error') }
  finally { paying.value = false }
}

async function cancelOrder() {
  if (!confirm('确定取消订单？')) return
  try {
    await fetch(`${BACKEND_BASE_URL}/transaction_order/${order.value.orderId}`, { method: 'DELETE' })
    showToast('订单已取消', 'success')
    router.push('/myorders')
  } catch (e) { showToast('取消失败', 'error') }
}

async function confirmOrder() {
  if (!confirm('确认成交？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.value.orderId, transactionState: 2 }) }); showToast('已确认成交', 'success'); loadOrder() }
  catch (e) { showToast('操作失败', 'error') }
}

async function startAfterSale() {
  if (!confirm('发起售后？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.value.orderId, transactionState: 3 }) }); showToast('已发起售后', 'success'); loadOrder() }
  catch (e) { showToast('操作失败', 'error') }
}

async function finishAfterSale() {
  if (!confirm('完成售后？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.value.orderId, transactionState: 2 }) }); showToast('售后完成', 'success'); loadOrder() }
  catch (e) { showToast('操作失败', 'error') }
}

async function openPayConfirm() {
  if (countdown.value <= 0) { showToast('订单已超时', 'error'); return }
  // 查询余额
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`)
    const data = await res.json()
    if (data.code === 200 && data.data) {
      balance.value = data.data.balance || 0
    }
  } catch (e) { balance.value = 0 }
  showPayConfirm.value = true
}

function closePayConfirm() { showPayConfirm.value = false }

async function doPay() {
  closePayConfirm()
  await payOrder()
}

function openReview() { reviewScore.value = 5; reviewContent.value = ''; showReviewModal.value = true }
function closeReview() { showReviewModal.value = false }

async function submitReview() {
  if (!reviewContent.value.trim()) { showToast('请输入评价内容', 'error'); return }
  submittingReview.value = true
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/comment`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.value.orderId, userId: currentUserId.value, score: String(reviewScore.value), content: reviewContent.value }) })
    if ((await res.json()).code === 200) { showToast('评价成功', 'success'); closeReview() }
    else { showToast('评价失败', 'error') }
  } catch (e) { showToast('评价失败', 'error') }
  finally { submittingReview.value = false }
}

onMounted(loadOrder)
onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer) })
</script>

<style scoped>
.detail-page { max-width: 720px; margin: 0 auto; }
.back-btn { background: white; border: 1.5px solid #e2e8f0; border-radius: 10px; padding: 10px 18px; font-size: 14px; cursor: pointer; margin-bottom: 16px; font-weight: 500; }
.detail-card { display: flex; flex-direction: column; gap: 16px; }
.section { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.section h3 { margin: 0 0 16px; font-size: 16px; color: #0f172a; display: flex; align-items: center; gap: 6px; }
.title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title-row h2 { margin: 0; font-size: 20px; color: #0f172a; display: flex; align-items: center; gap: 6px; }
.status-badge-lg { padding: 6px 16px; border-radius: 999px; font-size: 14px; font-weight: 600; }
.status-badge-lg.pending { background: #fef3c7; color: #92400e; }
.status-badge-lg.paid { background: #dbeafe; color: #1e40af; }
.status-badge-lg.done { background: #d1fae5; color: #065f46; }
.status-badge-lg.after-sale { background: #fce7f3; color: #9d174d; }
.info-list { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.info-row:last-child { border: none; }
.info-label { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 4px; }
.info-value { font-size: 14px; color: #0f172a; font-weight: 600; }
.info-value.price { color: #dc2626; font-size: 16px; }
.car-summary { background: #f8fafc; border-radius: 12px; padding: 12px 16px; display: flex; flex-direction: column; gap: 4px; }
.car-summary strong { font-size: 14px; color: #166534; }
.car-summary span { font-size: 13px; color: #64748b; }
.status-detail p { font-size: 13px; color: #64748b; margin: 4px 0; }
.link-btn { background: none; border: none; color: #2563eb; font-size: 14px; font-weight: 600; cursor: pointer; padding: 0; margin-top: 8px; }
.countdown-section { text-align: center; }
.countdown-box { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.countdown-label { font-size: 14px; color: #64748b; }
.countdown-value { font-size: 28px; font-weight: 800; color: #d97706; }
.countdown-value.urgent { color: #dc2626; animation: pulse 1s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.action-buttons { display: flex; gap: 12px; }
.action-btn { flex: 1; padding: 12px; border: none; border-radius: 12px; font-size: 14px; font-weight: 700; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; }
.action-btn.pay { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; }
.action-btn.cancel { background: #fee2e2; color: #991b1b; }
.action-btn.confirm { background: linear-gradient(135deg, #059669, #047857); color: white; }
.action-btn.aftersale { background: #fce7f3; color: #9d174d; }
.action-btn.review { background: #fef9c3; color: #854d0e; }
.action-btn.done { background: #d1fae5; color: #065f46; }
.btn-spinner { display: inline-block; width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.loading-state { text-align: center; padding: 80px; color: #64748b; }
.spinner { width: 32px; height: 32px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.7s linear infinite; margin: 0 auto 12px; }
.error-state { text-align: center; padding: 40px; color: #dc2626; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 20px; width: 420px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0; }
.modal-header h3 { margin: 0; font-size: 18px; display: flex; align-items: center; gap: 6px; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-close:hover { color: #0f172a; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #334155; }
.form-group textarea { padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0; font-size: 14px; outline: none; background: #f8fafc; resize: vertical; }
.form-group textarea:focus { border-color: #2563eb; }
.pay-detail-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f1f5f9; font-size: 15px; color: #334155; }
.pay-amount { font-size: 18px; font-weight: 700; }
.pay-amount.enough { color: #059669; }
.pay-amount.insufficient { color: #dc2626; }
.balance-warning { background: #fef3c7; border: 1px solid #fde68a; border-radius: 10px; padding: 12px 16px; font-size: 13px; color: #92400e; text-align: center; }
.balance-ok { background: #d1fae5; border: 1px solid #a7f3d0; border-radius: 10px; padding: 12px 16px; font-size: 13px; color: #065f46; text-align: center; }
.star-rating { display: flex; align-items: center; gap: 4px; }
.star { font-size: 28px; color: #d1d5db; cursor: pointer; }
.star.active { color: #f59e0b; }
.score-text { margin-left: 8px; font-size: 14px; color: #64748b; }
.review-display { padding: 8px 0; }
.review-stars { display: flex; align-items: center; gap: 2px; margin-bottom: 8px; }
.star-display { font-size: 20px; color: #d1d5db; }
.star-display.active { color: #f59e0b; }
.review-score-text { margin-left: 8px; font-size: 14px; color: #64748b; font-weight: 600; }
.review-content-text { font-size: 14px; color: #475569; line-height: 1.6; margin: 8px 0; }
.review-time-text { font-size: 12px; color: #94a3b8; }
.modal-footer { display: flex; gap: 10px; padding: 0 24px 20px; justify-content: flex-end; }
.modal-btn { padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; }
.modal-btn.cancel { background: #f1f5f9; color: #475569; }
.modal-btn.confirm { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>