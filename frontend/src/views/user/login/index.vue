<template>
  <LoginCard
    title="宠物领养管理系统 · 用户端"
    subtitle="登录后可提交领养申请、查看我的申请"
    :loading="loading"
    @submit="handleLogin"
  >
    <template #footer>
      <span class="login-hint">还没有账号？</span>
      <el-link type="primary" @click="goRegister">去注册</el-link>
      <el-divider direction="vertical" />
      <el-link @click="goAdmin">管理端入口</el-link>
    </template>
  </LoginCard>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { setToken, setUser } from '../../../utils/userAuth'
import { login } from '../../../api/user/auth'
import LoginCard from '../../../components/LoginCard.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)

/**
 * 用户端登录：调后端真实登录接口，
 * 成功后把令牌与用户信息分别写入 userToken / userInfo（与管理端完全分开存储）
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
      role: data.role
    })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/user/home')
  } catch (e) {
    // 失败提示（用户名或密码错误）由 axios 响应拦截器统一弹出
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/user/register')
}

function goAdmin() {
  router.push('/admin/login')
}
</script>

