import { useUserStore } from '~/stores/user'
export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) {
    return
  }

  const userStore = useUserStore()
  userStore.initToken()

  if (!userStore.token) {
    return
  }

  if (userStore.userInfo && userStore.hasFetchedMe) {
    return navigateTo('/', { replace: true })  }

  try {
    await userStore.fetchMe()
    return navigateTo('/', { replace: true })  } catch {
    userStore.logout()
  }
})
