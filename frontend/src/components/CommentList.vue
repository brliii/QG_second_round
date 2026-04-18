<template>
  <div class="comment-section">
    <h3>评论</h3>
    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <strong>{{ comment.fromUsername }}</strong>
          <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div v-if="comment.toUsername" class="comment-reply">
          回复 @{{ comment.toUsername }}
        </div>
        <div class="comment-actions">
          <el-button v-if="userStore.isLoggedIn && userStore.userInfo?.username !== comment.fromUsername" type="text" size="small" @click="reportComment(comment)">
            举报
          </el-button>
        </div>
      </div>
    </div>
    <div v-if="showForm" class="comment-form">
      <el-form :model="commentForm" :rules="rules" ref="commentFormRef">
        <el-form-item prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitComment" :loading="submitting">
            发送评论
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendComment } from '@/api/comment'
import { createReport } from '@/api/report'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const props = defineProps({
  comments: {
    type: Array,
    default: () => []
  },
  targetType: {
    type: Number,
    required: true
  },
  targetId: {
    type: Number,
    required: true
  },
  showForm: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['comment-added'])

const commentForm = ref({ content: '' })
const commentFormRef = ref()
const submitting = ref(false)

const rules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论内容长度在1到500个字符', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const submitComment = async () => {
  try {
    await commentFormRef.value.validate()
    submitting.value = true

    const data = {
      targetType: props.targetType,
      targetId: props.targetId,
      content: commentForm.value.content
    }

    const response = await sendComment(data)
    if (response.code === 200) {
      ElMessage.success('评论发送成功')
      commentForm.value.content = ''
      emit('comment-added')
    } else {
      ElMessage.error(response.message || '评论发送失败')
    }
  } catch (error) {
    ElMessage.error('评论发送失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const reportComment = async (comment) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入举报原因', '举报评论', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /\S/,
      inputErrorMessage: '举报原因不能为空'
    })

    const response = await createReport({
      targetType: 2, // 2 表示评论
      targetId: comment.id,
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
</script>

<style scoped>
.comment-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-section h3 {
  margin: 0 0 16px 0;
  color: #333;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-item {
  background: white;
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header strong {
  color: #1677ff;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 4px;
}

.comment-reply {
  color: #666;
  font-size: 14px;
  font-style: italic;
}

.comment-actions {
  margin-top: 8px;
  text-align: right;
}

.comment-form {
  background: white;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}
</style>
