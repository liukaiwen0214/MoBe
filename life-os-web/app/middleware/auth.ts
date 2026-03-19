import { useUserStore } from '~/stores/user'
export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) {
    return
  }

  const userStore = useUserStore()
  userStore.initToken()

  console.log('auth middleware running, token=', userStore.token)

  if (!userStore.token) {
    return navigateTo('/login', { replace: true })  }

  if (userStore.userInfo && userStore.hasFetchedMe) {
    return
  }

  try {
    console.log('calling fetchMe...')
    await userStore.fetchMe()
  } catch (error) {
    console.error('fetchMe failed', error)
    userStore.logout()
    return navigateTo('/login', { replace: true })  }
})
