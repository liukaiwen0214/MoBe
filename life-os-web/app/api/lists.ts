import request from '~/utils/request'

/**
 * 清单执行状态
 */
export type ChecklistExecutionStatus = 'PENDING' | 'DONE' | 'SKIPPED' | 'MISSED'

/**
 * 新增清单执行项请求
 */
export interface ChecklistCreateRequest {
  taskId?: string
  taskName: string
  habitId?: string
  habitName?: string
  title: string
  description?: string
  executeDate: string
  executeTime?: string
  note?: string
  status?: ChecklistExecutionStatus
  sort?: number
}

/**
 * 更新清单执行项请求
 */
export interface ChecklistUpdateRequest {
  id: string
  taskId?: string
  taskName?: string
  habitId?: string
  habitName?: string
  title: string
  description?: string
  executeDate?: string
  executeTime?: string
  note?: string
  status?: ChecklistExecutionStatus
  sort?: number
}

/**
 * 分页查询请求
 */
export interface ChecklistPageRequest {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: ChecklistExecutionStatus
  executeDate?: string
}

/**
 * 清单执行项响应
 */
export interface ChecklistItemResponse {
  id: string
  taskId?: string
  taskName: string
  habitId?: string
  habitName?: string
  checklistId?: string
  title: string
  description?: string
  status: ChecklistExecutionStatus
  executeDate?: string
  executeTime?: string
  note?: string
  sort: number
  completedAt?: string
  createdAt?: string
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

/**
 * 新增清单
 */
export function createChecklistApi(data: ChecklistCreateRequest) {
  return request.post('/checklists/create', data)
}

/**
 * 分页查询清单
 */
export function pageChecklistsApi(data: ChecklistPageRequest) {
  return request.post('/checklists/page', data)
}

/**
 * 更新清单
 */
export function updateChecklistApi(data: ChecklistUpdateRequest) {
  return request.post('/checklists/update', data)
}

/**
 * 完成清单
 */
export function completeChecklistApi(id: string) {
  return request.post(`/checklists/complete/${id}`)
}

/**
 * 恢复清单
 */
export function restoreChecklistApi(id: string) {
  return request.post(`/checklists/restore/${id}`)
}

/**
 * 删除清单
 */
export function deleteChecklistApi(id: string) {
  return request.post(`/checklists/delete/${id}`)
}
/**
 * 跳过清单
 */
export function skipChecklistApi(id: string) {
  return request.post(`/checklists/skip/${id}`)
}