// pages/index/index.js
Page({
  data: {
    userInfo: null
  },

  onShow() {
    const sessionToken = wx.getStorageSync('sessionToken')
    const userInfo = wx.getStorageSync('userInfo')

    if (!sessionToken) {
      wx.reLaunch({
        url: '/pages/login/login'
      })
      return
    }

    this.setData({
      userInfo: userInfo || null
    })
  },

  handleLogout() {
    wx.removeStorageSync('sessionToken')
    wx.removeStorageSync('userInfo')

    wx.showToast({
      title: '已退出登录',
      icon: 'success'
    })

    setTimeout(() => {
      wx.reLaunch({
        url: '/pages/login/login'
      })
    }, 300)
  }
})