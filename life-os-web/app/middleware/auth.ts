/**
 * 导入用户状态管理
 * @import useUserStore 用户状态管理
 */
import { useUserStore } from '~/stores/user'

/**
 * 认证路由中间件
 * <p>
 * 功能：验证用户是否已登录，未登录则重定向到登录页
 * 说明：用于需要认证的页面，确保只有登录用户才能访问
 * 应用场景：保护需要登录才能访问的页面
 */
export default defineNuxtRouteMiddleware(async () => {
  // 服务器端不执行中间件
  if (import.meta.server) {
    return
  }

  // 初始化用户状态管理
  const userStore = useUserStore()
  // 初始化令牌
  userStore.initToken()

  // 如果没有令牌，重定向到登录页
  if (!userStore.token) {
    return navigateTo('/login', { replace: true })
  }

  // 如果已经获取了用户信息，直接返回
  if (userStore.userInfo && userStore.hasFetchedMe) {
    return
  }

  try {
    // 获取用户信息
    await userStore.fetchMe()
  } catch (error) {
    // 获取用户信息失败，注销用户并重定向到登录页
    userStore.logout()
    return navigateTo('/login', { replace: true })
  }
})
