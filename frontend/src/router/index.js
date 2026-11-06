import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/index.vue'
import UserLayout from '../layout/user/index.vue'
import { getToken as getAdminToken } from '../utils/auth'
import { getToken as getUserToken } from '../utils/userAuth'

/**
 * 路由表分两端：
 * - 管理端统一 /admin/**，放在后台布局下，除 /admin/login 外均需 adminToken
 * - 用户端统一 /user/**，放在用户端布局下，部分页面需 userToken
 */

/** 管理端登录页（独立整页，不套后台布局） */
const adminLoginRoute = {
  path: '/admin/login',
  name: 'AdminLogin',
  component: () => import('../views/login/index.vue'),
  meta: { title: '管理端登录' }
}

/** 用户端登录 / 注册（独立整页，不套用户端布局） */
const userAuthRoutes = [
  {
    path: '/user/login',
    name: 'UserLogin',
    component: () => import('../views/user/login/index.vue'),
    meta: { title: '用户登录' }
  },
  {
    path: '/user/register',
    name: 'UserRegister',
    component: () => import('../views/user/register/index.vue'),
    meta: { title: '用户注册' }
  }
]

export const routes = [
  {
    path: '/',
    redirect: '/user/home'
  },
  adminLoginRoute,
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'pet',
        name: 'Pet',
        component: () => import('../views/pet/index.vue'),
        meta: { title: '宠物档案', icon: 'Shop' }
      },
      {
        path: 'apply',
        name: 'Apply',
        component: () => import('../views/apply/index.vue'),
        meta: { title: '领养申请', icon: 'Document' }
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('../views/record/index.vue'),
        meta: { title: '领养记录', icon: 'Finished' }
      },
      {
        path: 'visit',
        name: 'Visit',
        component: () => import('../views/visit/index.vue'),
        meta: { title: '回访管理', icon: 'ChatDotRound' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('../views/notice/index.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      }
    ]
  },
  ...userAuthRoutes,
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/home',
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('../views/user/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'pets',
        name: 'UserPets',
        component: () => import('../views/user/pets/index.vue'),
        meta: { title: '宠物浏览' }
      },
      {
        path: 'pets/:id',
        name: 'UserPetDetail',
        component: () => import('../views/user/petDetail/index.vue'),
        meta: { title: '宠物详情' }
      },
      {
        path: 'apply',
        name: 'UserApply',
        component: () => import('../views/user/apply/index.vue'),
        meta: { title: '领养申请提交' }
      },
      {
        path: 'myapply',
        name: 'UserMyApply',
        component: () => import('../views/user/myapply/index.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: 'notices',
        name: 'UserNotices',
        component: () => import('../views/user/notices/index.vue'),
        meta: { title: '公告列表' }
      },
      {
        path: 'notices/:id',
        name: 'UserNoticeDetail',
        component: () => import('../views/user/noticeDetail/index.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/user/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/user/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 用户端允许游客访问的页面 */
const GUEST_PATHS = [
  '/user/home',
  '/user/pets',
  '/user/notices',
  '/user/login',
  '/user/register'
]

/** 用户端需要登录的页面 */
const USER_AUTH_PATHS = ['/user/apply', '/user/myapply', '/user/profile']

/**
 * 判断路径是否属于某个前缀（宠物详情 /user/pets/:id 也算 /user/pets）
 */
function matchPath(path, list) {
  return list.some((p) => path === p || path.startsWith(`${p}/`))
}

/**
 * 全局前置守卫：
 * - /admin/**（除 /admin/login）：必须已有 adminToken
 * - /user/apply、/user/myapply、/user/profile：必须已有 userToken
 * - 其余 /user/** 页面允许游客访问
 */
router.beforeEach((to) => {
  if (to.path === '/admin/login') {
    return true
  }
  if (to.path.startsWith('/admin')) {
    if (!getAdminToken()) {
      return { path: '/admin/login', query: { redirect: to.fullPath } }
    }
    return true
  }
  if (to.path.startsWith('/user')) {
    if (matchPath(to.path, GUEST_PATHS)) {
      return true
    }
    if (matchPath(to.path, USER_AUTH_PATHS) && !getUserToken()) {
      return { path: '/user/login', query: { redirect: to.fullPath } }
    }
    return true
  }
  return true
})

export default router
