import request from './request'

/**
 * 用户管理 API(管理员)
 */

// 分页查询: { current, size, keyword }
export function pageUsers(params) {
  return request.get('/user/page', { params })
}

// 新增用户
export function addUser(data) {
  return request.post('/user', data)
}

// 编辑用户(姓名 / 角色 / 状态, 可选重置密码)
export function updateUser(data) {
  return request.put('/user', data)
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

// 重置他人密码
export function resetUserPassword(id, newPassword) {
  return request.put(`/user/${id}/password`, { newPassword })
}
