<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="申请状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model.trim="query.keyword"
            placeholder="申请人 / 申请编号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">领养申请</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="applyNo" label="申请编号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" min-width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="petName" label="宠物名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="applyStatusType[row.status] || 'info'" effect="light">
              {{ applyStatusText[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button
              link
              type="success"
              :disabled="row.status !== 0"
              @click="handleAudit(row)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无领养申请" :image-size="90" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        @current-change="loadList"
        @size-change="handleSizeChange"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号" :span="2">{{ detail.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="宠物名">{{ detail.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(detail.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="领养理由" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态" :span="2">
          <el-tag :type="applyStatusType[detail.status] || 'info'" effect="light">
            {{ applyStatusText[detail.status] || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">
          {{ detail.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核领养申请" width="560px">
      <el-descriptions :column="2" border class="audit-info">
        <el-descriptions-item label="申请人">{{ auditForm.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ auditForm.phone }}</el-descriptions-item>
        <el-descriptions-item label="宠物名">{{ auditForm.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请编号">{{ auditForm.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="领养理由" :span="2">{{ auditForm.reason || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="90px" class="audit-form">
        <el-form-item label="驳回原因">
          <el-input
            v-model.trim="auditForm.rejectReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="驳回时必填，如：暂不符合领养条件"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="danger" plain :loading="auditLoading" @click="handleReject">驳回</el-button>
        <el-button type="primary" :loading="auditLoading" @click="handlePass">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditApply, pageApplies } from '../../api/apply'

/** 申请状态：0 待审核、1 已通过、2 已驳回 */
const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 }
]
const applyStatusText = { 0: '待审核', 1: '已通过', 2: '已驳回' }
const applyStatusType = { 0: 'warning', 1: 'success', 2: 'info' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
  keyword: ''
})

/** 详情弹窗 */
const detailVisible = ref(false)
const detail = ref({})

/** 审核弹窗 */
const auditVisible = ref(false)
const auditLoading = ref(false)
const auditForm = reactive({
  id: null,
  applyNo: '',
  applicantName: '',
  phone: '',
  petName: '',
  reason: '',
  rejectReason: ''
})

/** 时间格式化 */
const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

/** 组装查询参数，过滤空值 */
const buildParams = () => {
  const params = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.status !== null && query.status !== undefined && query.status !== '') {
    params.status = query.status
  }
  if (query.keyword) {
    params.keyword = query.keyword
  }
  return params
}

/** 查询列表 */
const loadList = async () => {
  loading.value = true
  try {
    const data = await pageApplies(buildParams())
    list.value = (data && data.records) || []
    total.value = (data && data.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleSearch = () => {
  query.pageNum = 1
  loadList()
}

/** 重置 */
const handleReset = () => {
  query.status = null
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

/** 每页条数变化 */
const handleSizeChange = (size) => {
  query.pageSize = size
  query.pageNum = 1
  loadList()
}

/** 查看详情 */
const handleDetail = async (row) => {
  // 直接用列表行数据填充详情，不再单独请求接口：
  // 打开弹窗时不触发额外请求，表格也不会重新渲染 / 加 v-loading
  detail.value = { ...row }
  detailVisible.value = true
}

/** 打开审核弹窗 */
const handleAudit = (row) => {
  Object.assign(auditForm, {
    id: row.id,
    applyNo: row.applyNo,
    applicantName: row.applicantName,
    phone: row.phone,
    petName: row.petName,
    reason: row.reason,
    rejectReason: ''
  })
  auditVisible.value = true
}

/** 提交审核结果 */
const submitAudit = async (status) => {
  auditLoading.value = true
  try {
    const payload = { id: auditForm.id, status }
    if (status === 2) {
      payload.rejectReason = auditForm.rejectReason
    }
    await auditApply(payload)
    ElMessage.success(status === 1 ? '审核通过，已生成领养记录' : '已驳回该申请')
    auditVisible.value = false
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    auditLoading.value = false
  }
}

/** 审核通过 */
const handlePass = async () => {
  await submitAudit(1)
}

/** 审核驳回 */
const handleReject = async () => {
  if (!auditForm.rejectReason) {
    ElMessage.warning('驳回申请时请填写驳回原因')
    return
  }
  await submitAudit(2)
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  row-gap: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #3a3a36;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.audit-info {
  margin-bottom: 16px;
}
</style>
