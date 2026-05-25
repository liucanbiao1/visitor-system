import request from './request'

export function getOverview() {
  return request({ url: '/statistics/overview', method: 'get' })
}

export function getTrafficStats(period) {
  return request({ url: '/statistics/traffic', method: 'get', params: { period } })
}

export function getTimeDistribution() {
  return request({ url: '/statistics/time-distribution', method: 'get' })
}

export function getReasonDistribution() {
  return request({ url: '/statistics/reason-distribution', method: 'get' })
}
