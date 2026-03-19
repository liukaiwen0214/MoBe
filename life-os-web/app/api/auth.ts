import request from '~/utils/request'

/**
 * 获取验证码API
 * <p>
 * 功能：获取登录或注册时使用的验证码
 * @returns Promise<any> 验证码响应数据
 */
export function getCaptchaApi() {
  return request.get('/auth/captcha')
}

/**
 * 登录API
 * <p>
 * 功能：用户登录接口
 * @param data 登录请求参数
 * @param data.account 用户名或邮箱
 * @param data.password 密码
 * @param data.rememberMe 是否记住登录状态，1表示记住，0表示不记住
 * @returns Promise<any> 登录响应数据，包含用户信息和会话令牌
 */
export function loginApi(data: {
  account: string
  password: string
  rememberMe: number
}) {
  return request.post('/auth/login', data)
}