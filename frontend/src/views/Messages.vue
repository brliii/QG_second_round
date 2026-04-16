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
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const conversations = ref([])
const activeConversation = ref(null)
const messageInput = ref('')

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const fetchConversations = async () => {
  try {
    // 假设后端有获取会话列表的API
    // const response = await getConversations()
    // if (response.code === 200 && response.data) {
    //   conversations.value = response.data
    // } else {
    //   conversations.value = []
    // }
    
    // 模拟数据
    conversations.value = [
      {
        id: 1,
        otherUser: { id: 2, username: '张三' },
        lastMessage: '你好，请问这个物品是你丢的吗？',
        lastMessageTime: new Date().toISOString(),
        unreadCount: 1,
        messages: [
          { id: 1, senderId: 2, content: '你好，请问这个物品是你丢的吗？', sendTime: new Date().toISOString() }
        ]
      },
      {
        id: 2,
        otherUser: { id: 3, username: '李四' },
        lastMessage: '是的，谢谢！',
        lastMessageTime: new Date(Date.now() - 3600000).toISOString(),
        unreadCount: 0,
        messages: [
          { id: 1, senderId: 3, content: '你好，我看到你发布的失物信息', sendTime: new Date(Date.now() - 7200000).toISOString() },
          { id: 2, senderId: userStore.userInfo?.id, content: '是的，请问你捡到了吗？', sendTime: new Date(Date.now() - 3600000).toISOString() },
          { id: 3, senderId: 3, content: '是的，谢谢！', sendTime: new Date(Date.now() - 3600000).toISOString() }
        ]
      }
    ]
  } catch (error) {
    console.error('获取会话列表失败', error)
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
    // 假设后端有发送消息的API
    // const response = await sendMessage({
    //   conversationId: activeConversation.value.id,
    //   content: messageInput.value
    // })
    // if (response.code === 200) {
    //   // 添加消息到列表
    //   const newMessage = {
    //     id: Date.now(),
    //     senderId: userStore.userInfo.id,
    //     content: messageInput.value,
    //     sendTime: new Date().toISOString()
    //   }
    //   activeConversation.value.messages.push(newMessage)
    //   messageInput.value = ''
    // } else {
    //   ElMessage.error('发送失败')
    // }
    
    // 模拟发送
    const newMessage = {
      id: Date.now(),
      senderId: userStore.userInfo?.id || 1,
      content: messageInput.value,
      sendTime: new Date().toISOString()
    }
    activeConversation.value.messages.push(newMessage)
    messageInput.value = ''
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchConversations()
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