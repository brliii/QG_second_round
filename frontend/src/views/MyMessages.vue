<template>
  <div>
    <NavBar />
    <div class="messages-container">
      <div class="messages-card">
        <h2>我的消息</h2>

        <div class="messages-header">
          <el-button type="primary" @click="markAllAsRead" :disabled="unreadCount === 0">
            全部标记为已读
          </el-button>
          <span class="unread-count">未读消息: {{ unreadCount }}</span>
        </div>

        <div v-if="messages.length === 0" class="no-data">
          暂无消息
        </div>

        <div v-else class="messages-list">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ unread: !message.isRead }"
            @click="markAsRead(message.id)"
          >
            <div class="message-header">
              <strong>{{ message.fromUsername }}</strong>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <div class="message-content">{{ message.content }}</div>
            <div v-if="message.targetType !== null" class="message-target">
              关于: {{ getTargetText(message.targetType) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPrivateMessages, getUnreadCount, markAsRead as markAsReadApi } from '@/api/comment'
import NavBar from '@/components/NavBar.vue'

const messages = ref([])
const unreadCount = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getTargetText = (targetType) => {
  switch (targetType) {
    case 0: return '失物'
    case 1: return '拾取'
    case 2: return '评论'
    default: return '其他'
  }
}

const loadMessages = async () => {
  try {
    messages.value = await getPrivateMessages()
    updateUnreadCount()
  } catch (error) {
    ElMessage.error('获取消息失败')
  }
}

const updateUnreadCount = async () => {
  try {
    unreadCount.value = await getUnreadCount()
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const markAsRead = async (messageId) => {
  try {
    await markAsReadApi(messageId)
    // 更新本地状态
    const message = messages.value.find(m => m.id === messageId)
    if (message) {
      message.isRead = true
    }
    updateUnreadCount()
  } catch (error) {
    ElMessage.error('标记已读失败')
  }
}

const markAllAsRead = async () => {
  try {
    // 标记所有未读消息为已读
    const unreadMessages = messages.value.filter(m => !m.isRead)
    for (const message of unreadMessages) {
      await markAsRead(message.id)
    }
    ElMessage.success('全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.messages-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.messages-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.messages-card h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.unread-count {
  color: #1677ff;
  font-weight: bold;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 60px 20px;
  font-size: 16px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.message-item:hover {
  background: #f8f9fa;
}

.message-item.unread {
  background: #e6f7ff;
  border-color: #1677ff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-header strong {
  color: #1677ff;
}

.message-time {
  color: #999;
  font-size: 12px;
}

.message-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 4px;
}

.message-target {
  color: #666;
  font-size: 14px;
}
</style>
