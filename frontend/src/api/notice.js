import request from './request'

/**
 * 分页查询公告
 * @param {{pageNum?: number, pageSize?: number, keyword?: string}} params
 */
export function pageNotices(params) {
  return request.get('/notice/page', { params })
}

/**
 * 公告详情
 * @param {number|string} id
 */
export function getNotice(id) {
  return request.get(`/notice/${id}`)
}

/**
 * 新增公告
 * @param {object} data
 */
export function addNotice(data) {
  return request.post('/notice', data)
}

/**
 * 修改公告
 * @param {object} data
 */
export function updateNotice(data) {
  return request.put('/notice', data)
}

/**
 * 删除公告
 * @param {number|string} id
 */
export function deleteNotice(id) {
  return request.delete(`/notice/${id}`)
}

/**
 * 切换公告置顶状态
 * @param {number|string} id
 */
export function toggleNoticeTop(id) {
  return request.put(`/notice/top/${id}`)
}
