import { reactive } from 'vue'
import { loginApi, getMeApi, logoutApi } from '../api/auth'
import { getToken, setToken, clearToken } from '../utils/session'

/**
 * 登录状态仓库(轻量响应式 store, 无需额外引入 Pinia)
 *
 * 登录成功后由后端下发: 用户信息 / 权限编码 / 可访问菜单,
 * 前端路由守卫、菜单渲染、按钮级指令均以此为准。
 */
export const authState = reactive({
  token: getToken(),
  user: null,
  permissions: [],
  menus: [],
  // 是否已从后端加载过用户信息(刷新页面后需要重新拉取)
  loaded: false
})

function applyUser(user) {
  authState.user = user || null
  authState.permissions = (user && user.permissions) || []
  authState.menus = (user && user.menus) || []
  authState.loaded = true
}

/**
 * 是否拥有指定权限
 */
export function hasPermission(permission) {
  return authState.permissions.includes(permission)
}

/**
 * 当前用户可访问的第一个菜单(登录后 / 无权访问时的兜底跳转)
 */
export function firstAccessiblePath() {
  return authState.menus.length ? authState.menus[0].path : '/profile'
}

/**
 * 登录
 */
export async function login(username, password) {
  const res = await loginApi({ username, password })
  const data = res.data || {}
  authState.token = data.token
  setToken(data.token)
  applyUser(data.user)
  return data
}

/**
 * 拉取当前用户信息(刷新页面 / 令牌仍然有效时恢复登录态)
 */
export async function loadProfile() {
  const res = await getMeApi()
  applyUser(res.data)
  return res.data
}

/**
 * 退出登录
 */
export async function logout() {
  try {
    await logoutApi()
  } catch (e) {
    /* 令牌失效时忽略, 直接清理本地状态 */
  }
  clearSession()
}

/**
 * 清理本地登录态
 */
export function clearSession() {
  clearToken()
  authState.token = ''
  authState.user = null
  authState.permissions = []
  authState.menus = []
  authState.loaded = false
}
