import request from './request'

export function getAiSettings() {
  return request({ url: '/ai/settings', method: 'get' })
}

export function updateAiSettings(enabled) {
  return request({ url: '/ai/settings', method: 'put', data: { enabled } })
}

export function getAiLogs(params) {
  return request({ url: '/ai/logs', method: 'get', params })
}
