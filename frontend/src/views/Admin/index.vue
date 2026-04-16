<template>
  <div>
    <NavBar />
    <div class="admin-container">
      <div class="admin-sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="statistics">
            <el-icon><DataLine /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="items">
            <el-icon><DocumentCopy /></el-icon>
            <span>物品管理</span>
          </el-menu-item>
          <el-menu-item index="reports">
            <el-icon><Warning /></el-icon>
            <span>举报处理</span>
          </el-menu-item>
          <el-menu-item index="top-requests">
            <el-icon><Star /></el-icon>
            <span>置顶申请</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="admin-content">
        <!-- 数据统计 -->
        <div v-if="activeMenu === 'statistics'" class="stats-section">
          <h2>数据统计</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <h3>{{ statistics.totalPosts }}</h3>
              <p>总发布数量</p>
            </div>
            <div class="stat-card">
              <h3>{{ statistics.totalClaimed }}</h3>
              <p>已找回物品</p>
            </div>
            <div class="stat-card">
              <h3>{{ statistics.activeUsers }}</h3>
              <p>活跃用户数</p>
            </div>
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeMenu === 'users'" class="users-section">
          <h2>用户管理</h2>
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                  {{ scope.row.status === 0 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  type="danger"
                  size="small"
                  @click="banUser(scope.row.id, 1)"
                >
                  封禁
                </el-button>
                <el-button
                  v-else
                  type="success"
                  size="small"
                  @click="banUser(scope.row.id, 0)"
                >
                  解封
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 物品管理 -->
        <div v-if="activeMenu === 'items'" class="items-section">
          <h2>物品管理</h2>
          <el-tabs v-model="itemType" @tab-click="loadItems">
            <el-tab-pane label="失物" name="lost" />
            <el-tab-pane label="拾取" name="picked" />
          </el-tabs>

          <el-table :data="items" style="width: 100%">
            <el-table-column prop="name" label="物品名称" width="150" />
            <el-table-column prop="location" label="地点" width="120" />
            <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                  {{ scope.row.status === 0 ? '未找回' : '已找回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteItem(scope.row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 举报处理 -->
        <div v-if="activeMenu === 'reports'" class="reports-section">
          <h2>举报处理</h2>
          <el-table :data="reports" style="width: 100%">
            <el-table-column prop="reporterUsername" label="举报人" width="120" />
            <el-table-column prop="reason" label="举报原因" width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getReportStatusType(scope.row.status)">
                  {{ getReportStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="举报时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button-group v-if="scope.row.status === 0">
                  <el-button type="success" size="small" @click="handleReport(scope.row.id, 1)">
                    忽略
                  </el-button>
                  <el-button type="danger" size="small" @click="handleReport(scope.row.id, 2)">
                    删除内容
                  </el-button>
                  <el-button type="warning" size="small" @click="handleReport(scope.row.id, 3)">
                    封禁用户
                  </el-button>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 置顶申请 -->
        <div v-if="activeMenu === 'top-requests'" class="top-requests-section">
          <h2>置顶申请</h2>
          <el-table :data="topRequests" style="width: 100%">
            <el-table-column prop="username" label="申请人" width="120" />
            <el-table-column prop="itemName" label="物品名称" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                  {{ scope.row.status === 0 ? '待审核' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button-group v-if="scope.row.status === 0">
                  <el-button type="success" size="small" @click="approveTopRequest(scope.row.id, 1)">
                    同意
                  </el-button>
                  <el-button type="danger" size="small" @click="approveTopRequest(scope.row.id, 2)">
                    拒绝
                  </el-button>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataLine, User, DocumentCopy, Warning, Star } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/admin'
import { banUser as banUserApi } from '@/api/user'
import { getLostList, deleteLost } from '@/api/lost'
import { getPickedList, deletePicked } from '@/api/picked'
import { getPendingReports, handleReport as handleReportApi } from '@/api/report'
import { getPendingTopRequests, approveTopRequest as approveTopRequestApi } from '@/api/top'
import NavBar from '@/components/NavBar.vue'

const activeMenu = ref('statistics')
const itemType = ref('lost')

const statistics = ref({
  totalPosts: 0,
  totalClaimed: 0,
  activeUsers: 0
})

const users = ref([])
const items = ref([])
const reports = ref([])
const topRequests = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getReportStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    case 3: return 'danger'
    default: return ''
  }
}

const getReportStatusText = (status) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '已忽略'
    case 2: return '已删除内容'
    case 3: return '已封禁用户'
    default: return '未知'
  }
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'statistics') {
    loadStatistics()
  } else if (index === 'users') {
    // 这里需要用户管理API，暂时跳过
  } else if (index === 'items') {
    loadItems()
  } else if (index === 'reports') {
    loadReports()
  } else if (index === 'top-requests') {
    loadTopRequests()
  }
}

const loadStatistics = async () => {
  try {
    statistics.value = await getStatistics()
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
}

const loadItems = async () => {
  try {
    if (itemType.value === 'lost') {
      const res = await getLostList({ page: 1, size: 100 })
      items.value = res.records || []
    } else {
      const res = await getPickedList({ page: 1, size: 100 })
      items.value = res.records || []
    }
  } catch (error) {
    ElMessage.error('获取物品列表失败')
  }
}

const loadReports = async () => {
  try {
    reports.value = await getPendingReports()
  } catch (error) {
    ElMessage.error('获取举报列表失败')
  }
}

const loadTopRequests = async () => {
  try {
    topRequests.value = await getPendingTopRequests()
  } catch (error) {
    ElMessage.error('获取置顶申请失败')
  }
}

const banUser = async (userId, status) => {
  try {
    await banUserApi(userId, status)
    ElMessage.success('操作成功')
    // 重新加载用户列表
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteItem = async (itemId) => {
  try {
    if (itemType.value === 'lost') {
      await deleteLost(itemId)
    } else {
      await deletePicked(itemId)
    }
    ElMessage.success('删除成功')
    loadItems()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleReport = async (reportId, status) => {
  try {
    await handleReportApi(reportId, status)
    ElMessage.success('处理成功')
    loadReports()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

const approveTopRequest = async (requestId, approveStatus) => {
  try {
    await approveTopRequestApi(requestId, approveStatus)
    ElMessage.success('处理成功')
    loadTopRequests()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: calc(100vh - 80px);
  background: #f0f2f5;
}

.admin-sidebar {
  width: 200px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.admin-content {
  flex: 1;
  padding: 24px;
}

.admin-content h2 {
  margin-bottom: 24px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  font-size: 32px;
  color: #1677ff;
  margin: 0 0 8px 0;
}

.stat-card p {
  color: #666;
  margin: 0;
}
</style>
