import { defineStore } from 'pinia'
import { login, getUserInfo, register, registerAdmin, updateUserInfo, changePassword } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    isLoggedIn: false
  }),

  getters: {
    isAdmin: (state) => state.userInfo?.role === 1
  },

  actions: {
    async login(account, password) {
      try {
        const response = await login(account, password)
        if (response.code === 200 && response.data) {
          const token = response.data
          this.token = token
          localStorage.setItem('token', token)
          await this.getUserInfo()
          ElMessage.success('登录成功')
          return true
        } else {
          ElMessage.error(response.message || '登录失败')
          return false
        }
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试')
        return false
      }
    },

    async register(userData) {
      try {
        let response
        if (userData.userType === 'admin') {
          // 调用管理员注册 API
          response = await registerAdmin(userData, userData.adminKey)
        } else {
          // 调用普通用户注册 API
          response = await register(userData)
        }
        if (response.code === 200) {
          ElMessage.success('注册成功')
          return true
        } else {
          ElMessage.error(response.message || '注册失败')
          return false
        }
      } catch (error) {
        ElMessage.error('注册失败，请稍后重试')
        return false
      }
    },

    async getUserInfo() {
      try {
        const response = await getUserInfo()
        if (response.code === 200 && response.data) {
          this.userInfo = response.data
          this.isLoggedIn = true
          // 将角色存储到 localStorage 中，用于路由守卫检查
          localStorage.setItem('role', this.userInfo.role)
        } else {
          this.logout()
        }
      } catch (error) {
        this.logout()
      }
    },

    async updateUserInfo(userData) {
      try {
        const response = await updateUserInfo(userData)
        if (response.code === 200) {
          await this.getUserInfo()
          ElMessage.success('修改成功')
          return true
        } else {
          ElMessage.error(response.message || '修改失败')
          return false
        }
      } catch (error) {
        ElMessage.error('修改失败，请稍后重试')
        return false
      }
    },

    async changePassword(passwordData) {
      try {
        const response = await changePassword(passwordData)
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          return true
        } else {
          ElMessage.error(response.message || '密码修改失败')
          return false
        }
      } catch (error) {
        ElMessage.error('密码修改失败，请稍后重试')
        return false
      }
    },

    logout() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('role')
    },

    initializeAuth() {
      if (this.token) {
        this.getUserInfo()
      }
    }
  }
})
