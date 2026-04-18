<template>
  <div>
    <NavBar />
    <div class="detail-container" v-loading="loading">
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      <div v-else-if="!loading">
        <div class="item-detail">
          <img :src="(item.imageUrl ? 'http://localhost:8080' + item.imageUrl : '/placeholder.jpg')" :alt="item.name" class="detail-image" />
          <div class="detail-info">
            <h1 class="detail-title">{{ item.name }}</h1>
            <p class="detail-location">📍 地点: {{ item.location }}</p>
            <p class="detail-time">🕒 时间: {{ formatTime(item.lostTime || item.pickTime) }}</p>
            <p class="detail-description">{{ item.description }}</p>
            <p v-if="canShowContact && (isOwner ? (userStore.userInfo.phone || item.contact) : item.contact)" class="detail-contact">📞 联系方式: {{ isOwner ? (userStore.userInfo.phone || item.contact) : item.contact }}</p>
            <p v-if="item.aiDescription" class="ai-description">
              🤖 AI描述: {{ item.aiDescription }}
            </p>

            <!-- 操作按钮 -->
            <div class="action-buttons" v-if="userStore.isLoggedIn">
              <el-button v-if="isPicked && !isOwner" type="primary" @click="goClaim">
                认领物品
              </el-button>
              <el-button v-if="!isOwner" type="info" @click="startChat">
                私聊
              </el-button>
              <el-button v-if="isOwner" type="warning" @click="editItem">
                编辑物品
              </el-button>
              <el-button v-if="isOwner" type="danger" @click="deleteItem">
                删除物品
              </el-button>
              <el-button v-if="isPicked && isOwner" type="success" @click="regenerateAi">
                重新生成AI描述
              </el-button>
              <el-button v-if="isPicked && isOwner" type="info" @click="toggleClaimed">
                {{ item.status === 1 ? '标记为未认领' : '标记为已认领' }}
              </el-button>
              <el-button v-if="!isPicked && isOwner" type="info" @click="toggleFound">
                {{ item.status === 1 ? '标记为未找回' : '标记为已找回' }}
              </el-button>
              <el-button @click="reportItem">举报</el-button>
            </div>
          </div>
        </div>

        <!-- 评论区域 -->
        <CommentList
          :comments="comments"
          :target-type="targetType"
          :target-id="targetId"
          @comment-added="fetchComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLostDetail, deleteLost, updateLost } from '@/api/lost'
import { getPickedDetail, regenerateAiDesc, deletePicked, updatePicked } from '@/api/picked'
import { getCommentsByTarget } from '@/api/comment'
import { createReport } from '@/api/report'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import CommentList from '@/components/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref({})
const comments = ref([])
const loading = ref(true)
const error = ref('')
const { type, id } = route.params
const targetType = computed(() => type === 'lost' ? 0 : 1)
const targetId = computed(() => parseInt(id))

const isPicked = computed(() => type === 'picked')
const isOwner = computed(() => userStore.userInfo?.id === item.value.userId)
const canShowContact = computed(() => !!item.value.contact)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const fetchItem = async () => {
  loading.value = true
  error.value = ''
  try {
    if (type === 'lost') {
      const response = await getLostDetail(id)
      if (response.code === 200 && response.data) {
        item.value = response.data
      } else {
        error.value = response.message || '获取物品详情失败，请稍后重试'
      }
    } else {
      const response = await getPickedDetail(id)
      if (response.code === 200 && response.data) {
        item.value = response.data
      } else {
        error.value = response.message || '获取物品详情失败，请稍后重试'
      }
    }
  } catch (err) {
    console.error('获取详情失败', err)
    error.value = '获取物品详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const response = await getCommentsByTarget(targetType.value, targetId.value)
    if (response.code === 200 && response.data) {
      comments.value = response.data
    } else {
      comments.value = []
    }
  } catch (err) {
    console.error('获取评论失败', err)
    comments.value = []
  }
}

const goClaim = () => {
  router.push(`/claim/create/${id}`)
}

const editItem = () => {
  router.push(`/${type}/create?id=${id}`)
}

const deleteItem = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个物品吗？此操作不可撤销。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 这里需要调用删除API
    if (type === 'lost') {
      const response = await deleteLost(id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        router.push('/')
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } else {
      const response = await deletePicked(id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        router.push('/')
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    }
  } catch (err) {
    // 用户取消
  }
}

const regenerateAi = async () => {
  try {
    const response = await regenerateAiDesc(id)
    if (response.code === 200 && response.data) {
      item.value.aiDescription = response.data
      ElMessage.success('AI描述重新生成成功')
    } else {
      ElMessage.error(response.message || '重新生成失败')
    }
  } catch (err) {
    ElMessage.error('重新生成失败')
  }
}

const reportItem = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入举报原因', '举报物品', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /\S/,
      inputErrorMessage: '举报原因不能为空'
    })

    const response = await createReport({
      targetType: targetType.value,
      targetId: targetId.value,
      reason
    })

    if (response.code === 200) {
      ElMessage.success('举报成功，等待管理员处理')
    } else {
      ElMessage.error(response.message || '举报失败')
    }
  } catch (err) {
    // 用户取消
  }
}

const startChat = () => {
  // 跳转到消息页面，传递对方用户ID
  router.push({
    path: '/messages',
    query: {
      userId: item.value.userId
    }
  })
}

const toggleClaimed = async () => {
  try {
    const response = await updatePicked(id, {
      status: item.value.status === 1 ? 0 : 1
    })
    if (response.code === 200) {
      item.value.status = item.value.status === 1 ? 0 : 1
      ElMessage.success(item.value.status === 1 ? '标记为已认领成功' : '标记为未认领成功')
      // 询问用户是否要返回首页查看更新后的状态
      ElMessageBox.confirm('是否要返回首页查看更新后的状态？', '操作成功', {
        confirmButtonText: '返回首页',
        cancelButtonText: '留在当前页',
        type: 'success'
      }).then(() => {
        router.push('/')
      }).catch(() => {
        // 用户选择留在当前页
      })
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('标记认领状态失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const toggleFound = async () => {
  try {
    const response = await updateLost(id, {
      status: item.value.status === 1 ? 0 : 1
    })
    if (response.code === 200) {
      item.value.status = item.value.status === 1 ? 0 : 1
      ElMessage.success(item.value.status === 1 ? '标记为已找回成功' : '标记为未找回成功')
      // 询问用户是否要返回首页查看更新后的状态
      ElMessageBox.confirm('是否要返回首页查看更新后的状态？', '操作成功', {
        confirmButtonText: '返回首页',
        cancelButtonText: '留在当前页',
        type: 'success'
      }).then(() => {
        router.push('/')
      }).catch(() => {
        // 用户选择留在当前页
      })
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('标记找回状态失败', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => {
  fetchItem()
  fetchComments()
})
</script>

<style scoped>
.detail-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.item-detail {
  display: flex;
  gap: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.detail-image {
  width: 300px;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.detail-info {
  flex: 1;
}

.detail-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #333;
}

.detail-location,
.detail-time,
.detail-description,
.detail-contact,
.ai-description {
  font-size: 16px;
  margin-bottom: 12px;
  color: #555;
  line-height: 1.5;
}

.ai-description {
  background: #f0f9ff;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #1677ff;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.error-message {
  text-align: center;
  color: red;
  margin: 20px;
}
</style>
