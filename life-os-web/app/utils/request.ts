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
  baseURL: 'http://119.91.216.175:8080/api/v1',
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

  console.log('request token =', userStore.token)

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
  // 成功响应：直接返回响应数据
  (response) => response.data,
  // 错误响应：将错误传递给调用方
  (error) => Promise.reject(error)
)

export default request
