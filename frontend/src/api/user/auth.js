import userRequest from '../userRequest'

/**
 * 普通用户注册
 * @param {{username: string, password: string, nickname?: string, phone?: string}} data
 */
export function register(data) {
  return userRequest.post('/user/register', data)
}

/**
 * 普通用户登录，成功返回 token 与用户信息
 * @param {{username: string, password: string}} data
 */
export function login(data) {
  return userRequest.post('/user/login', data)
}

/**
 * 获取当前登录用户信息（不含密码）
 */
export function getUserInfo() {
  return userRequest.get('/user/info')
}

/**
 * 修改密码
 * @param {{oldPassword: string, newPassword: string}} data
 */
export function updatePassword(data) {
  return userRequest.put('/user/updatePassword', data)
}

/**
 * 修改个人资料
 * @param {{nickname?: string, phone?: string, address?: string}} data
 */
export function updateProfile(data) {
  return userRequest.put('/user/profile', data)
}

/**
 * 我的领养申请
 * @param {{pageNum?: number, pageSize?: number}} params
 */
export function getMyApply(params) {
  return userRequest.get('/user/myApply', { params })
}

