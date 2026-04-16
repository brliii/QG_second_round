import request from '../utils/request'

export function applyTop(data) {
  return request({
    url: '/top/apply',
    method: 'post',
    data
  })
}

export function getPendingTopRequests() {
  return request({
    url: '/top/pending',
    method: 'get'
  })
}

export function approveTopRequest(requestId, approveStatus) {
  return request({
    url: `/top/approve/${requestId}`,
    method: 'put',
    params: { approveStatus }
  })
}
