import request from '../utils/request'

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function registerAdmin(data, secretKey) {
  return request({
    url: '/user/registerAdmin',
    method: 'post',
    data,
    params: { secretKey }
  })
}

export function login(account, password) {
  return request({
    url: '/user/login',
    method: 'post',
    params: { account, password }
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function banUser(userId, status) {
  return request({
    url: `/user/ban/${userId}`,
    method: 'put',
    params: { status }
  })
}

export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}
