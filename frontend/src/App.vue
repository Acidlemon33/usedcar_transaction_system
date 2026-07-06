<template>
  <div class="app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="sidebar">
      <div class="brand-row">
        <div class="brand">
          <h1>{{ sidebarCollapsed ? '' : '二手车管理台' }}</h1>
          <p v-if="!sidebarCollapsed">智能交易 · 轻量管理</p>
        </div>
        <button class="collapse-btn" @click="toggleSidebar" :title="sidebarCollapsed ? '展开侧栏' : '收起侧栏'">
          <span class="collapse-icon">{{ sidebarCollapsed ? '›' : '‹' }}</span>
        </button>
      </div>
      <!-- 普通用户菜单 -->
      <template v-if="!isAdmin">
        <nav>
          <router-link to="/" exact-active-class="active" :title="sidebarCollapsed ? '首页' : ''">
            <i class="iconfont icon-a-217_zhuye nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">首页</span>
          </router-link>
          <router-link to="/inventory" active-class="active" :title="sidebarCollapsed ? '车辆管理' : ''">
            <i class="iconfont icon-a-217_shangdian nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">车辆管理</span>
          </router-link>
          <router-link to="/orders" active-class="active" :title="sidebarCollapsed ? '我发布的' : ''">
            <i class="iconfont icon-a-217_dingdan nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">我发布的</span>
          </router-link>
          <router-link to="/myorders" active-class="active" :title="sidebarCollapsed ? '我的订单' : ''">
            <i class="iconfont icon-a-217_gouwuche nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">我的订单</span>
          </router-link>
          <router-link to="/favorites" active-class="active" :title="sidebarCollapsed ? '我的收藏' : ''">
            <i class="iconfont icon-a-217_shoucang nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">我的收藏</span>
          </router-link>
        </nav>
      </template>
      <!-- 管理员菜单 -->
      <template v-else>
        <nav>
          <router-link to="/" exact-active-class="active" :title="sidebarCollapsed ? '首页' : ''">
            <i class="iconfont icon-a-217_zhuye nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">首页</span>
          </router-link>
          <router-link to="/admin/stats" active-class="active" :title="sidebarCollapsed ? '数据统计' : ''">
            <i class="iconfont icon-a-217_dingdan nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">数据统计</span>
          </router-link>
          <router-link to="/admin/notices" active-class="active" :title="sidebarCollapsed ? '公告管理' : ''">
            <i class="iconfont icon-a-217_xiaoxi nav-icon"></i>
            <span v-if="!sidebarCollapsed" class="nav-text">公告管理</span>
          </router-link>
        </nav>
      </template>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div>
          <h2>二手车交易系统</h2>
          <p v-if="!sidebarCollapsed" style="font-size:12px;margin-top:6px;color:#999;">个人二手车智能管理平台</p>
        </div>
        <div class="topbar-actions">
          <template v-if="isLoggedIn">
            <router-link to="/profile" class="user-info"> {{ isAdmin ? (currentUser?.adminName || '管理员') : (currentUser?.username || '用户') }}</router-link>
            <button class="ghost-btn" @click="doLogout">退出登录</button>
          </template>
          <template v-else>
            <router-link class="ghost-btn" to="/account/login">登录</router-link>
            <router-link class="primary-btn" to="/account/register">注册</router-link>
          </template>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from './store'
import { showToast } from './toast'

const router = useRouter()
const sidebarCollapsed = ref(false)
const isLoggedIn = computed(() => store.isLoggedIn)
const isAdmin = computed(() => store.isAdmin)
const currentUser = computed(() => store.currentUser)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function doLogout() {
  store.logout()
  showToast('已退出登录', 'success')
  router.push('/')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  background: #f3f6fb;
}
.sidebar {
  width: 240px;
  min-width: 240px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  color: #1e293b;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  overflow: hidden;
  border-right: 1px solid #e5e7eb;
}
.sidebar-collapsed .sidebar {
  width: 64px;
  min-width: 64px;
  padding: 24px 8px;
  align-items: center;
}

/* 标题行：品牌 + 收起按钮在同一行 */
.brand-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 24px;
}
.sidebar-collapsed .brand-row {
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.brand {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.brand h1 { margin: 0; font-size: 20px; white-space: nowrap; }
.sidebar-collapsed .brand h1 { font-size: 24px; text-align: center; }
.brand p { margin: 6px 0 0; color: #64748b; font-size: 13px; white-space: nowrap; }

/* 收起按钮 - 小矩形，与标题同一高度 */
.collapse-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: all 0.2s;
  margin-top: 2px;
}
.collapse-btn:hover {
  background: #e2e8f0;
  border-color: #cbd5e1;
}
.sidebar-collapsed .collapse-btn {
  width: 32px;
  height: 32px;
}
.collapse-icon {
  color: #64748b;
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
  transition: color 0.2s;
}
.collapse-btn:hover .collapse-icon {
  color: #0f172a;
}

nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}
.sidebar-collapsed nav {
  align-items: center;
}
nav a {
  text-decoration: none;
  color: #475569;
  padding: 10px 12px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
  transition: background 0.2s;
}
.sidebar-collapsed nav a {
  justify-content: center;
  padding: 10px;
  width: 44px;
  border-radius: 50%;
}
nav a.active, nav a:hover {
  background: #e2e8f0;
  color: #0f172a;
}
.sidebar-collapsed nav a.active, .sidebar-collapsed nav a:hover {
  background: #e2e8f0;
}
.nav-icon {
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
}
.nav-text {
  font-size: 14px;
  line-height: 1;
}
.nav-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 8px 0;
  width: 100%;
}
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: all 0.3s ease;
}
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}
.topbar h2 { margin: 0; font-size: 20px; }
.topbar p { margin: 2px 0 0; color: #6b7280; }
.topbar-actions { display: flex; gap: 10px; align-items: center; }
.user-info {
  font-size: 14px; color: #334155; font-weight: 600; text-decoration: none;
  padding: 6px 12px; border-radius: 8px;
}
.user-info:hover { background: #f1f5f9; }
.ghost-btn, .primary-btn {
  text-decoration: none;
  padding: 9px 14px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
}
.ghost-btn {
  background: #f3f4f6; color: #111827;
  border: none;
  cursor: pointer;
}
.ghost-btn:hover { background: #e5e7eb; }
.primary-btn { background: #2563eb; color: white; }
.content { flex: 1; padding: 24px; }
</style>