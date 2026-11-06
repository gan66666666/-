<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="宠物类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="领养状态">
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
            placeholder="宠物名称 / 品种"
            clearable
            style="width: 200px"
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

    <!-- 档案列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">宠物档案</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增宠物
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="name" label="宠物名" min-width="110" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column prop="breed" label="品种" min-width="110" show-overflow-tooltip />
        <el-table-column prop="age" label="年龄（月）" width="100" align="center">
          <template #default="{ row }">{{ row.age ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">{{ row.gender || '-' }}</template>
        </el-table-column>
        <el-table-column prop="health" label="健康状态" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.health || '-' }}</template>
        </el-table-column>
        <el-table-column label="领养状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="petStatusType[row.status] || 'info'" effect="light">
              {{ petStatusText[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无宠物档案" :image-size="90" />
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
      :title="isEdit ? '编辑宠物' : '新增宠物'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="宠物名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入宠物名" maxlength="30" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择宠物类型" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model.trim="form.breed" placeholder="如：金毛" maxlength="30" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="360" controls-position="right" />
          <span class="form-tip">单位：月</span>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" clearable style="width: 100%">
            <el-option v-for="item in genderOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像地址" prop="avatar">
          <el-input v-model.trim="form.avatar" placeholder="图片地址，可不填" maxlength="255" />
        </el-form-item>
        <el-form-item label="健康状态" prop="health">
          <el-input v-model.trim="form.health" placeholder="如：健康 / 已驱虫" maxlength="50" />
        </el-form-item>
        <el-form-item label="领养状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="宠物详情" width="520px">
      <div class="pet-detail">
        <el-image
          v-if="detail.avatar"
          :src="detail.avatar"
          fit="cover"
          class="pet-avatar"
        >
          <template #error>
            <div class="pet-avatar-error"><el-icon><Picture /></el-icon></div>
          </template>
        </el-image>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="宠物名">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detail.breed || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ detail.age ?? '-' }} 月</el-descriptions-item>
          <el-descriptions-item label="性别">{{ detail.gender || '-' }}</el-descriptions-item>
          <el-descriptions-item label="领养状态">
            <el-tag :type="petStatusType[detail.status] || 'info'" effect="light">
              {{ petStatusText[detail.status] || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="健康状态" :span="2">
            {{ detail.health || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="头像地址" :span="2">
            {{ detail.avatar || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPet, deletePet, pagePets, updatePet } from '../../api/pet'

/** 宠物类型 */
const typeOptions = ['狗狗', '猫咪', '兔子', '其他']
/** 性别 */
const genderOptions = ['公', '母']
/** 领养状态：1 可领养、2 已领养、3 救助中 */
const statusOptions = [
  { label: '可领养', value: 1 },
  { label: '已领养', value: 2 },
  { label: '救助中', value: 3 }
]
const petStatusText = { 1: '可领养', 2: '已领养', 3: '救助中' }
const petStatusType = { 1: 'success', 2: 'warning', 3: 'info' }

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  type: '',
  status: null,
  keyword: ''
})

/** 新增 / 编辑弹窗 */
const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  name: '',
  type: '',
  breed: '',
  age: null,
  gender: '',
  avatar: '',
  health: '',
  status: 1
})
const rules = {
  name: [{ required: true, message: '请输入宠物名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择宠物类型', trigger: 'change' }]
}

/** 详情弹窗 */
const detailVisible = ref(false)
const detail = ref({})

/** 组装查询参数，过滤空值 */
const buildParams = () => {
  const params = { pageNum: query.pageNum, pageSize: query.pageSize }
  if (query.type) {
    params.type = query.type
  }
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
    const data = await pagePets(buildParams())
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
  query.type = ''
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

/** 打开新增弹窗 */
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    type: '',
    breed: '',
    age: null,
    gender: '',
    avatar: '',
    health: '',
    status: 1
  })
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}

/** 打开编辑弹窗并回填数据 */
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    breed: row.breed || '',
    age: row.age === null || row.age === undefined ? null : row.age,
    gender: row.gender || '',
    avatar: row.avatar || '',
    health: row.health || '',
    status: row.status
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
      await updatePet({ ...form })
      ElMessage.success('修改成功')
    } else {
      const { id, ...payload } = form
      await addPet(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

/** 查看详情 */
const handleDetail = async (row) => {
  // 直接用列表行数据填充详情，不再单独请求接口：
  // 打开弹窗时不触发额外请求，表格也不会重新渲染 / 加 v-loading
  detail.value = { ...row }
  detailVisible.value = true
}

/** 删除档案 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除宠物「${row.name}」吗？删除后不可恢复。`,
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
    await deletePet(row.id)
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

.pet-detail {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.pet-avatar {
  flex-shrink: 0;
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
}

.pet-avatar-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 26px;
  color: #c9c9c9;
  background-color: #f5f4ef;
}
</style>
