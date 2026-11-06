<template>
  <div class="user-myapply">
    <h1 class="page-title">我的申请</h1>
    <p class="page-desc">这里是你提交的领养申请及处理进度。</p>

    <el-card class="table-card" shadow="never">
      <div v-loading="loading" class="table-wrap">
        <el-table v-if="rows.length > 0" :data="rows" border stripe>
          <el-table-column prop="applyNo" label="申请号" min-width="180" />
          <el-table-column prop="petName" label="宠物名" min-width="110">
            <template #default="{ row }">
              <span class="pet-cell">{{ row.petName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="applyTime" label="申请时间" min-width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.reason || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="light">
                {{ APPLY_STATUS_TEXT[row.status] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="驳回原因" min-width="160">
            <template #default="{ row }">
              <span v-if="row.status === 2" class="reject-text">{{ row.rejectReason || '未填写' }}</span>
              <span v-else class="dash-text">-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-else-if="!loading"
          description="还没有提交过领养申请"
          :image-size="110"
        >
          <el-button type="primary" @click="go('/user/pets')">去看看可领养的宠物</el-button>
        </el-empty>
      </div>

      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyApply } from '../../../api/user/auth'
import { APPLY_STATUS_TEXT, formatDateTime } from '../../../utils/petView'

const router = useRouter()

const rows = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10
})

/** 状态标签配色 */
function statusTagType(status) {
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'danger'
  }
  return 'warning'
}

/**
 * 加载我的申请列表
 */
async function loadList() {
  loading.value = true
  try {
    const res = await getMyApply({ pageNum: query.pageNum, pageSize: query.pageSize })
    rows.value = (res && res.records) || []
    total.value = (res && res.total) || 0
  } catch (e) {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSizeChange() {
  query.pageNum = 1
  loadList()
}

function go(path) {
  router.push(path)
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.user-myapply {
  max-width: 1000px;
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

.table-card {
  border-radius: var(--user-card-radius);
  border: 1px solid var(--user-card-border);
  background-color: var(--user-card-bg);
}

.table-wrap {
  min-height: 180px;
}

.pet-cell {
  font-weight: 500;
  color: var(--user-primary-dark);
}

.reject-text {
  color: #d9534f;
}

.dash-text {
  color: var(--user-text-faint);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

