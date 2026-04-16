import request from '../utils/request'

export function createClaim(data) {
  return request({
    url: '/claim/create',
    method: 'post',
    data
  })
}

export function getClaimsByPicker() {
  return request({
    url: '/claim/listByPicker',
    method: 'get'
  })
}

export function processClaim(requestId, data) {
  return request({
    url: `/claim/process/${requestId}`,
    method: 'put',
    data
  })
}

export function getMyClaims() {
  return request({
    url: '/claim/myRequests',
    method: 'get'
  })
}
