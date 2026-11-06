<template>
  <div class="notice-detail">
    <el-button link type="primary" class="back-button" @click="goBack">
      <el-icon>
        <ArrowLeft />
      </el-icon>
      <span>返回公告列表</span>
    </el-button>

    <div v-loading="loading" class="detail-wrap">
      <article v-if="notice" class="detail-card">
        <h1 class="detail-title">{{ notice.title }}</h1>
        <div class="detail-meta">
          <el-tag v-if="notice.isTop === 1" type="warning" size="small" effect="light">置顶</el-tag>
          <span class="detail-time">{{ formatDateTime(notice.createTime) }}</span>
          <span class="detail-source">宠物领养管理系统</span>
        </div>
        <p class="detail-body">{{ notice.content }}</p>
      </article>

      <el-empty v-else-if="!loading" description="公告不存在或已被删除" :image-size="110">
        <el-button type="primary" @click="goBack">返回公告列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNotice } from '../../../api/user/notice'
import { formatDateTime } from '../../../utils/petView'

const route = useRoute()
const router = useRouter()

const notice = ref(null)
const loading = ref(false)

const noticeId = computed(() => route.params.id)

/**
 * 加载公告详情
 */
async function loadNotice() {
  loading.value = true
  try {
    notice.value = await getNotice(noticeId.value)
  } catch (e) {
    notice.value = null
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/user/notices')
}

onMounted(loadNotice)
</script>

<style scoped lang="scss">
.notice-detail {
  max-width: 800px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 4px;
}

.detail-wrap {
  min-height: 260px;
}

.detail-card {
  padding: 28px 30px 32px;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
  box-shadow: var(--user-card-shadow);
}

.detail-title {
  margin: 0 0 12px;
  font-size: 24px;
  line-height: 1.5;
  font-weight: 700;
  color: var(--user-title-color);
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--user-card-border);
}

.detail-time {
  font-size: 13px;
  color: var(--user-text-muted);
}

.detail-source {
  font-size: 13px;
  color: var(--user-text-faint);
}

.detail-body {
  margin: 0;
  font-size: 14px;
  line-height: 2;
  color: var(--user-text-color);
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

