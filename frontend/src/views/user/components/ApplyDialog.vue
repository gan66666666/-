<template>
  <el-dialog
    :model-value="modelValue"
    title="提交领养申请"
    width="520px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <div v-if="pet" class="apply-pet">
      <span class="apply-pet-emoji">{{ petIcon }}</span>
      <div class="apply-pet-info">
        <span class="apply-pet-name">{{ pet.name }}</span>
        <span class="apply-pet-meta">{{ pet.type }} · {{ formatPetBreed(pet.breed) }}</span>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="领养人姓名" prop="applicantName">
        <el-input v-model.trim="form.applicantName" placeholder="请输入领养人姓名" clearable />
      </el-form-item>

      <el-form-item label="联系电话" prop="phone">
        <el-input v-model.trim="form.phone" placeholder="请输入联系电话" clearable />
      </el-form-item>

      <el-form-item label="申请理由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="请简单介绍你的养宠条件与领养理由，至少 10 个字"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createApply } from '../../../api/user/apply'
import { getUserInfo } from '../../../api/user/auth'
import { getUser } from '../../../utils/userAuth'
import { PET_TYPE_ICONS, formatPetBreed } from '../../../utils/petView'

const props = defineProps({
  /** 弹窗显隐 */
  modelValue: {
    type: Boolean,
    default: false
  },
  /** 申请的宠物 */
  pet: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  applicantName: '',
  phone: '',
  reason: ''
})

const rules = {
  applicantName: [{ required: true, message: '请输入领养人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请填写申请理由', trigger: 'blur' },
    { min: 10, message: '申请理由至少 10 个字', trigger: 'blur' }
  ]
}

const petIcon = computed(() => PET_TYPE_ICONS[props.pet?.type] || PET_TYPE_ICONS['其他'])

/**
 * 打开弹窗时预填当前用户的昵称与手机号
 * 昵称来自登录时缓存，手机号需要单独调接口取
 */
async function handleOpen() {
  const cached = getUser()
  form.applicantName = cached?.nickname || cached?.username || ''
  form.phone = cached?.phone || ''
  form.reason = ''
  formRef.value?.clearValidate()

  try {
    const info = await getUserInfo()
    if (info && info.phone) {
      form.phone = info.phone
    }
    if (info && !form.applicantName) {
      form.applicantName = info.nickname || info.username || ''
    }
  } catch (e) {
    // 资料拉取失败不阻塞填写，用户可以手动输入
  }
}

/**
 * 提交申请
 */
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  if (!props.pet) {
    ElMessage.warning('请先选择要领养的宠物')
    return
  }

  submitting.value = true
  try {
    await createApply({
      applicantName: form.applicantName,
      phone: form.phone,
      petId: props.pet.id,
      petName: props.pet.name,
      reason: form.reason
    })
    ElMessage.success('提交成功，请等待审核')
    emit('update:modelValue', false)
    emit('success')
  } catch (e) {
    // 错误提示已由 axios 响应拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.apply-pet {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  padding: 12px 14px;
  background-color: var(--user-primary-soft);
  border: 1px solid var(--user-primary-soft-border);
  border-radius: 10px;
}

.apply-pet-emoji {
  font-size: 30px;
  line-height: 1;
}

.apply-pet-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.apply-pet-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--user-title-color);
}

.apply-pet-meta {
  font-size: 12px;
  color: var(--user-text-muted);
}
</style>

