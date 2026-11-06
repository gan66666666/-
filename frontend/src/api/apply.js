import request from './request'

/**
 * 分页查询领养申请
 * @param {{pageNum?: number, pageSize?: number, status?: number, keyword?: string}} params
 */
export function pageApplies(params) {
  return request.get('/apply/page', { params })
}

/**
 * 申请详情
 * @param {number|string} id
 */
export function getApply(id) {
  return request.get(`/apply/${id}`)
}

/**
 * 提交领养申请（用户端，免登录）
 * @param {object} data
 */
export function addApply(data) {
  return request.post('/apply', data)
}

/**
 * 审核领养申请
 * @param {{id: number, status: number, rejectReason?: string}} data
 */
export function auditApply(data) {
  return request.put('/apply/audit', data)
}
