import userRequest from '../userRequest'

/**
 * 分页查询公告
 * @param {{pageNum?: number, pageSize?: number, keyword?: string}} params
 */
export function pageNotices(params) {
  return userRequest.get('/notice/page', { params })
}

/**
 * 公告详情
 * @param {number|string} id
 */
export function getNotice(id) {
  return userRequest.get(`/notice/${id}`)
}

