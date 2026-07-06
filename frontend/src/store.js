/**
 * 全局用户状态管理（简易 Store）
 */
import { reactive, computed } from 'vue'

export const store = reactive({
  /** 当前登录用户, null 表示未登录 */
  currentUser: null,

  /** 登录 */
  login(user) {
    this.currentUser = user
  },

  /** 登出 */
  logout() {
    this.currentUser = null
  },

  /** 是否已登录 */
  get isLoggedIn() {
    return this.currentUser !== null
  },

  /** 当前用户 ID */
  get userId() {
    return this.currentUser?.userId || this.currentUser?.adminId || null
  },

  /** 是否为管理员 */
  get isAdmin() {
    return this.currentUser?.role === 'admin'
  },

  /** 当前用户名 */
  get username() {
    return this.currentUser?.username || this.currentUser?.adminName || ''
  }
})