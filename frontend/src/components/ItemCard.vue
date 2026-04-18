<template>
  <div class="item-card" @click="handleClick">
    <img :src="(item.imageUrl ? 'http://localhost:8080' + item.imageUrl : '/placeholder.jpg')" :alt="item.name" class="item-image" />
    <div class="item-info">
      <h3 class="item-title">{{ item.name }}</h3>
      <p class="item-location">📍 {{ item.location }}</p>
      <p class="item-time">{{ formatTime(item.lostTime || item.pickTime) }}</p>
      <p class="item-description">{{ truncateText(item.description, 50) }}</p>
      <div class="item-meta">
        <div class="badges">
          <span v-if="item.isTop" class="top-badge">置顶</span>
          <span v-if="item.status === 1 && type === 'picked'" class="claimed-badge">已认领</span>
          <span v-if="item.status === 1 && type === 'lost'" class="found-badge">已找回</span>
        </div>
        <span class="time-ago">{{ timeAgo(item.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  type: {
    type: String,
    required: true
  }
})

const router = useRouter()

const handleClick = () => {
  router.push(`/detail/${props.type}/${props.item.id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString()
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const timeAgo = (time) => {
  if (!time) return ''
  const now = new Date()
  const past = new Date(time)
  const diff = now - past
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}
</script>

<style scoped>
.item-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.item-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.item-info {
  padding: 16px;
}

.item-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.item-location,
.item-time {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.item-description {
  margin: 8px 0;
  color: #555;
  font-size: 14px;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.badges {
  display: flex;
  gap: 8px;
}

.top-badge {
  background: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.claimed-badge {
  background: #52c41a;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.found-badge {
  background: #1890ff;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.time-ago {
  color: #999;
  font-size: 12px;
}
</style>
