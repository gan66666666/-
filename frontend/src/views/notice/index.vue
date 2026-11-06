<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model.trim="query.keyword"
            placeholder="公告标题"
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

    <!-- 公告列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">公告管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            发布公告
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="是否置顶" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isTop === 1 ? 'warning' : 'info'" effect="light">
              {{ row.isTop === 1 ? '置顶' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" align="center">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="warning" @click="handleToggleTop(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无公告" :image-size="90" />
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

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model.trim="form.title" placeholder="请输入公告标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model.trim="form.content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="是否置顶" prop="isTop">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
          <span class="form-tip">置顶公告会优先展示</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addNotice, deleteNotice, pageNotices, toggleNoticeTop, updateNotice } from '../../api/notice'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

/** 新增 / 编辑弹窗 */
const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  title: '',
  content: '',
  isTop: 0
})
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

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
  if (query.keyword) {
    params.keyword = query.keyword
  }
  return params
}

/** 查询列表 */
const loadList = async () => {
  loading.value = true
  try {
    const data = await pageNotices(buildParams())
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

/** 打开新增弹窗 */
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, title: '', content: '', isTop: 0 })
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

/** 打开编辑弹窗并回填数据 */
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title,
    content: row.content || '',
    isTop: row.isTop === 1 ? 1 : 0
  })
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

/** 提交表单 */
const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateNotice({ ...form })
      ElMessage.success('修改成功')
    } else {
      const { id, ...payload } = form
      await addNotice(payload)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

/** 置顶 / 取消置顶 */
const handleToggleTop = async (row) => {
  try {
    await toggleNoticeTop(row.id)
    ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已置顶')
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

/** 删除公告 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告「${row.title}」吗？删除后不可恢复。`,
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
    await deleteNotice(row.id)
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #a5a59d;
}
</style>
