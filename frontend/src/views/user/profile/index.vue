<template>
  <div class="user-profile">
    <h1 class="page-title">个人中心</h1>
    <p class="page-desc">管理你的账号资料与登录密码。</p>

    <div v-loading="loading" class="profile-body">
      <!-- 账号概览 -->
      <el-card class="info-card" shadow="never">
        <div class="profile-head">
          <el-avatar :size="56" class="profile-avatar">{{ avatarText }}</el-avatar>
          <div class="profile-meta">
            <h2 class="profile-name">{{ info.nickname || info.username || '用户' }}</h2>
            <p class="profile-sub">@{{ info.username || '-' }}</p>
          </div>
        </div>

        <el-descriptions :column="2" border class="profile-desc">
          <el-descriptions-item label="用户名">{{ info.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ info.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ info.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ info.address || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ formatDateTime(info.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="profile-actions">
          <el-button @click="go('/user/myapply')">我的申请</el-button>
          <el-button @click="go('/user/pets')">申请领养</el-button>
        </div>
      </el-card>

      <!-- 修改资料 -->
      <el-card class="form-card" shadow="never">
        <template #header>
          <span class="card-title">修改资料</span>
        </template>

        <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-position="top">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model.trim="profileForm.nickname" placeholder="请输入昵称" maxlength="20" clearable />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="profileForm.phone" placeholder="请输入手机号" maxlength="11" clearable />
          </el-form-item>

          <el-form-item label="地址" prop="address">
            <el-input
              v-model.trim="profileForm.address"
              type="textarea"
              :rows="2"
              maxlength="255"
              placeholder="请输入联系地址"
            />
          </el-form-item>

          <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">
            保存资料
          </el-button>
        </el-form>
      </el-card>

      <!-- 修改密码 -->
      <el-card class="form-card" shadow="never">
        <template #header>
          <span class="card-title">修改密码</span>
        </template>

        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="pwdForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="pwdForm.newPassword"
              type="password"
              placeholder="请输入新密码（不少于 6 位）"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="pwdForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>

          <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">
            修改密码
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updatePassword, updateProfile } from '../../../api/user/auth'
import { getUser, setUser } from '../../../utils/userAuth'
import { formatDateTime } from '../../../utils/petView'

const router = useRouter()

const info = ref({})
const loading = ref(false)

const profileRef = ref(null)
const profileSaving = ref(false)
const profileForm = reactive({
  nickname: '',
  phone: '',
  address: ''
})

const profileRules = {
  nickname: [{ max: 20, message: '昵称最多 20 个字', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdRef = ref(null)
const pwdSaving = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const avatarText = computed(() => {
  const name = info.value.nickname || info.value.username || 'U'
  return name.charAt(0).toUpperCase()
})

/**
 * 加载当前用户资料并回填表单
 */
async function loadInfo() {
  loading.value = true
  try {
    const data = await getUserInfo()
    info.value = data || {}
    profileForm.nickname = info.value.nickname || ''
    profileForm.phone = info.value.phone || ''
    profileForm.address = info.value.address || ''
  } catch (e) {
    // 拉取失败时保留空表单，不阻断页面
  } finally {
    loading.value = false
  }
}

/**
 * 保存资料：成功后同步更新本地缓存，顶部昵称立即生效
 */
async function handleSaveProfile() {
  const valid = await profileRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  profileSaving.value = true
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      address: profileForm.address
    })
    ElMessage.success('资料已保存')
    const cached = getUser() || {}
    setUser({ ...cached, nickname: profileForm.nickname, phone: profileForm.phone })
    await loadInfo()
  } catch (e) {
    // 错误提示由 axios 响应拦截器统一处理
  } finally {
    profileSaving.value = false
  }
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  pwdSaving.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdRef.value?.clearValidate()
  } catch (e) {
    // 错误提示由 axios 响应拦截器统一处理
  } finally {
    pwdSaving.value = false
  }
}

function go(path) {
  router.push(path)
}

onMounted(loadInfo)
</script>

<style scoped lang="scss">
.user-profile {
  max-width: 760px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 6px;
  font-size: 22px;
  color: var(--user-title-color);
}

.page-desc {
  margin: 0 0 20px;
  font-size: 13px;
  color: var(--user-text-muted);
}

.profile-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.info-card,
.form-card {
  border-radius: var(--user-card-radius);
  border: 1px solid var(--user-card-border);
  background-color: var(--user-card-bg);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--user-title-color);
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.profile-avatar {
  background-color: var(--user-primary);
  color: #ffffff;
  font-weight: 600;
}

.profile-name {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--user-title-color);
}

.profile-sub {
  margin: 0;
  font-size: 13px;
  color: var(--user-text-muted);
}

.profile-desc {
  border-radius: 10px;
}

.profile-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
}
</style>

