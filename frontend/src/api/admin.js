import request from '../utils/request'

export function getStatistics() {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}
