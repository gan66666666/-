<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="回访状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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

    <!-- 回访列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">回访管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增回访
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="petName" label="宠物名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="adopterName" label="领养人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="planTime" label="计划回访时间" width="160" align="center">
          <template #default="{ row }">{{ formatDate(row.planTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="visitStatusType[row.status] || 'info'" effect="light">
              {{ visitStatusText[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="回访内容" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="success"
              :disabled="row.status === 1"
              @click="handleRegister(row)"
            >
              登记结果
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无回访记录" :image-size="90" />
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

    <!-- 新增回访弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增回访计划" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="宠物名" prop="petName">
          <el-input v-model.trim="form.petName" placeholder="请输入宠物名" maxlength="30" />
        </el-form-item>
        <el-form-item label="领养人" prop="adopterName">
          <el-input v-model.trim="form.adopterName" placeholder="请输入领养人姓名" maxlength="30" />
        </el-form-item>
        <el-form-item label="计划回访" prop="planTime">
          <el-date-picker
            v-model="form.planTime"
            type="date"
            placeholder="请选择计划回访日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 登记结果弹窗 -->
    <el-dialog v-model="resultVisible" title="登记回访结果" width="520px" destroy-on-close>
      <el-descriptions :column="2" border class="result-info">
        <el-descriptions-item label="宠物名">{{ current.petName }}</el-descriptions-item>
        <el-descriptions-item label="领养人">{{ current.adopterName }}</el-descriptions-item>
        <el-descriptions-item label="计划回访" :span="2">
          {{ formatDate(current.planTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form ref="resultRef" :model="resultForm" :rules="resultRules" label-width="90px">
        <el-form-item label="回访内容" prop="content">
          <el-input
            v-model.trim="resultForm.content"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请填写回访情况，如：宠物适应良好，已完成绝育"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultVisible = false">取消</el-button>
        <el-button type="primary" :loading="resultLoading" @click="submitResult">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addVisit, deleteVisit, pageVisits, updateVisit } from '../../api/visit'

/** 回访状态：0 待回访、1 已完成 */
const statusOptions = [
  { label: '待回访', value: 0 },
  { label: '已完成', value: 1 }
]
const visitStatusText = { 0: '待回访', 1: '已完成' }
const visitStatusType = { 0: 'warning', 1: 'success' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null
})

/** 新增弹窗 */
const formRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const form = reactive({
  petName: '',
  adopterName: '',
  planTime: ''
})
const rules = {
  petName: [{ required: true, message: '请输入宠物名', trigger: 'blur' }],
  adopterName: [{ required: true, message: '请输入领养人姓名', trigger: 'blur' }],
  planTime: [{ required: true, message: '请选择计划回访日期', trigger: 'change' }]
}

/** 登记结果弹窗 */
const resultRef = ref(null)
const resultVisible = ref(false)
const resultLoading = ref(false)
const current = ref({})
const resultForm = reactive({
  id: null,
  content: ''
})
const resultRules = {
  content: [{ required: true, message: '请填写回访内容', trigger: 'blur' }]
}

/** 日期格式化 */
const formatDate = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 10)
}

/** 组装查询参数，过滤空值 */
const buildParams = () => {
  const params = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.status !== null && query.status !== undefined && query.status !== '') {
    params.status = query.status
  }
  return params
}

/** 查询列表 */
const loadList = async () => {
  loading.value = true
  try {
    const data = await pageVisits(buildParams())
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
  query.pageNum = 1
  loadList()
}

/** 每页条数变化 */
const handleSizeChange = (size) => {
  query.pageSize = size
  query.pageNum = 1
  loadList()
}

/** 打开新增弹窗 */
const handleAdd = () => {
  Object.assign(form, { petName: '', adopterName: '', planTime: '' })
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

/** 提交新增回访 */
const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitLoading.value = true
  try {
    await addVisit({ ...form })
    ElMessage.success('新增回访计划成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

/** 打开登记结果弹窗 */
const handleRegister = (row) => {
  current.value = row
  Object.assign(resultForm, { id: row.id, content: row.content || '' })
  resultVisible.value = true
  nextTick(() => resultRef.value && resultRef.value.clearValidate())
}

/** 提交回访结果 */
const submitResult = async () => {
  const valid = await resultRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  resultLoading.value = true
  try {
    await updateVisit({
      id: resultForm.id,
      petName: current.value.petName,
      adopterName: current.value.adopterName,
      planTime: formatDate(current.value.planTime),
      content: resultForm.content,
      status: 1
    })
    ElMessage.success('回访结果登记成功')
    resultVisible.value = false
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    resultLoading.value = false
  }
}

/** 删除回访记录 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除「${row.petName}」的回访记录吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch (e) {
    return
  }
  try {
    await deleteVisit(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1
    }
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
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

.result-info {
  margin-bottom: 16px;
}
</style>
