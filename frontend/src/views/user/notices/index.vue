<template>
  <div class="user-notices">
    <h1 class="page-title">公告列表</h1>
    <p class="page-desc">领养须知、活动安排等通知都会在这里发布。</p>

    <div v-loading="loading" class="notice-list">
      <div
        v-for="notice in notices"
        :key="notice.id"
        class="notice-item"
        @click="goDetail(notice.id)"
      >
        <div class="notice-main">
          <div class="notice-title-row">
            <el-tag v-if="notice.isTop === 1" type="warning" size="small" effect="light">置顶</el-tag>
            <span class="notice-title">{{ notice.title }}</span>
          </div>
          <p class="notice-desc">{{ notice.content }}</p>
        </div>
        <span class="notice-time">{{ formatDateTime(notice.createTime) }}</span>
      </div>
    </div>

    <el-empty
      v-if="!loading && notices.length === 0"
      description="暂时还没有公告"
      :image-size="100"
    />

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadNotices"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageNotices } from '../../../api/user/notice'
import { formatDateTime } from '../../../utils/petView'

const router = useRouter()

const notices = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 加载公告列表（后端已按置顶优先、时间倒序）
 */
async function loadNotices() {
  loading.value = true
  try {
    const res = await pageNotices({ pageNum: query.pageNum, pageSize: query.pageSize })
    notices.value = (res && res.records) || []
    total.value = (res && res.total) || 0
  } catch (e) {
    notices.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/user/notices/${id}`)
}

onMounted(loadNotices)
</script>

<style scoped lang="scss">
.user-notices {
  max-width: 860px;
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

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
  box-shadow: var(--user-card-shadow);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: var(--user-card-shadow-hover);
    transform: translateY(-2px);
  }
}

.notice-main {
  min-width: 0;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--user-title-color);
}

.notice-desc {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--user-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--user-text-faint);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media screen and (max-width: 640px) {
  .notice-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>

