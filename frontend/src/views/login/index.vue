<template>
  <LoginCard
    title="宠物领养管理系统 · 管理端"
    :loading="loading"
    @submit="handleLogin"
  >
    <template #subtitle>
      愿每一只小动物都能找到<span class="warm">温暖的家</span>
    </template>

    <template #footer>
      <span class="login-hint">默认账号：admin / 123456</span>
      <el-divider direction="vertical" />
      <el-link type="primary" @click="goUserLogin">用户端入口</el-link>
    </template>
  </LoginCard>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { setToken, setUser } from '../../utils/auth'
import LoginCard from '../../components/LoginCard.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)

/**
 * 管理端登录：成功后存 adminToken 与管理员信息，再跳转到工作台
 */
async function handleLogin({ username, password }) {
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    const data = await login({ username, password })
    setToken(data.token)
    setUser({
      id: data.id,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar
    })
    ElMessage.success('登录成功，欢迎回来')
    const redirect = route.query.redirect || '/admin/dashboard'
    router.replace(redirect)
  } catch (e) {
    // 错误提示已在 axios 响应拦截器中统一处理
  } finally {
    loading.value = false
  }
}

/** 跳转到用户端登录页 */
function goUserLogin() {
  router.push('/user/login')
}
</script>
