import request from './request'

/**
 * 图书分类 API
 */

// 分类列表(图书表单下拉框同用)
export function listCategories() {
  return request.get('/category/list')
}

// 新增分类
export function addCategory(data) {
  return request.post('/category', data)
}

// 编辑分类
export function updateCategory(data) {
  return request.put('/category', data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
