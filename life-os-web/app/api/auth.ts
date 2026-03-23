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
  code:String
  rememberMe: number
  captchaId: string
}) {
  return request.post('/auth/login', data)
}
/**
 * 发送注册验证码API
 * <p>
 * 功能：发送注册验证码到指定邮箱
 * @param data 发送注册验证码请求参数
 * @param data.email 注册邮箱
 * @returns Promise<any> 发送注册验证码响应数据
 */
export function sendRegisterCodeApi(data: {
  email: string
}) {
  return request.post('/auth/register/send-code', data)
}
/**
 * 注册API
 * <p>
 * 功能：用户注册接口
 * @param data 注册请求参数
 * @param data.username 用户名
 * @param data.email 注册邮箱
 * @param data.code 注册验证码
 * @param data.password 密码
 * @returns Promise<any> 注册响应数据，包含用户信息和会话令牌
 */
export function registerApi(data: {
  username: string
  email: string
  code: string
  password: string
}) {
  return request.post('/auth/register', data)
}
