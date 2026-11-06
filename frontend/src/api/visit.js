import request from './request'

/**
 * 分页查询回访记录
 * @param {{pageNum?: number, pageSize?: number, status?: number}} params
 */
export function pageVisits(params) {
  return request.get('/visit/page', { params })
}

/**
 * 创建回访计划
 * @param {object} data
 */
export function addVisit(data) {
  return request.post('/visit', data)
}

/**
 * 登记回访结果
 * @param {object} data
 */
export function updateVisit(data) {
  return request.put('/visit', data)
}

/**
 * 删除回访记录
 * @param {number|string} id
 */
export function deleteVisit(id) {
  return request.delete(`/visit/${id}`)
}
