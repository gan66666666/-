<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="领养人">
          <el-input
            v-model.trim="query.adopterName"
            placeholder="请输入领养人姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="领养日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
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

    <!-- 记录列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">领养记录</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="petName" label="宠物名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="adopterName" label="领养人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="adopterPhone" label="联系电话" width="140" />
        <el-table-column prop="adoptDate" label="领养日期" width="130" align="center">
          <template #default="{ row }">{{ formatDate(row.adoptDate) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无领养记录" :image-size="90" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageRecords } from '../../api/record'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  adopterName: ''
})
/** 领养日期范围 */
const dateRange = ref([])

/** 日期格式化 */
const formatDate = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 10)
}
const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

/** 组装查询参数：日期范围转换为 startDate / endDate */
const buildParams = () => {
  const params = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.adopterName) {
    params.adopterName = query.adopterName
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  return params
}

/** 查询列表 */
const loadList = async () => {
  loading.value = true
  try {
    const data = await pageRecords(buildParams())
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
  query.adopterName = ''
  dateRange.value = []
  query.pageNum = 1
  loadList()
}

/** 每页条数变化 */
const handleSizeChange = (size) => {
  query.pageSize = size
  query.pageNum = 1
  loadList()
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
</style>
