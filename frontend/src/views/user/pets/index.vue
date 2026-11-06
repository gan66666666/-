<template>
  <div class="user-pets">
    <h1 class="page-title">宠物领养</h1>
    <p class="page-desc">这些小家伙正在等待一个愿意接纳它们的家庭。</p>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable class="filter-item">
            <el-option v-for="t in petTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" class="filter-item">
            <el-option label="全部" value="" />
            <el-option label="可领养" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键字">
          <el-input
            v-model.trim="query.keyword"
            placeholder="按宠物名字搜索"
            clearable
            class="filter-item"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon>
              <Search />
            </el-icon>
            <span>搜索</span>
          </el-button>
          <el-button @click="handleReset">
            <el-icon>
              <Refresh />
            </el-icon>
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 宠物卡片网格 -->
    <div v-loading="loading" class="pet-grid">
      <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" @detail="goDetail" />
    </div>

    <el-empty
      v-if="!loading && pets.length === 0"
      description="没有找到符合条件的宠物，换个条件试试吧"
      :image-size="100"
    />

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[12, 24, 36]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="loadPets"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pagePets } from '../../../api/user/pet'
import { PET_TYPES } from '../../../utils/petView'
import PetCard from '../components/PetCard.vue'

const router = useRouter()

const petTypes = PET_TYPES

const pets = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 12,
  type: '',
  status: '',
  keyword: ''
})

/**
 * 加载宠物列表
 * 接口异常时清空列表交给 el-empty 兜底，保证不白屏
 */
async function loadPets() {
  loading.value = true
  try {
    const res = await pagePets({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      type: query.type || undefined,
      status: query.status === '' ? undefined : query.status,
      keyword: query.keyword || undefined
    })
    pets.value = (res && res.records) || []
    total.value = (res && res.total) || 0
  } catch (e) {
    pets.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 搜索：回到第一页再查 */
function handleSearch() {
  query.pageNum = 1
  loadPets()
}

function handleReset() {
  query.pageNum = 1
  query.type = ''
  query.status = ''
  query.keyword = ''
  loadPets()
}

function handleSizeChange() {
  query.pageNum = 1
  loadPets()
}

function goDetail(pet) {
  router.push(`/user/pets/${pet.id}`)
}

onMounted(loadPets)
</script>

<style scoped lang="scss">
.user-pets {
  max-width: 1120px;
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

.filter-card {
  margin-bottom: 20px;
  border-radius: var(--user-card-radius);
  border: 1px solid var(--user-card-border);
  background-color: var(--user-card-bg);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.filter-item {
  width: 180px;
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
  min-height: 160px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>

