/**
 * 导入用户状态管理
 * @import useUserStore 用户状态管理
 */
import { useUserStore } from '~/stores/user'

/**
 * 访客路由中间件
 * <p>
 * 功能：验证用户是否未登录，已登录则重定向到首页
 * 说明：用于登录、注册等不需要登录的页面，确保已登录用户不会访问这些页面
 * 应用场景：保护登录、注册等页面，防止已登录用户访问
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

  // 如果没有令牌，直接返回
  if (!userStore.token) {
    return
  }

  // 如果已经获取了用户信息，重定向到首页
  if (userStore.userInfo && userStore.hasFetchedMe) {
    return navigateTo('/', { replace: true })
  }

  try {
    // 获取用户信息
    await userStore.fetchMe()
    // 获取成功，重定向到首页
    return navigateTo('/', { replace: true })
  } catch {
    // 获取失败，注销用户
    userStore.logout()
  }
})
