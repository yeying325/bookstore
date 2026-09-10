<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios' // 1. 引入 axios

const router = useRouter()

const username = ref('')
const password = ref('')
const errorMsg = ref('')

const handleLogin = async () => { // 2. 将函数改为 async
  errorMsg.value = ''

  if (!username.value || !password.value) {
    errorMsg.value = '用户名和密码不能为空！'
    return
  }

  try {
    // 3. 发送登录请求到你的后端接口
    const response = await axios.post('http://localhost:8080/api/login', {
      username: username.value,
      password: password.value
    });

    // 4. 处理登录成功
    // 后端返回 { code: 200, message: "登录成功", data: { username, role, adminToken } }
    // 管理员登录时 role = 'admin'，并多返回一个 adminToken，前端靠它调用管理员接口
    if (response.data.code === 200) {
      const data = response.data.data || {}

      // 将登录状态、账号和角色存入 localStorage
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('username', data.username || username.value)
      localStorage.setItem('role', data.role || 'user')

      // 管理员令牌：管理员接口需要带上它，普通用户登录时清掉旧的
      if (data.adminToken) {
        localStorage.setItem('adminToken', data.adminToken)
      } else {
        localStorage.removeItem('adminToken')
      }

      // 跳转到首页
      router.push('/home')
    } else {
      // 5. 处理登录失败（如用户名或密码错误）
      errorMsg.value = response.data.message || '登录失败'
    }
  } catch (error) {
    // 6. 处理网络请求错误
    console.error('登录请求出错:', error)
    if (error.response) {
      // 服务器返回了错误状态码，例如 401
      errorMsg.value = error.response.data.message || '用户名或密码错误'
    } else {
      // 网络错误，例如后端服务未启动
      errorMsg.value = '网络请求失败，请检查后端服务是否已启动！'
    }
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <h2>📚 网上书店</h2>
      <p class="subtitle">请登录您的账号</p>

      <div class="error-msg" v-if="errorMsg">
        {{ errorMsg }}
      </div>

      <div class="form-group">
        <label>用户名</label>
        <input type="text" v-model="username" placeholder="请输入用户名" />
      </div>

      <div class="form-group">
        <label>密码</label>
        <input type="password" v-model="password" placeholder="请输入密码" />
      </div>

      <button class="login-btn" @click="handleLogin">登 录</button>

<p class="register-link">
  还没有账号？<a href="#" @click.prevent="router.push('/register')">立即注册</a>
</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/images/bookstore1.png') no-repeat center center fixed;
  background-size: cover;
  font-family: sans-serif;
}

.login-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 380px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.login-box h2 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 24px;
}

.subtitle {
  color: #999;
  font-size: 14px;
  margin-bottom: 30px;
}

.error-msg {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #e63946;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  margin-bottom: 15px;
}

.form-group {
  text-align: left;
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #555;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
  margin-top: 10px;
}

.login-btn:hover {
  opacity: 0.9;
}

.register-link {
  margin-top: 20px;
  font-size: 13px;
  color: #999;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
