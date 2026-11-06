<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside class="layout-aside" width="220px">
      <div class="aside-brand">
        <span class="brand-logo">🐾</span>
        <span class="brand-name">宠物领养管理系统</span>
      </div>

      <el-menu
        class="aside-menu"
        :default-active="activeMenu"
        router
        :collapse="false"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="layout-main">
      <el-header class="layout-header" height="60px">
        <div class="header-left">
          <span class="header-title">{{ pageTitle }}</span>
          <span class="header-welcome">{{ welcomeText }}</span>
        </div>

        <div class="header-right">
          <el-avatar :size="32" class="header-avatar">
            {{ avatarText }}
          </el-avatar>
          <span class="header-nickname">{{ nickname }}</span>
          <el-button type="danger" plain size="small" @click="handleLogout">
            <el-icon>
              <SwitchButton />
            </el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-header>

      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getUser, removeToken } from '../utils/auth'

const route = useRoute()
const router = useRouter()

/** 侧边栏菜单：与 router/index.js 中的路由一一对应 */
const menus = [
  { path: '/admin/dashboard', title: '工作台', icon: 'Odometer' },
  { path: '/admin/pet', title: '宠物档案', icon: 'Shop' },
  { path: '/admin/apply', title: '领养申请', icon: 'Document' },
  { path: '/admin/record', title: '领养记录', icon: 'Finished' },
  { path: '/admin/visit', title: '回访管理', icon: 'ChatDotRound' },
  { path: '/admin/notice', title: '公告管理', icon: 'Bell' }
]

/** 当前高亮的菜单项 */
const activeMenu = computed(() => route.path)

/** 当前页面标题 */
const pageTitle = computed(() => route.meta.title || '宠物领养管理系统')

/** 登录用户信息 */
const user = getUser() || {}

const nickname = computed(() => user.nickname || user.username || '管理员')

const avatarText = computed(() => (nickname.value || 'A').charAt(0).toUpperCase())

const welcomeText = computed(() => `你好，${nickname.value}，欢迎回来`)

/** 退出登录：确认后清除 token 并跳转登录页 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    removeToken()
    // 等确认框及其遮罩彻底关闭后再跳转，否则遮罩淡出的过程中登录页会“闪一下”
    await waitMessageBoxClosed()
    router.replace('/admin/login')
  } catch (e) {
    // 用户取消，无需处理
  }
}

/**
 * 等待 Element Plus 确认框的遮罩从 DOM 中移除
 * 遮罩是全屏半透明层，若在它淡出时切换路由，新页面会被罩住并出现闪动
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
    // 兜底：遮罩一直未移除时也不要卡住跳转
    setTimeout(() => {
      observer.disconnect()
      resolve()
    }, 800)
  })
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  background-color: #faf9f6;
}

.layout-aside {
  width: 220px;
  background-color: #fbfaf7;
  border-right: 1px solid #f0efe9;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.aside-brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  font-size: 16px;
  font-weight: 700;
  color: #3a3a36;
  border-bottom: 1px solid #f0efe9;
}

.brand-logo {
  font-size: 20px;
  line-height: 1;
}

.brand-name {
  white-space: nowrap;
}

.aside-menu {
  padding: 12px 10px;
}

.aside-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  margin-bottom: 4px;
  color: #5c5c56;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background-color: #eef5eb;
}

.layout-main {
  min-width: 0;
}

.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #f0efe9;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #3a3a36;
}

.header-welcome {
  font-size: 13px;
  color: #8a8a83;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-avatar {
  background-color: #87c47c;
  color: #ffffff;
  font-weight: 600;
}

.header-nickname {
  font-size: 14px;
  color: #3a3a36;
}

.layout-content {
  /* 主内容区占满剩余高度，滚动只在这里发生：
     flex:1 + min-height:0 保证 el-main 不会被子元素撑开，
     页面级滚动条永远不出现，弹窗开关时布局宽度不会变化。 */
  flex: 1;
  min-height: 0;
  padding: 16px;
  overflow-y: auto;
  /* 始终为垂直滚动条预留位置，内容变长变短都不会改变可用宽度 */
  scrollbar-gutter: stable;
  background-color: #faf9f6;
}
</style>
