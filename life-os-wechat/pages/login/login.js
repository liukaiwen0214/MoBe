// pages/login/login.js
const { getCaptchaApi, loginApi, getMeApi } = require('../../utils/auth')

Page({
  data: {
    loginForm: {
      account: '',
      password: '',
      captchaCode: '',
      rememberMe: false
    },
    captcha: {
      captchaId: '',
      captchaCode: '',
      expiresAt: ''
    },
    loginLoading: false
  },

  onLoad() {
    this.fetchCaptcha()
  },
  onShow() {
    const sessionToken = wx.getStorageSync('sessionToken')
    if (sessionToken) {
      wx.reLaunch({
        url: '/pages/index/index'
      })
      return
    }
  },
  onAccountInput(e) {
    this.setData({
      'loginForm.account': e.detail.value
    })
  },

  onPasswordInput(e) {
    this.setData({
      'loginForm.password': e.detail.value
    })
  },

  onCaptchaInput(e) {
    this.setData({
      'loginForm.captchaCode': e.detail.value
    })
  },

  toggleRemember() {
    this.setData({
      'loginForm.rememberMe': !this.data.loginForm.rememberMe
    })
  },

  async fetchCaptcha() {
    try {
      const res = await getCaptchaApi()

      if (!res?.success || !res?.data?.captchaId) {
        wx.showToast({
          title: res?.message || '获取验证码失败',
          icon: 'none'
        })
        return
      }

      this.setData({
        captcha: {
          captchaId: res.data.captchaId,
          captchaCode: res.data.captchaCode,
          expiresAt: res.data.expiresAt
        }
      })
    } catch (error) {
      wx.showToast({
        title: '获取验证码失败',
        icon: 'none'
      })
    }
  },

  validateLoginForm() {
    const { account, password, captchaCode } = this.data.loginForm

    if (!account || !account.trim()) {
      wx.showToast({
        title: '请输入账号',
        icon: 'none'
      })
      return false
    }

    if (!password || !password.trim()) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      })
      return false
    }

    if (!captchaCode || !captchaCode.trim()) {
      wx.showToast({
        title: '请输入验证码',
        icon: 'none'
      })
      return false
    }

    return true
  },

  async handleLogin() {
    if (!this.validateLoginForm()) {
      return
    }
  
    this.setData({
      loginLoading: true
    })
  
    try {
      const res = await loginApi({
        account: this.data.loginForm.account.trim(),
        password: this.data.loginForm.password,
        rememberMe: this.data.loginForm.rememberMe ? 1 : 0,
        captchaId: this.data.captcha.captchaId,
        code: this.data.loginForm.captchaCode.trim()
      })
  
      if (!res?.success || !res?.data?.sessionToken) {
        await this.fetchCaptcha()
  
        wx.showToast({
          title: res?.message || '登录失败',
          icon: 'none'
        })
        return
      }
  
      wx.setStorageSync('sessionToken', res.data.sessionToken)
  
      const meRes = await getMeApi()
  
      if (!meRes?.success || !meRes?.data) {
        wx.showToast({
          title: meRes?.message || '获取用户信息失败',
          icon: 'none'
        })
        return
      }
  
      wx.setStorageSync('userInfo', meRes.data)
  
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      })
  
      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/index/index'
        })
      }, 300)
    } catch (error) {
      await this.fetchCaptcha()
  
      wx.showToast({
        title: '登录失败',
        icon: 'none'
      })
    } finally {
      this.setData({
        loginLoading: false
      })
    }
  },

  handleGoRegister() {
    wx.showToast({
      title: '注册功能后续接入',
      icon: 'none'
    })
  }
})