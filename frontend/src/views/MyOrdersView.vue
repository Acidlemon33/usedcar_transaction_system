<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>我的订单</h2>
        <p>管理您的交易订单。</p>
      </div>
    </div>

    <!-- 统计卡片（可点击跳转到对应状态） -->
    <div v-if="!loading && allOrders.length" class="stats-row">
      <div class="stat-card" :class="{ 'active-stat': filterState === 0 }" @click="filterState = 0">
        <span class="stat-num">{{ allOrders.filter(o => o.transactionState === 0).length }}</span>
        <span class="stat-label">待支付</span>
      </div>
      <div class="stat-card" :class="{ 'active-stat': filterState === 1 }" @click="filterState = 1">
        <span class="stat-num">{{ allOrders.filter(o => o.transactionState === 1).length }}</span>
        <span class="stat-label">已支付</span>
      </div>
      <div class="stat-card" :class="{ 'active-stat': filterState === 2 }" @click="filterState = 2">
        <span class="stat-num">{{ allOrders.filter(o => o.transactionState === 2).length }}</span>
        <span class="stat-label">已成交</span>
      </div>
      <div class="stat-card" :class="{ 'active-stat': filterState === 4 }" @click="filterState = 4">
        <span class="stat-num">{{ allOrders.filter(o => o.transactionState === 4).length }}</span>
        <span class="stat-label">已取消</span>
      </div>
    </div>

    <!-- 评价统计 -->
    <div v-if="!loading && reviewedOrders.length" class="review-stats-row">
      <div class="review-stats-card" @click="showReviewed = !showReviewed">
        <i class="iconfont icon-a-217_xihuan-26"></i>
        <span>已有评价 ({{ reviewedOrders.length }})</span>
        <span :class="['toggle-arrow', showReviewed ? 'expanded' : '']">▼</span>
      </div>
      <div v-if="showReviewed" class="review-list">
        <div v-for="item in reviewedOrders" :key="item.order.orderId" class="review-item" @click="goToOrder(item.order)">
          <div class="review-item-header">
            <strong>{{ item.order.carName || '车辆' }}</strong>
            <span class="review-stars-sm">
              <span v-for="s in 5" :key="s" class="sm-star" :class="{ active: s <= Number(item.review.score) }">★</span>
              {{ item.review.score }}/5
            </span>
          </div>
          <p class="review-item-content">{{ item.review.content }}</p>
          <p class="review-item-time">{{ formatTime(item.review.createTime) }}</p>
        </div>
      </div>
    </div>

    <!-- Tab 切换：我买入的 / 我卖出的 -->
    <div class="tab-bar">
      <button :class="['tab', activeTab === 'buy' ? 'active' : '']" @click="activeTab = 'buy'">
        <i class="iconfont icon-a-217_gouwuche"></i> 我买入的 ({{ buyOrders.length }})
      </button>
      <button :class="['tab', activeTab === 'sell' ? 'active' : '']" @click="activeTab = 'sell'">
        <i class="iconfont icon-a-217_shangdian"></i> 我卖出的 ({{ sellOrders.length }})
      </button>
    </div>

    <div class="panel">
      <h3><i class="iconfont icon-a-217_dingdan"></i> {{ activeTab === 'buy' ? '我买入的' : '我卖出的' }}（{{ filteredOrders.length }}）</h3>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredOrders.length" class="list">
        <div v-for="order in filteredOrders" :key="order.orderId" class="order-card" @click="goToOrder(order)">
          <div class="order-header">
            <strong><i class="iconfont icon-a-217_dingdan"></i> {{ order.carName || '车辆' }} — {{ order.otherName || ('用户#' + (String(order.buyerId) === String(currentUserId) ? order.sellerId : order.buyerId)) }}</strong>
            <span :class="['status-badge', statusClass(order.transactionState)]">
              {{ statusText(order.transactionState) }}
            </span>
          </div>
          <div class="order-meta">
            <span><i class="iconfont icon-a-217_xihuan-26"></i> ¥{{ formatPrice(order.transactionPrice) }}</span>
            <span v-if="order.createTime"><i class="iconfont2 icon2-calendar-full"></i> {{ formatTime(order.createTime) }}</span>
            <span v-if="order.transactionState === 0 && countdowns[order.orderId]" :class="['countdown', countdowns[order.orderId] <= 60 ? 'urgent' : '']">
              <i class="iconfont icon-a-217_redian"></i> {{ formatCountdown(countdowns[order.orderId]) }}
            </span>
          </div>
          <div class="order-actions">
            <template v-if="order.transactionState === 0 && order.buyerId === currentUserId">
              <button class="action-btn pay" @click="payOrder(order)"><i class="iconfont icon-a-217_gouwuche"></i> 立即支付</button>
              <button class="action-btn cancel-order" @click="cancelOrder(order)"><i class="iconfont icon-a-217_shanchu"></i> 取消订单</button>
            </template>
            <template v-if="order.transactionState === 1">
              <button class="action-btn confirm" @click="confirmOrder(order)"><i class="iconfont icon-a-217_dianzan-36"></i> 确认成交</button>
            </template>
            <template v-if="order.transactionState === 2 && order.buyerId === currentUserId">
              <button class="action-btn aftersale" @click="startAfterSale(order)"><i class="iconfont icon-a-217_shezhi"></i> 售后</button>
              <button class="action-btn review" @click="openReview(order)"><i class="iconfont icon-a-217_xihuan-26"></i> 评价</button>
            </template>
            <template v-if="order.transactionState === 3">
              <button class="action-btn done" @click="finishAfterSale(order)"><i class="iconfont icon-a-217_dianzan-36"></i> 完成售后</button>
            </template>
            <template v-if="order.transactionState === 4">
              <button class="action-btn delete" @click="deleteOrder(order)"><i class="iconfont icon-a-217_shanchu"></i> 删除</button>
            </template>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon"><i class="iconfont icon-a-217_dingdan" style="font-size:48px;color:#94a3b8;"></i></div>
        <p class="empty-text">{{ activeTab === 'buy' ? '暂无买入订单' : '暂无卖出订单' }}</p>
        <p class="empty-hint">{{ activeTab === 'buy' ? '去首页购买车辆后订单会显示在这里' : '发布车辆出售后订单会显示在这里' }}</p>
      </div>
    </div>

    <!-- 评价弹窗 -->
    <div v-if="showReviewModal" class="modal-overlay" @click.self="closeReview">
      <div class="modal-content" style="width:420px;">
        <div class="modal-header">
          <h3><i class="iconfont icon-a-217_xihuan-26"></i> 评价订单</h3>
          <button class="modal-close" @click="closeReview">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>评分</label>
            <div class="star-rating">
              <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= reviewForm.score }" @click="reviewForm.score = s">★</span>
              <span class="score-text">{{ reviewForm.score }}/5</span>
            </div>
          </div>
          <div class="form-group">
            <label>评价内容</label>
            <textarea v-model="reviewForm.content" placeholder="说说您的购车体验..." rows="4"></textarea>
          </div>
        </div>
        <div class="modal-footer" style="display:flex;gap:10px;padding:0 24px 20px;justify-content:flex-end;">
          <button class="modal-btn cancel" @click="closeReview">取消</button>
          <button class="modal-btn confirm" @click="submitReview" :disabled="submittingReview">
            <span v-if="submittingReview" class="btn-spinner"></span>
            <span v-else>提交评价</span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from '../toast'
import { store } from '../store'

const router = useRouter()
const allOrders = ref([])
const loading = ref(false)
const activeTab = ref('buy')
const currentUserId = computed(() => store.userId)
const BACKEND_BASE_URL = 'http://localhost:8080'
const PAYMENT_TIMEOUT_MINUTES = 15

const filterState = ref(null)
const showReviewed = ref(false)
const countdowns = ref({})
let countdownTimer = null

const showReviewModal = ref(false)
const submittingReview = ref(false)
const reviewingOrder = ref(null)
const reviewForm = ref({ score: 5, content: '' })

// 按角色过滤
const buyOrders = computed(() => allOrders.value.filter(o => String(o.buyerId) === String(currentUserId.value)))
const sellOrders = computed(() => allOrders.value.filter(o => String(o.sellerId) === String(currentUserId.value)))
const filteredOrders = computed(() => activeTab.value === 'buy' ? buyOrders.value : sellOrders.value)
const reviewedOrders = computed(() => allOrders.value.filter(o => o.review).map(o => ({ order: o, review: o.review })))

function statusText(state) {
  const map = { 0: '待支付', 1: '已支付', 2: '已成交', 3: '售后中', 4: '已取消' }
  return map[state] || '未知'
}
function statusClass(state) {
  const map = { 0: 'pending', 1: 'paid', 2: 'done', 3: 'after-sale', 4: 'cancelled' }
  return map[state] || ''
}
function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN')
}
function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}
function goToOrder(order) {
  router.push(`/order/${order.orderId}`)
}
function formatCountdown(seconds) {
  if (seconds <= 0) return '已超时'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}
function computeCountdown(order) {
  if (order.transactionState !== 0) return -1
  const createTime = new Date(order.createTime).getTime()
  const deadline = createTime + PAYMENT_TIMEOUT_MINUTES * 60 * 1000
  return Math.max(0, Math.floor((deadline - Date.now()) / 1000))
}

// 自动取消超时订单 + 恢复出售信息
async function autoCancelExpired(order) {
  if (computeCountdown(order) > 0) return false
  try {
    // 恢复出售信息为"在售"
    if (order.saleInfoId) {
      await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: order.saleInfoId, carStatus: '在售' })
      })
    }
    // 更新订单状态为4(已取消)
    await fetch(`${BACKEND_BASE_URL}/transaction_order`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId: order.orderId, transactionState: 4 })
    })
    return true
  } catch (e) {
    return false
  }
}

function startCountdowns() {
  if (countdownTimer) clearInterval(countdownTimer)
  async function update() {
    let hasActive = false
    for (const order of allOrders.value) {
      if (order.transactionState === 0) {
        const remaining = computeCountdown(order)
        countdowns.value[order.orderId] = remaining
        if (remaining > 0) {
          hasActive = true
        } else if (remaining === 0) {
          // 超时 → 自动取消
          const cancelled = await autoCancelExpired(order)
          if (cancelled) {
            order.transactionState = 4
            delete countdowns.value[order.orderId]
          }
        }
      }
    }
    if (!hasActive && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }
  update()
  countdownTimer = setInterval(update, 1000)
}

async function loadOrders() {
  if (!currentUserId.value) return
  loading.value = true
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/transaction_order/page?pageSize=50`)
    const data = await res.json()
    if (data.code === 200) {
      const uid = String(currentUserId.value)
      const records = data.data?.records || []
      // 先过滤出当前用户的订单，按创建时间降序排序
      allOrders.value = records.filter(o => String(o.buyerId) === uid || String(o.sellerId) === uid)
                             .sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''))
      // 为每个订单加载车辆名称和对方用户名
      for (const order of allOrders.value) {
        // 加载对方用户名
        const otherId = String(order.buyerId) === uid ? order.sellerId : order.buyerId
        if (otherId) {
          try {
            // 先查用户表
            let userRes = await fetch(`${BACKEND_BASE_URL}/user/${otherId}`)
            let userData = await userRes.json()
            if (userData.code === 200 && userData.data) {
              order.otherName = userData.data.username || ('用户#' + otherId)
            } else {
              // 查管理员表
              userRes = await fetch(`${BACKEND_BASE_URL}/admin/${otherId}`)
              userData = await userRes.json()
              if (userData.code === 200 && userData.data) {
                order.otherName = userData.data.adminName || ('管理员#' + otherId)
              }
            }
          } catch (e) { /* ignore */ }
        }
        if (order.carId) {
          try {
            const carRes = await fetch(`${BACKEND_BASE_URL}/carInfo/${order.carId}`)
            const carData = await carRes.json()
            if (carData.code === 200 && carData.data) {
              order.carName = (carData.data.brand || '') + ' ' + (carData.data.model || '')
            } else {
              order.carName = '车辆#' + order.carId
            }
          } catch (e) {
            order.carName = '车辆#' + order.carId
          }
        }
      }
      // 检查是否有超时待支付的订单
      for (const order of allOrders.value) {
        if (order.transactionState === 0 && computeCountdown(order) <= 0) {
          await autoCancelExpired(order)
          order.transactionState = 4
          delete countdowns.value[order.orderId]
        }
      }
      startCountdowns()
    }
  } catch (e) {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

async function payOrder(order) {
  try {
    const remaining = computeCountdown(order)
    if (remaining <= 0) { showToast('订单已超时', 'error'); await cancelOrder(order); return }
    const userRes = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`)
    const userData = await userRes.json()
    if (userData.code !== 200) { showToast('获取用户信息失败', 'error'); return }
    const balance = userData.data?.balance || 0
    if (balance < order.transactionPrice) { showToast('余额不足', 'error'); return }
    await fetch(`${BACKEND_BASE_URL}/user`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: currentUserId.value, balance: balance - order.transactionPrice }) })
    const sellerRes = await fetch(`${BACKEND_BASE_URL}/user/${order.sellerId}`)
    const sellerData = await sellerRes.json()
    if (sellerData.code === 200 && sellerData.data) {
      await fetch(`${BACKEND_BASE_URL}/user`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: order.sellerId, balance: (sellerData.data.balance || 0) + order.transactionPrice }) })
    }
    await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.orderId, transactionState: 1, paymentTime: new Date().toISOString() }) })
    // 支付成功 → 出售信息改为已售
    if (order.saleInfoId) {
      await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: order.saleInfoId, carStatus: '已售' })
      })
    }
    showToast('支付成功！', 'success')
    loadOrders()
  } catch (e) { showToast('支付失败', 'error') }
}

async function cancelOrder(order) {
  if (!confirm('确定取消此订单？')) return
  try {
    if (order.saleInfoId) {
      await fetch(`${BACKEND_BASE_URL}/saleInfo`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ saleInfoId: order.saleInfoId, carStatus: '在售' })
      })
    }
    await fetch(`${BACKEND_BASE_URL}/transaction_order/${order.orderId}`, { method: 'DELETE' })
    showToast('订单已取消', 'success'); loadOrders()
  } catch (e) { showToast('取消失败', 'error') }
}

async function confirmOrder(order) {
  if (!confirm('确认成交？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.orderId, transactionState: 2 }) }); showToast('已确认成交', 'success'); loadOrders() }
  catch (e) { showToast('操作失败', 'error') }
}
async function startAfterSale(order) {
  if (!confirm('发起售后？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.orderId, transactionState: 3 }) }); showToast('已发起售后', 'success'); loadOrders() }
  catch (e) { showToast('操作失败', 'error') }
}
async function finishAfterSale(order) {
  if (!confirm('完成售后？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: order.orderId, transactionState: 2 }) }); showToast('售后完成', 'success'); loadOrders() }
  catch (e) { showToast('操作失败', 'error') }
}
async function deleteOrder(order) {
  if (!confirm('确定删除此订单？')) return
  try { await fetch(`${BACKEND_BASE_URL}/transaction_order/${order.orderId}`, { method: 'DELETE' }); showToast('已删除', 'success'); loadOrders() }
  catch (e) { showToast('删除失败', 'error') }
}
function openReview(order) { reviewingOrder.value = order; reviewForm.value = { score: 5, content: '' }; showReviewModal.value = true }
function closeReview() { showReviewModal.value = false; reviewingOrder.value = null }
async function submitReview() {
  if (!reviewForm.value.content.trim()) { showToast('请输入评价内容', 'error'); return }
  submittingReview.value = true
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/comment`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId: reviewingOrder.value.orderId, userId: currentUserId.value, score: String(reviewForm.value.score), content: reviewForm.value.content }) })
    if ((await res.json()).code === 200) { showToast('评价成功', 'success'); closeReview(); loadOrders() }
    else { showToast('评价失败', 'error') }
  } catch (e) { showToast('评价失败', 'error') }
  finally { submittingReview.value = false }
}

onMounted(loadOrders)
onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer) })
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.panel { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.panel h3 { margin: 0 0 14px; font-size: 16px; color: #0f172a; display: flex; align-items: center; gap: 6px; }

/* Tab 切换 */
.tab-bar { display: flex; gap: 4px; background: #f1f5f9; padding: 4px; border-radius: 10px; }
.tab { flex: 1; padding: 10px; border: none; border-radius: 8px; background: transparent; color: #64748b; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.tab.active { background: white; color: #0f172a; box-shadow: 0 2px 8px rgba(0,0,0,.08); }

.list { display: flex; flex-direction: column; gap: 10px; }
.order-card { padding: 16px; background: #f8fafc; border-radius: 12px; }
.order-card:hover { background: #f1f5f9; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-badge.pending { background: #fef3c7; color: #92400e; }
.status-badge.paid { background: #dbeafe; color: #1e40af; }
.status-badge.done { background: #d1fae5; color: #065f46; }
.status-badge.after-sale { background: #fce7f3; color: #9d174d; }
.status-badge.cancelled { background: #e2e8f0; color: #475569; }
.order-meta { display: flex; gap: 16px; font-size: 12px; color: #64748b; flex-wrap: wrap; margin-bottom: 6px; align-items: center; }
.countdown { font-weight: 700; color: #d97706; }
.countdown.urgent { color: #dc2626; animation: pulse 1s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.order-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.action-btn { padding: 6px 14px; border: none; border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.action-btn.pay { background: #dbeafe; color: #1e40af; }
.action-btn.confirm { background: #d1fae5; color: #065f46; }
.action-btn.aftersale { background: #fce7f3; color: #9d174d; }
.action-btn.done { background: #d1fae5; color: #065f46; }
.action-btn.delete { background: #fee2e2; color: #991b1b; }
.action-btn.cancel-order { background: #fef3c7; color: #92400e; }
.action-btn.review { background: #fef9c3; color: #854d0e; }
.action-btn:hover { opacity: 0.8; }
.loading-state { text-align: center; padding: 40px; }
.spinner { width: 24px; height: 24px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.6s linear infinite; margin: 0 auto 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 60px; }
.empty-text { color: #64748b; font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.empty-hint { color: #94a3b8; font-size: 14px; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat-card { background: white; border-radius: 14px; padding: 16px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,.06); cursor: pointer; transition: all 0.2s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.1); }
.stat-card.active-stat { border: 2px solid #2563eb; }
.stat-num { display: block; font-size: 26px; font-weight: 800; color: #2563eb; }
.stat-label { font-size: 12px; color: #64748b; margin-top: 4px; }
.review-stats-row { background: white; border-radius: 14px; padding: 0; box-shadow: 0 2px 8px rgba(0,0,0,.06); overflow: hidden; }
.review-stats-card { display: flex; align-items: center; gap: 8px; padding: 14px 16px; cursor: pointer; font-size: 14px; color: #d97706; font-weight: 600; }
.review-stats-card:hover { background: #fffbeb; }
.toggle-arrow { margin-left: auto; transition: transform 0.2s; font-size: 10px; }
.toggle-arrow.expanded { transform: rotate(180deg); }
.review-list { border-top: 1px solid #f1f5f9; }
.review-item { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; cursor: pointer; }
.review-item:last-child { border: none; }
.review-item:hover { background: #f8fafc; }
.review-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.review-item-header strong { font-size: 14px; color: #0f172a; }
.review-stars-sm { font-size: 12px; color: #64748b; }
.sm-star { color: #d1d5db; }
.sm-star.active { color: #f59e0b; }
.review-item-content { font-size: 13px; color: #475569; margin: 0 0 4px; }
.review-item-time { font-size: 11px; color: #94a3b8; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 20px; width: 360px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0; }
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; display: flex; align-items: center; gap: 6px; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-close:hover { color: #0f172a; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #334155; }
.form-group input, .form-group textarea { padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0; font-size: 14px; outline: none; background: #f8fafc; resize: vertical; }
.form-group textarea:focus, .form-group input:focus { border-color: #2563eb; background: white; }
.star-rating { display: flex; align-items: center; gap: 4px; }
.star { font-size: 28px; color: #d1d5db; cursor: pointer; transition: color 0.15s; }
.star.active { color: #f59e0b; }
.star:hover { color: #fbbf24; }
.score-text { margin-left: 8px; font-size: 14px; color: #64748b; }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
.modal-btn { padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; }
.modal-btn.cancel { background: #f1f5f9; color: #475569; }
.modal-btn.cancel:hover { background: #e2e8f0; }
.modal-btn.confirm { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; }
.modal-btn.confirm:hover:not(:disabled) { box-shadow: 0 3px 10px rgba(37,99,235,.3); }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-spinner { display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
</style>