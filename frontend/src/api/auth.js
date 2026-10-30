import request from './request'

/**
 * 登录 / 当前用户 API
 */

// 登录: { username, password } -> { token, tokenType, expiresIn, user }
export function loginApi(data) {
  return request.post('/auth/login', data)
}

// 获取当前登录用户信息(含权限与菜单)
export function getMeApi() {
  return request.get('/auth/me')
}

// 修改本人密码
export function changePasswordApi(data) {
  return request.put('/auth/password', data)
}

// 退出登录
export function logoutApi() {
  return request.post('/auth/logout')
}
