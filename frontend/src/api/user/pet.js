import userRequest from '../userRequest'

/**
 * 分页查询宠物档案
 * @param {{pageNum?: number, pageSize?: number, type?: string, status?: number, keyword?: string}} params
 */
export function pagePets(params) {
  return userRequest.get('/pet/page', { params })
}

/**
 * 宠物详情
 * @param {number|string} id
 */
export function getPet(id) {
  return userRequest.get(`/pet/${id}`)
}

