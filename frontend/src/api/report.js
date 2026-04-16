import request from '../utils/request'

export function createReport(data) {
  return request({
    url: '/report/create',
    method: 'post',
    data
  })
}

export function getPendingReports() {
  return request({
    url: '/report/pending',
    method: 'get'
  })
}

export function handleReport(reportId, status) {
  return request({
    url: `/report/handle/${reportId}`,
    method: 'put',
    params: { status }
  })
}
