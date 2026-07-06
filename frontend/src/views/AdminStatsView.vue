<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>数据统计</h2>
        <p>系统运营数据概览</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载统计数据...</p>
    </div>

    <div v-else>
      <!-- 概览卡片 -->
      <div class="overview-grid">
        <div class="overview-card">
          <div class="ov-icon users"><i class="iconfont icon-a-217_kehu" style="font-size:36px;color:#2563eb;"></i></div>
          <div class="ov-info">
            <span class="ov-num">{{ stats.userCount }}</span>
            <span class="ov-label">注册用户</span>
          </div>
        </div>
        <div class="overview-card">
          <div class="ov-icon cars"><i class="iconfont icon-a-217_shangdian" style="font-size:36px;color:#059669;"></i></div>
          <div class="ov-info">
            <span class="ov-num">{{ stats.carCount }}</span>
            <span class="ov-label">车辆总数</span>
          </div>
        </div>
        <div class="overview-card">
          <div class="ov-icon orders"><i class="iconfont icon-a-217_dingdan" style="font-size:36px;color:#d97706;"></i></div>
          <div class="ov-info">
            <span class="ov-num">{{ stats.orderCount }}</span>
            <span class="ov-label">总订单数</span>
          </div>
        </div>
        <div class="overview-card">
          <div class="ov-icon revenue"><i class="iconfont icon-a-217_xihuan-26" style="font-size:36px;color:#ec4899;"></i></div>
          <div class="ov-info">
            <span class="ov-num">¥{{ formatPrice(stats.totalRevenue) }}</span>
            <span class="ov-label">交易总额</span>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-row">
        <!-- 订单状态分布 -->
        <div class="chart-card">
          <h3><i class="iconfont icon-a-217_dingdan" style="color:#d97706;"></i> 订单状态分布</h3>
          <div class="bar-chart">
            <div class="bar-item" v-for="item in orderStatusData" :key="item.label">
              <div class="bar-label">{{ item.label }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <div class="bar-value">{{ item.count }}</div>
            </div>
          </div>
        </div>

        <!-- 交易趋势 -->
        <div class="chart-card">
          <div class="chart-title-row">
            <h3><i class="iconfont icon-a-217_redian" style="color:#3b82f6;"></i> 交易趋势</h3>
            <select v-model="trendMode" @change="computeTrend" class="trend-select">
              <option value="day">按天（小时）</option>
              <option value="week">按周</option>
              <option value="month">按月</option>
              <option value="year">按年</option>
            </select>
          </div>
          <div class="trend-chart">
            <div class="trend-bar-wrap" v-for="(item, i) in trendData" :key="i">
              <div class="trend-bar-stacked" :style="{ height: item.percent + '%' }" :title="item.label + ': ¥' + formatPrice(item.amount) + ' / ' + item.count + '笔'">
                <div v-for="seg in item.segments" :key="seg.state" class="trend-bar-seg" :style="{ height: seg.percent + '%', background: seg.color }"></div>
              </div>
              <div class="trend-label">{{ item.label }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近订单 -->
      <div class="chart-card" style="margin-top:16px;">
        <h3><i class="iconfont icon-a-217_xihuan-26" style="color:#8b5cf6;"></i> 最近订单</h3>
        <table class="recent-table" v-if="recentOrders.length">
          <thead>
            <tr>
              <th>订单编号</th>
              <th>金额</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in recentOrders" :key="o.orderId">
              <td>{{ o.orderReference }}</td>
              <td>¥{{ formatPrice(o.transactionPrice) }}</td>
              <td><span :class="['status-badge-sm', statusClass(o.transactionState)]">{{ statusText(o.transactionState) }}</span></td>
              <td>{{ formatTime(o.createTime) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="no-data">暂无订单数据</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from '../toast'

const BACKEND_BASE_URL = 'http://localhost:8080'
const loading = ref(true)

const stats = ref({
  userCount: 0,
  carCount: 0,
  orderCount: 0,
  totalRevenue: 0
})

const orderStatusData = ref([])
const monthlyData = ref([])
const recentOrders = ref([])
const trendMode = ref('day')
const trendData = ref([])
let allOrders = []

function statusText(state) {
  const map = { 0: '待支付', 1: '已支付', 2: '已成交', 3: '售后中', 4: '已取消' }
  return map[state] || '未知'
}
function statusClass(state) {
  const map = { 0: 'pending', 1: 'paid', 2: 'done', 3: 'after-sale', 4: 'cancelled' }
  return map[state] || ''
}
function formatPrice(p) {
  if (!p) return '0'
  return Number(p).toLocaleString('zh-CN')
}
function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function loadStats() {
  loading.value = true
  try {
    // 获取用户数量
    const userRes = await fetch(`${BACKEND_BASE_URL}/user/page?pageSize=1`)
    const userData = await userRes.json()
    const userCount = userData.code === 200 ? (userData.data?.total || 0) : 0

    // 获取车辆数量
    const carRes = await fetch(`${BACKEND_BASE_URL}/carInfo/page?pageSize=1`)
    const carData = await carRes.json()
    const carCount = carData.code === 200 ? (carData.data?.total || 0) : 0

    // 获取订单数据
    const orderRes = await fetch(`${BACKEND_BASE_URL}/transaction_order/page?pageSize=100`)
    const orderData = await orderRes.json()
    let orders = []
    if (orderData.code === 200) {
      orders = orderData.data?.records || []
    }

    const orderCount = orders.length
    let totalRevenue = 0
    const statusCounts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 }

    orders.forEach(o => {
      if (o.transactionState === 1 || o.transactionState === 2) {
        totalRevenue += Number(o.transactionPrice || 0)
      }
      statusCounts[o.transactionState] = (statusCounts[o.transactionState] || 0) + 1
    })

    stats.value = { userCount, carCount, orderCount, totalRevenue }

    // 订单状态分布
    const total = orders.length || 1
    orderStatusData.value = Object.entries(statusLabels).map(([k, label]) => ({
      label,
      count: statusCounts[k] || 0,
      percent: Math.round(((statusCounts[k] || 0) / total) * 100),
      color: statusColors[k]
    }))

    // 月度交易趋势
    const monthlyMap = {}
    orders.filter(o => o.transactionState >= 1).forEach(o => {
      if (o.paymentTime || o.createTime) {
        const t = o.paymentTime || o.createTime
        const month = t.substring(0, 7) // YYYY-MM
        if (!monthlyMap[month]) monthlyMap[month] = 0
        monthlyMap[month] += Number(o.transactionPrice || 0)
      }
    })
    const sortedMonths = Object.entries(monthlyMap).sort(([a], [b]) => a.localeCompare(b))
    const maxAmount = Math.max(...sortedMonths.map(([, v]) => v), 1)
    monthlyData.value = sortedMonths.map(([label, amount]) => ({
      label: label.substring(5), // MM
      amount,
      percent: Math.round((amount / maxAmount) * 100)
    }))

    // 保存所有订单用于趋势计算
    allOrders = orders

    // 计算趋势
    computeTrend()

    // 最近5条订单
    recentOrders.value = orders.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || '')).slice(0, 5)

  } catch (e) {
    showToast('加载统计数据失败', 'error')
  } finally {
    loading.value = false
  }
}

const statusColors = { 0: '#f59e0b', 1: '#3b82f6', 2: '#10b981', 3: '#ec4899', 4: '#94a3b8' }
const statusLabels = { 0: '待支付', 1: '已支付', 2: '已成交', 3: '售后中', 4: '已取消' }

function getBucketKey(t, mode) {
  if (mode === 'day') return t.substring(11, 13) // hour
  if (mode === 'week') { const d = new Date(t.substring(0,10)); const idx = d.getDay(); return idx === 0 ? 6 : idx - 1 }
  if (mode === 'month') return t.substring(0, 7)
  if (mode === 'year') return t.substring(0, 4)
  return ''
}

function getBucketLabel(key, mode) {
  if (mode === 'day') return String(key).padStart(2,'0') + '时'
  if (mode === 'week') return ['周一','周二','周三','周四','周五','周六','周日'][key]
  if (mode === 'month') return key.substring(5)
  if (mode === 'year') return key
  return key
}

function computeTrend() {
  const paidOrders = allOrders.filter(o => o.transactionState >= 1 && (o.paymentTime || o.createTime))
  const mode = trendMode.value

  // Build raw data: { bucketKey: { totalAmount, totalCount, byStatus: { state: { amount, count } } } }
  const rawMap = {}

  // Initialize buckets
  if (mode === 'day') {
    for (let i = 0; i < 24; i++) {
      const k = String(i).padStart(2,'0')
      rawMap[k] = { amount: 0, count: 0, byStatus: {} }
    }
    const today = new Date().toISOString().substring(0, 10)
    paidOrders.forEach(o => {
      const t = o.paymentTime || o.createTime
      if (t.startsWith(today)) {
        const key = t.substring(11, 13)
        if (rawMap[key]) {
          const st = o.transactionState
          rawMap[key].amount += Number(o.transactionPrice || 0)
          rawMap[key].count++
          if (!rawMap[key].byStatus[st]) rawMap[key].byStatus[st] = { amount: 0, count: 0 }
          rawMap[key].byStatus[st].amount += Number(o.transactionPrice || 0)
          rawMap[key].byStatus[st].count++
        }
      }
    })
  } else if (mode === 'week') {
    const now = new Date()
    const dayOfWeek = now.getDay()
    const monday = new Date(now)
    monday.setDate(now.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
    const mondayStr = monday.toISOString().substring(0, 10)
    for (let i = 0; i < 7; i++) rawMap[i] = { amount: 0, count: 0, byStatus: {} }
    paidOrders.forEach(o => {
      const t = o.paymentTime || o.createTime
      if (t >= mondayStr) {
        const d = new Date(t.substring(0, 10))
        const idx = d.getDay()
        const key = idx === 0 ? 6 : idx - 1
        const st = o.transactionState
        rawMap[key].amount += Number(o.transactionPrice || 0)
        rawMap[key].count++
        if (!rawMap[key].byStatus[st]) rawMap[key].byStatus[st] = { amount: 0, count: 0 }
        rawMap[key].byStatus[st].amount += Number(o.transactionPrice || 0)
        rawMap[key].byStatus[st].count++
      }
    })
  } else {
    paidOrders.forEach(o => {
      const t = o.paymentTime || o.createTime
      const key = getBucketKey(t, mode)
      if (!rawMap[key]) rawMap[key] = { amount: 0, count: 0, byStatus: {} }
      const st = o.transactionState
      rawMap[key].amount += Number(o.transactionPrice || 0)
      rawMap[key].count++
      if (!rawMap[key].byStatus[st]) rawMap[key].byStatus[st] = { amount: 0, count: 0 }
      rawMap[key].byStatus[st].amount += Number(o.transactionPrice || 0)
      rawMap[key].byStatus[st].count++
    })
  }

  // Sort keys
  let sortedKeys
  if (mode === 'day') sortedKeys = Object.keys(rawMap).sort()
  else if (mode === 'week') sortedKeys = [0,1,2,3,4,5,6]
  else sortedKeys = Object.keys(rawMap).sort()

  // Build trend data with segments
  trendData.value = sortedKeys.map(key => {
    const data = rawMap[key]
    return {
      label: getBucketLabel(mode === 'week' ? Number(key) : key, mode),
      amount: data.amount,
      count: data.count,
      segments: []
    }
  })

  // 计算百分比 (total bar height)
  const maxAmount = Math.max(...trendData.value.map(d => d.amount), 1)
  trendData.value.forEach(d => {
    d.percent = Math.round((d.amount / maxAmount) * 100)
  })

  // For each bucket, generate segments by status
  trendData.value.forEach((d, i) => {
    const key = sortedKeys[i]
    const raw = rawMap[key]
    const segs = []
    const orderedStates = [1, 2, 3, 4]
    const bucketMax = raw.amount || 1
    orderedStates.forEach(st => {
      const sd = raw.byStatus[st]
      if (sd && sd.amount > 0) {
        segs.push({
          state: st,
          label: statusLabels[st] || '未知',
          amount: sd.amount,
          count: sd.count,
          percent: Math.round((sd.amount / bucketMax) * 100),
          color: statusColors[st] || '#94a3b8'
        })
      }
    })
    // If no segments but has amount (shouldn't happen)
    if (segs.length === 0 && d.amount > 0) {
      segs.push({ state: 1, label: '已支付', amount: d.amount, count: d.count, percent: 100, color: '#3b82f6' })
    }
    d.segments = segs
  })
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.loading-state { text-align: center; padding: 80px; color: #64748b; }
.spinner { width: 32px; height: 32px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.7s linear infinite; margin: 0 auto 12px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 概览卡片 */
.overview-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.overview-card { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); display: flex; align-items: center; gap: 16px; }
.ov-icon { font-size: 36px; width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; border-radius: 14px; }
.ov-icon.users { background: #dbeafe; }
.ov-icon.cars { background: #d1fae5; }
.ov-icon.orders { background: #fef3c7; }
.ov-icon.revenue { background: #fce7f3; }
.ov-info { display: flex; flex-direction: column; }
.ov-num { font-size: 24px; font-weight: 800; color: #0f172a; }
.ov-label { font-size: 13px; color: #64748b; margin-top: 2px; }

/* 图表区 */
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 8px; }
.chart-card { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
.chart-card h3 { margin: 0 0 16px; font-size: 16px; color: #0f172a; }

/* 条形图 */
.bar-chart { display: flex; flex-direction: column; gap: 12px; }
.bar-item { display: flex; align-items: center; gap: 10px; }
.bar-label { width: 60px; font-size: 13px; color: #64748b; flex-shrink: 0; }
.bar-track { flex: 1; height: 20px; background: #f1f5f9; border-radius: 10px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 10px; transition: width 0.6s ease; min-width: 4px; }
.bar-value { width: 40px; font-size: 13px; font-weight: 700; color: #0f172a; text-align: right; }

/* 趋势图 */
.chart-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.chart-title-row h3 {
  margin: 0 !important;
}
.trend-select {
  padding: 6px 10px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  background: #f8fafc;
  outline: none;
  cursor: pointer;
}
.trend-select:focus {
  border-color: #2563eb;
}
.trend-chart { display: flex; align-items: flex-end; gap: 8px; height: 160px; padding-top: 10px; }
.trend-bar-wrap { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
.trend-bar-stacked { width: 100%; max-width: 40px; border-radius: 6px 6px 0 0; overflow: hidden; display: flex; flex-direction: column; justify-content: flex-end; transition: height 0.6s ease; }
.trend-bar-seg { width: 100%; transition: height 0.6s ease; }
.trend-label { font-size: 11px; color: #94a3b8; margin-top: 6px; }

/* 表格 */
.recent-table { width: 100%; border-collapse: collapse; }
.recent-table th { text-align: left; font-size: 12px; color: #94a3b8; font-weight: 600; padding: 8px 12px; border-bottom: 1px solid #f1f5f9; }
.recent-table td { padding: 10px 12px; font-size: 13px; color: #0f172a; border-bottom: 1px solid #f1f5f9; }
.recent-table tr:last-child td { border: none; }
.status-badge-sm { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.status-badge-sm.pending { background: #fef3c7; color: #92400e; }
.status-badge-sm.paid { background: #dbeafe; color: #1e40af; }
.status-badge-sm.done { background: #d1fae5; color: #065f46; }
.status-badge-sm.after-sale { background: #fce7f3; color: #9d174d; }
.status-badge-sm.cancelled { background: #f1f5f9; color: #64748b; }
.no-data { text-align: center; padding: 30px; color: #94a3b8; font-size: 14px; }
</style>