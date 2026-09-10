<template>
  <div class="home-container">
    <!-- 1. 顶部导航栏 -->
    <header class="top-bar">
      <h2>网上书店</h2>
      <div class="user-area">
        <span class="user-info">欢迎, {{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <!-- 2. 主体内容区 -->
    <div class="main-content">
      
      <!-- 3. 左侧导航菜单 -->
      <aside class="sidebar">
        <div 
          class="menu-item" 
          :class="{ active: currentMenu === 'home' }"
          @click="currentMenu = 'home'"
        >
          首页
        </div>
        <div 
          class="menu-item" 
          :class="{ active: currentMenu === 'recommend' }"
          @click="currentMenu = 'recommend'"
        >
          推荐
        </div>
        <div 
          class="menu-item" 
          :class="{ active: currentMenu === 'hot' }"
          @click="currentMenu = 'hot'"
        >
          热门
        </div>
        
        <div 
          class="menu-item bottom-item" 
          :class="{ active: currentMenu === 'mine' }"
          @click="currentMenu = 'mine'"
        >
          我的
        </div>
      </aside>

      <!-- 4. 右侧内容展示区 -->
      <main class="content-area">
        <h3 class="area-title">{{ currentMenu === 'home' ? '全部书籍' : currentMenu === 'recommend' ? '为你推荐' : currentMenu === 'hot' ? '热门榜单' : '个人中心' }}</h3>
        
        <!-- 根据 currentMenu 的值，条件渲染不同的内容 -->
        
        <!-- 首页：展示全部书籍 -->
        <div v-if="currentMenu === 'home'" class="book-grid">
          <div class="book-card" v-for="book in books" :key="book.id">
            <div class="book-cover" :style="{ background: book.color }">{{ book.title }}</div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.intro }}</p>
              <div class="book-price">
                <span class="price-tag">{{ book.price }}</span>
                <div class="book-actions">
                  <button
                    class="fav-btn"
                    :class="{ active: isFavorite(book.id) }"
                    @click="toggleFavorite(book)"
                  >
                    {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                  </button>
                  <button class="buy-btn">加入购物车</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 推荐：只展示标记为 recommend 的书籍 -->
        <div v-else-if="currentMenu === 'recommend'" class="book-grid">
          <div class="book-card" v-for="book in recommendedBooks" :key="book.id">
            <div class="book-cover" :style="{ background: book.color }">{{ book.title }}</div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.intro }}</p>
              <div class="book-price">
                <span class="price-tag">{{ book.price }}</span>
                <div class="book-actions">
                  <button
                    class="fav-btn"
                    :class="{ active: isFavorite(book.id) }"
                    @click="toggleFavorite(book)"
                  >
                    {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                  </button>
                  <button class="buy-btn">加入购物车</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 热门：只展示标记为 hot 的书籍 -->
        <div v-else-if="currentMenu === 'hot'" class="book-grid">
          <div class="book-card" v-for="book in hotBooks" :key="book.id">
            <div class="book-cover" :style="{ background: book.color }">{{ book.title }}</div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.intro }}</p>
              <div class="book-price">
                <span class="price-tag">{{ book.price }}</span>
                <div class="book-actions">
                  <button
                    class="fav-btn"
                    :class="{ active: isFavorite(book.id) }"
                    @click="toggleFavorite(book)"
                  >
                    {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                  </button>
                  <button class="buy-btn">加入购物车</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 我的：个人资料与阅读偏好 -->
        <div v-else class="mine-page">
          <section class="profile-card">
            <div class="avatar">{{ avatarChar }}</div>

            <div class="profile-info">
              <h4 class="profile-name">{{ username }}</h4>
              <p class="profile-slogan">欢迎回来，愿好书常伴左右</p>
            </div>

            <div class="contact-panel">
              <div class="contact-row">
                <span class="contact-label">邮箱</span>
                <span v-if="profileLoading" class="contact-value loading">加载中...</span>
                <span v-else-if="profileError" class="contact-empty">--</span>
                <span v-else-if="profile && profile.emailMasked" class="contact-value">
                  {{ profile.emailMasked }}
                </span>
                <span v-else class="contact-empty">未绑定</span>
              </div>
              <div class="contact-row">
                <span class="contact-label">手机号</span>
                <span v-if="profileLoading" class="contact-value loading">加载中...</span>
                <span v-else-if="profileError" class="contact-empty">--</span>
                <span v-else-if="profile && profile.phoneMasked" class="contact-value">
                  {{ profile.phoneMasked }}
                </span>
                <span v-else class="contact-empty">未绑定</span>
              </div>
              <p class="privacy-tip">🔒 出于隐私保护，联系方式仅展示脱敏信息</p>
              <p v-if="profileError" class="profile-error">{{ profileError }}</p>
            </div>
          </section>

          <section class="profile-section">
            <h4 class="section-title">喜欢的书籍</h4>
            <p v-if="favoriteBooks.length === 0" class="empty-tip">
              还没有收藏书籍，在书架上点击“☆ 收藏”即可添加
            </p>
            <div v-else class="favorite-grid">
              <div class="favorite-item" v-for="book in favoriteBooks" :key="book.id">
                <div class="favorite-cover" :style="{ background: book.color }">{{ book.title }}</div>
                <div class="favorite-detail">
                  <p class="favorite-title">{{ book.title }}</p>
                  <p class="favorite-author">{{ book.author }}</p>
                </div>
              </div>
            </div>
          </section>

          <section class="profile-section">
            <h4 class="section-title">偏好标签</h4>
            <p v-if="favoriteTags.length === 0" class="empty-tip">
              收藏书籍后，这里会根据书籍类型自动生成标签
            </p>
            <div v-else class="tag-list">
              <span class="tag-item" v-for="tag in favoriteTags" :key="tag">{{ tag }}</span>
            </div>
          </section>
        </div>

      </main>
      
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')
const currentMenu = ref('home')

// “我的”页面需要的个人资料
const profile = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

// 用户收藏的书籍 id（按账号保存在浏览器本地）
const favoriteIds = ref([])

// 用户头像首字符
const avatarChar = computed(() => {
  return (username.value || '用').trim().charAt(0).toUpperCase()
})

// 已收藏的书籍
const favoriteBooks = computed(() => {
  return books.value.filter(book => favoriteIds.value.includes(book.id))
})

// 收藏书籍的标签去重合并
const favoriteTags = computed(() => {
  const tagSet = new Set()
  favoriteBooks.value.forEach(book => {
    (book.tags || []).forEach(tag => tagSet.add(tag))
  })
  return Array.from(tagSet)
})

// 从后端获取当前用户的脱敏资料
const fetchProfile = async () => {
  profileLoading.value = true
  profileError.value = ''
  try {
    const response = await axios.get('http://localhost:8080/api/user/profile', {
      params: { username: username.value }
    })
    if (response.data.code === 200) {
      profile.value = response.data.data
    } else {
      profileError.value = response.data.message || '获取个人资料失败'
    }
  } catch (error) {
    console.error('获取个人资料出错:', error)
    profileError.value = '个人资料加载失败，请确认后端服务已启动'
  } finally {
    profileLoading.value = false
  }
}

// 收藏数据的读取与写入（localStorage 按账号隔离）
const favoriteStorageKey = () => `bookstore_favorites_${username.value}`

const loadFavorites = () => {
  try {
    const saved = localStorage.getItem(favoriteStorageKey())
    favoriteIds.value = saved ? JSON.parse(saved) : []
  } catch {
    favoriteIds.value = []
  }
}

const isFavorite = (bookId) => favoriteIds.value.includes(bookId)

const toggleFavorite = (book) => {
  if (isFavorite(book.id)) {
    favoriteIds.value = favoriteIds.value.filter(id => id !== book.id)
  } else {
    favoriteIds.value = [...favoriteIds.value, book.id]
  }
  localStorage.setItem(favoriteStorageKey(), JSON.stringify(favoriteIds.value))
}

onMounted(() => {
  fetchProfile()
  loadFavorites()
})

// 书籍数据（增加了封面背景色和分类字段，方便模拟展示）
const books = ref([
  { 
    id: 1, 
    title: '三体', 
    author: '刘慈欣', 
    price: '¥38.00', 
    intro: '中国科幻文学里程碑之作。',
    color: '#5e81ac',
    tags: ['科幻', '中国文学'],
    category: ['home', 'recommend', 'hot']
  },
  { 
    id: 2, 
    title: '活着', 
    author: '余华', 
    price: '¥25.00', 
    intro: '讲述了人是为了活着本身而活着的。',
    color: '#a3be8c',
    tags: ['文学', '人生'],
    category: ['home', 'recommend']
  },
  { 
    id: 3, 
    title: '百年孤独', 
    author: '马尔克斯', 
    price: '¥55.00', 
    intro: '魔幻现实主义文学的代表作。',
    color: '#ebcb8b',
    tags: ['文学', '经典'],
    category: ['home', 'hot']
  },
  { 
    id: 4, 
    title: '解忧杂货店', 
    author: '东野圭吾', 
    price: '¥42.00', 
    intro: '现代人内心流失的东西，这家杂货店能帮你找回。',
    color: '#bf616a',
    tags: ['治愈', '推理'],
    category: ['home', 'recommend', 'hot']
  },
  { 
    id: 5, 
    title: '你当像鸟飞往你的山', 
    author: '塔拉·韦斯特弗', 
    price: '¥49.00', 
    intro: '教育意味着获得不同的视角，理解不同的人。',
    color: '#b48ead',
    tags: ['成长', '传记'],
    category: ['home']
  }
])

// 使用 computed 计算属性，自动过滤出“推荐”和“热门”的书籍
const recommendedBooks = computed(() => {
  return books.value.filter(book => book.category.includes('recommend'))
})

const hotBooks = computed(() => {
  return books.value.filter(book => book.category.includes('hot'))
})

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
/* 整体容器 */
.home-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 注意：确保 public/images/ 下有 bookstore.png，否则背景可能是纯色或报错 */
  background: url('/images/bookstore.png') no-repeat center center fixed;
  background-size: cover;
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
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  z-index: 10;
}

.top-bar h2 {
  margin: 0;
  color: #2c3e50;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 20px;
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

/* 主体内容区 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧侧边栏 */
.sidebar {
  width: 200px;
  background-color: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  padding: 20px 0;
  border-right: 1px solid #e0e0e0;
}

.menu-item {
  padding: 18px 30px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
  color: #555;
}

.menu-item:hover {
  background-color: #f0f2f5;
  color: #3498db;
}

.menu-item.active {
  background-color: #3498db;
  color: white;
}

.bottom-item {
  margin-top: auto;
  border-top: 1px solid #e0e0e0;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto; /* 允许内容区内部滚动 */
}

.area-title {
  margin-top: 0;
  margin-bottom: 25px;
  font-size: 22px;
  color: #333;
  border-bottom: 2px solid #3498db;
  display: inline-block;
  padding-bottom: 8px;
}

/* --- 书籍卡片网格布局 --- */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); /* 响应式多列 */
  gap: 25px;
}

.book-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.book-cover {
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.3);
}

.book-info {
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  color: #2c3e50;
}

.book-author {
  margin: 0 0 10px;
  font-size: 13px;
  color: #7f8c8d;
}

.book-intro {
  margin: 0 0 15px;
  font-size: 13px;
  color: #555;
  line-height: 1.5;
  flex: 1;
}

.book-price {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  border-top: 1px solid #eee;
  padding-top: 12px;
}

.price-tag {
  color: #e74c3c;
  font-size: 16px;
  font-weight: bold;
}

.buy-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.buy-btn:hover {
  background-color: #2980b9;
}

/* 卡片上的收藏按钮 */
.book-actions {
  display: flex;
  width: 100%;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.fav-btn {
  background: #fff;
  border: 1px solid #ddd;
  color: #888;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
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

/* --- “我的”页面 --- */
.mine-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
  max-width: 900px;
}

.profile-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 26px 30px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.07);
  display: flex;
  align-items: center;
  gap: 26px;
  flex-wrap: wrap;
}

.avatar {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 150px;
}

.profile-name {
  margin: 0 0 6px;
  font-size: 20px;
  color: #2c3e50;
}

.profile-slogan {
  margin: 0;
  font-size: 13px;
  color: #7f8c8d;
}

.contact-panel {
  background-color: #f8f9fb;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 14px 20px;
  min-width: 280px;
}

.contact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
}

.contact-label {
  color: #888;
}

.contact-value {
  color: #2c3e50;
  font-family: Consolas, Monaco, monospace;
  letter-spacing: 0.5px;
}

.contact-value.loading {
  color: #aaa;
}

.contact-empty {
  color: #bdc3c7;
}

.privacy-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #b0b8c4;
  border-top: 1px dashed #e3e6ea;
  padding-top: 8px;
}

.profile-error {
  margin: 8px 0 0;
  font-size: 12px;
  color: #e74c3c;
}

.profile-section {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 22px 26px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.06);
}

.section-title {
  margin: 0 0 18px;
  font-size: 17px;
  color: #2c3e50;
  border-left: 4px solid #3498db;
  padding-left: 10px;
}

.empty-tip {
  margin: 0;
  font-size: 13px;
  color: #999;
  padding: 12px 0;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 16px;
}

.favorite-item {
  background-color: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
}

.favorite-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
}

.favorite-cover {
  height: 92px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: bold;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.3);
  padding: 0 8px;
  text-align: center;
}

.favorite-detail {
  padding: 10px 12px;
}

.favorite-title {
  margin: 0 0 4px;
  font-size: 14px;
  color: #2c3e50;
}

.favorite-author {
  margin: 0;
  font-size: 12px;
  color: #7f8c8d;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  background-color: rgba(52, 152, 219, 0.12);
  color: #2471a3;
  border: 1px solid rgba(52, 152, 219, 0.25);
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
}
</style>
