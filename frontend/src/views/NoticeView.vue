<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>公告管理</h2>
        <p>发布和管理系统公告</p>
      </div>
      <button class="add-btn" @click="openAdd">＋ 发布公告</button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="notices.length" class="list">
      <div v-for="notice in notices" :key="notice.noticeId" class="notice-card">
        <div class="notice-header">
          <strong>📢 {{ notice.title }}</strong>
          <span :class="['status-badge', notice.status === 1 ? 'published' : 'draft']">
            {{ notice.status === 1 ? '已发布' : '已下架' }}
          </span>
        </div>
        <div class="notice-content">{{ notice.content }}</div>
        <div class="notice-meta">
          <span>📅 {{ formatTime(notice.publishTime || notice.createTime) }}</span>
        </div>
        <div class="notice-actions">
          <button v-if="notice.status === 1" class="action-btn unpublish" @click="toggleStatus(notice, 0)">下架</button>
          <button v-if="notice.status === 0" class="action-btn publish" @click="toggleStatus(notice, 1)">发布</button>
          <button class="action-btn delete" @click="doDelete(notice)">删除</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon"><i class="iconfont3 icon3-kong" style="font-size:48px;color:#cbd5e1;"></i></div>
      <p class="empty-text">暂无公告</p>
      <p class="empty-hint">点击右上角"发布公告"创建</p>
    </div>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingNotice ? '✏️ 编辑公告' : '📢 发布公告' }}</h3>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>公告标题</label>
            <input v-model="form.title" placeholder="请输入公告标题" />
          </div>
          <div class="form-group">
            <label>公告内容</label>
            <textarea v-model="form.content" placeholder="请输入公告内容..." rows="6"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="closeModal">取消</button>
          <button class="modal-btn confirm" @click="submitNotice" :disabled="saving">{{ saving ? '提交中...' : '提交' }}</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from '../toast'
import { store } from '../store'

const BACKEND_BASE_URL = 'http://localhost:8080'
const notices = ref([])
const loading = ref(false)
const showModal = ref(false)
const saving = ref(false)
const editingNotice = ref(null)
const form = ref({ title: '', content: '' })

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

async function loadNotices() {
  loading.value = true
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/notice/page?pageSize=50`)
    const data = await res.json()
    if (data.code === 200) {
      notices.value = (data.data?.records || []).sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''))
    }
  } catch (e) {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingNotice.value = null
  form.value = { title: '', content: '' }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingNotice.value = null
}

async function submitNotice() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    showToast('请填写标题和内容', 'error')
    return
  }
  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      content: form.value.content,
      adminId: store.userId,
      status: 1,
      publishTime: new Date().toISOString()
    }
    if (editingNotice.value) {
      payload.noticeId = editingNotice.value.noticeId
    }
    const method = editingNotice.value ? 'PUT' : 'POST'
    const res = await fetch(`${BACKEND_BASE_URL}/notice`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast(editingNotice.value ? '公告已更新' : '公告已发布', 'success')
      closeModal()
      loadNotices()
    } else {
      showToast(data.message || '操作失败', 'error')
    }
  } catch (e) {
    showToast('操作失败', 'error')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(notice, status) {
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/notice`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ noticeId: notice.noticeId, status })
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast(status === 1 ? '已发布' : '已下架', 'success')
      loadNotices()
    }
  } catch (e) {
    showToast('操作失败', 'error')
  }
}

async function doDelete(notice) {
  if (!confirm('确定删除此公告？')) return
  try {
    await fetch(`${BACKEND_BASE_URL}/notice/${notice.noticeId}`, { method: 'DELETE' })
    showToast('已删除', 'success')
    loadNotices()
  } catch (e) {
    showToast('删除失败', 'error')
  }
}

onMounted(loadNotices)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }
.add-btn { padding: 10px 20px; background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; font-size: 14px; }
.list { display: flex; flex-direction: column; gap: 12px; }
.notice-card { background: white; border-radius: 14px; padding: 18px 20px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.notice-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.notice-header strong { font-size: 15px; color: #0f172a; }
.status-badge { padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.status-badge.published { background: #d1fae5; color: #065f46; }
.status-badge.draft { background: #f1f5f9; color: #64748b; }
.notice-content { font-size: 14px; color: #475569; line-height: 1.6; margin-bottom: 10px; }
.notice-meta { font-size: 12px; color: #94a3b8; }
.notice-actions { display: flex; gap: 8px; margin-top: 10px; }
.action-btn { padding: 5px 12px; border: none; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; }
.action-btn.publish { background: #d1fae5; color: #065f46; }
.action-btn.unpublish { background: #fef3c7; color: #92400e; }
.action-btn.delete { background: #fee2e2; color: #991b1b; }
.loading-state { text-align: center; padding: 60px; color: #64748b; }
.spinner { width: 24px; height: 24px; border: 3px solid #e2e8f0; border-top-color: #2563eb; border-radius: 50%; animation: spin 0.6s linear infinite; margin: 0 auto 10px; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 60px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { color: #64748b; font-size: 16px; font-weight: 600; }
.empty-hint { color: #94a3b8; font-size: 14px; margin-top: 4px; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 20px; width: 500px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px 0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #334155; }
.form-group input, .form-group textarea { padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0; font-size: 14px; outline: none; background: #f8fafc; font-family: inherit; }
.form-group textarea:focus, .form-group input:focus { border-color: #2563eb; background: white; }
.modal-footer { display: flex; gap: 10px; padding: 0 24px 20px; justify-content: flex-end; }
.modal-btn { padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; }
.modal-btn.cancel { background: #f1f5f9; color: #475569; }
.modal-btn.confirm { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>