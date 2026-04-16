<template>
  <el-header class="navbar" style="background-color: #1677ff; color: white; display: flex; justify-content: space-between; align-items: center; padding: 0 20px;">
    <div class="logo">
      <h2>校园失物招领平台</h2>
    </div>
    <div class="nav-menu">
      <el-menu :default-active="$route.path" class="el-menu-demo" mode="horizontal" background-color="#1677ff" text-color="#fff" active-text-color="#ffd04b" :router="true">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn" index="/messages">私聊</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn" index="/profile">个人中心</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn && userStore.isAdmin" index="/admin">管理后台</el-menu-item>
        <el-menu-item v-if="!userStore.isLoggedIn" index="/login">登录</el-menu-item>
        <el-menu-item v-if="!userStore.isLoggedIn" index="/register">注册</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn" @click="logout">退出登录</el-menu-item>
      </el-menu>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const logout = async () => {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo h2 {
  margin: 0;
  font-weight: bold;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
</style>
