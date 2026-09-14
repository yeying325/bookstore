<template>
  <div class="detail-container">
    <!-- 顶部导航栏：回到书架 + 当前登录的用户 -->
    <header class="top-bar">
      <div class="bar-left">
        <button class="back-btn" @click="goHome">← 返回书架</button>
        <h2>书籍详情</h2>
      </div>
      <div class="user-area">
        <span v-if="isAdmin" class="role-badge">管理员模式</span>
        <span class="user-info">欢迎, {{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="detail-body">
      <p v-if="loading" class="state-tip">正在加载书籍信息...</p>
      <p v-else-if="loadError" class="state-tip error">{{ loadError }}</p>

      <template v-else-if="book">
        <!-- 1. 书籍信息 -->
        <section class="book-panel">
          <div class="book-cover-large" :style="{ background: book.coverColor }">
            <span class="cover-title">{{ book.title }}</span>
            <span class="cover-author">{{ book.author }}</span>
          </div>

          <div class="book-info-large">
            <h1 class="detail-title">{{ book.title }}</h1>
            <p class="detail-author">作者：{{ book.author }}</p>

            <div class="detail-flags">
              <span v-if="book.recommend" class="mini-flag">书店推荐</span>
              <span v-if="book.hot" class="mini-flag hot">热门</span>
              <span class="rating-chip">
                {{ starsText(summary.average) }}
                <b>{{ summary.count === 0 ? '暂无评分' : averageText + ' 分' }}</b>
              </span>
              <span class="review-count">{{ summary.count }} 条评价</span>
            </div>

            <div class="tag-list">
              <span class="tag-item" v-for="tag in book.tags" :key="tag">{{ tag }}</span>
              <span v-if="!book.tags || book.tags.length === 0" class="tag-empty">暂无标签</span>
            </div>

            <p class="detail-intro">{{ book.intro || '这本书还没有简介。' }}</p>

            <div class="detail-price-row">
              <span class="detail-price">{{ formatPrice(book.price) }}</span>
              <div class="detail-actions">
                <button class="fav-btn" :class="{ active: isFavorite }" @click="toggleFavorite">
                  {{ isFavorite ? '★ 已收藏' : '☆ 收藏' }}
                </button>
                <button class="buy-btn">加入购物车</button>
              </div>
            </div>

            <ul class="detail-facts">
              <li>
                <span class="fact-label">书籍编号</span>
                <span class="fact-value">#{{ book.id }}</span>
              </li>
              <li>
                <span class="fact-label">上架时间</span>
                <span class="fact-value">{{ formatTime(book.createTime) }}</span>
              </li>
              <li>
                <span class="fact-label">读者评分</span>
                <span class="fact-value">
                  {{ summary.count === 0 ? '还没有人评分' : averageText + ' / 5.0' }}
                </span>
              </li>
            </ul>
          </div>
        </section>

        <!-- 2. 评论区 -->
        <section class="review-section">
          <div class="section-head">
            <h4 class="section-title">读者评价</h4>
            <span class="section-hint">
              {{ isAdmin ? '当前是管理员模式，可以删除任意一条评价' : '每个账号对一本书可以写一条评价，再次提交会更新原来的评价' }}
            </span>
          </div>

          <!-- 评分统计：平均分 + 每个星级各有多少条 -->
          <div class="rating-summary">
            <div class="score-box">
              <span class="score-number">{{ summary.count === 0 ? '--' : averageText }}</span>
              <span class="score-stars">{{ starsText(summary.average) }}</span>
              <span class="score-count">共 {{ summary.count }} 条评价</span>
            </div>
            <div class="score-bars">
              <div class="score-bar-row" v-for="row in distributionRows" :key="row.star">
                <span class="bar-label">{{ row.star }} 星</span>
                <span class="bar-track">
                  <span class="bar-fill" :style="{ width: row.percent + '%' }"></span>
                </span>
                <span class="bar-count">{{ row.count }}</span>
              </div>
            </div>
          </div>

          <!-- 写评价 -->
          <div class="review-form">
            <p class="form-title">{{ myReview ? '修改我的评价' : '写下你的评价' }}</p>

            <div class="star-picker">
              <button
                v-for="star in 5"
                :key="star"
                class="star-btn"
                :class="{ on: star <= form.rating }"
                :title="star + ' 星'"
                @click="form.rating = star"
              >
                ★
              </button>
              <span class="star-tip">{{ RATING_WORDS[form.rating] }}</span>
            </div>

            <textarea
              v-model="form.content"
              class="review-input"
              rows="4"
              maxlength="500"
              placeholder="说说这本书哪里打动你，或者哪里不太喜欢…"
            ></textarea>

            <div class="form-foot">
              <span class="counter">{{ form.content.length }} / 500</span>
              <div class="form-buttons">
                <button v-if="myReview" class="admin-btn" :disabled="submitting" @click="resetForm">
                  清空重写
                </button>
                <button class="admin-btn primary" :disabled="submitting" @click="submitReview">
                  {{ submitting ? '保存中...' : (myReview ? '更新我的评价' : '发表评价') }}
                </button>
              </div>
            </div>

            <p v-if="formMsg" class="form-msg" :class="{ error: formMsgType === 'error' }">
              {{ formMsg }}
            </p>
          </div>

          <!-- 评价列表 -->
          <p v-if="reviews.length === 0" class="empty-tip">
            还没有人评价这本书，来写下第一条评价吧。
          </p>
          <div v-else class="review-list">
            <article class="review-item" v-for="review in reviews" :key="review.id">
              <div class="review-avatar">{{ avatarOf(review.username) }}</div>

              <div class="review-main">
                <div class="review-head">
                  <span class="review-user">{{ review.username }}</span>
                  <span class="review-stars" :title="review.rating + ' 星'">
                    {{ starsText(review.rating) }}
                  </span>
                  <span v-if="isMine(review)" class="mine-flag">我的评价</span>
                  <span class="review-time">
                    {{ formatTime(review.createTime) }}
                    <template v-if="isEdited(review)">（已修改）</template>
                  </span>
                </div>

                <p class="review-content">{{ review.content }}</p>

                <div v-if="canDelete(review)" class="review-foot">
                  <button class="admin-btn danger" @click="deleteReview(review)">
                    {{ isAdmin ? '删除评价（管理员）' : '删除我的评价' }}
                  </button>
                </div>
              </div>
            </article>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const API_BASE = 'http://localhost:8080/api'

// 路由里的书籍 id：/book/3 -> 3
const bookId = route.params.id

const username = ref(localStorage.getItem('username') || '用户')
const role = ref(localStorage.getItem('role') || 'user')
const isAdmin = computed(() => role.value === 'admin')
const adminToken = localStorage.getItem('adminToken') || ''
const adminHeaders = () => ({ 'X-Admin-Token': adminToken })

// 评分对应的文字说明
const RATING_WORDS = {
  1: '很差',
  2: '一般',
  3: '还行',
  4: '不错',
  5: '推荐'
}

/* ==================== 页面数据 ==================== */

const book = ref(null)
const reviews = ref([])
const summary = ref({ count: 0, average: 0, distribution: {} })
const loading = ref(true)
const loadError = ref('')

// 写评价的表单
const form = reactive({ rating: 5, content: '' })
const submitting = ref(false)
const formMsg = ref('')
const formMsgType = ref('info')

const averageText = computed(() => Number(summary.value.average || 0).toFixed(1))

// 评分分布：5 星到 1 星，每行显示条数和占比
const distributionRows = computed(() => {
  const distribution = summary.value.distribution || {}
  const total = summary.value.count || 0
  const rows = []
  for (let star = 5; star >= 1; star--) {
    const count = Number(distribution[star] || 0)
    rows.push({
      star,
      count,
      percent: total === 0 ? 0 : Math.round((count / total) * 100)
    })
  }
  return rows
})

/* ==================== 小工具 ==================== */

// 价格显示成 ¥38.00 的形式
const formatPrice = (price) => {
  const value = Number(price)
  return Number.isFinite(value) ? `¥${value.toFixed(2)}` : `¥${price}`
}

// 评分显示成 ★★★★☆ 的形式（四舍五入到整颗星）
const starsText = (rating) => {
  const value = Number(rating)
  const filled = Number.isFinite(value) ? Math.min(5, Math.max(0, Math.round(value))) : 0
  return '★'.repeat(filled) + '☆'.repeat(5 - filled)
}

// 后端返回的时间（2026-09-14T10:20:30）显示成 2026-09-14 10:20
const formatTime = (value) => {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

// 头像上显示用户名首字母
const avatarOf = (name) => (name || '用').trim().charAt(0).toUpperCase()

// 这条评价是不是当前登录用户写的
const isMine = (review) => (review.username || '').toLowerCase() === username.value.toLowerCase()

// 评价发出去之后又改过
const isEdited = (review) => {
  if (!review.updateTime || !review.createTime) {
    return false
  }
  return String(review.updateTime) !== String(review.createTime)
}

// 管理员可以删任意一条，普通用户只能删自己的
const canDelete = (review) => isAdmin.value || isMine(review)

// 当前用户在这本书下已经写过的评价
const myReview = computed(() => reviews.value.find(review => isMine(review)) || null)

/* ==================== 读取数据 ==================== */

// 书籍信息 + 评分统计：GET /api/books/{id}
const fetchBook = async () => {
  try {
    const response = await axios.get(`${API_BASE}/books/${bookId}`)
    if (response.data.code === 200) {
      book.value = response.data.data.book
      summary.value = response.data.data.reviewSummary || summary.value
    } else {
      loadError.value = response.data.message || '这本书不存在'
    }
  } catch (error) {
    console.error('获取书籍详情出错:', error)
    loadError.value = '书籍详情加载失败，请确认后端服务已启动'
  }
}

// 评价列表 + 评分统计：GET /api/books/{id}/reviews
const fetchReviews = async () => {
  try {
    const response = await axios.get(`${API_BASE}/books/${bookId}/reviews`)
    if (response.data.code === 200) {
      reviews.value = response.data.data.reviews || []
      summary.value = response.data.data.summary || summary.value
    } else {
      formMsg.value = response.data.message || '评价加载失败'
      formMsgType.value = 'error'
    }
  } catch (error) {
    console.error('获取评价出错:', error)
    formMsg.value = '评价加载失败：请确认已经执行过 sql/reviews.sql，并启动了后端服务'
    formMsgType.value = 'error'
  }
}

const loadPage = async () => {
  loading.value = true
  loadError.value = ''
  await Promise.all([fetchBook(), fetchReviews()])
  fillFormWithMyReview()
  loading.value = false
}

// 如果当前用户已经写过评价，就把内容填回表单，方便直接修改
const fillFormWithMyReview = () => {
  const mine = reviews.value.find(review => isMine(review))
  if (mine) {
    form.rating = mine.rating || 5
    form.content = mine.content || ''
  }
}

/* ==================== 写评价 / 删评价 ==================== */

const showFormMsg = (text, type = 'info') => {
  formMsg.value = text
  formMsgType.value = type
}

const submitReview = async () => {
  const content = form.content.trim()
  if (!content) {
    showFormMsg('评价内容不能为空', 'error')
    return
  }

  // 先记下这条评价是“新写的”还是“修改的”，保存后用来提示
  const hadReview = !!myReview.value

  submitting.value = true
  formMsg.value = ''
  try {
    const response = await axios.post(`${API_BASE}/books/${bookId}/reviews`, {
      username: username.value,
      rating: form.rating,
      content
    })

    if (response.data.code === 200) {
      await fetchReviews()
      fillFormWithMyReview()
      showFormMsg(hadReview ? '评价已更新' : '评价发表成功，感谢分享')
    } else {
      showFormMsg(response.data.message || '评价保存失败', 'error')
    }
  } catch (error) {
    console.error('发表评价出错:', error)
    showFormMsg(
      error.response?.data?.message || '评价保存失败，请确认后端服务已启动',
      'error'
    )
  } finally {
    submitting.value = false
  }
}

// 清空表单，相当于重新写一条（提交后会覆盖原来的评价）
const resetForm = () => {
  form.rating = 5
  form.content = ''
  formMsg.value = ''
}

// 管理员删任意一条，普通用户只能删自己的
const deleteReview = async (review) => {
  const tip = isAdmin.value
    ? `确定要删除 ${review.username} 的这条评价吗？`
    : '确定要删除自己写的这条评价吗？'
  if (!window.confirm(tip)) {
    return
  }

  try {
    const response = isAdmin.value
      ? await axios.delete(`${API_BASE}/admin/reviews/${review.id}`, { headers: adminHeaders() })
      : await axios.delete(`${API_BASE}/books/${bookId}/reviews/${review.id}`, {
          params: { username: username.value }
        })

    if (response.data.code === 200) {
      await fetchReviews()
      if (isMine(review)) {
        resetForm()
      }
      showFormMsg(isAdmin.value ? '评价已删除' : '你的评价已删除')
    } else {
      showFormMsg(response.data.message || '删除评价失败', 'error')
    }
  } catch (error) {
    console.error('删除评价出错:', error)
    showFormMsg(
      error.response?.data?.message || '删除评价失败，请确认后端服务已启动',
      'error'
    )
  }
}

/* ==================== 收藏（按账号保存在浏览器本地） ==================== */

const favoriteStorageKey = () => `bookstore_favorites_${username.value}`
const favoriteIds = ref([])
const isFavorite = computed(() => favoriteIds.value.includes(Number(bookId)))

const loadFavorites = () => {
  try {
    const saved = localStorage.getItem(favoriteStorageKey())
    favoriteIds.value = saved ? JSON.parse(saved) : []
  } catch {
    favoriteIds.value = []
  }
}

const toggleFavorite = () => {
  const id = Number(bookId)
  if (isFavorite.value) {
    favoriteIds.value = favoriteIds.value.filter(item => item !== id)
  } else {
    favoriteIds.value = [...favoriteIds.value, id]
  }
  localStorage.setItem(favoriteStorageKey(), JSON.stringify(favoriteIds.value))
}

/* ==================== 页面跳转 ==================== */

const goHome = () => {
  router.push('/home')
}

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('adminToken')
  router.push('/login')
}

onMounted(() => {
  loadFavorites()
  loadPage()
})
</script>

<style scoped>
/* 整体容器：和首页保持一致的排版，但用渐变色背景，保证详情页怎么看都清爽 */
.detail-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(160deg, #eef3f9 0%, #e6eef7 45%, #f6f1ea 100%);
  display: flex;
  flex-direction: column;
  color: #333;
}

/* 顶部导航栏 */
.top-bar {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-bar h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.back-btn {
  background-color: #fff;
  border: 1px solid #d5dbe3;
  color: #4a5568;
  padding: 7px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: #3498db;
  color: #2980b9;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  font-size: 14px;
  color: #666;
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.logout-btn:hover {
  background-color: #c0392b;
}

.role-badge {
  display: inline-block;
  background: linear-gradient(135deg, #e67e22, #d35400);
  color: #fff;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
}

/* 内容区 */
.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 26px 30px 40px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  align-items: center;
}

.state-tip {
  margin: 0;
  font-size: 14px;
  color: #7f8c8d;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 14px 18px;
  align-self: flex-start;
}

.state-tip.error {
  color: #e74c3c;
}

/* 书籍信息面板 */
.book-panel {
  width: 100%;
  max-width: 940px;
  background-color: #fff;
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  padding: 26px;
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.book-cover-large {
  width: 220px;
  height: 300px;
  border-radius: 10px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px;
  text-align: center;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.35);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
  flex-shrink: 0;
}

.cover-title {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.35;
}

.cover-author {
  font-size: 13px;
  opacity: 0.9;
}

.book-info-large {
  flex: 1;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-title {
  margin: 0;
  font-size: 26px;
  color: #2c3e50;
}

.detail-author {
  margin: 0;
  font-size: 14px;
  color: #7f8c8d;
}

.detail-flags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #7f8c8d;
}

.rating-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #fff8e6;
  border: 1px solid #f5d98b;
  border-radius: 999px;
  padding: 3px 12px;
  color: #b9770e;
  letter-spacing: 1px;
}

.rating-chip b {
  font-weight: 600;
  letter-spacing: 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  background-color: rgba(52, 152, 219, 0.12);
  color: #2471a3;
  border: 1px solid rgba(52, 152, 219, 0.25);
  border-radius: 999px;
  padding: 4px 14px;
  font-size: 13px;
}

.tag-empty {
  font-size: 12px;
  color: #bdc3c7;
}

.detail-intro {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: #4a5568;
  background-color: #f8f9fb;
  border-left: 3px solid #3498db;
  border-radius: 0 8px 8px 0;
  padding: 12px 16px;
}

.detail-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  border-top: 1px dashed #e3e6ea;
  padding-top: 14px;
}

.detail-price {
  color: #e74c3c;
  font-size: 26px;
  font-weight: bold;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

.fav-btn {
  background: #fff;
  border: 1px solid #ddd;
  color: #888;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.fav-btn:hover {
  border-color: #f0ad4e;
  color: #e67e22;
}

.fav-btn.active {
  background-color: #fff7e6;
  border-color: #f0ad4e;
  color: #e67e22;
}

.buy-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.buy-btn:hover {
  background-color: #2980b9;
}

.detail-facts {
  list-style: none;
  margin: 4px 0 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 26px;
  font-size: 13px;
}

.detail-facts li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fact-label {
  color: #a0a8b3;
}

.fact-value {
  color: #2c3e50;
}

/* 评论区 */
.review-section {
  width: 100%;
  max-width: 940px;
  background-color: rgba(255, 255, 255, 0.96);
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  padding: 24px 26px 28px;
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
  border-left: 4px solid #3498db;
  padding-left: 10px;
}

.section-hint {
  font-size: 12px;
  color: #7f8c8d;
}

/* 评分统计 */
.rating-summary {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
  background-color: #f8f9fb;
  border: 1px solid #eef0f4;
  border-radius: 12px;
  padding: 16px 22px;
  margin-bottom: 22px;
}

.score-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  border-right: 1px dashed #e0e5ec;
  padding-right: 24px;
}

.score-number {
  font-size: 38px;
  font-weight: bold;
  color: #e67e22;
  line-height: 1.1;
}

.score-stars {
  color: #f0ad4e;
  letter-spacing: 2px;
  font-size: 15px;
  margin: 4px 0;
}

.score-count {
  font-size: 12px;
  color: #7f8c8d;
}

.score-bars {
  flex: 1;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #7f8c8d;
}

.bar-label {
  width: 38px;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 8px;
  background-color: #e8ecf2;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #f0ad4e, #e67e22);
  border-radius: 999px;
  transition: width 0.3s;
}

.bar-count {
  width: 30px;
  text-align: right;
  flex-shrink: 0;
}

/* 写评价 */
.review-form {
  border: 1px solid #eef0f4;
  border-radius: 12px;
  padding: 16px 18px;
  margin-bottom: 24px;
}

.form-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.star-picker {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
}

.star-btn {
  border: none;
  background: none;
  font-size: 24px;
  line-height: 1;
  color: #dfe4ea;
  cursor: pointer;
  padding: 0 2px;
  transition: color 0.15s, transform 0.15s;
}

.star-btn:hover {
  transform: scale(1.12);
}

.star-btn.on {
  color: #f0ad4e;
}

.star-tip {
  margin-left: 10px;
  font-size: 13px;
  color: #7f8c8d;
}

.review-input {
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  outline: none;
}

.review-input:focus {
  border-color: #3498db;
}

.form-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.counter {
  font-size: 12px;
  color: #a0a8b3;
}

.form-buttons {
  display: flex;
  gap: 10px;
}

.form-msg {
  margin: 10px 0 0;
  font-size: 13px;
  color: #1e8449;
}

.form-msg.error {
  color: #c0392b;
}

/* 评价列表 */
.empty-tip {
  margin: 0;
  font-size: 13px;
  color: #999;
  padding: 6px 0;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-item {
  display: flex;
  gap: 14px;
  background-color: #fff;
  border: 1px solid #eef0f4;
  border-radius: 12px;
  padding: 14px 16px;
}

.review-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: bold;
}

.review-main {
  flex: 1;
  min-width: 0;
}

.review-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.review-user {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.review-stars {
  color: #f0ad4e;
  font-size: 13px;
  letter-spacing: 1px;
}

.mine-flag {
  font-size: 11px;
  color: #2471a3;
  background-color: rgba(52, 152, 219, 0.12);
  border: 1px solid rgba(52, 152, 219, 0.25);
  border-radius: 999px;
  padding: 1px 8px;
}

.review-time {
  font-size: 12px;
  color: #a0a8b3;
}

.review-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #4a5568;
  white-space: pre-wrap;
  word-break: break-word;
}

.review-foot {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eef0f4;
}

/* 按钮：和管理页面保持一致的样式 */
.admin-btn {
  border: 1px solid #d5dbe3;
  background-color: #fff;
  color: #4a5568;
  border-radius: 6px;
  padding: 7px 14px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.admin-btn:hover:not(:disabled) {
  border-color: #3498db;
  color: #2980b9;
}

.admin-btn.primary {
  background-color: #3498db;
  border-color: #3498db;
  color: #fff;
}

.admin-btn.primary:hover:not(:disabled) {
  background-color: #2980b9;
  color: #fff;
}

.admin-btn.danger {
  color: #c0392b;
  border-color: #f0b7b1;
}

.admin-btn.danger:hover:not(:disabled) {
  background-color: #fdecea;
  border-color: #e74c3c;
  color: #c0392b;
}

.admin-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
