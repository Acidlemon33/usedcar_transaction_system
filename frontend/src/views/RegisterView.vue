<template>
  <section class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h2>创建账号</h2>
        <p class="auth-subtitle">注册后即可使用系统的全部功能</p>
      </div>

      <div class="role-switch">
        <button :class="['role-btn', role === 'user' ? 'active' : '']" @click="role = 'user'">
          <i class="iconfont icon-a-217_kehu role-icon"></i> 普通用户
        </button>
        <button :class="['role-btn', role === 'admin' ? 'active' : '']" @click="role = 'admin'">
          <i class="iconfont icon-a-217_shezhi role-icon"></i> 管理员
        </button>
      </div>

      <form @submit.prevent="doRegister" class="auth-form">
        <div class="form-group">
          <label>{{ role === 'admin' ? '管理员账号' : '用户名' }}</label>
          <div class="input-wrap">
            <i class="iconfont5 icon5-yonghu input-icon"></i>
            <input v-model="form.username" :placeholder="role === 'admin' ? '请设置管理员账号' : '请设置用户名'" />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrap">
            <i class="iconfont5 icon5-mima input-icon"></i>
            <input v-model="form.password" type="password" placeholder="请设置密码（至少6位）" />
          </div>
        </div>

        <template v-if="role === 'user'">
          <div class="form-group">
            <label>真实姓名</label>
            <div class="input-wrap">
              <i class="iconfont5 icon5-zuoxixingming input-icon"></i>
              <input v-model="form.realName" placeholder="请输入真实姓名" />
            </div>
          </div>
          <div class="form-group">
            <label>手机号</label>
            <div class="input-wrap">
              <i class="iconfont5 icon5-shoujihao input-icon"></i>
              <input v-model="form.phone" placeholder="请输入手机号" />
            </div>
          </div>
        </template>
        <template v-else>
          <div class="form-group">
            <label>邮箱</label>
            <div class="input-wrap">
              <i class="iconfont5 icon5-yonghu input-icon"></i>
              <input v-model="form.mail" placeholder="请输入邮箱地址" />
            </div>
          </div>
        </template>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="btn-spinner"></span>
          <span v-else>注 册</span>
        </button>
      </form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/account/login" class="link">立即登录</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { showToast } from '../toast'

const router = useRouter()
const role = ref('user')
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  mail: ''
})

function validate() {
  if (!form.username.trim()) {
    showToast(role.value === 'admin' ? '请输入管理员账号' : '请输入用户名', 'error')
    return false
  }
  if (!form.password.trim() || form.password.length < 6) {
    showToast('密码至少6位', 'error')
    return false
  }
  if (role.value === 'user') {
    if (!form.realName.trim()) { showToast('请输入真实姓名', 'error'); return false }
    if (!form.phone.trim()) { showToast('请输入手机号', 'error'); return false }
  } else if (!form.mail.trim()) {
    showToast('请输入邮箱', 'error'); return false
  }
  return true
}

async function doRegister() {
  if (!validate()) return
  loading.value = true

  const payload = role.value === 'admin'
    ? { adminName: form.username, password: form.password, mail: form.mail }
    : { username: form.username, password: form.password, realName: form.realName, phone: form.phone }

  const res = await api.register(payload, role.value)
  loading.value = false

  if (res.code === 200) {
    showToast('注册成功！欢迎加入', 'success')
    setTimeout(() => router.push('/account/login'), 1500)
  } else {
    showToast(res.message || '注册失败，请稍后重试', 'error')
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  padding: 20px;
}

.auth-card {
  width: min(420px, 100%);
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.12);
  padding: 36px 32px 28px;
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-header {
  text-align: center;
  margin-bottom: 24px;
}

.auth-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.auth-subtitle {
  color: #64748b;
  margin: 6px 0 0;
  font-size: 14px;
}

.role-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
}

.role-btn {
  flex: 1;
  border: none;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  background: transparent;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.role-btn.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.role-icon { font-size: 16px; }

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
  display: block;
}

.input-wrap {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 0 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-wrap:focus-within {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.input-icon {
  font-size: 16px;
  margin-right: 10px;
  opacity: 0.7;
  font-style: normal;
}

.input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  font-size: 14px;
  color: #0f172a;
  outline: none;
}

.input-wrap input::placeholder { color: #94a3b8; }

.submit-btn {
  margin-top: 6px;
  padding: 13px;
  background: linear-gradient(135deg, #059669, #047857);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(5, 150, 105, 0.3);
}

.submit-btn:active:not(:disabled) { transform: translateY(0); }

.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer {
  text-align: center;
  margin-top: 24px;
  color: #64748b;
  font-size: 14px;
}

.link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.link:hover { text-decoration: underline; }
</style>