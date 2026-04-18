import request from '../utils/request'

export function getLostList(params) {
  return request({
    url: '/lost/list',
    method: 'get',
    params
  })
}

export function getLostDetail(id) {
  return request({
    url: `/lost/detail/${id}`,
    method: 'get'
  })
}

export function createLost(data) {
  return request({
    url: '/lost/create',
    method: 'post',
    data
  })
}

export function updateLost(id, data) {
  return request({
    url: `/lost/update`,
    method: 'put',
    params: { id },
    data
  })
}

export function deleteLost(id) {
  return request({
    url: `/lost/delete/${id}`,
    method: 'delete'
  })
}

export function adminDeleteLost(id) {
  return request({
    url: `/lost/admin/delete/${id}`,
    method: 'delete'
  })
}

export function searchLostByDescription(description) {
  return request({
    url: '/lost/search',
    method: 'get',
    params: { description }
  })
}
