import request from '~/utils/request'

export function getCaptchaApi() {
  return request.get('/auth/captcha')
}

export function loginApi(data: {
  account: string
  password: string
  rememberMe: number
}) {
  return request.post('/auth/login', data)
}