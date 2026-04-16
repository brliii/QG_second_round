import request from '@/utils/request'

// 获取会话列表
export const getConversations = () => {
  return request({
    url: '/message/conversations',
    method: 'get'
  })
}

// 获取或创建会话
export const getOrCreateConversation = (userId) => {
  return request({
    url: '/message/conversation',
    method: 'get',
    params: {
      userId: parseInt(userId)
    }
  })
}

// 发送消息
export const sendMessage = (conversationId, content) => {
  return request({
    url: '/message/send',
    method: 'post',
    data: {
      conversationId,
      content
    }
  })
}

// 获取消息历史
export const getMessages = (conversationId, page = 1, size = 20) => {
  return request({
    url: '/message/history',
    method: 'get',
    params: {
      conversationId,
      page,
      size
    }
  })
}
