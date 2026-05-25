import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    hasPermission: (state) => (permCode) => {
      if (!permCode) return true
      return state.permissions.includes(permCode)
    }
  },
  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      const { token, userInfo } = res.data
      this.token = token
      this.userInfo = userInfo
      this.permissions = userInfo.permissions || []
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
      return res
    },
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    setUserInfo(info) {
      this.userInfo = info
      this.permissions = info.permissions || []
      localStorage.setItem('userInfo', JSON.stringify(info))
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
    },
    async fetchUserInfo() {
      try {
        const res = await getUserInfoApi()
        this.setUserInfo(res.data)
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.permissions = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
    }
  }
})
