<template>
  <div class="home-container">
    <!-- 1. 顶部导航栏 -->
    <header class="top-bar">
      <h2>网上书店</h2>
      <div class="user-area">
        <span v-if="isAdmin" class="role-badge">管理员模式</span>
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

        <!-- 书籍数据加载状态：数据来自后端 MySQL 的 books 表 -->
        <p v-if="booksLoading && currentMenu !== 'mine'" class="state-tip">正在从数据库加载书籍...</p>
        <p v-else-if="booksError && currentMenu !== 'mine'" class="state-tip error">{{ booksError }}</p>
        <template v-else>

        <!-- 首页：展示全部书籍 -->
        <div v-if="currentMenu === 'home'" class="book-grid">
          <div
            class="book-card clickable"
            v-for="book in displayBooks"
            :key="book.id"
            title="点击查看书籍详情"
            @click="openBookDetail(book)"
          >
            <div class="book-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.intro }}</p>
              <div class="book-price">
                <span class="price-tag">{{ formatPrice(book.price) }}</span>
                <div class="book-actions">
                  <button
                    class="fav-btn"
                    :class="{ active: isFavorite(book.id) }"
                    @click.stop="toggleFavorite(book)"
                  >
                    {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                  </button>
                  <button class="buy-btn" @click.stop>加入购物车</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 推荐：根据「我的」页面里自己设置的偏好标签来推荐书籍 -->
        <div v-else-if="currentMenu === 'recommend'" class="recommend-page">
          <!-- 情况一：还没有设置任何偏好标签 -->
          <div v-if="preferenceTags.length === 0" class="empty-block">
            <p class="empty-tip">
              还没有设置偏好标签。到「我的 → 偏好标签」里添加自己喜欢的标签，这里就会推荐相关的书籍。
            </p>
            <button class="admin-btn primary" @click="currentMenu = 'mine'">去设置偏好标签</button>
          </div>

          <!-- 情况二：设置了标签，但书架上没有匹配的书籍 -->
          <p v-else-if="recommendedBooks.length === 0" class="empty-tip">
            暂时没有和你的偏好标签匹配的书籍，可以回「我的」页面再添加几个标签试试。
          </p>

          <!-- 情况三：按偏好标签推荐的书籍 -->
          <template v-else>
            <p class="recommend-tip">
              根据你的偏好标签：
              <span class="tag-item tag-small" v-for="tag in preferenceTags" :key="tag">{{ tag }}</span>
            </p>
            <div class="book-grid">
              <div
                class="book-card clickable"
                v-for="book in recommendedBooks"
                :key="book.id"
                title="点击查看书籍详情"
                @click="openBookDetail(book)"
              >
                <div class="book-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
                <div class="book-info">
                  <h4 class="book-title">{{ book.title }}</h4>
                  <p class="book-author">{{ book.author }}</p>
                  <p class="book-intro">{{ book.intro }}</p>
                  <!-- 展示这本书命中了哪些偏好标签 -->
                  <div class="tag-list matched-tags">
                    <span class="tag-item tag-small" v-for="tag in matchedTags(book)" :key="tag">
                      {{ tag }}
                    </span>
                  </div>
                  <div class="book-price">
                    <span class="price-tag">{{ formatPrice(book.price) }}</span>
                    <div class="book-actions">
                      <button
                        class="fav-btn"
                        :class="{ active: isFavorite(book.id) }"
                        @click.stop="toggleFavorite(book)"
                      >
                        {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                      </button>
                      <button class="buy-btn" @click.stop>加入购物车</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- 书店推荐：管理员在书架上勾选“放到推荐”的书籍 -->
          <section v-if="shopRecommendedBooks.length > 0" class="recommend-block">
            <h4 class="section-title">书店推荐</h4>
            <div class="book-grid">
              <div
                class="book-card clickable"
                v-for="book in shopRecommendedBooks"
                :key="book.id"
                title="点击查看书籍详情"
                @click="openBookDetail(book)"
              >
                <div class="book-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
                <div class="book-info">
                  <h4 class="book-title">{{ book.title }}</h4>
                  <p class="book-author">{{ book.author }}</p>
                  <p class="book-intro">{{ book.intro }}</p>
                  <div class="book-price">
                    <span class="price-tag">{{ formatPrice(book.price) }}</span>
                    <div class="book-actions">
                      <button
                        class="fav-btn"
                        :class="{ active: isFavorite(book.id) }"
                        @click.stop="toggleFavorite(book)"
                      >
                        {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                      </button>
                      <button class="buy-btn" @click.stop>加入购物车</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
        
        <!-- 热门：只展示标记为 hot 的书籍 -->
        <div v-else-if="currentMenu === 'hot'" class="book-grid">
          <div
            class="book-card clickable"
            v-for="book in hotBooks"
            :key="book.id"
            title="点击查看书籍详情"
            @click="openBookDetail(book)"
          >
            <div class="book-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-intro">{{ book.intro }}</p>
              <div class="book-price">
                <span class="price-tag">{{ formatPrice(book.price) }}</span>
                <div class="book-actions">
                  <button
                    class="fav-btn"
                    :class="{ active: isFavorite(book.id) }"
                    @click.stop="toggleFavorite(book)"
                  >
                    {{ isFavorite(book.id) ? '★ 已收藏' : '☆ 收藏' }}
                  </button>
                  <button class="buy-btn" @click.stop>加入购物车</button>
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
              <h4 class="profile-name">
                {{ username }}
                <span v-if="isAdmin" class="role-badge">管理员</span>
              </h4>
              <p class="profile-slogan">欢迎回来，愿好书常伴左右</p>
            </div>

            <div class="contact-panel">
              <div class="contact-head">
                <span class="contact-title">联系方式</span>
                <button
                  v-if="!editingContact"
                  class="admin-btn contact-edit-btn"
                  :disabled="contactLoading"
                  @click="startEditContact"
                >
                  {{ contactLoading ? '加载中...' : '✎ 修改' }}
                </button>
              </div>

              <!-- 展示模式：只显示脱敏后的联系方式 -->
              <template v-if="!editingContact">
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
                <p class="privacy-tip">🔒 平时只展示脱敏信息，点「修改」即可更新邮箱和手机号</p>
              </template>

              <!-- 编辑模式：注册时没填的，也可以在这里补上 -->
              <template v-else>
                <div class="contact-edit-row">
                  <label>邮箱</label>
                  <input v-model="contactForm.email" type="email" placeholder="选填，如 tom@qq.com" />
                </div>
                <div class="contact-edit-row">
                  <label>手机号</label>
                  <input v-model="contactForm.phone" type="text" placeholder="选填，11 位手机号" />
                </div>
                <p class="contact-hint">邮箱和手机号都是选填，清空后保存就等于解除绑定</p>
                <div class="contact-actions">
                  <button class="admin-btn primary" :disabled="contactSaving" @click="saveContact">
                    {{ contactSaving ? '保存中...' : '保存' }}
                  </button>
                  <button class="admin-btn" :disabled="contactSaving" @click="cancelEditContact">取消</button>
                </div>
              </template>

              <p
                v-if="contactMsg"
                class="contact-msg"
                :class="{ error: contactMsgType === 'error' }"
              >
                {{ contactMsg }}
              </p>
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
                <div class="favorite-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
                <div class="favorite-detail">
                  <p class="favorite-title">{{ book.title }}</p>
                  <p class="favorite-author">{{ book.author }}</p>
                </div>
              </div>
            </div>
          </section>

          <section class="profile-section">
            <div class="section-head">
              <h4 class="section-title">偏好标签</h4>
              <span class="section-hint">设置后，「推荐」会按这些标签为你找书</span>
            </div>

            <p v-if="preferenceTags.length === 0" class="empty-tip">
              还没有设置偏好标签，在下面输入或点击书架上的标签即可添加
            </p>
            <div v-else class="tag-list">
              <span class="tag-item tag-editable" v-for="tag in preferenceTags" :key="tag">
                {{ tag }}
                <button class="tag-del" title="删除这个标签" @click="removePreferenceTag(tag)">×</button>
              </span>
            </div>

            <!-- 自己输入标签 -->
            <div class="pref-tag-form">
              <input
                class="tag-input pref-tag-input"
                v-model="newPreferenceTag"
                type="text"
                placeholder="输入自己的喜好标签，如 科幻"
                @keyup.enter="addPreferenceTag()"
              />
              <button class="admin-btn primary" @click="addPreferenceTag()">添加标签</button>
            </div>

            <p v-if="preferenceMsg" class="pref-msg" :class="{ error: preferenceMsgType === 'error' }">
              {{ preferenceMsg }}
            </p>

            <!-- 书架上的常见标签，点一下就能加进偏好 -->
            <div v-if="allBookTags.length > 0" class="tag-suggest">
              <span class="tag-suggest-label">书架上的标签（点击快速添加）：</span>
              <span class="tag-suggest-list">
                <button
                  class="tag-suggest-item"
                  v-for="tag in allBookTags"
                  :key="tag"
                  :disabled="hasPreferenceTag(tag)"
                  @click="addPreferenceTag(tag)"
                >
                  {{ tag }}
                </button>
              </span>
            </div>
          </section>

          <!-- ==================== 管理员专属功能 ==================== -->
          <template v-if="isAdmin">
            <p class="admin-tip">
              🔑 当前是管理员模式，下面的书架改动会直接保存到数据库
            </p>
            <p v-if="adminMsg" class="admin-msg" :class="{ error: adminMsgType === 'error' }">
              {{ adminMsg }}
            </p>

            <!-- 1. 书籍管理 -->
            <section class="profile-section admin-section">
              <div class="section-head">
                <h4 class="section-title">书籍管理</h4>
                <button class="admin-btn primary" @click="toggleAddBookForm">
                  {{ showAddBookForm ? '取消' : '＋ 加入书籍' }}
                </button>
              </div>

              <!-- 加入书籍表单：保存后写入数据库 -->
              <div v-if="showAddBookForm" class="admin-form">
                <div class="form-row">
                  <label>书名</label>
                  <input v-model="newBook.title" type="text" placeholder="如 三体" />
                </div>
                <div class="form-row">
                  <label>作者</label>
                  <input v-model="newBook.author" type="text" placeholder="如 刘慈欣" />
                </div>
                <div class="form-row">
                  <label>价格</label>
                  <input v-model="newBook.price" type="number" step="0.01" min="0" placeholder="如 38.00" />
                </div>
                <div class="form-row">
                  <label>简介</label>
                  <input v-model="newBook.intro" type="text" placeholder="一句话简介" />
                </div>
                <div class="form-row">
                  <label>标签</label>
                  <input v-model="newBook.tags" type="text" placeholder="多个标签用英文逗号分隔，如 科幻,中国文学" />
                </div>
                <div class="form-row">
                  <label>封面颜色</label>
                  <input v-model="newBook.coverColor" type="color" class="color-input" />
                </div>
                <div class="form-row checkbox-row">
                  <label><input type="checkbox" v-model="newBook.recommend" /> 放到“推荐”</label>
                  <label><input type="checkbox" v-model="newBook.hot" /> 放到“热门”</label>
                </div>
                <button class="admin-btn primary" @click="submitNewBook">保存到数据库</button>
              </div>

              <p v-if="books.length === 0" class="empty-tip">数据库里还没有书籍</p>
              <div v-else class="admin-list">
                <div class="admin-item" v-for="book in books" :key="book.id">
                  <div class="admin-item-head">
                    <div class="admin-cover" :style="{ background: book.coverColor }">{{ book.title }}</div>
                    <div class="admin-item-main">
                      <p class="admin-item-title">
                        {{ book.title }}
                        <span class="admin-item-id">#{{ book.id }}</span>
                      </p>
                      <p class="admin-item-sub">
                        {{ book.author }} · {{ formatPrice(book.price) }}
                        <span v-if="book.recommend" class="mini-flag">推荐</span>
                        <span v-if="book.hot" class="mini-flag hot">热门</span>
                      </p>
                      <div class="tag-list">
                        <span class="tag-item tag-editable" v-for="tag in book.tags" :key="tag">
                          {{ tag }}
                          <button class="tag-del" title="删除这个标签" @click="removeTag(book, tag)">×</button>
                        </span>
                        <span v-if="!book.tags || book.tags.length === 0" class="tag-empty">暂无标签</span>
                      </div>
                    </div>
                  </div>

                  <div class="admin-actions">
                    <input
                      class="tag-input"
                      v-model="tagInputs[book.id]"
                      type="text"
                      placeholder="新标签"
                      @keyup.enter="addTag(book)"
                    />
                    <button class="admin-btn" @click="addTag(book)">加标签</button>
                    <button class="admin-btn" @click="startEdit(book)">
                      {{ editingId === book.id ? '收起修改' : '改价格/简介' }}
                    </button>
                    <button class="admin-btn danger" @click="deleteBook(book)">删除书籍</button>
                  </div>

                  <!-- 修改书籍：价格、简介等 -->
                  <div v-if="editingId === book.id" class="admin-form edit-form">
                    <div class="form-row">
                      <label>书名</label>
                      <input v-model="editForm.title" type="text" />
                    </div>
                    <div class="form-row">
                      <label>作者</label>
                      <input v-model="editForm.author" type="text" />
                    </div>
                    <div class="form-row">
                      <label>价格</label>
                      <input v-model="editForm.price" type="number" step="0.01" min="0" />
                    </div>
                    <div class="form-row">
                      <label>简介</label>
                      <input v-model="editForm.intro" type="text" />
                    </div>
                    <div class="form-row">
                      <label>封面颜色</label>
                      <input v-model="editForm.coverColor" type="color" class="color-input" />
                    </div>
                    <div class="form-row checkbox-row">
                      <label><input type="checkbox" v-model="editForm.recommend" /> 放到“推荐”</label>
                      <label><input type="checkbox" v-model="editForm.hot" /> 放到“热门”</label>
                    </div>
                    <button class="admin-btn primary" @click="submitEdit(book)">保存修改</button>
                  </div>
                </div>
              </div>
            </section>

            <!-- 2. 用户管理 -->
            <section class="profile-section admin-section">
              <div class="section-head">
                <h4 class="section-title">用户管理</h4>
                <button class="admin-btn primary" @click="toggleAddUserForm">
                  {{ showAddUserForm ? '取消' : '＋ 创建用户' }}
                </button>
              </div>

              <div v-if="showAddUserForm" class="admin-form">
                <div class="form-row">
                  <label>用户名</label>
                  <input v-model="newUser.username" type="text" placeholder="登录用的用户名" />
                </div>
                <div class="form-row">
                  <label>密码</label>
                  <input v-model="newUser.password" type="text" placeholder="登录用的密码" />
                </div>
                <div class="form-row">
                  <label>邮箱</label>
                  <input v-model="newUser.email" type="email" placeholder="选填" />
                </div>
                <div class="form-row">
                  <label>手机号</label>
                  <input v-model="newUser.phone" type="text" placeholder="选填" />
                </div>
                <button class="admin-btn primary" @click="submitNewUser">创建用户</button>
              </div>

              <p v-if="adminUsers.length === 0" class="empty-tip">还没有查询到用户</p>
              <div v-else class="admin-list">
                <div class="admin-item admin-user" v-for="user in adminUsers" :key="user.id">
                  <div class="admin-item-main">
                    <p class="admin-item-title">
                      {{ user.username }}
                      <span class="admin-item-id">#{{ user.id }}</span>
                      <span v-if="isReservedUser(user)" class="mini-flag">系统账号</span>
                    </p>
                    <p class="admin-item-sub">
                      邮箱：{{ user.email || '未绑定' }} · 手机号：{{ user.phone || '未绑定' }}
                    </p>
                  </div>
                  <button
                    class="admin-btn danger"
                    :disabled="isReservedUser(user)"
                    @click="deleteUser(user)"
                  >
                    {{ isReservedUser(user) ? '不可删除' : '删除用户' }}
                  </button>
                </div>
              </div>
            </section>

            <!-- 3. 评价管理：管理员可以删除任意一条评价 -->
            <section class="profile-section admin-section">
              <div class="section-head">
                <h4 class="section-title">评价管理</h4>
                <button
                  class="admin-btn"
                  :disabled="adminReviewsLoading"
                  @click="fetchAdminReviews"
                >
                  {{ adminReviewsLoading ? '刷新中...' : '↻ 刷新评价' }}
                </button>
              </div>

              <p class="section-hint review-hint">
                这里是用户在书籍详情页写下的评价，删除后那本书的评分和评价列表也会跟着更新。
              </p>

              <p v-if="adminReviewsLoading" class="empty-tip">正在加载评价...</p>
              <p v-else-if="adminReviews.length === 0" class="empty-tip">
                数据库里还没有任何评价，用户可以在书籍详情页写下自己的评价
              </p>
              <div v-else class="admin-list">
                <div class="admin-item admin-review" v-for="review in adminReviews" :key="review.id">
                  <div class="admin-item-main">
                    <p class="admin-item-title">
                      《{{ review.bookTitle }}》
                      <span class="admin-item-id">评价 #{{ review.id }}</span>
                    </p>
                    <p class="admin-item-sub">
                      {{ review.username }} · {{ starsText(review.rating) }} {{ review.rating }} 星 ·
                      {{ formatTime(review.createTime) }}
                    </p>
                    <p class="review-text">{{ review.content }}</p>
                  </div>
                  <button class="admin-btn danger" @click="deleteAdminReview(review)">删除评价</button>
                </div>
              </div>
            </section>
          </template>
        </div>

        </template>

      </main>
      
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')
const currentMenu = ref('home')

// 后端接口地址
const API_BASE = 'http://localhost:8080/api'

// 登录身份：role = 'admin' 时是管理员模式（由登录接口返回，存在 localStorage 里）
const role = ref(localStorage.getItem('role') || 'user')
const isAdmin = computed(() => role.value === 'admin')

// 管理员令牌：调用 /api/admin/** 接口时必须带上
const adminToken = localStorage.getItem('adminToken') || ''
const adminHeaders = () => ({ 'X-Admin-Token': adminToken })

// 书籍列表（数据来自后端 MySQL 的 books 表）
const books = ref([])
const booksLoading = ref(false)
const booksError = ref('')

// “我的”页面需要的个人资料
const profile = ref(null)
const profileLoading = ref(false)
const profileError = ref('')

// “我的”页面：修改邮箱 / 手机号
const editingContact = ref(false)
const contactLoading = ref(false)
const contactSaving = ref(false)
const contactForm = reactive({ email: '', phone: '' })
const contactMsg = ref('')
const contactMsgType = ref('info')

// 用户收藏的书籍 id（按账号保存在浏览器本地）
const favoriteIds = ref([])

/* ==================== 管理员模式用到的数据 ==================== */

// 管理员看到的用户列表
const adminUsers = ref([])

// 管理员看到的评价列表（来自 reviews 表）
const adminReviews = ref([])
const adminReviewsLoading = ref(false)

// 操作结果提示
const adminMsg = ref('')
const adminMsgType = ref('info')

// “加入书籍”表单
const showAddBookForm = ref(false)
const newBook = reactive({
  title: '',
  author: '',
  price: '',
  intro: '',
  tags: '',
  coverColor: '#5e81ac',
  recommend: false,
  hot: false
})

// 正在修改的书籍 id 和表单内容
const editingId = ref(null)
const editForm = reactive({
  title: '',
  author: '',
  price: '',
  intro: '',
  coverColor: '#5e81ac',
  recommend: false,
  hot: false
})

// 每本书对应的“新标签”输入框，键是书籍 id
const tagInputs = reactive({})

// “创建用户”表单
const showAddUserForm = ref(false)
const newUser = reactive({
  username: '',
  password: '',
  email: '',
  phone: ''
})

// 用户头像首字符
const avatarChar = computed(() => {
  return (username.value || '用').trim().charAt(0).toUpperCase()
})

// 已收藏的书籍
const favoriteBooks = computed(() => {
  return books.value.filter(book => favoriteIds.value.includes(book.id))
})

// 首页展示全部书籍（推荐、热门各自有单独的列表）
const displayBooks = computed(() => books.value)

/* ==================== 偏好标签（由用户自己设置） ==================== */

// 用户自己设置的喜好标签，按账号保存在浏览器本地
const preferenceTags = ref([])
const newPreferenceTag = ref('')
const preferenceMsg = ref('')
const preferenceMsgType = ref('info')

// 最多允许设置的偏好标签数量
const MAX_PREFERENCE_TAGS = 20

const preferenceStorageKey = () => `bookstore_preference_tags_${username.value}`

const showPreferenceMsg = (text, type = 'info') => {
  preferenceMsg.value = text
  preferenceMsgType.value = type
}

const loadPreferenceTags = () => {
  try {
    const saved = localStorage.getItem(preferenceStorageKey())
    const parsed = saved ? JSON.parse(saved) : []
    preferenceTags.value = Array.isArray(parsed)
      ? parsed.map(tag => String(tag).trim()).filter(Boolean)
      : []
  } catch {
    preferenceTags.value = []
  }
}

const savePreferenceTags = () => {
  localStorage.setItem(preferenceStorageKey(), JSON.stringify(preferenceTags.value))
}

// 比较标签时忽略大小写和首尾空格
const normalizeTag = (tag) => (tag || '').trim().toLowerCase()

const hasPreferenceTag = (tag) => {
  return preferenceTags.value.some(item => normalizeTag(item) === normalizeTag(tag))
}

// 添加偏好标签：点推荐标签会传入标签内容，输入框回车时用输入框里的内容
const addPreferenceTag = (tagFromClick) => {
  const tag = (typeof tagFromClick === 'string' ? tagFromClick : newPreferenceTag.value).trim()
  if (!tag) {
    showPreferenceMsg('请先输入标签内容', 'error')
    return
  }
  if (hasPreferenceTag(tag)) {
    showPreferenceMsg(`标签「${tag}」已经添加过了`, 'error')
    return
  }
  if (preferenceTags.value.length >= MAX_PREFERENCE_TAGS) {
    showPreferenceMsg(`最多只能设置 ${MAX_PREFERENCE_TAGS} 个偏好标签`, 'error')
    return
  }
  preferenceTags.value = [...preferenceTags.value, tag]
  savePreferenceTags()
  newPreferenceTag.value = ''
  showPreferenceMsg(`已添加偏好标签：${tag}`)
}

const removePreferenceTag = (tag) => {
  preferenceTags.value = preferenceTags.value.filter(item => item !== tag)
  savePreferenceTags()
  showPreferenceMsg(`已删除偏好标签：${tag}`)
}

// 书架上出现过的所有标签，用来给用户一键添加
const allBookTags = computed(() => {
  const tagSet = new Set()
  books.value.forEach(book => {
    (book.tags || []).forEach(tag => {
      const value = (tag || '').trim()
      if (value) {
        tagSet.add(value)
      }
    })
  })
  return Array.from(tagSet)
})

// 一本书命中了哪些偏好标签
const matchedTags = (book) => {
  if (preferenceTags.value.length === 0) {
    return []
  }
  const wanted = new Set(preferenceTags.value.map(normalizeTag))
  return (book.tags || []).filter(tag => wanted.has(normalizeTag(tag)))
}

// 推荐：只保留和偏好标签匹配的书籍，命中的标签越多排得越前
const recommendedBooks = computed(() => {
  if (preferenceTags.value.length === 0) {
    return []
  }
  return books.value
    .filter(book => matchedTags(book).length > 0)
    .sort((a, b) => matchedTags(b).length - matchedTags(a).length)
})

// 书店推荐：管理员勾选了“放到推荐”的书籍（已经出现在上面列表里的不重复展示）
const shopRecommendedBooks = computed(() => {
  const shownIds = new Set(recommendedBooks.value.map(book => book.id))
  return books.value.filter(book => book.recommend && !shownIds.has(book.id))
})

// 从后端获取当前用户的脱敏资料
const fetchProfile = async () => {
  profileLoading.value = true
  profileError.value = ''
  try {
    const response = await axios.get(`${API_BASE}/user/profile`, {
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

/* ==================== 修改邮箱 / 手机号 ==================== */

// 校验规则，和后端的校验保持一致
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const PHONE_PATTERN = /^1[3-9]\d{9}$/

const showContactMsg = (text, type = 'info') => {
  contactMsg.value = text
  contactMsgType.value = type
}

// 点“修改”：先取回未脱敏的邮箱和手机号，填进输入框
const startEditContact = async () => {
  contactMsg.value = ''
  contactLoading.value = true
  try {
    const response = await axios.get(`${API_BASE}/user/contact`, {
      params: { username: username.value }
    })
    if (response.data.code === 200) {
      contactForm.email = response.data.data.email || ''
      contactForm.phone = response.data.data.phone || ''
      editingContact.value = true
    } else {
      showContactMsg(response.data.message || '读取联系方式失败', 'error')
    }
  } catch (error) {
    console.error('读取联系方式出错:', error)
    showContactMsg('读取联系方式失败，请确认后端服务是否已启动', 'error')
  } finally {
    contactLoading.value = false
  }
}

// 取消编辑，回到只读展示
const cancelEditContact = () => {
  editingContact.value = false
  contactMsg.value = ''
}

// 保存：先做格式校验，成功后用后端返回的脱敏资料刷新展示区
const saveContact = async () => {
  const email = contactForm.email.trim()
  const phone = contactForm.phone.trim()

  if (email && !EMAIL_PATTERN.test(email)) {
    showContactMsg('邮箱格式不正确，请检查后重试', 'error')
    return
  }
  if (phone && !PHONE_PATTERN.test(phone)) {
    showContactMsg('手机号格式不正确，请输入 11 位手机号', 'error')
    return
  }

  contactSaving.value = true
  contactMsg.value = ''
  try {
    const response = await axios.put(`${API_BASE}/user/contact`, {
      username: username.value,
      email,
      phone
    })
    if (response.data.code === 200) {
      profile.value = response.data.data
      profileError.value = ''
      editingContact.value = false
      showContactMsg(response.data.message || '联系方式已更新')
    } else {
      showContactMsg(response.data.message || '保存失败，请稍后重试', 'error')
    }
  } catch (error) {
    console.error('保存联系方式出错:', error)
    showContactMsg('保存失败，请确认后端服务是否已启动', 'error')
  } finally {
    contactSaving.value = false
  }
}

// 从后端数据库读取书籍列表（首页展示全部，推荐/热门用数据库里的标记过滤）
const fetchBooks = async () => {
  booksLoading.value = true
  booksError.value = ''
  try {
    const response = await axios.get(`${API_BASE}/books`)
    if (response.data.code === 200) {
      books.value = response.data.data || []
    } else {
      booksError.value = response.data.message || '获取书籍失败'
    }
  } catch (error) {
    console.error('获取书籍出错:', error)
    booksError.value = '书籍加载失败，请确认后端服务已启动'
  } finally {
    booksLoading.value = false
  }
}

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

// 点击书籍卡片：跳转到这本书的详情页
const openBookDetail = (book) => {
  router.push(`/book/${book.id}`)
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

/* ==================== 管理员模式的操作 ==================== */

// 页面上的操作提示（成功用绿色，失败用红色）
const showAdminMsg = (text, type = 'info') => {
  adminMsg.value = text
  adminMsgType.value = type
}

// 请求失败时，尽量把后端返回的中文提示显示出来
const readErrorMessage = (error, fallback) => {
  return error.response?.data?.message || fallback
}

// 用后端返回的最新书籍替换列表里的旧数据
const applyBook = (updated) => {
  if (!updated) return
  const index = books.value.findIndex(book => book.id === updated.id)
  if (index === -1) {
    books.value.push(updated)
  } else {
    books.value[index] = updated
  }
}

// 展开 / 收起“加入书籍”表单
const toggleAddBookForm = () => {
  showAddBookForm.value = !showAddBookForm.value
}

// 加入书籍：POST /api/admin/books，保存成功后写入数据库
const submitNewBook = async () => {
  if (!newBook.title.trim() || !newBook.author.trim()) {
    showAdminMsg('书名和作者不能为空', 'error')
    return
  }
  try {
    const response = await axios.post(`${API_BASE}/admin/books`, {
      title: newBook.title.trim(),
      author: newBook.author.trim(),
      price: newBook.price === '' ? null : Number(newBook.price),
      intro: newBook.intro,
      // 标签按英文逗号拆分，后端会存成“科幻,中国文学”这样的字符串
      tags: newBook.tags.split(',').map(tag => tag.trim()).filter(Boolean),
      coverColor: newBook.coverColor,
      recommend: newBook.recommend,
      hot: newBook.hot
    }, { headers: adminHeaders() })

    if (response.data.code === 200) {
      showAdminMsg(`《${response.data.data.title}》已加入数据库`)
      newBook.title = ''
      newBook.author = ''
      newBook.price = ''
      newBook.intro = ''
      newBook.tags = ''
      newBook.recommend = false
      newBook.hot = false
      showAddBookForm.value = false
      fetchBooks()
    } else {
      showAdminMsg(response.data.message || '加入书籍失败', 'error')
    }
  } catch (error) {
    console.error('加入书籍出错:', error)
    showAdminMsg(readErrorMessage(error, '加入书籍失败，请确认后端服务已启动'), 'error')
  }
}

// 展开 / 收起某本书的“修改价格、简介”表单
const startEdit = (book) => {
  if (editingId.value === book.id) {
    editingId.value = null
    return
  }
  editingId.value = book.id
  editForm.title = book.title
  editForm.author = book.author
  editForm.price = book.price
  editForm.intro = book.intro || ''
  editForm.coverColor = book.coverColor || '#5e81ac'
  editForm.recommend = !!book.recommend
  editForm.hot = !!book.hot
}

// 保存修改：PUT /api/admin/books/{id}
const submitEdit = async (book) => {
  try {
    const response = await axios.put(`${API_BASE}/admin/books/${book.id}`, {
      title: editForm.title,
      author: editForm.author,
      price: editForm.price === '' ? null : Number(editForm.price),
      intro: editForm.intro,
      coverColor: editForm.coverColor,
      recommend: editForm.recommend,
      hot: editForm.hot
    }, { headers: adminHeaders() })

    if (response.data.code === 200) {
      applyBook(response.data.data)
      editingId.value = null
      showAdminMsg(`《${response.data.data.title}》已修改`)
    } else {
      showAdminMsg(response.data.message || '修改失败', 'error')
    }
  } catch (error) {
    console.error('修改书籍出错:', error)
    showAdminMsg(readErrorMessage(error, '修改失败，请确认后端服务已启动'), 'error')
  }
}

// 删除书籍：DELETE /api/admin/books/{id}
const deleteBook = async (book) => {
  if (!window.confirm(`确定要删除《${book.title}》吗？数据库里的这条记录也会一起删除。`)) {
    return
  }
  try {
    const response = await axios.delete(`${API_BASE}/admin/books/${book.id}`, { headers: adminHeaders() })
    if (response.data.code === 200) {
      books.value = books.value.filter(item => item.id !== book.id)
      // 收藏里也去掉这本书
      favoriteIds.value = favoriteIds.value.filter(id => id !== book.id)
      localStorage.setItem(favoriteStorageKey(), JSON.stringify(favoriteIds.value))
      if (editingId.value === book.id) {
        editingId.value = null
      }
      showAdminMsg(`《${book.title}》已删除`)
    } else {
      showAdminMsg(response.data.message || '删除失败', 'error')
    }
  } catch (error) {
    console.error('删除书籍出错:', error)
    showAdminMsg(readErrorMessage(error, '删除失败，请确认后端服务已启动'), 'error')
  }
}

// 给书籍加标签：POST /api/admin/books/{id}/tags
const addTag = async (book) => {
  const tag = (tagInputs[book.id] || '').trim()
  if (!tag) {
    showAdminMsg('请先输入要添加的标签', 'error')
    return
  }
  try {
    const response = await axios.post(`${API_BASE}/admin/books/${book.id}/tags`,
      { tag },
      { headers: adminHeaders() })

    if (response.data.code === 200) {
      applyBook(response.data.data)
      tagInputs[book.id] = ''
      showAdminMsg(`已给《${book.title}》加上标签：${tag}`)
    } else {
      showAdminMsg(response.data.message || '添加标签失败', 'error')
    }
  } catch (error) {
    console.error('添加标签出错:', error)
    showAdminMsg(readErrorMessage(error, '添加标签失败，请确认后端服务已启动'), 'error')
  }
}

// 删除书籍的某个标签：DELETE /api/admin/books/{id}/tags?tag=xxx
const removeTag = async (book, tag) => {
  try {
    const response = await axios.delete(`${API_BASE}/admin/books/${book.id}/tags`, {
      params: { tag },
      headers: adminHeaders()
    })

    if (response.data.code === 200) {
      applyBook(response.data.data)
      showAdminMsg(`已删除《${book.title}》的标签：${tag}`)
    } else {
      showAdminMsg(response.data.message || '删除标签失败', 'error')
    }
  } catch (error) {
    console.error('删除标签出错:', error)
    showAdminMsg(readErrorMessage(error, '删除标签失败，请确认后端服务已启动'), 'error')
  }
}

// 获取全部用户：GET /api/admin/users
const fetchAdminUsers = async () => {
  try {
    const response = await axios.get(`${API_BASE}/admin/users`, { headers: adminHeaders() })
    if (response.data.code === 200) {
      adminUsers.value = response.data.data || []
    } else {
      showAdminMsg(response.data.message || '获取用户列表失败', 'error')
    }
  } catch (error) {
    console.error('获取用户列表出错:', error)
    showAdminMsg(readErrorMessage(error, '获取用户列表失败，请确认后端服务已启动'), 'error')
  }
}

// 获取全部评价：GET /api/admin/reviews
const fetchAdminReviews = async () => {
  adminReviewsLoading.value = true
  try {
    const response = await axios.get(`${API_BASE}/admin/reviews`, { headers: adminHeaders() })
    if (response.data.code === 200) {
      adminReviews.value = response.data.data || []
    } else {
      showAdminMsg(response.data.message || '获取评价列表失败', 'error')
    }
  } catch (error) {
    console.error('获取评价列表出错:', error)
    showAdminMsg(readErrorMessage(error, '获取评价列表失败，请确认后端服务已启动'), 'error')
  } finally {
    adminReviewsLoading.value = false
  }
}

// 删除任意一条评价：DELETE /api/admin/reviews/{id}
const deleteAdminReview = async (review) => {
  if (!window.confirm(`确定要删除 ${review.username} 对《${review.bookTitle}》的评价吗？`)) {
    return
  }
  try {
    const response = await axios.delete(`${API_BASE}/admin/reviews/${review.id}`, {
      headers: adminHeaders()
    })
    if (response.data.code === 200) {
      adminReviews.value = adminReviews.value.filter(item => item.id !== review.id)
      showAdminMsg(`已删除 ${review.username} 对《${review.bookTitle}》的评价`)
    } else {
      showAdminMsg(response.data.message || '删除评价失败', 'error')
    }
  } catch (error) {
    console.error('删除评价出错:', error)
    showAdminMsg(readErrorMessage(error, '删除评价失败，请确认后端服务已启动'), 'error')
  }
}

// 展开 / 收起“创建用户”表单
const toggleAddUserForm = () => {
  showAddUserForm.value = !showAddUserForm.value
}

// 创建用户：POST /api/admin/users
const submitNewUser = async () => {
  if (!newUser.username.trim() || !newUser.password.trim()) {
    showAdminMsg('用户名和密码不能为空', 'error')
    return
  }
  try {
    const response = await axios.post(`${API_BASE}/admin/users`, {
      username: newUser.username.trim(),
      password: newUser.password,
      email: newUser.email,
      phone: newUser.phone
    }, { headers: adminHeaders() })

    if (response.data.code === 200) {
      showAdminMsg(`用户 ${newUser.username.trim()} 创建成功`)
      newUser.username = ''
      newUser.password = ''
      newUser.email = ''
      newUser.phone = ''
      showAddUserForm.value = false
      fetchAdminUsers()
    } else {
      showAdminMsg(response.data.message || '创建用户失败', 'error')
    }
  } catch (error) {
    console.error('创建用户出错:', error)
    showAdminMsg(readErrorMessage(error, '创建用户失败，请确认后端服务已启动'), 'error')
  }
}

// 删除用户：DELETE /api/admin/users/{id}
const deleteUser = async (user) => {
  if (!window.confirm(`确定要删除用户 ${user.username} 吗？`)) {
    return
  }
  try {
    const response = await axios.delete(`${API_BASE}/admin/users/${user.id}`, { headers: adminHeaders() })
    if (response.data.code === 200) {
      showAdminMsg(`用户 ${user.username} 已删除`)
      fetchAdminUsers()
    } else {
      showAdminMsg(response.data.message || '删除用户失败', 'error')
    }
  } catch (error) {
    console.error('删除用户出错:', error)
    showAdminMsg(readErrorMessage(error, '删除用户失败，请确认后端服务已启动'), 'error')
  }
}

// 管理员账号本身不允许被删除，按钮会置灰
const isReservedUser = (user) => (user.username || '').toLowerCase() === 'admin'

onMounted(() => {
  fetchProfile()
  fetchBooks()
  loadFavorites()
  loadPreferenceTags()
  // 管理员模式额外加载用户列表
  if (isAdmin.value) {
    fetchAdminUsers()
    fetchAdminReviews()
  }
})

// 使用 computed 计算属性，自动过滤出“热门”的书籍
const hotBooks = computed(() => {
  return books.value.filter(book => book.hot)
})

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  localStorage.removeItem('adminToken')
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

/* 书籍列表的加载 / 报错提示 */
.state-tip {
  margin: 0 0 18px;
  font-size: 14px;
  color: #7f8c8d;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 14px 18px;
  display: inline-block;
}

.state-tip.error {
  color: #e74c3c;
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

/* 整张卡片可以点击，点击后进入书籍详情页 */
.book-card.clickable {
  cursor: pointer;
}

.book-card.clickable:hover .book-title {
  color: #2980b9;
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
  min-width: 300px;
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

.contact-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
  margin-bottom: 4px;
  border-bottom: 1px solid #eceff3;
}

.contact-title {
  font-size: 13px;
  color: #7f8c8d;
}

.contact-edit-btn {
  padding: 4px 10px;
  font-size: 12px;
}

.contact-edit-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 0;
}

.contact-edit-row label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  color: #666;
}

.contact-edit-row input {
  flex: 1;
  min-width: 0;
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}

.contact-edit-row input:focus {
  border-color: #3498db;
}

.contact-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #a0a8b3;
}

.contact-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.contact-msg {
  margin: 8px 0 0;
  font-size: 12px;
  color: #1e8449;
}

.contact-msg.error {
  color: #c0392b;
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

/* --- 偏好标签（用户自己设置） --- */
.section-hint {
  font-size: 12px;
  color: #7f8c8d;
}

.pref-tag-form {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.pref-tag-input {
  width: 260px;
}

.pref-msg {
  margin: 10px 0 0;
  font-size: 13px;
  color: #1e8449;
}

.pref-msg.error {
  color: #c0392b;
}

.tag-suggest {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #e8ebf0;
}

.tag-suggest-label {
  display: block;
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.tag-suggest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-suggest-item {
  border: 1px dashed rgba(52, 152, 219, 0.55);
  background-color: rgba(52, 152, 219, 0.06);
  color: #2471a3;
  border-radius: 999px;
  padding: 4px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.tag-suggest-item:hover:not(:disabled) {
  background-color: rgba(52, 152, 219, 0.2);
}

.tag-suggest-item:disabled {
  opacity: 0.45;
  cursor: default;
}

/* --- 推荐页 --- */
.empty-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
}

.recommend-tip {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 13px;
  color: #5b6b7c;
}

.tag-item.tag-small {
  padding: 3px 12px;
  font-size: 12px;
}

.matched-tags {
  margin-top: 10px;
  gap: 6px;
}

.recommend-block {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px dashed #dfe4ea;
}

.recommend-block .section-title {
  margin-bottom: 16px;
}

/* --- 管理员模式 --- */
.role-badge {
  display: inline-block;
  background: linear-gradient(135deg, #e67e22, #d35400);
  color: #fff;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  margin-left: 6px;
  vertical-align: middle;
}

.admin-tip {
  margin: 0;
  font-size: 13px;
  color: #b9770e;
  background-color: #fff8e6;
  border: 1px solid #f5d98b;
  border-radius: 8px;
  padding: 10px 16px;
}

.admin-msg {
  margin: 0;
  font-size: 13px;
  color: #1e8449;
  background-color: #eafaf1;
  border: 1px solid #a9dfbf;
  border-radius: 8px;
  padding: 10px 16px;
}

.admin-msg.error {
  color: #c0392b;
  background-color: #fdecea;
  border-color: #f5b7b1;
}

.admin-section {
  max-width: 100%;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.section-head .section-title {
  margin: 0;
}

/* 管理表单 */
.admin-form {
  background-color: #f8f9fb;
  border: 1px solid #e8ebf0;
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-row label {
  width: 76px;
  flex-shrink: 0;
  font-size: 13px;
  color: #666;
  text-align: right;
}

.form-row input[type="text"],
.form-row input[type="email"],
.form-row input[type="number"] {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}

.form-row input:focus {
  border-color: #3498db;
}

.form-row .color-input {
  width: 52px;
  height: 30px;
  padding: 0;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: none;
  cursor: pointer;
}

.checkbox-row {
  justify-content: flex-start;
}

.checkbox-row label {
  width: auto;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.checkbox-row input[type="checkbox"] {
  width: auto;
  margin: 0;
}

/* 管理按钮 */
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

/* 管理列表 */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.admin-item {
  background-color: #fff;
  border: 1px solid #eef0f4;
  border-radius: 10px;
  padding: 14px 16px;
}

.admin-item-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.admin-cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 4px;
  flex-shrink: 0;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

.admin-item-main {
  flex: 1;
  min-width: 0;
}

.admin-item-title {
  margin: 0 0 4px;
  font-size: 15px;
  color: #2c3e50;
  font-weight: 600;
}

.admin-item-id {
  color: #b0b8c4;
  font-size: 12px;
  font-weight: normal;
  margin-left: 6px;
}

.admin-item-sub {
  margin: 0 0 8px;
  font-size: 12px;
  color: #7f8c8d;
}

.mini-flag {
  display: inline-block;
  background-color: rgba(230, 126, 34, 0.14);
  color: #b9770e;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  margin-left: 6px;
}

.mini-flag.hot {
  background-color: rgba(231, 76, 60, 0.14);
  color: #c0392b;
}

.tag-empty {
  font-size: 12px;
  color: #bdc3c7;
}

.tag-editable {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
}

.tag-del {
  border: none;
  background: none;
  color: #2471a3;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  padding: 0;
}

.tag-del:hover {
  color: #c0392b;
}

.admin-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8ebf0;
}

.tag-input {
  width: 120px;
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
}

.tag-input:focus {
  border-color: #3498db;
}

.edit-form {
  margin-top: 12px;
  margin-bottom: 0;
}

/* 用户管理列表 */
.admin-user {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.admin-user .admin-item-sub {
  margin-bottom: 0;
}

/* 评价管理列表 */
.review-hint {
  display: block;
  margin: -8px 0 16px;
}

.admin-review {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.admin-review .admin-item-sub {
  margin-bottom: 6px;
}

.review-text {
  margin: 0;
  font-size: 13px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
