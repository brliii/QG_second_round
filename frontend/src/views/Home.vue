<template>
  <div>
    <NavBar />
    <div class="home-container">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-radio-group v-model="type" @change="resetAndFetch">
          <el-radio-button value="lost">失物</el-radio-button>
          <el-radio-button value="picked">拾取</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="location"
          placeholder="输入地点"
          clearable
          style="width: 200px;"
          @clear="doSearch"
          @keyup.enter="doSearch"
        />
        <el-input
          v-model="keyword"
          placeholder="搜索物品名称"
          clearable
          style="width: 200px;"
          @keyup.enter="doSearch"
        />
        <el-date-picker
          v-model="startTime"
          type="datetime"
          placeholder="起始时间"
          clearable
          style="width: 200px;"
        />
        <el-date-picker
          v-model="endTime"
          type="datetime"
          placeholder="结束时间"
          clearable
          style="width: 200px;"
        />
        <el-button type="primary" @click="doSearch">搜索</el-button>
        <span v-if="isAiSearch" style="margin-left: 10px; color: #1677ff;">✨ AI 智能排序结果</span>
      </div>

      <!-- 物品列表 -->
      <div class="items-grid">
        <ItemCard
          v-for="item in list"
          :key="item.id"
          :item="item"
          :type="type"
        />
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="!isAiSearch"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchList"
      />

      <!-- 发布按钮 -->
      <el-button v-if="userStore.isLoggedIn" type="primary" circle class="fab" @click="showPublishMenu">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getLostList } from '@/api/lost'
import { getPickedList, searchPickedByDescription } from '@/api/picked'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import ItemCard from '@/components/ItemCard.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const type = ref('lost')
const location = ref('')
const keyword = ref(route.query.keyword || '')
const startTime = ref('')
const endTime = ref('')
const page = ref(1)
const size = ref(4)
const list = ref([])
const total = ref(0)
const isAiSearch = ref(false)

const resetAndFetch = () => {
  page.value = 1
  fetchList()
}

const fetchList = async () => {
  if (type.value === 'lost') {
    const params = {
      location: location.value,
      name: keyword.value,
      startTime: startTime.value ? new Date(startTime.value).toISOString() : '',
      endTime: endTime.value ? new Date(endTime.value).toISOString() : '',
      page: page.value,
      size: size.value,
      sortBy: 'createTime',
    }
    try {
      const response = await getLostList(params)
      if (response.code === 200 && response.data) {
        list.value = response.data.records || []
        total.value = response.data.total || 0
        isAiSearch.value = false
      } else {
        console.error('获取失物列表失败', response.message)
        list.value = []
        total.value = 0
      }
    } catch (err) {
      console.error('获取失物列表失败', err)
      list.value = []
      total.value = 0
    }
  } else {
    if (keyword.value && keyword.value.trim()) {
      try {
        const response = await searchPickedByDescription(keyword.value)
        if (response.code === 200 && response.data) {
          list.value = response.data || []
          total.value = list.value.length
          isAiSearch.value = true
        } else {
          console.error('AI 搜索失败', response.message)
          list.value = []
          total.value = 0
          isAiSearch.value = false
        }
      } catch (err) {
        console.error('AI 搜索失败', err)
        list.value = []
        total.value = 0
        isAiSearch.value = false
      }
    } else {
      const params = {
        location: location.value,
        name: keyword.value,
        startTime: startTime.value ? new Date(startTime.value).toISOString() : '',
        endTime: endTime.value ? new Date(endTime.value).toISOString() : '',
        page: page.value,
        size: size.value,
        sortBy: 'createTime',
      }
      try {
        const response = await getPickedList(params)
        if (response.code === 200 && response.data) {
          list.value = response.data.records || []
          total.value = response.data.total || 0
          isAiSearch.value = false
        } else {
          console.error('获取拾取列表失败', response.message)
          list.value = []
          total.value = 0
        }
      } catch (err) {
        console.error('获取拾取列表失败', err)
        list.value = []
        total.value = 0
      }
    }
  }
}

const doSearch = () => {
  page.value = 1
  fetchList()
}

const showPublishMenu = () => {
  ElMessageBox.confirm('发布失物还是拾取？', '选择类型', {
    distinguishCancelAndClose: true,
    confirmButtonText: '失物',
    cancelButtonText: '拾取',
  })
    .then(() => {
      // 用户点击了"失物"按钮
      router.push('/lost/create')
    })
    .catch((action) => {
      if (action === 'cancel') {
        // 用户点击了"拾取"按钮
        router.push('/picked/create')
      } else {
        // 点击×或空白处时返回到主页
        router.push('/')
      }
    })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 64px;
  height: 64px;
  font-size: 28px;
}
</style>
