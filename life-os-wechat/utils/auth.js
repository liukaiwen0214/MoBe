/**
 * auth.js
 * 文件用途：提供微信小程序的认证相关API接口，包括登录、获取验证码、获取用户信息等功能
 * 所属模块：life-os-wechat（微信小程序模块）
 * 核心职责：封装认证相关的HTTP请求，与后端认证接口交互
 * 与其他模块的关联：被登录、注册等页面调用
 * 在整体业务流程中的位置：位于API工具层，为认证流程提供数据交互支持
 * 说明：使用request工具函数发送HTTP请求
 */

// utils/auth.js
const { request } = require('./request')

/**
 * 获取验证码API
 * 功能：获取登录或注册时使用的验证码
 * 调用时机：用户打开登录或注册页面时
 * @returns Promise<any> 验证码响应数据，包含验证码ID、验证码内容和过期时间
 */
function getCaptchaApi() {
  return request({
    url: '/api/v1/auth/captcha',
    method: 'GET'
  })
}

/**
 * 登录API
 * 功能：用户登录接口
 * 调用时机：用户提交登录表单时
 * @param {Object} data 登录请求参数
 * @param {string} data.account 用户名或邮箱
 * @param {string} data.password 密码
 * @param {string} data.code 验证码
 * @param {number} data.rememberMe 是否记住登录状态，1表示记住，0表示不记住
 * @param {string} data.captchaId 验证码ID
 * @returns {Promise<any>} 登录响应数据，包含用户信息和会话令牌
 */
function loginApi(data) {
  return request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data
  })
}

/**
 * 获取当前用户信息API
 * 功能：获取当前登录用户的详细信息
 * 调用时机：用户需要查看自己的个人信息时
 * @returns {Promise<any>} 当前用户信息响应数据，包含用户的详细信息
 */
function getMeApi() {
  return request({
    url: '/api/v1/users/me',
    method: 'GET'
  })
}

module.exports = {
  getCaptchaApi,
  loginApi,
  getMeApi
}