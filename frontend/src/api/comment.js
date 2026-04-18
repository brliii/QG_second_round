import request from '../utils/request'

export function sendComment(data) {
  return request({
    url: '/comment/send',
    method: 'post',
    data
  })
}

export function getCommentsByTarget(targetType, targetId) {
  return request({
    url: '/comment/target',
    method: 'get',
    params: { targetType, targetId }
  })
}

export function getPrivateMessages() {
  return request({
    url: '/comment/private',
    method: 'get'
  })
}

export function getUnreadCount() {
  return request({
    url: '/comment/unread/count',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/comment/read/${id}`,
    method: 'put'
  })
}

export function adminDeleteComment(id) {
  return request({
    url: `/comment/admin/delete/${id}`,
    method: 'delete'
  })
}

export function getCommentDetail(id) {
  return request({
    url: `/comment/admin/detail/${id}`,
    method: 'get'
  })
}
