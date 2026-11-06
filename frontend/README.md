# frontend（宠物领养管理系统前端）

Vue 3 + Element Plus + Vite 管理端与用户端。接口由 Vite 代理到后端 `http://localhost:8080`。

## 启动

```bash
npm install   # 首次克隆后执行，node_modules 已在仓库内则可跳过
npm run dev   # http://localhost:5173
```

## 路由

管理端统一 `/admin/**`，登录后使用 `adminToken`：

| 路径 | 说明 |
| --- | --- |
| `/admin/login` | 管理端登录 |
| `/admin/dashboard` | 工作台 |
| `/admin/pet` | 宠物档案 |
| `/admin/apply` | 领养申请 |
| `/admin/record` | 领养记录 |
| `/admin/visit` | 回访管理 |
| `/admin/notice` | 公告管理 |
| `/admin/password` | 修改密码 |

用户端统一 `/user/**`，登录后使用 `userToken`：

| 路径 | 说明 | 游客可访问 |
| --- | --- | --- |
| `/user/login` | 用户登录 | 是 |
| `/user/register` | 用户注册 | 是 |
| `/user/home` | 首页 | 是 |
| `/user/pets` | 宠物浏览 | 是 |
| `/user/pets/:id` | 宠物详情 | 是 |
| `/user/apply` | 领养申请提交 | 否 |
| `/user/myapply` | 我的申请 | 否 |
| `/user/notices` | 公告列表 | 是 |
| `/user/notices/:id` | 公告详情 | 是 |
| `/user/profile` | 个人中心 | 否 |

根路径 `/` 重定向到 `/user/home`。

## 登录状态

两端 token 完全分离，可同时登录互不覆盖：

- 管理端：`adminToken` / `adminUserInfo`，工具 `src/utils/auth.js`，请求实例 `src/api/request.js`
- 用户端：`userToken` / `userInfo`，工具 `src/utils/userAuth.js`，请求实例 `src/api/userRequest.js`

401 响应分别清理各自 token 并跳回 `/admin/login`、`/user/login`。

## 目录

```
src
├── api            # 接口封装（request 管理端 / userRequest 用户端）
├── layout
│   ├── index.vue  # 管理端侧边栏布局
│   └── user       # 用户端顶部导航布局
├── router         # 路由表与守卫
├── utils          # token 存取（auth / userAuth）
└── views
    ├── ...        # 管理端业务页面
    └── user       # 用户端页面
```
