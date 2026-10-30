import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from '../utils/session'

/**
 * axios 统一封装
 * - baseURL: /api (开发环境由 Vite 代理转发到 http://localhost:8080)
 * - 请求拦截器: 自动携带 Authorization: Bearer <token>
 * - 响应拦截器: 统一解析后端 Result { code, message, data }
 *   code === 200 -> 直接返回响应体
 *   401 -> 清理登录态并跳转登录页
 *   403 -> 提示无权限
 *   其它 -> 弹出错误提示并 reject
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let redirecting = false

function handleUnauthorized() {
  clearToken()
  if (redirecting) return
  redirecting = true
  if (window.location.pathname !== '/login') {
    // 使用整页跳转, 保证所有内存状态被重置
    window.location.replace('/login')
  } else {
    redirecting = false
  }
}

// 请求拦截器: 注入登录令牌
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器: 统一处理 Result 结构
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code === 200) {
      // 业务成功, 返回 { code, message, data }
      return res
    }
    if (res && res.code === 401) {
      ElMessage.error(res.message || '登录状态已失效, 请重新登录')
      handleUnauthorized()
      return Promise.reject(new Error(res.message || '未登录'))
    }
    const msg = (res && res.message) || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    const status = error.response && error.response.status
    const data = error.response && error.response.data
    let msg = '网络异常, 请检查后端服务是否启动'

    if (status === 401) {
      ElMessage.error((data && data.message) || '登录状态已失效, 请重新登录')
      handleUnauthorized()
      return Promise.reject(error)
    }
    if (status === 403) {
      msg = (data && data.message) || '无权限执行该操作'
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时, 请稍后重试'
    } else if (status) {
      msg = (data && data.message) || `服务异常 [HTTP ${status}]`
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
