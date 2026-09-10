<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 定义响应式数据
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const phone = ref('')
const errorMsg = ref('')
const successMsg = ref('')

// 处理注册逻辑
const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  // 1. 前端基础校验
  if (!username.value || !password.value) {
    errorMsg.value = '用户名和密码不能为空！'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致！'
    return
  }

  try {
    // 2. 发送注册请求到后端
    const response = await axios.post('http://localhost:8080/api/register', {
      username: username.value,
      password: password.value,
      email: email.value,
      phone: phone.value
    })

    // 3. 处理后端返回的结果
    if (response.data.code === 200) {
      successMsg.value = '注册成功！即将跳转到登录页...'
      // 2秒后跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      errorMsg.value = response.data.message || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('注册请求出错:', error)
    errorMsg.value = '网络请求失败，请检查后端服务是否已启动！'
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-box">
      <h2>📚 网上书店</h2>
      <p class="subtitle">创建您的账号</p>

      <div class="error-msg" v-if="errorMsg">
        {{ errorMsg }}
      </div>
      <div class="success-msg" v-if="successMsg">
        {{ successMsg }}
      </div>

      <div class="form-group">
        <label>用户名</label>
        <input type="text" v-model="username" placeholder="请输入用户名" />
      </div>

      <div class="form-group">
        <label>密码</label>
        <input type="password" v-model="password" placeholder="请输入密码" />
      </div>

      <div class="form-group">
        <label>确认密码</label>
        <input type="password" v-model="confirmPassword" placeholder="请再次输入密码" />
      </div>

      <div class="form-group">
        <label>邮箱 (选填)</label>
        <input type="email" v-model="email" placeholder="请输入邮箱" />
      </div>

      <div class="form-group">
        <label>手机号 (选填)</label>
        <input type="text" v-model="phone" placeholder="请输入手机号" />
      </div>

      <button class="register-btn" @click="handleRegister">注 册</button>

      <p class="login-link">
        已有账号？<a href="#" @click.prevent="router.push('/login')">立即登录</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/images/bookstore1.png') no-repeat center center fixed;
  background-size: cover;
  font-family: sans-serif;
}

.register-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.register-box h2 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 24px;
}

.subtitle {
  color: #999;
  font-size: 14px;
  margin-bottom: 25px;
}

.error-msg, .success-msg {
  padding: 10px;
  margin-bottom: 20px;
  border-radius: 5px;
  font-size: 14px;
}

.error-msg {
  background-color: #fee;
  color: #c00;
  border: 1px solid #fcc;
}

.success-msg {
  background-color: #efe;
  color: #080;
  border: 1px solid #cfc;
}

.form-group {
  margin-bottom: 15px;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-sizing: border-box;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}

.register-btn:hover {
  background-color: #66b1ff;
}

.login-link {
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}
</style>
