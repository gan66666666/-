<template>
  <div class="user-apply">
    <h1 class="page-title">领养申请</h1>
    <p class="page-desc">先选一只中意的宠物，再填写领养理由提交，管理员会尽快帮你审核。</p>

    <el-card class="picker-card" shadow="never">
      <div class="picker-head">
        <span class="picker-label">选择想领养的宠物</span>
        <span class="picker-tip">共 {{ total }} 只可领养</span>
      </div>

      <div v-loading="loading" class="picker-list">
        <div
          v-for="pet in pets"
          :key="pet.id"
          class="picker-item"
          :class="{ 'is-active': current && current.id === pet.id }"
          @click="current = pet"
        >
          <span class="picker-emoji" :style="{ backgroundColor: coverColor(pet) }">
            <img v-if="pet.avatar" :src="pet.avatar" :alt="pet.name" class="picker-photo" />
            <template v-else>{{ PET_TYPE_ICONS[pet.type] || PET_TYPE_ICONS['其他'] }}</template>
          </span>
          <span class="picker-info">
            <span class="picker-name">{{ pet.name }}</span>
            <span class="picker-meta">{{ pet.type }} · {{ formatPetBreed(pet.breed) }}</span>
          </span>
          <el-icon v-if="current && current.id === pet.id" class="picker-check">
            <CircleCheckFilled />
          </el-icon>
        </div>
      </div>

      <el-empty
        v-if="!loading && pets.length === 0"
        description="暂时没有可领养的宠物"
        :image-size="90"
      />

      <div class="picker-actions">
        <el-button type="primary" :disabled="!current" @click="applyVisible = true">
          填写申请信息
        </el-button>
      </div>
    </el-card>

    <ApplyDialog v-model="applyVisible" :pet="current" @success="handleSuccess" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pagePets } from '../../../api/user/pet'
import { PET_TYPE_COLORS, PET_TYPE_ICONS, formatPetBreed } from '../../../utils/petView'
import ApplyDialog from '../components/ApplyDialog.vue'

const router = useRouter()

const pets = ref([])
const total = ref(0)
const loading = ref(false)
const current = ref(null)
const applyVisible = ref(false)

/**
 * 加载可领养宠物供选择
 */
async function loadPets() {
  loading.value = true
  try {
    const res = await pagePets({ pageNum: 1, pageSize: 20, status: 1 })
    pets.value = (res && res.records) || []
    total.value = (res && res.total) || 0
  } catch (e) {
    pets.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function coverColor(pet) {
  return PET_TYPE_COLORS[pet.type] || PET_TYPE_COLORS['其他']
}

function handleSuccess() {
  router.push('/user/myapply')
}

onMounted(loadPets)
</script>

<style scoped lang="scss">
.user-apply {
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

.picker-card {
  border-radius: var(--user-card-radius);
  border: 1px solid var(--user-card-border);
  background-color: var(--user-card-bg);
}

.picker-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.picker-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--user-title-color);
}

.picker-tip {
  font-size: 12px;
  color: var(--user-text-faint);
}

.picker-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  min-height: 100px;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background-color: #fbfbf8;
  border: 1px solid var(--user-card-border);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: var(--user-primary-light-5);
  }

  &.is-active {
    border-color: var(--user-primary);
    box-shadow: 0 0 0 2px rgba(135, 196, 124, 0.16);
  }
}

.picker-emoji {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-size: 22px;
  border-radius: 8px;
}

.picker-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.picker-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.picker-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--user-title-color);
}

.picker-meta {
  font-size: 12px;
  color: var(--user-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picker-check {
  margin-left: auto;
  font-size: 18px;
  color: var(--user-primary);
}

.picker-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>

