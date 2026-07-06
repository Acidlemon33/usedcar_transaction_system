<template>
  <section class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h2>欢迎回来</h2>
        <p class="auth-subtitle">登录您的账号继续使用系统</p>
      </div>

      <div class="role-switch">
        <button :class="['role-btn', role === 'user' ? 'active' : '']" @click="role = 'user'">
          <i class="iconfont icon-a-217_kehu role-icon"></i> 普通用户
        </button>
        <button :class="['role-btn', role === 'admin' ? 'active' : '']" @click="role = 'admin'">
          <i class="iconfont icon-a-217_shezhi role-icon"></i> 管理员
        </button>
      </div>

      <form @submit.prevent="doLogin" class="auth-form">
        <div class="form-group">
          <label>{{ role === 'admin' ? '管理员账号' : '用户名' }}</label>
          <div class="input-wrap">
            <i class="iconfont5 icon5-yonghu input-icon"></i>
            <input v-model="loginForm.username" :placeholder="role === 'admin' ? '请输入管理员账号' : '请输入用户名'" />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrap">
            <i class="iconfont5 icon5-mima input-icon"></i>
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="btn-spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/account/register" class="link">立即注册</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import { showToast } from '../toast'
import { store } from '../store'

const route = useRoute()
const router = useRouter()
const role = ref('user')
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

async function doLogin() {
  if (!loginForm.username.trim()) {
    showToast(role.value === 'admin' ? '请输入管理员账号' : '请输入用户名', 'error')
    return
  }
  if (!loginForm.password.trim()) {
    showToast('请输入密码', 'error')
    return
  }

  loading.value = true
  let res
  // 同时发送 username 和 adminName 以兼容不同后端字段名
  const loginPayload = {
    username: loginForm.username,
    adminName: loginForm.username,
    password: loginForm.password
  }
  res = await api.login(loginPayload, role.value)
  loading.value = false

  if (res.code === 200) {
    // 保存用户信息到全局状态
    const userData = res.data
    if (userData) {
      store.login({ ...userData, role: role.value })
    }
    showToast('登录成功！欢迎回来', 'success')
    setTimeout(() => router.push('/'), 1200)
  } else {
    showToast(res.message || '登录失败，请检查账号和密码', 'error')
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
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
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
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.3);
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