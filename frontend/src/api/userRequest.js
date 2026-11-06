import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken, removeToken } from '../utils/userAuth'

/**
 * 用户端 axios 实例：baseURL 走 vite 代理到后端 /api
 * 只携带 userToken；管理端请求请用 api/request.js
 */
const userRequest = axios.create({
  baseURL: '/api',
  timeout: 10000
})

/**
 * 请求拦截器：带上用户端登录 token
 */
userRequest.interceptors.request.use(
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
 * code = 401（未登录或令牌过期）清除 userToken 并跳转用户端登录页；
 * HTTP 401 同样清除并跳转。
 */
userRequest.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code === 200) {
      return res.data
    }
    const msg = (res && res.msg) || '操作失败，请稍后重试'
    // 后端把「未登录或登录已过期」放在响应体 code=401 里（HTTP 状态仍是 200），
    // 这里统一按登录失效处理，避免用户带着过期令牌继续操作
    if (res && res.code === 401) {
      ElMessage.warning(msg)
      handleUnauthorized()
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
      if (router.currentRoute.value.path !== '/user/login') {
        router.replace('/user/login')
      }
      return Promise.reject(new Error('登录已过期'))
    }
    const msg = serverMsg || '网络异常，请稍后重试'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
)

/**
 * 登录失效：清掉本地 token 并回到用户端登录页
 * 已经在登录页时不再跳转，避免重复跳转
 */
function handleUnauthorized() {
  removeToken()
  if (router.currentRoute.value.path !== '/user/login') {
    router.replace('/user/login')
  }
}

export default userRequest
