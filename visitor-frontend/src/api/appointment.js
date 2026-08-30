import request from './request'

export function submitAppointment(data) {
  return request({ url: '/appointment/submit', method: 'post', data, timeout: 60000 })
}

export function queryAppointments(phone) {
  return request({ url: '/appointment/query', method: 'get', params: { phone } })
}

export function getAppointmentList(params) {
  return request({ url: '/appointment/list', method: 'get', params })
}

export function reviewAppointment(id, data) {
  return request({ url: `/appointment/${id}/review`, method: 'put', data })
}
