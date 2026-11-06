/**
 * 管理端 token 存取工具
 * 约定：token 与用户信息统一存 localStorage
 * 与用户端（utils/userAuth.js）完全分离，两端可同时登录互不覆盖
 */

const TOKEN_KEY = 'adminToken'
const USER_KEY = 'adminUserInfo'

/**
 * 读取 token
 * @returns {string} 不存在时返回空字符串
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 写入 token
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 清除 token 与缓存的用户信息
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/**
 * 读取缓存的登录用户信息
 * @returns {object|null}
 */
export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

/**
 * 缓存登录用户信息
 * @param {object} user
 */
export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}
