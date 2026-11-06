import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken, removeToken } from '../utils/auth'

/**
 * 管理端 axios 实例：baseURL 走 vite 代理到后端 /api
 * 只携带 adminToken；用户端请求请用 api/userRequest.js
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

/**
 * 请求拦截器：带上登录 token
 */
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 响应拦截器：
 * code = 200 直接返回 data；其余用 ElMessage 提示 msg 并 reject；
 * HTTP 401 清除 token 并跳转登录页。
 * 任何情况都不把后端原始报错堆栈抛给用户。
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code === 200) {
      return res.data
    }
    const msg = (res && res.msg) || '操作失败，请稍后重试'
    // 后端把「未登录或登录已过期」放在响应体 code=401 里（HTTP 状态仍是 200），
    // 这里统一按登录失效处理：清掉 adminToken 并跳回管理端登录页，
    // 否则令牌过期后管理员会停留在当前页面，只看到一条错误提示。
    if (res && res.code === 401) {
      removeToken()
      ElMessage.error(msg)
      if (router.currentRoute.value.path !== '/admin/login') {
        router.replace('/admin/login')
      }
      return Promise.reject(new Error(msg))
    }
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    const status = error.response && error.response.status
    const serverMsg = error.response && error.response.data && error.response.data.msg
    if (status === 401) {
      removeToken()
      ElMessage.error('登录已过期，请重新登录')
      if (router.currentRoute.value.path !== '/admin/login') {
        router.replace('/admin/login')
      }
      return Promise.reject(new Error('登录已过期'))
    }
    const msg = serverMsg || '网络异常，请稍后重试'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
)

export default request
