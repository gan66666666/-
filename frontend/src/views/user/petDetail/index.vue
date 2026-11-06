<template>
  <div class="pet-detail">
    <el-button link type="primary" class="back-button" @click="goBack">
      <el-icon>
        <ArrowLeft />
      </el-icon>
      <span>返回列表</span>
    </el-button>

    <div v-loading="loading" class="detail-wrap">
      <template v-if="pet">
        <div class="detail-card">
          <!-- 大图 -->
          <div class="detail-cover" :style="{ backgroundColor: coverColor }">
            <img v-if="pet.avatar" class="cover-photo" :src="pet.avatar" :alt="pet.name" />
            <span v-else class="cover-emoji">{{ coverIcon }}</span>
          </div>

          <!-- 资料 -->
          <div class="detail-body">
            <div class="detail-title-row">
              <h1 class="detail-title">{{ pet.name }}</h1>
              <el-tag :type="pet.status === 1 ? 'success' : 'info'" effect="light">
                {{ statusText }}
              </el-tag>
            </div>

            <div class="detail-tags">
              <span class="detail-tag">{{ pet.type }}</span>
              <span class="detail-tag">{{ formatPetBreed(pet.breed) }}</span>
              <span class="detail-tag">{{ formatPetAge(pet.age) }}</span>
              <span class="detail-tag">{{ formatPetGender(pet.gender) }}</span>
            </div>

            <el-descriptions :column="1" border class="detail-desc">
              <el-descriptions-item label="宠物类型">{{ pet.type }}</el-descriptions-item>
              <el-descriptions-item label="品种">{{ formatPetBreed(pet.breed) }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ formatPetAge(pet.age) }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ formatPetGender(pet.gender) }}</el-descriptions-item>
              <el-descriptions-item label="健康状况">{{ pet.health || '暂无记录' }}</el-descriptions-item>
              <el-descriptions-item label="领养状态">{{ statusText }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 底部操作条 -->
        <div class="detail-footer">
          <el-button @click="goBack">再看看别的</el-button>
          <el-button type="primary" :disabled="pet.status !== 1" @click="handleApply">
            {{ pet.status === 1 ? '申请领养' : '暂不可领养' }}
          </el-button>
        </div>
      </template>

      <el-empty
        v-if="!loading && !pet"
        description="没有找到这只宠物"
        :image-size="110"
      >
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>

    <!-- 申请领养弹窗 -->
    <ApplyDialog v-model="applyVisible" :pet="pet" @success="handleApplySuccess" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPet } from '../../../api/user/pet'
import { getToken } from '../../../utils/userAuth'
import {
  PET_STATUS_TEXT,
  PET_TYPE_COLORS,
  PET_TYPE_ICONS,
  formatPetAge,
  formatPetBreed,
  formatPetGender
} from '../../../utils/petView'
import ApplyDialog from '../components/ApplyDialog.vue'

const route = useRoute()
const router = useRouter()

const pet = ref(null)
const loading = ref(false)
const applyVisible = ref(false)

const petId = computed(() => route.params.id)

const coverIcon = computed(() => PET_TYPE_ICONS[pet.value?.type] || PET_TYPE_ICONS['其他'])

const coverColor = computed(() => PET_TYPE_COLORS[pet.value?.type] || PET_TYPE_COLORS['其他'])

const statusText = computed(() => PET_STATUS_TEXT[pet.value?.status] || '状态未知')

/**
 * 加载宠物详情
 */
async function loadPet() {
  loading.value = true
  try {
    pet.value = await getPet(petId.value)
  } catch (e) {
    // 详情拉取失败时展示空状态，避免白屏
    pet.value = null
  } finally {
    loading.value = false
  }
}

/**
 * 点击「申请领养」：未登录先引导登录，已登录打开申请弹窗
 */
function handleApply() {
  if (!pet.value || pet.value.status !== 1) {
    return
  }
  if (!getToken()) {
    ElMessage.warning('请先登录后再提交领养申请')
    router.push('/user/login')
    return
  }
  applyVisible.value = true
}

function handleApplySuccess() {
  router.push('/user/myapply')
}

function goBack() {
  router.push('/user/pets')
}

onMounted(loadPet)
</script>

<style scoped lang="scss">
.pet-detail {
  max-width: 940px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 4px;
}

.detail-wrap {
  min-height: 320px;
}

.detail-card {
  display: flex;
  gap: 28px;
  padding: 24px;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
  box-shadow: var(--user-card-shadow);
}

.detail-cover {
  width: 320px;
  height: 320px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 10px;
}

.cover-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-emoji {
  font-size: 96px;
  line-height: 1;
}

.detail-body {
  flex: 1;
  min-width: 0;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--user-title-color);
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.detail-tag {
  padding: 3px 10px;
  font-size: 12px;
  color: var(--user-text-muted);
  background-color: var(--user-primary-soft);
  border-radius: 6px;
}

.detail-desc {
  border-radius: 10px;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding: 16px 20px;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
}

@media screen and (max-width: 760px) {
  .detail-card {
    flex-direction: column;
  }

  .detail-cover {
    width: 100%;
    height: 240px;
  }
}
</style>

