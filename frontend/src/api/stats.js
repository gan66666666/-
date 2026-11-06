import request from './request'

/**
 * 工作台概览：待审核申请数、可领养宠物数、今日新增申请数、本月成功领养数
 */
export function getOverview() {
  return request.get('/stats/overview')
}

/**
 * 领养趋势
 * @param {'week'|'month'|'year'} type
 */
export function getTrend(type = 'week') {
  return request.get('/stats/trend', { params: { type } })
}

/**
 * 宠物类型分布
 */
export function getPetType() {
  return request.get('/stats/petType')
}
