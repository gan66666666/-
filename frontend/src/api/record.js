import request from './request'

/**
 * 分页查询领养记录
 * @param {{pageNum?: number, pageSize?: number, adopterName?: string, startDate?: string, endDate?: string}} params
 */
export function pageRecords(params) {
  return request.get('/record/page', { params })
}

/**
 * 领养记录详情
 * @param {number|string} id
 */
export function getRecord(id) {
  return request.get(`/record/${id}`)
}
