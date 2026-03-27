// utils/auth.js
const { request } = require('./request')

function getCaptchaApi() {
  return request({
    url: '/api/v1/auth/captcha',
    method: 'GET'
  })
}

function loginApi(data) {
  return request({
    url: '/api/v1/auth/login',
    method: 'POST',
    data
  })
}

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