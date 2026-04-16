<template>
  <div>
    <NavBar />
    <div class="create-container">
      <div class="create-card">
        <h2>{{ isEdit ? '编辑拾取信息' : '发布拾取信息' }}</h2>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="物品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入物品名称" />
          </el-form-item>

          <el-form-item label="拾取地点" prop="location">
            <el-input v-model="form.location" placeholder="请输入拾取地点" />
          </el-form-item>

          <el-form-item label="拾取时间" prop="pickTime">
            <el-date-picker
              v-model="form.pickTime"
              type="datetime"
              placeholder="选择拾取时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="物品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述物品特征、颜色、大小等信息"
            />
          </el-form-item>

          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="form.contact" placeholder="请输入联系方式（手机号/邮箱等）" />
          </el-form-item>

          <el-form-item label="可见性设置">
            <el-radio-group v-model="form.visibilityPreset">
              <el-radio :value="0">公开（所有人可见）</el-radio>
              <el-radio :value="1">隐藏联系方式（登录用户可见）</el-radio>
              <el-radio :value="2">仅注册用户可见</el-radio>
              <el-radio :value="3">仅发布者本人可见</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="认领流程">
            <el-radio-group v-model="form.enableClaimWorkflow">
              <el-radio :value="true">启用认领申请流程</el-radio>
              <el-radio :value="false">直接显示联系方式</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="物品图片">
            <el-upload
              ref="uploadRef"
              :action="''"
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="false"
              accept="image/*"
            >
              <div v-if="!imagePreview" class="upload-area">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div>点击上传图片</div>
              </div>
              <div v-else class="image-preview">
                <img :src="imagePreview" alt="预览" />
                <div class="preview-overlay">
                  <el-button type="danger" size="small" @click.stop="removeImage">
                    删除
                  </el-button>
                </div>
              </div>
            </el-upload>
          </el-form-item>

          <el-form-item label="置顶申请">
            <el-checkbox v-model="form.applyTop">申请置顶（需管理员审核）</el-checkbox>
          </el-form-item>

          <el-form-item v-if="!isEdit">
            <el-button type="success" @click="generateAiDescription" :loading="aiLoading">
              生成AI描述
            </el-button>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              {{ isEdit ? '更新' : '发布' }}
            </el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createPicked, updatePicked, getPickedDetail, generateAiDesc } from '@/api/picked'
import { applyTop } from '@/api/top'
import { uploadImage } from '@/api/upload'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()

const isEdit = ref(false)
const loading = ref(false)
const aiLoading = ref(false)
const imagePreview = ref('')
const selectedFile = ref(null)
const uploadRef = ref()

const form = reactive({
  name: '',
  location: '',
  pickTime: '',
  description: '',
  contact: '',
  imageUrl: '',
  visibilityPreset: 0,
  enableClaimWorkflow: true,
  applyTop: false
})

const rules = {
  name: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在1到50个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入拾取地点', trigger: 'blur' }
  ],
  pickTime: [
    { required: true, message: '请选择拾取时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入物品描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

const formRef = ref()

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const removeImage = () => {
  imagePreview.value = ''
  selectedFile.value = null
  uploadRef.value.clearFiles()
}

const generateAiDescription = async () => {
  if (!form.name || !form.description) {
    ElMessage.warning('请先填写物品名称和描述')
    return
  }

  aiLoading.value = true
  try {
    const response = await generateAiDesc(form.name, form.description)
    if (response.code === 200 && response.data) {
      form.description = response.data
      ElMessage.success('AI描述生成成功')
    } else {
      // 不再使用前端拼接，直接提示失败
      ElMessage.error(response.message || 'AI描述生成失败，请稍后重试')
    }
  } catch (error) {
    console.error('AI描述生成错误:', error)
    // 去掉备用描述，只提示错误
    ElMessage.error('AI服务调用失败，请检查网络或稍后重试')
  } finally {
    aiLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 上传图片
    if (selectedFile.value) {
      try {
        const response = await uploadImage(selectedFile.value)
        if (response.code === 200 && response.data) {
          form.imageUrl = response.data
        } else {
          ElMessage.error(response.message || '图片上传失败')
          return
        }
      } catch (error) {
        ElMessage.error('图片上传失败')
        return
      }
    }

    // 将布尔类型转换为整数类型
    form.enableClaimWorkflow = form.enableClaimWorkflow ? 1 : 0

    let itemId
    if (isEdit.value) {
      const response = await updatePicked(route.query.id, form)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        itemId = route.query.id
      } else {
        ElMessage.error(response.message || '更新失败')
        return
      }
    } else {
      const response = await createPicked(form)
      if (response.code === 200) {
        ElMessage.success('发布成功')
        // 假设返回的数据中包含了创建的物品ID
        itemId = response.data
      } else {
        ElMessage.error(response.message || '发布失败')
        return
      }
    }

    // 如果申请置顶，发送置顶申请
    if (form.applyTop) {
      try {
        await applyTop({
          itemId: itemId,
          itemType: 'picked'
        })
        ElMessage.success('置顶申请已提交，请等待管理员审核')
      } catch (error) {
        console.error('置顶申请失败', error)
        // 置顶申请失败不影响物品发布
      }
    }

    router.push('/')
  } catch (error) {
    // 验证错误已由表单处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const editId = route.query.id
  if (editId) {
    isEdit.value = true
    // 加载现有数据进行编辑
    getPickedDetail(editId).then(data => {
      // 转换日期格式
      if (data.pickTime) {
        data.pickTime = new Date(data.pickTime).toISOString().slice(0, 19).replace('T', ' ')
      }
      // 转换布尔值
      if (data.enableClaimWorkflow !== undefined) {
        data.enableClaimWorkflow = data.enableClaimWorkflow === 1
      }
      Object.assign(form, data)
      if (data.imageUrl) {
        imagePreview.value = data.imageUrl
      }
    })
  }
})
</script>

<style scoped>
.create-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.create-card {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.create-card h2 {
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #1677ff;
}

.upload-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 6px;
}

.image-preview:hover .preview-overlay {
  opacity: 1;
}
</style>
