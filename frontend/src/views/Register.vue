<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">注册校园失物招领平台</h2>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0">
        <el-form-item>
          <el-radio-group v-model="registerForm.userType">
            <el-radio-button label="user">普通用户</el-radio-button>
            <el-radio-button label="admin">管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="registerForm.userType === 'admin'" prop="adminKey">
          <el-input
            v-model="registerForm.adminKey"
            type="password"
            placeholder="请输入管理员密钥"
            prefix-icon="Key"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            @click="handleRegister"
            :loading="loading"
            loading-text="注册中..."
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-links">
        <router-link to="/login">已有账号？立即登录</router-link>
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

const registerForm = reactive({
  userType: 'user',
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  adminKey: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  adminKey: [
    {
      required: (rule, value, callback) => {
        return registerForm.userType === 'admin'
      },
      message: '请输入管理员密钥',
      trigger: 'blur'
    }
  ]
}

const registerFormRef = ref()
const loading = ref(false)

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    console.log('开始注册，用户名:', registerForm.username)

    const success = await userStore.register(registerForm)
    console.log('注册结果:', success)
    if (success) {
      router.push('/login')
    }
  } catch (error) {
    console.error('注册错误:', error)
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
  font-weight: bold;
}

.register-links {
  text-align: center;
  margin-top: 20px;
}

.register-links a {
  color: #1677ff;
  text-decoration: none;
}

.register-links a:hover {
  text-decoration: underline;
}
</style>
