import axios from 'axios'
import { useUserStore } from '~/stores/user'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()

  console.log('request token =', userStore.token)

  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
)

export default request
