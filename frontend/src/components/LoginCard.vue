<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <span class="login-logo">🐾</span>
        <h1 class="login-title">{{ title }}</h1>
        <p class="login-subtitle">
          <slot name="subtitle">{{ subtitle }}</slot>
        </p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model.trim="form.username"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <el-icon>
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
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
            @click="handleSubmit"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

defineProps({
  /** 页面标题，两端仅「管理端 / 用户端」不同 */
  title: {
    type: String,
    required: true
  },
  /** 副标题 */
  subtitle: {
    type: String,
    default: ''
  },
  /** 登录请求中 */
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit'])

const formRef = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 校验通过后把账号密码交给父组件去请求后端 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  emit('submit', { username: form.username, password: form.password })
}
</script>

<style lang="scss" src="../styles/login.scss"></style>
