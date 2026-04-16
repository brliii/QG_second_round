<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  await userStore.initializeAuth()
})
</script>

<template>
  <RouterView />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f5f5;
  color: #333;
}

#app {
  min-height: 100vh;
}

/* 全局样式 */
.el-button--primary {
  background-color: #1677ff;
  border-color: #1677ff;
}

.el-button--primary:hover {
  background-color: #4096ff;
  border-color: #4096ff;
}

.el-menu-item.is-active {
  background-color: #e6f7ff !important;
  color: #1677ff !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container,
  .create-container,
  .detail-container,
  .profile-container,
  .claims-container,
  .messages-container,
  .admin-container {
    padding: 10px !important;
  }

  .filter-bar {
    flex-direction: column !important;
    gap: 12px !important;
  }

  .items-grid {
    grid-template-columns: 1fr !important;
  }

  .item-detail {
    flex-direction: column !important;
  }

  .admin-container {
    flex-direction: column !important;
  }

  .admin-sidebar {
    width: 100% !important;
  }
}
</style>
