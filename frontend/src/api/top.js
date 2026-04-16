import request from '../utils/request'

export function applyTop(data) {
  return request({
    url: '/top/apply',
    method: 'post',
    data
  })
}

export function getPendingTopRequests(page = 1, size = 100) {
  return request({
    url: '/top/pending',
    method: 'get',
    params: { page, size }
  })
}

export function approveTopRequest(requestId, approveStatus) {
  return request({
    url: `/top/approve/${requestId}`,
    method: 'put',
    params: { approveStatus }
  })
}
