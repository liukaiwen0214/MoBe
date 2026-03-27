// utils/request.js
const BASE_URL = 'http://127.0.0.1:8080'

function request({ url, method = 'GET', data = {}, header = {} }) {
  const token = wx.getStorageSync('sessionToken') || ''

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}