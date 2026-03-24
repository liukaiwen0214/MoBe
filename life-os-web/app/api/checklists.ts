import request from '~/utils/request'

/**
 * 清单优先级
 */
export type ChecklistPriority = 'HIGH' | 'MEDIUM' | 'LOW'

/**
 * 清单状态
 */
export type ChecklistStatus = 'PENDING' | 'DONE'

/**
 * 清单频率
 */
export type ChecklistFrequency = 'ONCE' | 'DAILY' | 'WEEKLY' | 'MONTHLY'

/**
 * 来源类型
 */
export type ChecklistSourceType = 'CHECKLIST' | 'HABIT'

/**
 * 新增清单请求
 */
export type ChecklistCreateRequest = {
  taskId?: string
  taskName: string
  title: string
  description?: string
  priority?: ChecklistPriority
  reminderText?: string
  actionText?: string
  actionType?: string
  sort?: number
}

/**
 * 更新清单请求
 */
export type ChecklistUpdateRequest = {
  id: string
  taskId?: string
  taskName: string
  title: string
  description?: string
  priority?: ChecklistPriority
  reminderText?: string
  actionText?: string
  actionType?: string
  sort?: number
}

/**
 * 分页查询请求
 */
export type ChecklistPageRequest = {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: ChecklistStatus | 'ALL'
  priority?: ChecklistPriority | 'ALL'
  frequency?: ChecklistFrequency | 'ALL'
  reminderOnly?: number
  actionOnly?: number
}

/**
 * 清单项响应
 */
export type ChecklistItemResponse = {
  id: string
  taskId?: string
  taskName: string
  title: string
  description?: string
  priority: ChecklistPriority
  reminderText?: string
  actionText?: string
  actionType?: string
  status: ChecklistStatus
  sourceType: ChecklistSourceType
  frequency?: ChecklistFrequency
  sort: number
  completedAt?: string
  createdAt?: string
}

/**
 * 分页响应
 */
export type PageResponse<T> = {
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