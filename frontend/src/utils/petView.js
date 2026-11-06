/**
 * 宠物相关的展示映射
 * 后端 pet.type 取值为 狗狗/猫咪/兔子/其他，pet.status 为 1可领养/2已领养/3救助中
 */

/** 类型筛选项 */
export const PET_TYPES = ['猫咪', '狗狗', '兔子', '其他']

/** 类型对应的占位图标：档案里多数宠物没有上传照片，用图标兜底比空图好看 */
export const PET_TYPE_ICONS = {
  猫咪: '🐱',
  狗狗: '🐶',
  兔子: '🐰',
  其他: '🐾'
}

/** 类型对应的占位底色，保持整体清新不刺眼 */
export const PET_TYPE_COLORS = {
  猫咪: '#fdf1e3',
  狗狗: '#eef5eb',
  兔子: '#f6eef7',
  其他: '#eef2f7'
}

/** 领养状态文案 */
export const PET_STATUS_TEXT = {
  1: '可领养',
  2: '已领养',
  3: '救助中'
}

/** 领养申请状态文案 */
export const APPLY_STATUS_TEXT = {
  0: '待审核',
  1: '已通过',
  2: '已驳回'
}

/**
 * 年龄展示：后端 pet.age 的单位是「月」（管理端表单标注“单位：月”，
 * 输入框上限 360 对应 30 岁；数据库 pet.age 字段注释同为“年龄（月）”）。
 * 用户端与理端必须保持同一口径，否则同一只宠物在两端会显示成不同年龄。
 * 这里把月份换算成「3 个月 / 2 岁 / 1 岁 6 个月」这样更符合日常表达。
 */
export function formatPetAge(age) {
  if (age === null || age === undefined || age === '') {
    return '年龄未知'
  }
  const months = Number(age)
  if (!Number.isFinite(months) || months < 0) {
    return '年龄未知'
  }
  if (months < 12) {
    return `${months} 个月`
  }
  const years = Math.floor(months / 12)
  const rest = months % 12
  return rest === 0 ? `${years} 岁` : `${years} 岁 ${rest} 个月`
}

/**
 * 性别展示
 */
export function formatPetGender(gender) {
  if (!gender) {
    return '性别未知'
  }
  return gender
}

/**
 * 品种展示
 */
export function formatPetBreed(breed) {
  if (!breed) {
    return '品种未知'
  }
  return breed
}

/**
 * 日期时间展示：后端输出 "yyyy-MM-dd HH:mm:ss"，这里只取到分钟
 */
export function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

