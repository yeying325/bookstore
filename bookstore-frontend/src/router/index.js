import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../views/LoginPage.vue'
import HomePage from '../views/HomePage.vue'
import RegisterPage from '../views/RegisterPage.vue'
import BookDetailPage from '../views/BookDetailPage.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage
  },
  {
    path: '/home',
    name: 'Home',
    component: HomePage,
    meta: { requiresAuth: true }
  },
  {
    // 书籍详情页：在首页点任意一本书都会跳到这里，:id 是书籍的 id
    path: '/book/:id',
    name: 'BookDetail',
    component: BookDetailPage,
    meta: { requiresAuth: true }
  },
  {  
    path: '/register', // 2. 添加路由规则
    name: 'Register',
    component: RegisterPage
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn')

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
