<template>
  <div>
    <NavBar />
    <div class="messages-container">
      <div class="messages-card">
        <h2>我的私聊</h2>

        <div v-if="conversations.length === 0" class="no-data">
          暂无私聊消息
        </div>

        <div v-else class="conversations-list">
          <div v-for="conversation in conversations" :key="conversation.id" class="conversation-item" @click="openConversation(conversation)">
            <div class="conversation-info">
              <h4>{{ conversation.otherUser.username }}</h4>
              <p class="last-message">{{ conversation.lastMessage }}</p>
              <p class="timestamp">{{ formatTime(conversation.lastMessageTime) }}</p>
            </div>
            <div v-if="conversation.unreadCount > 0" class="unread-badge">{{ conversation.unreadCount }}</div>
          </div>
        </div>

        <!-- 聊天窗口 -->
        <div v-if="activeConversation" class="chat-window">
          <div class="chat-header">
            <h3>{{ activeConversation.otherUser.username }}</h3>
            <el-button type="text" @click="closeConversation">关闭</el-button>
          </div>
          <div class="chat-messages">
            <div v-for="message in activeConversation.messages" :key="message.id" :class="['message-item', message.senderId === userStore.userInfo.id ? 'my-message' : 'other-message']">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.sendTime) }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="messageInput"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            />
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getConversations, getOrCreateConversation, sendMessage as sendMessageApi } from '@/api/message'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const route = useRoute()
const conversations = ref([])
const activeConversation = ref(null)
const messageInput = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const fetchConversations = async () => {
  try {
    // 调用真实的API获取会话列表
    const response = await getConversations()
    if (response.code === 200 && response.data) {
      conversations.value = response.data
    } else {
      console.error('获取会话列表失败，响应:', response)
      conversations.value = []
    }
  } catch (error) {
    console.error('获取会话列表失败', error)
    // API调用失败，使用空数组
    conversations.value = []
  }
}

const openConversation = (conversation) => {
  activeConversation.value = conversation
  // 标记为已读
  conversation.unreadCount = 0
}

const closeConversation = () => {
  activeConversation.value = null
}

const sendMessage = async () => {
  if (!messageInput.value.trim()) return

  try {
    // 调用真实的API发送消息
    const response = await sendMessageApi(activeConversation.value.id, messageInput.value)
    if (response.code === 200) {
      // 添加消息到列表
      const newMessage = {
        id: Date.now(),
        senderId: userStore.userInfo.id,
        content: messageInput.value,
        sendTime: new Date().toISOString()
      }
      activeConversation.value.messages.push(newMessage)
      messageInput.value = ''
    } else {
      console.error('发送消息失败，响应:', response)
      // API调用失败，使用模拟数据
      const newMessage = {
        id: Date.now(),
        senderId: userStore.userInfo?.id || 1,
        content: messageInput.value,
        sendTime: new Date().toISOString()
      }
      activeConversation.value.messages.push(newMessage)
      messageInput.value = ''
      ElMessage.warning('后端API未实现，使用模拟数据')
    }
  } catch (error) {
    console.error('发送消息失败', error)
    // API调用失败，使用模拟数据
    const newMessage = {
      id: Date.now(),
      senderId: userStore.userInfo?.id || 1,
      content: messageInput.value,
      sendTime: new Date().toISOString()
    }
    activeConversation.value.messages.push(newMessage)
    messageInput.value = ''
    ElMessage.warning('后端API未实现，使用模拟数据')
  }
}

const openChatWithUser = async (userId) => {
  try {
    // 调用真实的API获取或创建会话
    const response = await getOrCreateConversation(userId)
    if (response.code === 200 && response.data) {
      activeConversation.value = response.data
    } else {
      console.error('打开聊天失败，响应:', response)
      // API调用失败，使用模拟数据
      activeConversation.value = {
        id: Date.now(),
        otherUser: { id: userId, username: '用户' + userId },
        messages: []
      }
      ElMessage.warning('后端API未实现，使用模拟数据')
    }
  } catch (error) {
    console.error('打开聊天失败', error)
    // API调用失败，使用模拟数据
    activeConversation.value = {
      id: Date.now(),
      otherUser: { id: userId, username: '用户' + userId },
      messages: []
    }
    ElMessage.warning('后端API未实现，使用模拟数据')
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchConversations()
    
    // 检查URL参数中是否包含userId
    const userId = route.query.userId
    if (userId) {
      await openChatWithUser(userId)
    }
  }
})
</script>

<style scoped>
.messages-container {
  max-width: 1000px;
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

.no-data {
  text-align: center;
  color: #999;
  padding: 60px 20px;
  font-size: 16px;
}

.conversations-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.conversation-item:hover {
  background-color: #f8f9fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.conversation-info h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.last-message {
  margin: 0 0 4px 0;
  color: #666;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.timestamp {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: bold;
}

.chat-window {
  border-top: 1px solid #e9ecef;
  padding-top: 24px;
  margin-top: 24px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chat-header h3 {
  margin: 0;
  color: #333;
}

.chat-messages {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  max-width: 70%;
  padding: 12px;
  border-radius: 8px;
  position: relative;
}

.my-message {
  align-self: flex-end;
  background-color: #e6f7ff;
  color: #333;
}

.other-message {
  align-self: flex-start;
  background-color: #f5f5f5;
  color: #333;
}

.message-content {
  margin-bottom: 4px;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input .el-input {
  flex: 1;
}
</style>