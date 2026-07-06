<template>
  <section class="page">
    <div class="auth-card">
      <div class="auth-tabs">
        <button :class="['tab-btn', mode === 'login' ? 'active' : '']" @click="switchMode('login')">登录</button>
        <button :class="['tab-btn', mode === 'register' ? 'active' : '']" @click="switchMode('register')">注册</button>
      </div>

      <div class="role-switch">
        <button :class="['role-btn', role === 'user' ? 'active' : '']" @click="role = 'user'">普通用户</button>
        <button :class="['role-btn', role === 'admin' ? 'active' : '']" @click="role = 'admin'">管理员</button>
      </div>

      <div v-if="mode === 'login'" class="panel">
        <h3>{{ role === 'admin' ? '管理员登录' : '用户登录' }}</h3>
        <p class="hint">请输入账号和密码继续使用系统。</p>
        <form @submit.prevent="doLogin">
          <input v-model="loginForm.username" :placeholder="role === 'admin' ? '管理员账号' : '用户名'" />
          <input v-model="loginForm.password" type="password" placeholder="密码" />
          <button type="submit">登录</button>
        </form>
        <p v-if="loginMessage" :class="loginMessageType">{{ loginMessage }}</p>
      </div>

      <div v-else class="panel">
        <h3>{{ role === 'admin' ? '创建管理员账号' : '创建普通用户账号' }}</h3>
        <p class="hint">填写以下信息完成注册。</p>
        <form @submit.prevent="doRegister">
          <input v-model="registerForm.username" :placeholder="role === 'admin' ? '管理员账号' : '用户名'" />
          <input v-model="registerForm.password" type="password" placeholder="密码" />
          <template v-if="role === 'user'">
            <input v-model="registerForm.realName" placeholder="真实姓名" />
            <input v-model="registerForm.phone" placeholder="手机号" />
          </template>
          <template v-else>
            <input v-model="registerForm.mail" placeholder="邮箱" />
          </template>
          <button type="submit">注册</button>
        </form>
        <p v-if="registerMessage" :class="registerMessageType">{{ registerMessage }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const router = useRouter()
const mode = ref('login')
const role = ref('user')
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', mail: '' })
const loginMessage = ref('')
const loginMessageType = ref('')
const registerMessage = ref('')
const registerMessageType = ref('')

function switchMode(nextMode) {
  mode.value = nextMode
  router.replace({ query: { mode: nextMode, role: role.value } })
}

function validateLogin() {
  if (!loginForm.username.trim()) {
    loginMessage.value = role.value === 'admin' ? '管理员账号不能为空' : '用户名不能为空'
    loginMessageType.value = 'error'
    return false
  }
  if (!loginForm.password.trim()) {
    loginMessage.value = '密码不能为空'
    loginMessageType.value = 'error'
    return false
  }
  return true
}

function validateRegister() {
  if (!registerForm.username.trim()) {
    registerMessage.value = role.value === 'admin' ? '管理员账号不能为空' : '用户名不能为空'
    registerMessageType.value = 'error'
    return false
  }
  if (!registerForm.password.trim()) {
    registerMessage.value = '密码不能为空'
    registerMessageType.value = 'error'
    return false
  }
  if (role.value === 'user') {
    if (!registerForm.realName.trim()) {
      registerMessage.value = '真实姓名不能为空'
      registerMessageType.value = 'error'
      return false
    }
    if (!registerForm.phone.trim()) {
      registerMessage.value = '手机号不能为空'
      registerMessageType.value = 'error'
      return false
    }
  } else if (!registerForm.mail.trim()) {
    registerMessage.value = '邮箱不能为空'
    registerMessageType.value = 'error'
    return false
  }
  return true
}

async function doLogin() {
  if (!validateLogin()) return
  loginMessage.value = ''
  loginMessageType.value = ''
  const res = await api.login({
    username: loginForm.username,
    password: loginForm.password,
    ...(role.value === 'admin' ? {} : {})
  }, role.value)
  if (res.code === 200) {
    loginMessage.value = '登录成功'
    loginMessageType.value = 'success'
  } else {
    loginMessage.value = res.message || '登录失败'
    loginMessageType.value = 'error'
  }
}

async function doRegister() {
  if (!validateRegister()) return
  registerMessage.value = ''
  registerMessageType.value = ''
  const payload = role.value === 'admin'
    ? { adminName: registerForm.username, password: registerForm.password, mail: registerForm.mail }
    : { username: registerForm.username, password: registerForm.password, realName: registerForm.realName, phone: registerForm.phone }
  const res = await api.register(payload, role.value)
  if (res.code === 200) {
    registerMessage.value = '注册成功'
    registerMessageType.value = 'success'
    switchMode('login')
  } else {
    registerMessage.value = res.message || '注册失败'
    registerMessageType.value = 'error'
  }
}

onMounted(() => {
  const queryMode = route.query.mode === 'register' ? 'register' : 'login'
  const queryRole = route.query.role === 'admin' ? 'admin' : 'user'
  mode.value = queryMode
  role.value = queryRole
})
</script>

<style scoped>
.page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}
.auth-card {
  width: min(480px, 100%);
  background: white;
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
  padding: 24px;
}
.auth-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.role-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tab-btn, .role-btn {
  flex: 1;
  border: none;
  border-radius: 999px;
  padding: 10px 12px;
  cursor: pointer;
  background: #f3f4f6;
  color: #374151;
  font-weight: 600;
}
.tab-btn.active, .role-btn.active {
  background: #2563eb;
  color: white;
}
.panel h3 { margin-bottom: 6px; }
.hint { color: #6b7280; margin-bottom: 14px; }
form { display: flex; flex-direction: column; gap: 10px; }
input, button { padding: 10px 12px; border-radius: 10px; border: 1px solid #d1d5db; }
button { background: #2563eb; color: white; border: none; cursor: pointer; }
.success { color: #059669; }
.error { color: #dc2626; }
</style>
