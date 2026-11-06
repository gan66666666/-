<template>
  <div class="pet-card" @click="emit('detail', pet)">
    <div class="pet-cover" :style="{ backgroundColor: coverColor }">
      <img v-if="pet.avatar" class="pet-photo" :src="pet.avatar" :alt="pet.name" />
      <span v-else class="pet-emoji">{{ coverIcon }}</span>
      <span class="pet-status" :class="statusClass">{{ statusText }}</span>
    </div>

    <div class="pet-body">
      <h3 class="pet-name">{{ pet.name }}</h3>
      <p class="pet-meta">{{ pet.type }} · {{ formatPetBreed(pet.breed) }}</p>

      <div class="pet-tags">
        <span class="pet-tag">{{ formatPetAge(pet.age) }}</span>
        <span class="pet-tag">{{ formatPetGender(pet.gender) }}</span>
      </div>

      <div class="pet-footer">
        <el-button type="primary" plain size="small" :disabled="pet.status !== 1" @click.stop="handleAction">
          {{ pet.status === 1 ? '查看详情' : '已领养' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  PET_STATUS_TEXT,
  PET_TYPE_COLORS,
  PET_TYPE_ICONS,
  formatPetAge,
  formatPetBreed,
  formatPetGender
} from '../../../utils/petView'

const props = defineProps({
  /** 宠物对象 */
  pet: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['detail'])

/** 没有照片时用类型图标占位 */
const coverIcon = computed(() => PET_TYPE_ICONS[props.pet.type] || PET_TYPE_ICONS['其他'])

const coverColor = computed(() => PET_TYPE_COLORS[props.pet.type] || PET_TYPE_COLORS['其他'])

const statusText = computed(() => PET_STATUS_TEXT[props.pet.status] || '状态未知')

const statusClass = computed(() => `is-${props.pet.status}`)

/**
 * 已领养的宠物不再跳详情，按钮保持置灰
 */
function handleAction() {
  if (props.pet.status !== 1) {
    return
  }
  emit('detail', props.pet)
}
</script>

<style scoped lang="scss">
.pet-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
  box-shadow: var(--user-card-shadow);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: var(--user-card-shadow-hover);
    transform: translateY(-4px);
  }
}

.pet-cover {
  position: relative;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pet-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-emoji {
  font-size: 56px;
  line-height: 1;
}

.pet-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 2px 10px;
  font-size: 12px;
  color: #ffffff;
  background-color: var(--user-primary);
  border-radius: 20px;

  &.is-2 {
    background-color: #b9b9b1;
  }

  &.is-3 {
    background-color: var(--user-accent);
  }
}

.pet-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 14px 16px 16px;
}

.pet-name {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: var(--user-title-color);
}

.pet-meta {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--user-primary-dark);
}

.pet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.pet-tag {
  padding: 2px 8px;
  font-size: 12px;
  color: var(--user-text-muted);
  background-color: #f7f7f4;
  border-radius: 6px;
}

.pet-footer {
  margin-top: auto;

  .el-button {
    width: 100%;
  }
}
</style>

