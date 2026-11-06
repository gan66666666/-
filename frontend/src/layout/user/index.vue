<template>
  <el-container class="user-layout">
    <el-header class="user-header" height="60px">
      <div class="header-left">
        <router-link to="/user/home" class="user-brand">
          <span class="brand-logo">🐾</span>
          <span class="brand-name">宠物领养管理系统</span>
        </router-link>

        <!--
          ellipsis 关闭横向菜单的溢出折叠：
          Element Plus 会在菜单右侧预留 64px 给「⋯」触发器，而本菜单宽度由内容自适应，
          预留宽度会把最后一项（我的申请）挤进折叠区，导航里就看不到入口了。
        -->
        <el-menu
          class="user-menu"
          mode="horizontal"
          :ellipsis="false"
          :default-active="activeMenu"
          router
        >
          <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
            {{ item.title }}
          </el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <template v-if="user">
          <el-avatar :size="32" class="user-avatar">{{ avatarText }}</el-avatar>
          <el-link type="primary" :underline="false" class="nickname-link" @click="goProfile">
            {{ nickname }}
          </el-link>
          <el-button type="danger" plain size="small" @click="handleLogout">
            <el-icon>
              <SwitchButton />
            </el-icon>
            <span>退出</span>
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" plain size="small" @click="goLogin">登录</el-button>
          <el-button type="primary" size="small" @click="goRegister">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="user-content">
      <router-view />
    </el-main>

    <el-footer class="user-footer" height="48px">
      <span>宠物领养管理系统 · 用户端</span>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getUser, removeToken } from '../../utils/userAuth'

const route = useRoute()
const router = useRouter()

/** 顶部导航：与 router/index.js 中的 /user/** 路由一一对应 */
const menus = [
  { path: '/user/home', title: '首页' },
  { path: '/user/pets', title: '宠物领养' },
  { path: '/user/notices', title: '公告' },
  { path: '/user/myapply', title: '我的申请' }
]

/** 当前高亮的导航项 */
const activeMenu = computed(() => {
  const hit = menus.find(
    (item) => route.path === item.path || route.path.startsWith(`${item.path}/`)
  )
  return hit ? hit.path : '/user/home'
})

/** 登录用户信息：在个人中心改过昵称后回到顶部要能同步显示 */
const user = ref(getUser() || {})

const nickname = computed(() => user.value.nickname || user.value.username || '用户')

const avatarText = computed(() => (nickname.value || 'U').charAt(0).toUpperCase())

// 路由切换时重新读一次本地缓存，保证登录 / 改昵称后顶部立即更新
watch(
  () => route.fullPath,
  () => {
    user.value = getUser() || {}
  }
)

function goLogin() {
  router.push('/user/login')
}

function goRegister() {
  router.push('/user/register')
}

/** 点击昵称进入个人中心 */
function goProfile() {
  router.push('/user/profile')
}

/** 退出登录：清除 userToken 并回到首页 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    removeToken()
    await waitMessageBoxClosed()
    user.value = {}
    router.replace('/user/home')
  } catch (e) {
    // 用户取消，无需处理
  }
}

/**
 * 等待 Element Plus 确认框的遮罩从 DOM 中移除，
 * 否则遮罩淡出过程中切换路由会闪动
 */
function waitMessageBoxClosed() {
  return new Promise((resolve) => {
    if (!document.querySelector('.el-overlay')) {
      resolve()
      return
    }
    const observer = new MutationObserver(() => {
      if (!document.querySelector('.el-overlay')) {
        observer.disconnect()
        resolve()
      }
    })
    observer.observe(document.body, { childList: true, subtree: true })
    setTimeout(() => {
      observer.disconnect()
      resolve()
    }, 800)
  })
}
</script>

<style scoped lang="scss">
.user-layout {
  height: 100vh;
  background-color: #faf9f6;
}

.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #f0efe9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 28px;
  min-width: 0;
}

.user-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 700;
  color: #3a3a36;
  white-space: nowrap;
}

.brand-logo {
  font-size: 20px;
  line-height: 1;
}

.user-menu {
  border-bottom: none;
  /* 菜单项不参与收缩，保证四项始终完整显示 */
  flex-shrink: 0;
}

.user-menu :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  color: #5c5c56;
}

.user-menu :deep(.el-menu-item.is-active) {
  color: #6aab60;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background-color: #87c47c;
  color: #ffffff;
  font-weight: 600;
}

.user-nickname {
  font-size: 14px;
  color: #3a3a36;
}

.nickname-link {
  font-size: 14px;
  font-weight: 500;
}

.user-content {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  scrollbar-gutter: stable;
  background-color: #faf9f6;
}

.user-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #a5a59d;
  background-color: #ffffff;
  border-top: 1px solid #f0efe9;
}
</style>
