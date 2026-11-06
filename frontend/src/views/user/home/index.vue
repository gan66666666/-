<template>
  <div class="user-home">
    <!-- 欢迎区：整幅照片做底，浅色渐变压住画面保证文字清晰 -->
    <section class="home-hero">
      <div class="hero-inner">
        <p class="hero-eyebrow">领养代替购买</p>
        <h1 class="hero-title">给它一个家，也给自己一份治愈</h1>
        <p class="hero-desc">
          每一次领养，都是一次双向的选择。它们会带来陪伴，你也会收获一份被需要的温暖。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="go('/user/pets')">看看待领养的小家伙</el-button>
          <el-button size="large" @click="go('/user/notices')">最新公告</el-button>
        </div>
      </div>
    </section>

    <!-- 可领养宠物推荐 -->
    <section class="home-section">
      <div class="section-head">
        <h2 class="section-title">正在等待领养</h2>
        <el-link type="primary" :underline="false" @click="go('/user/pets')">
          查看全部<el-icon class="link-icon"><ArrowRight /></el-icon>
        </el-link>
      </div>

      <div v-loading="petLoading" class="pet-grid">
        <PetCard v-for="pet in pets" :key="pet.id" :pet="pet" @detail="goPetDetail" />
      </div>

      <el-empty
        v-if="!petLoading && pets.length === 0"
        description="暂时没有可领养的宠物，过几天再来看看"
        :image-size="90"
      />
    </section>

    <!-- 最新公告 -->
    <section class="home-section">
      <div class="section-head">
        <h2 class="section-title">最新公告</h2>
        <el-link type="primary" :underline="false" @click="go('/user/notices')">
          更多公告<el-icon class="link-icon"><ArrowRight /></el-icon>
        </el-link>
      </div>

      <div v-loading="noticeLoading" class="notice-list">
        <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="goNoticeDetail(notice.id)">
          <div class="notice-main">
            <div class="notice-title-row">
              <el-tag v-if="notice.isTop === 1" type="warning" size="small" effect="light">置顶</el-tag>
              <span class="notice-title">{{ notice.title }}</span>
            </div>
            <p class="notice-summary">{{ notice.content }}</p>
          </div>
          <span class="notice-time">{{ formatDateTime(notice.createTime) }}</span>
        </div>
      </div>

      <el-empty
        v-if="!noticeLoading && notices.length === 0"
        description="暂时还没有公告"
        :image-size="90"
      />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pagePets } from '../../../api/user/pet'
import { pageNotices } from '../../../api/user/notice'
import { formatDateTime } from '../../../utils/petView'
import PetCard from '../components/PetCard.vue'

const router = useRouter()

const pets = ref([])
const notices = ref([])
const petLoading = ref(false)
const noticeLoading = ref(false)

/**
 * 加载可领养宠物：只取 status=1 的，按分页接口取前 8 条
 */
async function loadPets() {
  petLoading.value = true
  try {
    const res = await pagePets({ pageNum: 1, pageSize: 8, status: 1 })
    pets.value = (res && res.records) || []
  } catch (e) {
    // 接口异常时保持空列表，由 el-empty 兜底，避免白屏
    pets.value = []
  } finally {
    petLoading.value = false
  }
}

/**
 * 加载最新公告：后端已按置顶优先、创建时间倒序排序
 */
async function loadNotices() {
  noticeLoading.value = true
  try {
    const res = await pageNotices({ pageNum: 1, pageSize: 5 })
    notices.value = (res && res.records) || []
  } catch (e) {
    notices.value = []
  } finally {
    noticeLoading.value = false
  }
}

function go(path) {
  router.push(path)
}

function goPetDetail(pet) {
  router.push(`/user/pets/${pet.id}`)
}

function goNoticeDetail(id) {
  router.push(`/user/notices/${id}`)
}

onMounted(() => {
  loadPets()
  loadNotices()
})
</script>

<style scoped lang="scss">
.user-home {
  max-width: 1120px;
  margin: 0 auto;
}

/* 欢迎区：整幅照片铺底 + 浅色渐变，保证标题始终清晰 */
.home-hero {
  position: relative;
  height: 300px;
  border-radius: var(--user-card-radius);
  overflow: hidden;
  background-color: #eef5eb;
  background-image: url('/login-bg.jpg');
  background-size: cover;
  background-position: center 42%;
}

.home-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    100deg,
    rgba(250, 249, 246, 0.96) 0%,
    rgba(250, 249, 246, 0.85) 38%,
    rgba(250, 249, 246, 0.35) 70%,
    rgba(250, 249, 246, 0.2) 100%
  );
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 520px;
  padding: 48px 44px;
}

.hero-eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 3px;
  color: var(--user-primary-dark);
}

.hero-title {
  margin: 0 0 14px;
  font-size: 30px;
  line-height: 1.4;
  font-weight: 700;
  color: var(--user-title-color);
}

.hero-desc {
  margin: 0 0 24px;
  font-size: 14px;
  line-height: 1.9;
  color: var(--user-text-color);
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.home-section {
  margin-top: 32px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--user-title-color);
}

.link-icon {
  margin-left: 4px;
  vertical-align: -2px;
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 80px;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background-color: var(--user-card-bg);
  border: 1px solid var(--user-card-border);
  border-radius: var(--user-card-radius);
  box-shadow: var(--user-card-shadow);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: var(--user-card-shadow-hover);
    transform: translateY(-2px);
  }
}

.notice-main {
  min-width: 0;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--user-title-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-summary {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--user-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--user-text-faint);
}

@media screen and (max-width: 640px) {
  .hero-inner {
    padding: 36px 24px;
  }

  .hero-title {
    font-size: 24px;
  }

  .notice-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>

