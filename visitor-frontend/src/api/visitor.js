import request from './request'

export function getVisitorList() {
  return request({ url: '/visitor', method: 'get' })
}

export function getVisitorDetail(id) {
  return request({ url: `/visitor/${id}`, method: 'get' })
}

export function addVisitor(data) {
  return request({ url: '/visitor', method: 'post', data })
}

export function updateVisitor(id, data) {
  return request({ url: `/visitor/${id}`, method: 'put', data })
}

export function deleteVisitor(id) {
  return request({ url: `/visitor/${id}`, method: 'delete' })
}
