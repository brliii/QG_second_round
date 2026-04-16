<template>
  <div>
    <NavBar />
    <div class="profile-container">
      <div class="profile-card">
        <h2>个人中心</h2>

        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="info">
            <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userForm.email" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="userForm.phone" />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
              </el-form-item>

              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="userForm.avatar" :src="'http://localhost:8080' + userForm.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  <div v-if="!userForm.avatar" class="avatar-uploader-text">点击上传头像</div>
                </el-upload>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateUserInfo" :loading="loading">
                  更新信息
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 个人信息 -->
          <el-tab-pane label="个人信息" name="profile">
            <div class="profile-info">
              <div class="avatar-section">
                <img :src="userStore.userInfo.avatar ? 'http://localhost:8080' + userStore.userInfo.avatar : '/default-avatar.png'" alt="头像" class="profile-avatar" />
              </div>
              <div class="info-section">
                <p><strong>用户名:</strong> {{ userStore.userInfo.username }}</p>
                <p><strong>邮箱:</strong> {{ userStore.userInfo.email }}</p>
                <p><strong>手机号:</strong> {{ userStore.userInfo.phone }}</p>
                <p><strong>昵称:</strong> {{ userStore.userInfo.nickname }}</p>
              </div>
            </div>
          </el-tab-pane>

          <!-- 修改密码 -->
          <el-tab-pane label="修改密码" name="password">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="6-20位字符"
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="loading">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 我的发布 -->
          <el-tab-pane label="我的发布" name="posts">
            <div class="posts-section">
              <h3>失物发布</h3>
              <div v-if="myLostItems.length === 0" class="no-data">暂无失物发布</div>
              <div v-else class="posts-grid">
                <ItemCard
                  v-for="item in myLostItems"
                  :key="item.id"
                  :item="item"
                  type="lost"
                />
              </div>

              <h3 style="margin-top: 40px;">拾取发布</h3>
              <div v-if="myPickedItems.length === 0" class="no-data">暂无拾取发布</div>
              <div v-else class="posts-grid">
                <ItemCard
                  v-for="item in myPickedItems"
                  :key="item.id"
                  :item="item"
                  type="picked"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 认领申请 -->
          <el-tab-pane label="认领申请" name="claims">
            <div class="claims-section">
              <el-tabs v-model="activeClaimsTab" @tab-click="handleClaimsTabClick" class="claims-subtabs">
                <!-- 我的认领申请 -->
                <el-tab-pane label="我的认领申请" name="my-claims">
                  <div v-if="myClaims.length === 0" class="no-data">暂无认领申请</div>
                  <div v-else class="claims-list">
                    <div v-for="claim in myClaims" :key="claim.id" class="claim-item">
                      <div class="claim-header">
                        <h4>{{ claim.pickedItemName }}</h4>
                        <el-tag :type="getClaimStatusType(claim.status)">
                          {{ getClaimStatusText(claim.status) }}
                        </el-tag>
                      </div>
                      <div class="claim-info">
                        <p><strong>申请时间:</strong> {{ formatTime(claim.createTime) }}</p>
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
                </el-tab-pane>

                <!-- 我收到的认领申请 -->
                <el-tab-pane label="我收到的认领申请" name="received-claims">
                  <div v-if="receivedClaims.length === 0" class="no-data">暂无收到的认领申请</div>
                  <div v-else class="claims-list">
                    <div v-for="claim in receivedClaims" :key="claim.id" class="claim-item">
                      <div class="claim-header">
                        <h4>{{ claim.pickedItemName }}</h4>
                        <el-tag :type="getClaimStatusType(claim.status)">
                          {{ getClaimStatusText(claim.status) }}
                        </el-tag>
                      </div>
                      <div class="claim-info">
                        <p><strong>申请人:</strong> {{ claim.claimantUsername }}</p>
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
                        <div v-if="claim.status === 0" class="claim-actions">
                          <el-button type="primary" size="small" @click="approveClaim(claim.id)">
                            同意
                          </el-button>
                          <el-button type="danger" size="small" @click="rejectClaim(claim.id)">
                            拒绝
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getLostList } from '@/api/lost'
import { getPickedList } from '@/api/picked'
import { getMyClaims, getClaimsByPicker, processClaim } from '@/api/claim'
import { uploadImage } from '@/api/upload'
import NavBar from '@/components/NavBar.vue'
import ItemCard from '@/components/ItemCard.vue'

const userStore = useUserStore()

const activeTab = ref('info')
const activeClaimsTab = ref('my-claims')
const loading = ref(false)

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  nickname: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const myLostItems = ref([])
const myPickedItems = ref([])
const myClaims = ref([])
const receivedClaims = ref([])

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const userFormRef = ref()
const passwordFormRef = ref()

const uploadAvatar = async (options) => {
  try {
    const response = await uploadImage(options.file)
    if (response.code === 200 && response.data) {
      userForm.avatar = response.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (err) {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const updateUserInfo = async () => {
  try {
    await userFormRef.value.validate()
    loading.value = true

    const userData = {
      nickname: userForm.nickname,
      avatar: userForm.avatar,
      phone: userForm.phone
    }
    const success = await userStore.updateUserInfo(userData)
    if (success) {
      // 重新获取用户信息
      await userStore.getUserInfo()
    }
  } catch (error) {
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}

const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    loading.value = true

    const success = await userStore.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmNewPassword: passwordForm.confirmPassword
    })

    if (success) {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (error) {
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}

const loadMyPosts = async () => {
  try {
    // 加载我的失物
    const lostResponse = await getLostList({
      page: 1,
      size: 100 // 加载更多
    })
    if (lostResponse.code === 200 && lostResponse.data) {
      myLostItems.value = lostResponse.data.records.filter(item => item.userId === userStore.userInfo.id) || []
    } else {
      myLostItems.value = []
    }

    // 加载我的拾取
    const pickedResponse = await getPickedList({
      page: 1,
      size: 100
    })
    if (pickedResponse.code === 200 && pickedResponse.data) {
      myPickedItems.value = pickedResponse.data.records.filter(item => item.userId === userStore.userInfo.id) || []
    } else {
      myPickedItems.value = []
    }
  } catch (error) {
    console.error('加载我的发布失败', error)
    myLostItems.value = []
    myPickedItems.value = []
  }
}

const handleTabClick = (tab) => {
  if (tab.props.name === 'posts') {
    loadMyPosts()
  } else if (tab.props.name === 'claims') {
    if (activeClaimsTab.value === 'my-claims') {
      loadMyClaims()
    } else {
      loadReceivedClaims()
    }
  }
}

const handleClaimsTabClick = (tab) => {
  if (tab.props.name === 'my-claims') {
    loadMyClaims()
  } else if (tab.props.name === 'received-claims') {
    loadReceivedClaims()
  }
}

const loadMyClaims = async () => {
  try {
    const response = await getMyClaims()
    if (response.code === 200 && response.data) {
      myClaims.value = response.data
    } else {
      myClaims.value = []
    }
  } catch (error) {
    console.error('加载我的认领申请失败', error)
    myClaims.value = []
  }
}

const loadReceivedClaims = async () => {
  try {
    const response = await getClaimsByPicker()
    if (response.code === 200 && response.data) {
      receivedClaims.value = response.data
    } else {
      receivedClaims.value = []
    }
  } catch (error) {
    console.error('加载我收到的认领申请失败', error)
    receivedClaims.value = []
  }
}

const approveClaim = async (claimId) => {
  try {
    const response = await processClaim(claimId, {
      status: 1,
      comment: '同意认领申请'
    })
    if (response.code === 200) {
      ElMessage.success('已同意认领申请')
      loadReceivedClaims()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const rejectClaim = async (claimId) => {
  try {
    const response = await processClaim(claimId, {
      status: 2,
      comment: '拒绝认领申请'
    })
    if (response.code === 200) {
      ElMessage.success('已拒绝认领申请')
      loadReceivedClaims()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getClaimStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

const getClaimStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
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
  // 确保用户信息已加载
  if (!userStore.userInfo) {
    await userStore.getUserInfo()
  }
  // 初始化用户信息
  Object.assign(userForm, userStore.userInfo)
  // 如果当前标签是我的发布，加载数据
  if (activeTab.value === 'posts') {
    await loadMyPosts()
  }
  // 如果当前标签是认领申请，加载对应数据
  if (activeTab.value === 'claims') {
    if (activeClaimsTab.value === 'my-claims') {
      await loadMyClaims()
    } else {
      await loadReceivedClaims()
    }
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.profile-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-card h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.posts-section h3 {
  color: #333;
  margin-bottom: 16px;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 32px;
}

.avatar-section {
  text-align: center;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.info-section p {
  margin: 8px 0;
  font-size: 16px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
}

.avatar-uploader-text {
  font-size: 12px;
  color: #8c939d;
}

.claims-section h3 {
  color: #333;
  margin-bottom: 16px;
}

.claims-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.claim-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.claim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.claim-header h4 {
  margin: 0;
  color: #333;
}

.claim-info p {
  margin: 6px 0;
  color: #555;
  line-height: 1.5;
}

.claim-code {
  font-family: monospace;
  font-size: 16px;
  font-weight: bold;
  color: #1677ff;
  background: #e6f7ff;
  padding: 4px 8px;
  border-radius: 4px;
  margin-right: 8px;
}

.claims-subtabs {
  margin-bottom: 20px;
}

.claim-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.claims-section h3 {
  color: #333;
  margin-bottom: 16px;
}
</style>
