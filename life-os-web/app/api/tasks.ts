import request from '~/utils/request'

/**
 * 任务简单项
 */
export interface TaskSimpleItem {
  id: string
  name: string
}

/**
 * 新增任务请求
 */
export interface TaskCreateRequest {
  name: string
}

/**
 * 查询任务简单列表
 */
export function listSimpleTasksApi() {
  return request.get('/tasks/simple-list')
}

/**
 * 新增任务
 */
export function createTaskApi(data: TaskCreateRequest) {
  return request.post('/tasks/create', data)
}