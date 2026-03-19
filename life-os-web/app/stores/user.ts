import { defineStore } from 'pinia'

export interface UserInfo {
  id: string
  username: string
  email: string
  nickname: string
  avatarUrl: string | null
  status: string
}

const TOKEN_KEY = 'sessionToken'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null as UserInfo | null,
    hasFetchedMe: false
  }),

  actions: {
    initToken() {
      if (import.meta.client) {
        this.token = localStorage.getItem(TOKEN_KEY) || ''
      }
    },

    setToken(token: string) {
      this.token = token
      if (import.meta.client) {
        localStorage.setItem(TOKEN_KEY, token)
      }
    },

    clearToken() {
      this.token = ''
      if (import.meta.client) {
        localStorage.removeItem(TOKEN_KEY)
      }
    },

    setUserInfo(userInfo: UserInfo | null) {
      this.userInfo = userInfo
    },

    logout() {
      this.clearToken()
      this.setUserInfo(null)
      this.hasFetchedMe = false
    },

    async fetchMe() {
      const { getMeApi } = await import('~/api/user')
      const res = await getMeApi()

      if (!res?.data) {
        throw new Error(res?.data?.message || '获取当前用户失败')
      }

      this.userInfo = res.data
      this.hasFetchedMe = true
      return res.data
    }
  }
})