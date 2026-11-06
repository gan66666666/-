<template>
  <div class="dashboard">
    <!-- 一、数据概览卡片 -->
    <div class="stat-grid" v-loading="overviewLoading" element-loading-text="数据加载中...">
      <div class="stat-card" v-for="item in statCards" :key="item.key">
        <div class="stat-info">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value" :class="item.theme">{{ item.value }}</div>
        </div>
        <div class="stat-icon" :class="item.theme">{{ item.icon }}</div>
      </div>
    </div>

    <!-- 二、图表区：折线图占 2 份，环形图占 1 份 -->
    <div class="chart-row">
      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <span class="panel-title">领养趋势</span>
            <el-radio-group v-model="trendType" size="small" @change="handleTrendChange">
              <el-radio-button value="week">近7天</el-radio-button>
              <el-radio-button value="month">近30天</el-radio-button>
              <el-radio-button value="year">近12个月</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="trendChartRef" class="chart-box" v-loading="trendLoading"></div>
      </el-card>

      <el-card class="panel" shadow="never">
        <template #header>
          <span class="panel-title">宠物类型分布</span>
        </template>
        <div ref="pieChartRef" class="chart-box" v-loading="pieLoading"></div>
      </el-card>
    </div>

    <!-- 三、底部三栏 -->
    <div class="bottom-row">
      <el-card class="panel" shadow="never">
        <template #header>
          <span class="panel-title">最新申请</span>
        </template>
        <div class="list-box" v-loading="applyLoading">
          <ul class="list" v-if="applies.length">
            <li class="list-item" v-for="item in applies" :key="item.id">
              <div class="item-info">
                <span class="item-name">{{ item.applicantName }}</span>
                <span class="item-desc">申请领养 {{ item.petName }}</span>
              </div>
              <el-tag :type="applyTagType(item.status)" size="small" effect="light">
                {{ applyStatusText(item.status) }}
              </el-tag>
            </li>
          </ul>
          <el-empty v-else description="暂无申请记录" :image-size="60" />
        </div>
        <div class="panel-footer">
        <el-button link type="primary" @click="go('/admin/apply')">
            查看全部
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <el-card class="panel" shadow="never">
        <template #header>
          <span class="panel-title">最新公告</span>
        </template>
        <div class="list-box" v-loading="noticeLoading">
          <ul class="list" v-if="notices.length">
            <li class="list-item" v-for="item in notices" :key="item.id">
              <div class="item-info">
                <span class="item-name">
                  <el-tag v-if="item.isTop === 1" type="danger" size="small" effect="light">置顶</el-tag>
                  {{ item.title }}
                </span>
                <span class="item-desc">{{ formatDateTime(item.createTime) }}</span>
              </div>
            </li>
          </ul>
          <el-empty v-else description="暂无公告" :image-size="60" />
        </div>
        <div class="panel-footer">
        <el-button link type="primary" @click="go('/admin/notice')">
            查看全部
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <el-card class="panel" shadow="never">
        <template #header>
          <span class="panel-title">待回访提醒</span>
        </template>
        <div class="list-box" v-loading="visitLoading">
          <ul class="list" v-if="visits.length">
            <li class="list-item" v-for="item in visits" :key="item.id">
              <div class="item-info">
                <span class="item-name">{{ item.petName }}</span>
                <span class="item-desc">领养人 {{ item.adopterName }}</span>
              </div>
              <span class="item-time">{{ formatDate(item.planTime) }}</span>
            </li>
          </ul>
          <el-empty v-else description="暂无待回访记录" :image-size="60" />
        </div>
        <div class="panel-footer">
        <el-button link type="primary" @click="go('/admin/visit')">
            查看全部
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getOverview, getPetType, getTrend } from '../../api/stats'
import { pageApplies } from '../../api/apply'
import { pageNotices } from '../../api/notice'
import { pageVisits } from '../../api/visit'

/** 主题色 */
const PRIMARY = '#87C47C'
/** 宠物类型配色 */
const PET_TYPE_COLORS = {
  狗狗: '#F5A623',
  猫咪: '#87C47C',
  兔子: '#F8D775',
  其他: '#C9C9C9'
}
const FALLBACK_COLORS = ['#F5A623', '#87C47C', '#F8D775', '#C9C9C9', '#9EC7E8']

const router = useRouter()

/** 数据概览 */
const overview = ref({})
const overviewLoading = ref(false)
/** 趋势类型：week 近7天、month 近30天、year 近12个月 */
const trendType = ref('week')
const trendLoading = ref(false)
const pieLoading = ref(false)
/** 底部三栏数据 */
const applies = ref([])
const notices = ref([])
const visits = ref([])
const applyLoading = ref(false)
const noticeLoading = ref(false)
const visitLoading = ref(false)

/** 图表容器与实例 */
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null
/** 图表容器尺寸监听器：容器一变就重绘画布，保证画布和容器始终严丝合缝 */
let chartObserver = null

/** 顶部 4 个概览卡片 */
const statCards = computed(() => [
  {
    key: 'pendingApply',
    label: '待审核申请',
    value: overview.value.pendingApply ?? 0,
    icon: '🐶',
    theme: 'green'
  },
  {
    key: 'adoptablePet',
    label: '可领养宠物',
    value: overview.value.adoptablePet ?? 0,
    icon: '🐱',
    theme: 'green'
  },
  {
    key: 'todayApply',
    label: '今日新增申请',
    value: overview.value.todayApply ?? 0,
    icon: '🐰',
    theme: 'orange'
  },
  {
    key: 'monthAdopt',
    label: '本月成功领养',
    value: overview.value.monthAdopt ?? 0,
    icon: '🐾',
    theme: 'orange'
  }
])

/** 申请状态：0 待审核、1 已通过、2 已驳回 */
const applyStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] ?? '未知'
}
const applyTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
}

/** 时间格式化：后端返回 2026-11-04T10:30:08 / 2026-11-10 两种格式 */
const formatDateTime = (value) => {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 16)
}
const formatDate = (value) => {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 10)
}

/** 跳转其它页面 */
const go = (path) => router.push(path)

/**
 * 载入数据概览
 * 接口异常由 axios 响应拦截器统一弹出 ElMessage 错误提示，这里只做状态复位，避免重复弹窗
 */
const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const data = await getOverview()
    overview.value = data || {}
  } catch (e) {
    overview.value = {}
  } finally {
    overviewLoading.value = false
  }
}

/** 载入领养趋势 */
const loadTrend = async (type) => {
  trendLoading.value = true
  try {
    const data = await getTrend(type)
    renderTrend(Array.isArray(data) ? data : [])
  } catch (e) {
    renderTrend([])
  } finally {
    trendLoading.value = false
  }
}

/** 载入宠物类型分布 */
const loadPetType = async () => {
  pieLoading.value = true
  try {
    const data = await getPetType()
    renderPie(Array.isArray(data) ? data : [])
  } catch (e) {
    renderPie([])
  } finally {
    pieLoading.value = false
  }
}

/** 载入最新申请 */
const loadApplies = async () => {
  applyLoading.value = true
  try {
    const data = await pageApplies({ pageNum: 1, pageSize: 5 })
    applies.value = (data && data.records) || []
  } catch (e) {
    applies.value = []
  } finally {
    applyLoading.value = false
  }
}

/** 载入最新公告 */
const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const data = await pageNotices({ pageNum: 1, pageSize: 5 })
    notices.value = (data && data.records) || []
  } catch (e) {
    notices.value = []
  } finally {
    noticeLoading.value = false
  }
}

/** 载入待回访提醒 */
const loadVisits = async () => {
  visitLoading.value = true
  try {
    const data = await pageVisits({ pageNum: 1, pageSize: 5, status: 0 })
    visits.value = (data && data.records) || []
  } catch (e) {
    visits.value = []
  } finally {
    visitLoading.value = false
  }
}

/** 切换趋势统计维度 */
const handleTrendChange = async (type) => {
  await loadTrend(type)
}

/** 获取趋势图实例（懒初始化） */
const getTrendChart = () => {
  if (!trendChartRef.value) {
    return null
  }
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  return trendChart
}

/** 获取饼图实例（懒初始化） */
const getPieChart = () => {
  if (!pieChartRef.value) {
    return null
  }
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  return pieChart
}

/** 渲染领养趋势折线图 */
const renderTrend = (list) => {
  const chart = getTrendChart()
  if (!chart) {
    return
  }
  const dates = list.map((item) => item.date)
  const counts = list.map((item) => item.count ?? 0)
  const isEmpty = counts.length === 0 || counts.every((count) => !count)
  chart.setOption({
    // 更新数据时不做过渡动画，避免切维度时曲线在画布里"抖"一下
    animationDurationUpdate: 0,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line', lineStyle: { color: '#dbead6' } },
      formatter: (params) => {
        const point = params[0]
        return `${point.axisValue}<br/>成功领养：${point.value} 只`
      }
    },
    grid: { left: 8, right: 16, top: 24, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#ecebe4' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#8a8a83',
        fontSize: 12,
        hideOverlap: true,
        formatter: (value) => formatAxisLabel(value, trendType.value)
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#8a8a83', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f5f4ef' } }
    },
    series: [
      {
        name: '成功领养',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: counts.length <= 12,
        lineStyle: { width: 2, color: PRIMARY },
        itemStyle: { color: PRIMARY, borderColor: '#ffffff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(135,196,124,0.3)' },
            { offset: 1, color: 'rgba(135,196,124,0.05)' }
          ])
        },
        data: counts
      }
    ],
    graphic: isEmpty
      ? [
          {
            type: 'text',
            left: 'center',
            top: 'middle',
            style: { text: '暂无领养数据', fill: '#c9c9c9', fontSize: 13 }
          }
        ]
      : []
  })
}

/** 渲染宠物类型环形图 */
const renderPie = (list) => {
  const chart = getPieChart()
  if (!chart) {
    return
  }
  const data = list.map((item, index) => ({
    name: item.type,
    value: item.count ?? 0,
    itemStyle: {
      color: PET_TYPE_COLORS[item.type] || FALLBACK_COLORS[index % FALLBACK_COLORS.length]
    }
  }))
  // 占比为 0 的类型不进图例也不画扇区，避免"猫咪 0%"这类无效信息
  const validData = data.filter((item) => item.value > 0)
  const total = validData.reduce((sum, item) => sum + item.value, 0)
  const isEmpty = total === 0

  /** 图例文本：类型名 + 百分比（前面的圆点由 ECharts 图例图标提供） */
  const legendFormatter = (name) => {
    const item = validData.find((row) => row.name === name)
    if (!item) {
      return name
    }
    const percent = total ? Math.round((item.value / total) * 100) : 0
    return `${name} ${percent}%`
  }

  chart.setOption({
    // 同上：饼图更新不做过渡动画
    animationDurationUpdate: 0,
    tooltip: {
      trigger: 'item',
      // ECharts 的 percent 带两位小数，这里取整，和图例里的百分比保持一致
      formatter: (param) => `${param.name} ${param.value}只 ${Math.round(param.percent)}%`
    },
    legend: {
      // 只列出有数据的类型
      data: validData.map((item) => item.name),
      bottom: 0,
      left: 'center',
      orient: 'horizontal',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 18,
      formatter: legendFormatter,
      textStyle: { color: '#5c5c56', fontSize: 12 }
    },
    series: [
      {
        name: '宠物类型',
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#ffffff', borderWidth: 2, borderRadius: 4 },
        // 名称和百分比交给底部图例展示，扇区上不再叠文字
        label: { show: false },
        data: validData
      }
    ],
    // 圆环中心：宠物总数大字 + "宠物总数"小字；无数据时显示占位文案
    // 用固定 id 保证 setOption 合并时元素一一对应，不会残留上一次的文字
    graphic: [
      {
        id: 'pieCenterValue',
        type: 'text',
        left: 'center',
        top: isEmpty ? '42%' : '37%',
        style: isEmpty
          ? { text: '暂无数据', fill: '#c9c9c9', fontSize: 14, textAlign: 'center', textVerticalAlign: 'middle' }
          : { text: String(total), fill: PRIMARY, fontSize: 34, fontWeight: 'bold', textAlign: 'center', textVerticalAlign: 'middle' }
      },
      {
        id: 'pieCenterLabel',
        type: 'text',
        left: 'center',
        top: '53%',
        style: {
          text: isEmpty ? '' : '宠物总数',
          fill: '#8a8a83',
          fontSize: 12,
          textAlign: 'center',
          textVerticalAlign: 'middle'
        }
      }
    ]
  })
}

/** x 轴日期标签：周/月显示 MM-DD，年显示 YYYY-MM */
const formatAxisLabel = (value, type) => {
  if (!value) {
    return ''
  }
  if (type === 'year') {
    return String(value).slice(0, 7)
  }
  return String(value).slice(5)
}

/** 窗口尺寸变化时同步图表 */
const handleResize = () => {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
}

/**
 * 监听两个图表容器的尺寸变化
 * 只监听 window 的 resize 不够：像滚动条出现/消失这类只改容器宽度的变化
 * 并不会触发 window resize，画布就会和容器错位，看起来像画布在抖
 */
const observeChartSize = () => {
  if (typeof ResizeObserver === 'undefined') {
    return
  }
  chartObserver = new ResizeObserver(() => {
    trendChart && trendChart.resize()
    pieChart && pieChart.resize()
  })
  if (trendChartRef.value) {
    chartObserver.observe(trendChartRef.value)
  }
  if (pieChartRef.value) {
    chartObserver.observe(pieChartRef.value)
  }
}

onMounted(() => {
  try {
    // 先初始化图表，确保容器已有尺寸，再异步填充数据
    getTrendChart()
    getPieChart()
  } catch (e) {
    ElMessage.warning('图表初始化失败，请刷新页面重试')
  }
  window.addEventListener('resize', handleResize)
  observeChartSize()
  loadOverview()
  loadTrend(trendType.value)
  loadPetType()
  loadApplies()
  loadNotices()
  loadVisits()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartObserver) {
    chartObserver.disconnect()
    chartObserver = null
  }
  // 销毁 ECharts 实例，避免内存泄漏
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 一、数据概览卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px;
  background-color: #ffffff;
  border: 1px solid #f0efe9;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(58, 58, 54, 0.06);
}

.stat-label {
  margin-bottom: 8px;
  font-size: 13px;
  color: #8a8a83;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1;

  &.green {
    color: #87c47c;
  }

  &.orange {
    color: #f5a623;
  }
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  font-size: 30px;
  border-radius: 16px;

  &.green {
    background-color: rgba(135, 196, 124, 0.16);
  }

  &.orange {
    background-color: rgba(245, 166, 35, 0.16);
  }
}

/* 二、图表区 */
.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.bottom-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.panel {
  border-radius: 12px;
  border-color: #f0efe9;
  box-shadow: 0 2px 12px rgba(58, 58, 54, 0.04);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #3a3a36;
}

.chart-box {
  width: 100%;
  height: 300px;
}

/* 三、底部列表 */
.list-box {
  min-height: 240px;
  max-height: 260px;
  overflow-y: auto;
}

.list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #f0efe9;

  &:last-child {
    border-bottom: none;
  }
}

.item-info {
  min-width: 0;
}

.item-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #3a3a36;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: #8a8a83;
}

.item-time {
  flex-shrink: 0;
  font-size: 12px;
  color: #a5a59d;
}

.panel-footer {
  margin-top: 4px;
  padding-top: 10px;
  text-align: right;
  border-top: 1px solid #f5f4ef;
}

/* 窄屏自适应 */
@media screen and (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-row,
  .bottom-row {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 640px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
