import request from './request'

/**
 * 数据统计 API
 */

// 汇总统计: 图书总数/分类总数/库存总量
export function getSummary() {
  return request.get('/stats/summary')
}

// 按分类统计图书数量(柱状图)
export function getCategoryCount() {
  return request.get('/stats/category-count')
}
