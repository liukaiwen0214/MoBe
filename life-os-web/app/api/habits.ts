import request from '~/utils/request'

/**
 * 习惯简单项
 */
export interface HabitSimpleItem {
  id: string
  taskId: string
  name: string
}

/**
 * 查询习惯简单列表请求
 */
export interface HabitSimpleListRequest {
  taskId?: string
}

/**
 * 新增习惯请求
 */
export interface HabitCreateRequest {
  taskId: string
  name: string
}

/**
 * 查询习惯简单列表
 */
export function listSimpleHabitsApi(data: HabitSimpleListRequest) {
  return request.post('/habits/simple-list', data)
}

/**
 * 新增习惯
 */
export function createHabitApi(data: HabitCreateRequest) {
  return request.post('/habits/create', data)
}