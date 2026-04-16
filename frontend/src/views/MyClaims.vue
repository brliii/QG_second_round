<template>
  <div>
    <NavBar />
    <div class="claims-container">
      <div class="claims-card">
        <h2>我的认领申请</h2>

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
              <p><strong>申请时间:</strong> {{ formatTime(claim.createTime) }}</p>
              <p><strong>验证答案:</strong> {{ claim.verifyAnswer }}</p>
              <p v-if="claim.pickerComment"><strong>处理意见:</strong> {{ claim.pickerComment }}</p>
              <p v-if="claim.status === 1 && claim.pickupCode">
                <strong>取件码:</strong>
                <span class="claim-code">{{ claim.pickupCode }}</span>
                <el-button type="primary" size="small" @click="copyCode(claim.pickupCode)">
                  复制
                </el-button>
              </p>
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
import { getMyClaims } from '@/api/claim'
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

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('取件码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

onMounted(async () => {
  try {
    const response = await getMyClaims()
    if (response.code === 200 && response.data) {
      claims.value = response.data
    } else {
      claims.value = []
      ElMessage.error(response.message || '获取认领申请失败')
    }
  } catch (error) {
    ElMessage.error('获取认领申请失败')
  }
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

.claim-code {
  font-family: monospace;
  font-size: 18px;
  font-weight: bold;
  color: #1677ff;
  background: #e6f7ff;
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 8px;
}
</style>
