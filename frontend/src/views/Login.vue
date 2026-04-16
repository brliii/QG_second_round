<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">登录校园失物招领平台</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入用户名/邮箱/手机号"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            @click="handleLogin"
            :loading="loading"
            loading-text="登录中..."
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            type="default"
            style="width: 100%"
            @click="handleGuestLogin"
          >
            游客访问
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

const loginFormRef = ref()
const loading = ref(false)

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    console.log('开始登录，账号:', loginForm.account)

    const success = await userStore.login(loginForm.account, loginForm.password)
    console.log('登录结果:', success)
    if (success) {
      router.push('/')
    }
  } catch (error) {
    console.error('登录错误:', error)
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
  font-weight: bold;
}

.login-links {
  text-align: center;
  margin-top: 20px;
}

.login-links a {
  color: #1677ff;
  text-decoration: none;
}

.login-links a:hover {
  text-decoration: underline;
}
</style>
