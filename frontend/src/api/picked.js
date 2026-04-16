import request from '../utils/request'

export function getPickedList(params) {
  return request({
    url: '/picked/list',
    method: 'get',
    params
  })
}

export function getPickedDetail(id) {
  return request({
    url: `/picked/detail/${id}`,
    method: 'get'
  })
}

export function createPicked(data) {
  return request({
    url: '/picked/create',
    method: 'post',
    data
  })
}

export function updatePicked(id, data) {
  return request({
    url: `/picked/update`,
    method: 'put',
    params: { id },
    data
  })
}

export function deletePicked(id) {
  return request({
    url: `/picked/delete/${id}`,
    method: 'delete'
  })
}

export function adminDeletePicked(id) {
  return request({
    url: `/picked/admin/delete/${id}`,
    method: 'delete'
  })
}

export function regenerateAiDesc(id) {
  return request({
    url: `/picked/regenerateAiDesc/${id}`,
    method: 'post'
  })
}

export function generateAiDesc(name, description) {
  return request({
    url: '/picked/generateAiDesc',
    method: 'post',
    params: { name, description }
  })
}

export function searchPickedByDescription(description) {
  return request({
    url: '/picked/search',
    method: 'post',
    params: { description }
  })
}
