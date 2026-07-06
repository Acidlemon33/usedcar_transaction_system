import { createRouter, createWebHashHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import InventoryView from '../views/InventoryView.vue'
import OrdersView from '../views/OrdersView.vue' // 我发布的
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import CarDetailView from '../views/CarDetailView.vue'
import SaleDetailView from '../views/SaleDetailView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import ProfileView from '../views/ProfileView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import MyOrdersView from '../views/MyOrdersView.vue'
import AdminStatsView from '../views/AdminStatsView.vue'
import NoticeView from '../views/NoticeView.vue'

const routes = [
  { path: '/', name: 'dashboard', component: DashboardView },
  { path: '/inventory', name: 'inventory', component: InventoryView },
  { path: '/sales', redirect: '/inventory' },
  { path: '/orders', name: 'orders', component: OrdersView },
  { path: '/myorders', name: 'myorders', component: MyOrdersView },
  { path: '/sale/:id', name: 'sale-detail', component: SaleDetailView },
  { path: '/favorites', name: 'favorites', component: FavoritesView },
  { path: '/profile', name: 'profile', component: ProfileView },
  { path: '/account/login', name: 'login', component: LoginView },
  { path: '/account/register', name: 'register', component: RegisterView },
  { path: '/car/:id', name: 'car-detail', component: CarDetailView },
  { path: '/order/:id', name: 'order-detail', component: OrderDetailView },
  // 管理员页面
  { path: '/admin/stats', name: 'admin-stats', component: AdminStatsView },
  { path: '/admin/notices', name: 'admin-notices', component: NoticeView },
  // 兼容旧链接
  { path: '/account', redirect: '/account/login' },
  { path: '/publish', redirect: '/inventory' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router