import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '../layout/MainLayout.vue'
import {
  authState,
  loadProfile,
  hasPermission,
  firstAccessiblePath,
  clearSession
} from '../store/auth'

const routes = [
  // 独立登录页(不套用后台布局)
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据统计', permission: 'stats:view' }
      },
      {
        path: 'books',
        name: 'BookManage',
        component: () => import('../views/BookManage.vue'),
        meta: { title: '图书管理', permission: 'book:view' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('../views/CategoryManage.vue'),
        meta: { title: '分类管理', permission: 'category:view' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue'),
        meta: { title: '用户管理', permission: 'user:manage' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', permission: 'profile:view' }
      }
    ]
  },
  // 兜底: 未匹配路径重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守卫:
 * 1. 未登录 -> 登录页(带上原目标地址)
 * 2. 已登录但未加载用户信息 -> 调 /api/auth/me 校验令牌并恢复权限
 * 3. 页面 meta.permission 不满足 -> 拦截并跳转到有权限的第一个页面
 */
router.beforeEach(async (to) => {
  if (to.meta.public) {
    // 已登录用户访问登录页时直接进入系统
    if (!authState.token) return true
    if (!authState.loaded) {
      try {
        await loadProfile()
      } catch (e) {
        clearSession()
        return true
      }
    }
    return { path: firstAccessiblePath() }
  }

  if (!authState.token) {
    const target = to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : undefined
    return { path: '/login', query: target }
  }

  if (!authState.loaded) {
    try {
      await loadProfile()
    } catch (e) {
      clearSession()
      return { path: '/login' }
    }
  }

  if (to.meta.permission && !hasPermission(to.meta.permission)) {
    ElMessage.warning('没有访问该页面的权限')
    return { path: firstAccessiblePath() }
  }

  return true
})

export default router
