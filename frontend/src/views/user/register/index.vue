<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <span class="login-logo">🐾</span>
        <h1 class="login-title">宠物领养管理系统 · 用户端</h1>
        <p class="login-subtitle">注册账号，即可提交领养申请</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名（3-20 位）" clearable>
            <template #prefix>
              <el-icon>
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model.trim="form.nickname"
            placeholder="请输入昵称（可不填，默认与用户名相同）"
            maxlength="20"
            clearable
          >
            <template #prefix>
              <el-icon>
                <Avatar />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" maxlength="11" clearable>
            <template #prefix>
              <el-icon>
                <Iphone />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（不少于 6 位）" show-password>
            <template #prefix>
              <el-icon>
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          >
            <template #prefix>
              <el-icon>
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span class="login-hint">已有账号？</span>
        <el-link type="primary" @click="goLogin">去登录</el-link>
        <el-divider direction="vertical" />
        <el-link @click="goAdmin">管理端入口</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../../api/user/auth'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        if (value.length < 3 || value.length > 20) {
          callback(new Error('用户名长度为 3-20 位'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 提交注册：用户名重复等错误由后端返回，交给 axios 拦截器统一提示
 */
async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  if (loading.value) {
    return
  }

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/user/login')
  } catch (e) {
    // 「用户名已存在」等提示由 axios 响应拦截器统一弹出
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/user/login')
}

function goAdmin() {
  router.push('/admin/login')
}
</script>

<style lang="scss" src="../../../styles/login.scss"></style>

