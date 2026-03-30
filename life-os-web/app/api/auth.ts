/**
 * auth.ts
 * 文件用途：提供认证相关的API接口，包括登录、注册、获取验证码等功能
 * 所属模块：life-os-web（前端模块）
 * 核心职责：封装认证相关的HTTP请求，与后端认证接口交互
 * 与其他模块的关联：被登录、注册等页面调用
 * 在整体业务流程中的位置：位于API层，为认证流程提供数据交互支持
 * 说明：使用request工具函数发送HTTP请求
 */

import request from '~/utils/request'

/**
 * 获取验证码API
 * 功能：获取登录或注册时使用的验证码
 * 调用时机：用户打开登录或注册页面时
 * @returns Promise<any> 验证码响应数据，包含验证码ID、验证码内容和过期时间
 */
export function getCaptchaApi() {
  return request.get('/auth/captcha')
}

/**
 * 登录API
 * 功能：用户登录接口
 * 调用时机：用户提交登录表单时
 * @param data 登录请求参数
 * @param data.account 用户名或邮箱
 * @param data.password 密码
 * @param data.code 验证码
 * @param data.rememberMe 是否记住登录状态，1表示记住，0表示不记住
 * @param data.captchaId 验证码ID
 * @returns Promise<any> 登录响应数据，包含用户信息和会话令牌
 */
export function loginApi(data: {
  account: string
  password: string
  code: string
  rememberMe: number
  captchaId: string
}) {
  return request.post('/auth/login', data)
}
/**
 * 发送注册验证码API
 * 功能：发送注册验证码到指定邮箱
 * 调用时机：用户在注册页面点击发送验证码按钮时
 * @param data 发送注册验证码请求参数
 * @param data.email 注册邮箱
 * @returns Promise<any> 发送注册验证码响应数据，包含验证码ID、验证码内容和过期时间
 */
export function sendRegisterCodeApi(data: {
  email: string
}) {
  return request.post('/auth/register/send-code', data)
}
/**
 * 注册API
 * 功能：用户注册接口
 * 调用时机：用户提交注册表单时
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
