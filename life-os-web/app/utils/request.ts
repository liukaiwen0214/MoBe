import axios from 'axios'
import { useUserStore } from '~/stores/user'

/**
 * 请求工具类
 * <p>
 * 功能：创建并配置 axios 实例，添加请求和响应拦截器
 * 说明：用于处理前端与后端 API 的通信
 */
const request = axios.create({
  /**
   * API 基础路径
   */
  // baseURL: 'http://119.91.216.175:8080/api/v1',
  baseURL: 'http://127.0.0.1:8080/api/v1',
  /**
   * 请求超时时间（毫秒）
   */
  timeout: 10000
})

/**
 * 请求拦截器
 * <p>
 * 功能：在发送请求前添加认证令牌
 */
request.interceptors.request.use((config) => {
  const userStore = useUserStore()

  // 如果用户已登录，添加 Bearer 令牌到请求头
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }

  return config
})

/**
 * 响应拦截器
 * <p>
 * 功能：处理响应数据和错误
 */
request.interceptors.response.use(
  /**
   * 成功响应：
   * 直接返回后端响应体
   */
  (response) => response.data,

  /**
   * 错误响应：
   * 优先提取后端返回的 message，保证页面 catch 时能直接拿到可用提示
   */
  (error) => {
    const responseData = error?.response?.data

    const message =
      responseData?.message ||
      responseData?.msg ||
      responseData?.data ||
      error?.message ||
      '请求失败，请稍后重试'

    const normalizedError = new Error(message) as Error & {
      code?: number
      status?: number
      response?: any
      raw?: any
    }

    normalizedError.code = responseData?.code
    normalizedError.status = error?.response?.status
    normalizedError.response = responseData
    normalizedError.raw = error

    return Promise.reject(normalizedError)
  }
)

export default request