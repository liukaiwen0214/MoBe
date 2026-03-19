import request from '~/utils/request'

/**
 * 获取当前用户信息API
 * <p>
 * 功能：获取当前登录用户的详细信息
 * @returns Promise<any> 当前用户信息响应数据
 */
export function getMeApi() {
  return request.get('/users/me')
}
