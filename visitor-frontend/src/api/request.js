import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    const lang = localStorage.getItem('lang') || 'zh'
    config.headers['Accept-Language'] = lang === 'en' ? 'en-US' : 'zh-CN'
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('permissions')
          ElMessage.error(data?.message || '登录已过期，请重新登录')
          setTimeout(() => {
            window.location.href = '/login'
          }, 500)
          break
        case 403:
          ElMessage.error(data?.message || '权限不足，无法执行该操作')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求错误 ${status}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请重试')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
