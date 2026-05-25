import request from './request'

export function recordEntry(data) {
  return request({ url: '/access-log/entry', method: 'post', data })
}

export function recordExit(id) {
  return request({ url: `/access-log/${id}/exit`, method: 'put' })
}

export function getAccessLogPage(params) {
  return request({ url: '/access-log/page', method: 'get', params })
}

export function getOnCampusVisitors() {
  return request({ url: '/access-log/on-campus', method: 'get' })
}

export function getOverstayAlerts() {
  return request({ url: '/access-log/overstay', method: 'get' })
}
