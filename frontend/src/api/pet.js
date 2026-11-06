import request from './request'

/**
 * 分页查询宠物档案
 * @param {{pageNum?: number, pageSize?: number, type?: string, status?: number, keyword?: string}} params
 */
export function pagePets(params) {
  return request.get('/pet/page', { params })
}

/**
 * 宠物详情
 * @param {number|string} id
 */
export function getPet(id) {
  return request.get(`/pet/${id}`)
}

/**
 * 新增宠物
 * @param {object} data
 */
export function addPet(data) {
  return request.post('/pet', data)
}

/**
 * 修改宠物
 * @param {object} data
 */
export function updatePet(data) {
  return request.put('/pet', data)
}

/**
 * 修改宠物领养状态
 * @param {number|string} id
 * @param {number} status 1可领养 2已领养 3救助中
 */
export function updatePetStatus(id, status) {
  return request.put(`/pet/status/${id}/${status}`)
}

/**
 * 删除宠物
 * @param {number|string} id
 */
export function deletePet(id) {
  return request.delete(`/pet/${id}`)
}
