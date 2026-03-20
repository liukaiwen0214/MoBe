/**
 * 导入Pinia状态管理
 * @import defineStore 定义store的函数
 */
import { defineStore } from 'pinia'

/**
 * 用户信息接口
 * @interface UserInfo
 * @property {string} id - 用户ID
 * @property {string} username - 用户名
 * @property {string} email - 邮箱
 * @property {string} nickname - 昵称
 * @property {string | null} avatarUrl - 头像URL
 * @property {string} status - 用户状态
 */
export interface UserInfo {
  id: string
  username: string
  email: string
  nickname: string
  avatarUrl: string | null
  status: string
}

/**
 * 令牌存储键名
 */
const TOKEN_KEY = 'sessionToken'

/**
 * 用户状态管理
 * <p>
 * 功能：管理用户认证状态和用户信息
 * 说明：使用Pinia管理用户相关的状态和操作
 * 应用场景：用户登录、注销、获取用户信息等
 */
export const useUserStore = defineStore('user', {
  /**
   * 状态定义
   * @returns {Object} 状态对象
   * @property {string} token - 会话令牌
   * @property {UserInfo | null} userInfo - 用户信息
   * @property {boolean} hasFetchedMe - 是否已获取用户信息
   */
  state: () => ({
    token: '', // 会话令牌
    userInfo: null as UserInfo | null, // 用户信息
    hasFetchedMe: false // 是否已获取用户信息
  }),

  /**
   * 操作定义
   */
  actions: {
    /**
     * 初始化令牌
     * @returns 无
     * @description 从本地存储中获取令牌
     */
    initToken() {
      if (import.meta.client) {
        this.token = localStorage.getItem(TOKEN_KEY) || ''
      }
    },

    /**
     * 设置令牌
     * @param {string} token - 会话令牌
     * @returns 无
     * @description 设置令牌并存储到本地存储
     */
    setToken(token: string) {
      this.token = token
      if (import.meta.client) {
        localStorage.setItem(TOKEN_KEY, token)
      }
    },

    /**
     * 清除令牌
     * @returns 无
     * @description 清除令牌并从本地存储中移除
     */
    clearToken() {
      this.token = ''
      if (import.meta.client) {
        localStorage.removeItem(TOKEN_KEY)
      }
    },

    /**
     * 设置用户信息
     * @param {UserInfo | null} userInfo - 用户信息
     * @returns 无
     * @description 设置用户信息
     */
    setUserInfo(userInfo: UserInfo | null) {
      this.userInfo = userInfo
    },

    /**
     * 注销用户
     * @returns 无
     * @description 清除令牌、用户信息和获取状态
     */
    logout() {
      this.clearToken()
      this.setUserInfo(null)
      this.hasFetchedMe = false
    },

    /**
     * 获取当前用户信息
     * @returns {Promise<UserInfo>} 用户信息
     * @description 从API获取当前用户信息并更新状态
     */
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