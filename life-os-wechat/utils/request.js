/**
 * request.js
 * 文件用途：提供微信小程序的网络请求工具，封装wx.request方法
 * 所属模块：life-os-wechat（微信小程序模块）
 * 核心职责：
 * 1. 封装微信小程序的网络请求方法
 * 2. 统一处理请求头，包括Authorization令牌
 * 3. 统一处理请求和响应
 * 与其他模块的关联：被所有需要网络请求的模块调用
 * 在整体业务流程中的位置：位于工具层，为整个应用提供网络请求支持
 * 说明：使用Promise封装wx.request，简化异步请求处理
 */

// utils/request.js

/**
 * API基础URL
 * 后端服务的基础URL，用于拼接完整的请求地址
 */
const BASE_URL = 'http://127.0.0.1:8080'

/**
 * 网络请求函数
 * 功能：封装wx.request方法，提供Promise风格的网络请求
 * 调用时机：需要发送网络请求时
 * @param {Object} options 请求选项
 * @param {string} options.url 请求路径
 * @param {string} [options.method='GET'] 请求方法
 * @param {Object} [options.data={}] 请求数据
 * @param {Object} [options.header={}] 请求头
 * @returns {Promise<any>} 请求响应数据
 * 核心流程：
 * 1. 从本地存储中获取会话令牌
 * 2. 构建完整的请求URL和请求头
 * 3. 发送网络请求
 * 4. 处理请求成功和失败的回调
 */
function request({ url, method = 'GET', data = {}, header = {} }) {
  // 从本地存储中获取会话令牌
  const token = wx.getStorageSync('sessionToken') || ''

  return new Promise((resolve, reject) => {
    // 发送网络请求
    wx.request({
      // 构建完整的请求URL
      url: `${BASE_URL}${url}`,
      // 请求方法
      method,
      // 请求数据
      data,
      // 请求头
      header: {
        // 设置默认的Content-Type
        'Content-Type': 'application/json',
        // 如果存在令牌，添加Authorization头
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        // 合并用户自定义的请求头
        ...header
      },
      // 请求成功回调
      success: (res) => {
        // 解析响应数据
        resolve(res.data)
      },
      // 请求失败回调
      fail: (err) => {
        // 处理请求失败
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}