# frontend 前端开发约定

Vue 3 + Element Plus + Vite 的管理端。改任何页面前先读这一页。

## 一、布局稳定性（最重要的一条）

后台任何页面点击表格操作列的「详情 / 编辑 / 删除 / 新增」打开弹窗时，
**页面布局必须纹丝不动**：表格、分页、侧栏、内容区宽度、滚动位置都不能变。

已踩过的坑，别再犯：

1. **页面级滚动条**——弹窗开关时浏览器增减垂直滚动条，可用宽度变化 → 整页左右跳。
   → `html, body { overflow: hidden }`，滚动只发生在 `.layout-content` 内部。
2. **ElMain 必须自己撑开**——`.layout-content`（ElMain）必须
   `flex: 1; min-height: 0; overflow-y: auto; scrollbar-gutter: stable`。
   缺 `min-height: 0` 时 flex 子项会被内容撑开，滚动会跑到页面上。
3. **Element Plus 的滚动锁补偿**——弹层打开时会给 body 加
   `padding-right` / `width` 补偿，必须抵消：
   `body.el-popup-parent--hidden, body.el-message-box-parent--hidden { padding-right: 0 !important; width: auto !important; }`
4. **弹层动画自带位移**——`dialog-fade` / `msgbox-fade` 会给**全屏遮罩**加
   `translateY(-20px)`，点删除时整屏会真的往下窜 20px 再弹回来。
   → 在 theme.scss 里换成只做透明度渐变的自定义动画，并给 `.el-dialog` 加 `transform: none`。
5. **打开弹窗不要发请求、不要给表格加 v-loading**——详情弹窗直接用列表行数据回填
   （`detail.value = { ...row }`）；只有保存/删除成功后才 `loadList()` 刷新列表。
6. **el-dialog 保持默认 fixed 定位**，modal 遮罩不参与外部容器宽度计算。
7. **图表画布**用 `ResizeObserver` 跟随容器尺寸（只监听 window resize 不够，
   容器自身变宽变窄时画布会错位）；更新数据设 `animationDurationUpdate: 0`，
   避免切维度时曲线在画布里形变抖动。

### 验证方式（改完必须跑）

用 playwright 逐帧记录坐标，别靠肉眼猜：

- 逐帧记录 `.el-table` / `.el-pagination` / `.layout-aside` / `.layout-content`
  的 `getBoundingClientRect()` 与 `scrollTop`；
- 分别录「打开」和「关闭」各 1.5 秒；
- 判据：每段的**不同布局快照数必须等于 1**，且打开阶段 `/api/` 请求数为 0。

参考脚本：`%TEMP%\pwtest\verify-fix.js`（宠物档案 / 领养申请 / 回访管理 / 公告管理四页全绿）。

## 二、排障思路

1. **先量再改**：用 `requestAnimationFrame` 逐帧量 `getBoundingClientRect()`，
   分清是"真的位移"还是"遮罩闪烁"。Element Plus 的遮罩是半透明层，
   截图对比会被它污染，量坐标比截图靠谱。
2. **注意自动化自身的干扰**：`page.click()` 会先 `scrollIntoView`，
   看起来像页面被重置了滚动位置。用 `element.click()` 在页面内触发才准。
3. 父级滚动容器里的 `position: fixed` 子元素、`scrollbar-gutter`、
   `overflow-anchor` 都会影响测量结果，测量时一并确认。

## 三、代码风格

- Vue 3 `<script setup>`，Element Plus 组件按需全局注册（main.js 里全量注册图标）。
- 注释用中文，只写"为什么这么做"，不写"做了什么"。
- 新页面统一用 `page-container` / `search-card` / `table-card` 这套结构。

