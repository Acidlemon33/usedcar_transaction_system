<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2>{{ isAdmin ? '管理员中心' : '个人中心' }}</h2>
        <p>{{ isAdmin ? '管理您的管理员账户信息。' : '管理您的账户信息。' }}</p>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else class="profile-grid">
      <!-- 个人信息卡片 -->
      <div class="card">
        <h3>基本信息</h3>

        <!-- 管理员信息 -->
        <div v-if="isAdmin" class="info-list">
          <div class="info-row">
            <span class="info-label">管理员账号</span>
            <span class="info-value">{{ adminInfo?.adminName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">电子邮箱</span>
            <span class="info-value">{{ adminInfo?.mail || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">状态</span>
            <span class="info-value">{{ adminInfo?.state === 1 ? '在职' : '离职' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatTime(adminInfo?.createTime) }}</span>
          </div>
        </div>

        <!-- 普通用户信息 -->
        <div v-else class="info-list">
          <div class="info-row">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo?.username || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">真实姓名</span>
            <span class="info-value">{{ userInfo?.realName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo?.phone || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">信用分</span>
            <span class="info-value credit">{{ userInfo?.creditScore || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">账户余额</span>
            <span class="info-value balance">¥{{ userInfo?.balance || 0 }}</span>
          </div>
        </div>

        <div class="card-actions">
          <button class="action-btn edit" @click="openEditModal"><i class="iconfont icon-a-217_shezhi"></i> 编辑信息</button>
          <button v-if="!isAdmin" class="action-btn delete" @click="doDelete"><i class="iconfont icon-a-217_shanchu"></i> 注销账户</button>
        </div>
      </div>

      <!-- 统计卡片（仅普通用户） -->
      <div v-if="!isAdmin" class="card">
        <h3><i class="iconfont icon-a-217_redian" style="color:#3b82f6;"></i> 账户统计</h3>
        <div class="stats">
          <div class="stat-item">
            <span class="stat-value">{{ carCount }}</span>
            <span class="stat-label">车辆数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ publishCount }}</span>
            <span class="stat-label">发布数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ favCount }}</span>
            <span class="stat-label">收藏数</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="iconfont icon-a-217_shezhi" style="color:#2563eb;"></i> {{ isAdmin ? '编辑个人信息' : '编辑信息' }}</h3>
          <button class="modal-close" @click="closeEditModal">&times;</button>
        </div>
        <div class="modal-body">
          <!-- 管理员编辑字段 -->
          <template v-if="isAdmin">
            <div class="form-group">
              <label>管理员账号</label>
              <input v-model="adminEditForm.adminName" type="text" placeholder="管理员账号" />
            </div>
            <div class="form-group">
              <label>电子邮箱</label>
              <input v-model="adminEditForm.mail" type="text" placeholder="邮箱" />
            </div>
            <div class="form-group">
              <label>密码（留空不修改）</label>
              <input v-model="adminEditForm.password" type="password" placeholder="新密码" />
            </div>
          </template>
          <!-- 普通用户编辑字段 -->
          <template v-else>
            <div class="form-group">
              <label>真实姓名</label>
              <input v-model="editForm.realName" type="text" placeholder="真实姓名" />
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input v-model="editForm.phone" type="text" placeholder="手机号" />
            </div>
            <div class="form-group">
              <label>密码（留空不修改）</label>
              <input v-model="editForm.password" type="password" placeholder="新密码" />
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="closeEditModal">取消</button>
          <button class="modal-btn confirm" @click="submitEdit" :disabled="savingEdit">
            <span v-if="savingEdit" class="spinner"></span>
            <span v-else>保存</span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { showToast } from '../toast'
import { store } from '../store'

const router = useRouter()
const userInfo = ref(null)
const adminInfo = ref(null)
const loading = ref(true)
const showEditModal = ref(false)
const savingEdit = ref(false)
const isAdmin = computed(() => store.isAdmin)

const carCount = ref(0)
const publishCount = ref(0)
const favCount = ref(0)

// 普通用户编辑表单
const editForm = reactive({
  realName: '',
  phone: '',
  password: ''
})

// 管理员编辑表单
const adminEditForm = reactive({
  adminName: '',
  mail: '',
  password: ''
})

const currentUserId = computed(() => store.userId)
const BACKEND_BASE_URL = 'http://localhost:8080'

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

async function loadProfile() {
  if (!currentUserId.value) {
    router.push('/account/login')
    return
  }
  loading.value = true
  try {
    if (isAdmin.value) {
      // 加载管理员信息
      const res = await fetch(`${BACKEND_BASE_URL}/admin/${currentUserId.value}`)
      const data = await res.json()
      if (data.code === 200 && data.data) {
        adminInfo.value = data.data
        adminEditForm.adminName = data.data.adminName || ''
        adminEditForm.mail = data.data.mail || ''
      }
    } else {
      // 加载普通用户信息
      const res = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`)
      const data = await res.json()
      if (data.code === 200 && data.data) {
        userInfo.value = data.data
        editForm.realName = data.data.realName || ''
        editForm.phone = data.data.phone || ''
      }
      // 加载统计
      const [carRes, saleRes, favRes] = await Promise.all([
        api.getCars(1, 1, currentUserId.value),
        api.getMySales(currentUserId.value),
        api.getFavorites(currentUserId.value)
      ])
      if (carRes.code === 200) carCount.value = carRes.data?.total || 0
      if (saleRes.code === 200) publishCount.value = (saleRes.data || []).length
      if (favRes.code === 200) favCount.value = favRes.data?.total || 0
    }
  } catch (e) {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

function openEditModal() {
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
}

async function submitEdit() {
  savingEdit.value = true
  try {
    if (isAdmin.value) {
      // 更新管理员信息
      const payload = {
        adminId: currentUserId.value,
        adminName: adminEditForm.adminName,
        mail: adminEditForm.mail
      }
      if (adminEditForm.password) payload.password = adminEditForm.password

      const res = await fetch(`${BACKEND_BASE_URL}/admin`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      const data = await res.json()
      if (data.code === 200) {
        showToast('修改成功', 'success')
        closeEditModal()
        loadProfile()
      } else {
        showToast(data.message || '修改失败', 'error')
      }
    } else {
      // 更新普通用户信息
      if (!editForm.realName.trim() && !editForm.phone.trim()) {
        showToast('请填写至少一项', 'error')
        return
      }
      const payload = {
        userId: currentUserId.value,
        realName: editForm.realName,
        phone: editForm.phone
      }
      if (editForm.password) payload.password = editForm.password

      const res = await fetch(`${BACKEND_BASE_URL}/user`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      const data = await res.json()
      if (data.code === 200) {
        showToast('修改成功', 'success')
        closeEditModal()
        loadProfile()
      } else {
        showToast(data.message || '修改失败', 'error')
      }
    }
  } catch (e) {
    showToast('修改失败', 'error')
  } finally {
    savingEdit.value = false
  }
}

async function doDelete() {
  if (!confirm('确定要注销账户吗？此操作不可撤销！')) return
  if (!confirm('再次确认：删除后所有数据将丢失！')) return
  try {
    const res = await fetch(`${BACKEND_BASE_URL}/user/${currentUserId.value}`, {
      method: 'DELETE'
    })
    const data = await res.json()
    if (data.code === 200) {
      showToast('账户已注销', 'success')
      store.logout()
      router.push('/')
    } else {
      showToast(data.message || '注销失败', 'error')
    }
  } catch (e) {
    showToast('注销失败', 'error')
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: center; }
.page-head h2 { margin: 0; }
.page-head p { color: #64748b; margin: 4px 0 0; font-size: 14px; }

.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.card {
  background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,.06);
}
.card h3 { margin: 0 0 16px; font-size: 16px; color: #0f172a; }

.info-list { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
.info-row:last-child { border: none; }
.info-label { font-size: 13px; color: #64748b; }
.info-value { font-size: 14px; color: #0f172a; font-weight: 600; }
.info-value.credit { color: #059669; }
.info-value.balance { color: #dc2626; }

.card-actions { display: flex; gap: 10px; margin-top: 20px; }
.action-btn {
  flex: 1; padding: 10px; border-radius: 10px; border: none;
  font-size: 13px; font-weight: 600; cursor: pointer;
}
.action-btn.edit { background: #dbeafe; color: #1e40af; }
.action-btn.edit:hover { background: #bfdbfe; }
.action-btn.delete { background: #fce7f3; color: #9d174d; }
.action-btn.delete:hover { background: #fbcfe8; }

.stats { display: flex; gap: 16px; }
.stat-item {
  flex: 1; text-align: center; padding: 20px 10px;
  background: #f8fafc; border-radius: 12px;
}
.stat-value { display: block; font-size: 28px; font-weight: 800; color: #2563eb; }
.stat-label { font-size: 13px; color: #64748b; margin-top: 4px; }

/* Modal */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,.4); display: flex; align-items: center;
  justify-content: center; z-index: 1000;
}
.modal-content {
  background: white; border-radius: 20px; width: 420px;
  max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,.2); overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px 0;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.modal-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-close:hover { color: #0f172a; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #334155; }
.form-group input {
  padding: 11px 14px; border-radius: 10px; border: 1.5px solid #e2e8f0;
  font-size: 14px; outline: none; background: #f8fafc;
}
.form-group input:focus { border-color: #2563eb; background: white; }
.modal-footer {
  display: flex; gap: 10px; padding: 0 24px 20px; justify-content: flex-end;
}
.modal-btn {
  padding: 10px 20px; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; border: none;
}
.modal-btn.cancel { background: #f1f5f9; color: #475569; }
.modal-btn.cancel:hover { background: #e2e8f0; }
.modal-btn.confirm { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; }
.modal-btn.confirm:hover { box-shadow: 0 3px 10px rgba(37,99,235,.3); }
.modal-btn.confirm:disabled { opacity: 0.6; cursor: not-allowed; }

.spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }

.loading-state { text-align: center; padding: 80px; color: #64748b; }
</style>