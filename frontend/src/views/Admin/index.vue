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
                <el-tag :type="scope.row.status === 2 ? 'info' : (scope.row.status === 0 ? 'warning' : 'success')">
                  {{ scope.row.status === 2 ? '已删除' : (itemType === 'lost' ? (scope.row.status === 0 ? '未找回' : '已找回') : (scope.row.status === 0 ? '未认领' : '已认领')) }}
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
                  :disabled="scope.row.status === 2"
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
            <el-table-column prop="reportType" label="类型" width="100">
              <template #default="scope">
                {{ getReportTypeText(scope.row.reportType) }}
              </template>
            </el-table-column>
            <el-table-column prop="targetId" label="被举报ID" width="120" />
            <el-table-column prop="targetInfo" label="被举报对象" width="200" show-overflow-tooltip />
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
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewReportDetail(scope.row)" style="margin-right: 8px">
                  查看详情
                </el-button>
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
            <el-table-column prop="itemType" label="物品类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.itemType === 0 ? 'warning' : 'success'">
                  {{ scope.row.itemType === 0 ? '失物' : '拾取' }}
                </el-tag>
              </template>
            </el-table-column>
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

  <!-- 举报详情对话框 -->
  <el-dialog
    v-model="reportDetailDialogVisible"
    :title="`举报详情 - ${selectedReport ? getReportTypeText(selectedReport.reportType) : ''}`"
    width="600px"
  >
    <div v-if="selectedReport">
      <el-form label-width="80px">
        <el-form-item label="举报人">
          <span>{{ selectedReport.reporterUsername }}</span>
        </el-form-item>
        <el-form-item label="举报类型">
          <span>{{ getReportTypeText(selectedReport.reportType) }}</span>
        </el-form-item>
        <el-form-item label="被举报ID">
          <span>{{ selectedReport.reportedId }}</span>
        </el-form-item>
        <el-form-item label="被举报对象">
          <span>{{ selectedReport.targetInfo || '未知' }}</span>
        </el-form-item>
        <el-form-item label="举报原因">
          <span>{{ selectedReport.reason }}</span>
        </el-form-item>
        <el-form-item label="举报时间">
          <span>{{ formatTime(selectedReport.createTime) }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="getReportStatusType(selectedReport.status)">
            {{ getReportStatusText(selectedReport.status) }}
          </el-tag>
        </el-form-item>
        
        <!-- 详细信息部分 -->
        <el-form-item label="详细信息">
          <el-skeleton :loading="reportDetailLoading" animated>
            <template #template>
              <el-skeleton-item variant="p" style="margin-bottom: 16px" />
              <el-skeleton-item variant="p" style="margin-bottom: 16px" />
              <el-skeleton-item variant="p" />
            </template>
            <div v-if="!reportDetailLoading">
              <!-- 失物详情 -->
              <div v-if="selectedReport.reportType === 0 && reportDetailData">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="物品名称">{{ reportDetailData.name }}</el-descriptions-item>
                  <el-descriptions-item label="丢失地点">{{ reportDetailData.location }}</el-descriptions-item>
                  <el-descriptions-item label="丢失时间">{{ formatTime(reportDetailData.lostTime) }}</el-descriptions-item>
                  <el-descriptions-item label="物品描述">{{ reportDetailData.description }}</el-descriptions-item>
                  <el-descriptions-item label="联系方式">{{ reportDetailData.contact }}</el-descriptions-item>
                  <el-descriptions-item label="发布时间">{{ formatTime(reportDetailData.createTime) }}</el-descriptions-item>
                </el-descriptions>
              </div>
              
              <!-- 拾取详情 -->
              <div v-else-if="selectedReport.reportType === 1 && reportDetailData">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="物品名称">{{ reportDetailData.name }}</el-descriptions-item>
                  <el-descriptions-item label="拾取地点">{{ reportDetailData.location }}</el-descriptions-item>
                  <el-descriptions-item label="拾取时间">{{ formatTime(reportDetailData.pickTime) }}</el-descriptions-item>
                  <el-descriptions-item label="物品描述">{{ reportDetailData.description }}</el-descriptions-item>
                  <el-descriptions-item label="联系方式">{{ reportDetailData.contact }}</el-descriptions-item>
                  <el-descriptions-item label="发布时间">{{ formatTime(reportDetailData.createTime) }}</el-descriptions-item>
                </el-descriptions>
              </div>
              
              <!-- 评论详情 -->
              <div v-else-if="selectedReport.reportType === 2 && reportDetailData">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="评论内容">{{ reportDetailData.content }}</el-descriptions-item>
                  <el-descriptions-item label="评论者">{{ reportDetailData.fromUsername || '未知' }}</el-descriptions-item>
                  <el-descriptions-item label="接收者">{{ reportDetailData.toUsername || '无' }}</el-descriptions-item>
                  <el-descriptions-item label="评论时间">{{ formatTime(reportDetailData.createTime) }}</el-descriptions-item>
                </el-descriptions>
              </div>
              
              <!-- 用户详情 -->
              <div v-else-if="selectedReport.reportType === 3">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户名">{{ selectedReport.targetInfo || '未知' }}</el-descriptions-item>
                  <el-descriptions-item label="用户ID">{{ selectedReport.reportedId }}</el-descriptions-item>
                </el-descriptions>
              </div>
              
              <!-- 无详情 -->
              <div v-else>
                <el-alert
                  title="无法获取详细信息"
                  type="warning"
                  show-icon
                />
              </div>
            </div>
          </el-skeleton>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataLine, User, DocumentCopy, Warning, Star } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/admin'
import { banUser as banUserApi, getUserList } from '@/api/user'
import { getLostList, deleteLost, getLostDetail } from '@/api/lost'
import { getPickedList, deletePicked, getPickedDetail } from '@/api/picked'
import { getPendingReports, handleReport as handleReportApi } from '@/api/report'
import { getPendingTopRequests, approveTopRequest as approveTopRequestApi } from '@/api/top'
import { getCommentDetail } from '@/api/comment'
import NavBar from '@/components/NavBar.vue'

const activeMenu = ref('statistics')
const itemType = ref('lost')

// 举报详情相关状态
const reportDetailDialogVisible = ref(false)
const selectedReport = ref(null)
const reportDetailData = ref(null)
const reportDetailLoading = ref(false)

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

const getReportTypeText = (type) => {
  switch (type) {
    case 0: return '失物'
    case 1: return '拾取'
    case 2: return '评论'
    case 3: return '用户'
    default: return '未知'
  }
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'statistics') {
    loadStatistics()
  } else if (index === 'users') {
    loadUsers()
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
    const response = await getStatistics()
    if (response.code === 200 && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('获取统计数据失败')
  }
}

const loadUsers = async () => {
  try {
    const response = await getUserList()
    if (response.code === 200 && response.data) {
      users.value = response.data || []
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('获取用户列表失败')
  }
}

const loadItems = async () => {
  try {
    if (itemType.value === 'lost') {
      const response = await getLostList({ page: 1, size: 100, includeAllStatus: true })
      if (response.code === 200 && response.data) {
        items.value = response.data.records || []
      }
    } else {
      const response = await getPickedList({ page: 1, size: 100, includeAllStatus: true })
      if (response.code === 200 && response.data) {
        items.value = response.data.records || []
      }
    }
  } catch (error) {
    console.error('获取物品列表失败', error)
    ElMessage.error('获取物品列表失败')
  }
}

const loadReports = async () => {
  try {
    const response = await getPendingReports()
    console.log('举报列表响应:', response)
    if (response && response.code === 200 && response.data) {
      // 映射后端返回的字段到前端需要的字段
      reports.value = response.data.map(report => ({
        id: report.id,
        reporterUsername: report.reporterUsername || '未知',
        reportType: report.targetType ?? 0, // 使用 targetType 而不是 reportType
        reportedId: report.targetId ?? 0, // 使用 targetId 而不是 reportedId
        targetId: report.targetId ?? 0, // 保留 targetId 字段
        targetInfo: report.targetInfo || '', // 添加 targetInfo 字段
        reason: report.reason || '',
        status: report.status || 0,
        createTime: report.createTime || new Date().toISOString()
      }))
      console.log('处理后的举报数据:', reports.value)
    }
  } catch (error) {
    console.error('获取举报列表失败', error)
    ElMessage.error('获取举报列表失败')
  }
}

const loadTopRequests = async () => {
  try {
    const response = await getPendingTopRequests(1, 100)
    console.log('置顶申请响应:', response)
    // 检查响应结构
    if (response && response.code === 200 && response.data) {
      // 处理分页格式
      let requests = []
      if (Array.isArray(response.data)) {
        requests = response.data
      } else if (response.data.records) {
        requests = response.data.records
      }
      console.log('置顶申请数据:', requests)
      // 确保数据结构正确
      topRequests.value = requests.map(request => ({
        id: request.id,
        username: request.username || request.userName || '未知用户',
        itemType: request.itemType ?? 0,
        itemName: request.itemName || request.item?.name || `物品ID: ${request.itemId}`,
        status: request.status || 0,
        createTime: request.createTime || request.createdAt || request.requestTime || new Date().toISOString()
      }))
    } else {
      console.warn('置顶申请响应格式异常:', response)
      topRequests.value = []
    }
  } catch (error) {
    console.error('获取置顶申请失败', error)
    ElMessage.error('获取置顶申请失败')
  }
}

const banUser = async (userId, status) => {
  try {
    const response = await banUserApi(userId, status)
    if (response.code === 200) {
      ElMessage.success('操作成功')
      // 重新加载用户列表
      loadUsers()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

const deleteItem = async (itemId) => {
  try {
    let response
    if (itemType.value === 'lost') {
      response = await deleteLost(itemId)
    } else {
      response = await deletePicked(itemId)
    }
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadItems()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败', error)
    ElMessage.error('删除失败')
  }
}

const handleReport = async (reportId, status) => {
  try {
    const response = await handleReportApi(reportId, status)
    if (response.code === 200) {
      ElMessage.success('处理成功')
      loadReports()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败', error)
    ElMessage.error('处理失败')
  }
}

const viewReportDetail = async (report) => {
  try {
    selectedReport.value = report
    reportDetailData.value = null
    reportDetailLoading.value = true
    
    // 根据举报类型加载相关数据
    if (report.reportType === 0) {
      // 失物
      const response = await getLostDetail(report.reportedId)
      if (response.code === 200 && response.data) {
        reportDetailData.value = response.data
      }
    } else if (report.reportType === 1) {
      // 拾取
      const response = await getPickedDetail(report.reportedId)
      if (response.code === 200 && response.data) {
        reportDetailData.value = response.data
      }
    } else if (report.reportType === 2) {
      // 评论
      const response = await getCommentDetail(report.reportedId)
      if (response.code === 200 && response.data) {
        reportDetailData.value = response.data
      }
    } else if (report.reportType === 3) {
      // 用户 - 这里可以添加获取用户详情的逻辑
      // 暂时使用 targetInfo 字段显示用户名
    }
    
    reportDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取举报详情失败', error)
    ElMessage.error('获取详情失败')
  } finally {
    reportDetailLoading.value = false
  }
}

const approveTopRequest = async (requestId, approveStatus) => {
  try {
    const response = await approveTopRequestApi(requestId, approveStatus)
    if (response.code === 200) {
      ElMessage.success('处理成功')
      loadTopRequests()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败', error)
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
