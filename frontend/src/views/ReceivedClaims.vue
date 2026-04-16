<template>
  <div>
    <NavBar />
    <div class="claims-container">
      <div class="claims-card">
        <h2>收到的认领申请</h2>

        <div v-if="claims.length === 0" class="no-data">
          暂无认领申请
        </div>

        <div v-else class="claims-list">
          <div v-for="claim in claims" :key="claim.id" class="claim-item">
            <div class="claim-header">
              <h3>{{ claim.pickedItemName }}</h3>
              <el-tag :type="getStatusType(claim.status)">
                {{ getStatusText(claim.status) }}
              </el-tag>
            </div>

            <div class="claim-info">
              <p><strong>申请人:</strong> {{ claim.claimantUsername }}</p>
              <p><strong>申请时间:</strong> {{ formatTime(claim.createTime) }}</p>
              <p><strong>验证答案:</strong> {{ claim.verifyAnswer }}</p>
              <p v-if="claim.comment"><strong>处理意见:</strong> {{ claim.comment }}</p>
            </div>

            <div v-if="claim.status === 0" class="claim-actions">
              <el-button type="success" size="small" @click="processClaim(claim.id, 1)">
                通过申请
              </el-button>
              <el-button type="danger" size="small" @click="processClaim(claim.id, 2)">
                拒绝申请
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClaimsByPicker, processClaim as processClaimApi } from '@/api/claim'
import NavBar from '@/components/NavBar.vue'

const claims = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const processClaim = async (claimId, status) => {
  const actionText = status === 1 ? '通过' : '拒绝'
  const confirmText = `确定要${actionText}这个认领申请吗？`

  try {
    await ElMessageBox.confirm(confirmText, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 1 ? 'success' : 'warning'
    })

    let comment = ''
    if (status === 2) {
      const { value } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝申请', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPattern: /\S/,
        inputErrorMessage: '拒绝理由不能为空'
      })
      comment = value
    }

    await processClaimApi(claimId, { status, comment })
    ElMessage.success('处理成功')

    // 重新加载列表
    loadClaims()
  } catch (error) {
    // 用户取消操作
  }
}

const loadClaims = async () => {
  try {
    claims.value = await getClaimsByPicker()
  } catch (error) {
    ElMessage.error('获取认领申请失败')
  }
}

onMounted(() => {
  loadClaims()
})
</script>

<style scoped>
.claims-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.claims-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.claims-card h2 {
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

.claims-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.claim-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.claim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.claim-header h3 {
  margin: 0;
  color: #333;
}

.claim-info p {
  margin: 8px 0;
  color: #555;
  line-height: 1.5;
}

.claim-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
</style>
