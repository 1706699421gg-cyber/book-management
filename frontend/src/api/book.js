import request from './request'

/**
 * 图书管理 API
 */

// 分页查询 + 关键字搜索(书名/作者模糊)
export function pageBooks(params) {
  // params: { current, size, keyword }
  return request.get('/book/page', { params })
}

// 新增图书
export function addBook(data) {
  return request.post('/book', data)
}

// 编辑图书
export function updateBook(data) {
  return request.put('/book', data)
}

// 删除图书
export function deleteBook(id) {
  return request.delete(`/book/${id}`)
}
