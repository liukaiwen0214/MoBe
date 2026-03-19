import request from '~/utils/request'

export function getMeApi() {
  return request.get('/users/me')
}
