<template>
  <div>
    <NavBar />
    <div class="claim-container">
      <div class="claim-card">
        <h2>认领物品</h2>
        <el-form :model="claimForm" :rules="rules" ref="claimFormRef" label-width="100px">
          <el-form-item label="验证答案" prop="verifyAnswer">
            <el-input
              v-model="claimForm.verifyAnswer"
              placeholder="请输入验证答案（用于确认物品归属）"
            />
            <div class="form-tip">
              💡 提示：验证答案是物品的特征信息，只有失主知道的细节
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitClaim" :loading="loading">
              提交认领申请
            </el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createClaim } from '@/api/claim'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const claimForm = reactive({
  verifyAnswer: ''
})

const rules = {
  verifyAnswer: [
    { required: true, message: '请输入验证答案', trigger: 'blur' },
    { min: 1, max: 100, message: '验证答案长度在1到100个字符', trigger: 'blur' }
  ]
}

const claimFormRef = ref()

const submitClaim = async () => {
  try {
    await claimFormRef.value.validate()
    loading.value = true

    // 兼容 query 和 params 两种传参方式
    const pickedItemId = route.query.pickedItemId || route.params.pickedId
    if (!pickedItemId) {
      ElMessage.error('缺少物品ID，请从正确的入口进入')
      return
    }

    await createClaim({
      pickedItemId: parseInt(pickedItemId),
      verifyAnswer: claimForm.verifyAnswer
    })

    ElMessage.success('认领申请已提交，请等待发布者审核')
    router.push('/my-claims')
  } catch (error) {
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.claim-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.claim-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.claim-card h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.form-tip {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}
</style>
