import request from '~/utils/request'

/**
 * 习惯频率类型
 */
export type HabitFrequencyType = 'DAILY' | 'WEEKLY' | 'MONTHLY'

/**
 * 习惯状态
 */
export type HabitStatus = 'ENABLED' | 'DISABLED'

/**
 * 习惯打卡记录状态
 * 说明：
 * 这里只用于“查看历史记录”展示，不代表习惯页可以直接执行这些动作
 */
export type HabitRecordStatus = 'DONE' | 'MISSED' | 'SKIPPED'

/**
 * 习惯打卡记录来源
 */
export type HabitRecordSource = 'MANUAL' | 'SYSTEM' | 'LIST'

/**
 * 分页响应
 */
export interface PageResponse<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

/* =========================
 * 一、简单接口（清单页/习惯页共用）
 * ========================= */

/**
 * 习惯简单项
 * 用途：
 * 1. 清单页面“所属习惯”下拉选择
 * 2. 后续其他页面轻量选择器复用
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
 * 轻量新增习惯请求
 * 用途：
 * 1. 清单页面中快速新增习惯
 * 2. 只保留最小字段
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
 * 轻量新增习惯
 * 说明：
 * 主要给清单页面快速创建习惯使用
 */
export function createHabitApi(data: HabitCreateRequest) {
  return request.post('/habits/create', data)
}

/* =========================
 * 二、习惯页面主接口
 * ========================= */

/**
 * 习惯分页查询请求
 */
export interface HabitPageRequest {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: HabitStatus
  frequencyType?: HabitFrequencyType
  generateToTodo?: boolean
}

/**
 * 习惯分页项响应
 */
export interface HabitPageItemResponse {
  id: string
  taskId?: string
  taskName?: string
  checklistId?: string
  checklistTitle?: string
  name: string
  description?: string
  icon?: string
  color?: string
  frequencyType: HabitFrequencyType
  frequencyText?: string
  startDate?: string
  reminderTime?: string
  generateToTodo: boolean
  status: HabitStatus
  totalCheckInCount: number
  streakCount: number
  longestStreakCount: number
  lastCheckInAt?: string
  sort: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 习惯详情响应
 */
export interface HabitDetailResponse {
  id: string
  taskId?: string
  taskName?: string
  checklistId?: string
  checklistTitle?: string
  name: string
  description?: string
  icon?: string
  color?: string
  frequencyType: HabitFrequencyType
  frequencyText?: string
  startDate?: string
  reminderTime?: string
  generateToTodo: boolean
  status: HabitStatus
  totalCheckInCount: number
  streakCount: number
  longestStreakCount: number
  lastCheckInAt?: string
  sort: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 习惯完整新增请求
 * 用于习惯页面“新增习惯”
 */
export interface HabitCreateDetailRequest {
  taskId?: string
  checklistId?: string
  name: string
  description?: string
  icon?: string
  color?: string
  frequencyType: HabitFrequencyType
  frequencyText?: string
  startDate?: string
  reminderTime?: string
  generateToTodo: boolean
  status?: HabitStatus
  sort?: number
}

/**
 * 习惯更新请求
 * 用于习惯页面“编辑习惯”
 */
export interface HabitUpdateRequest {
  id: string
  taskId?: string
  checklistId?: string
  name: string
  description?: string
  icon?: string
  color?: string
  frequencyType: HabitFrequencyType
  frequencyText?: string
  startDate?: string
  reminderTime?: string
  generateToTodo: boolean
  status: HabitStatus
  sort?: number
}

/**
 * 分页查询习惯
 */
export function pageHabitsApi(data: HabitPageRequest) {
  return request.post('/habits/page', data)
}

/**
 * 查询习惯详情
 */
export function getHabitDetailApi(id: string) {
  return request.post(`/habits/detail/${id}`)
}

/**
 * 习惯页完整新增
 */
export function createHabitDetailApi(data: HabitCreateDetailRequest) {
  return request.post('/habits/create-detail', data)
}

/**
 * 更新习惯
 */
export function updateHabitApi(data: HabitUpdateRequest) {
  return request.post('/habits/update', data)
}

/**
 * 删除习惯
 */
export function deleteHabitApi(id: string) {
  return request.post(`/habits/delete/${id}`)
}

/* =========================
 * 三、状态/开关接口
 * ========================= */

/**
 * 启用习惯
 */
export function enableHabitApi(id: string) {
  return request.post(`/habits/enable/${id}`)
}

/**
 * 停用习惯
 */
export function disableHabitApi(id: string) {
  return request.post(`/habits/disable/${id}`)
}

/**
 * 修改是否生成到清单
 */
export interface HabitToggleGenerateRequest {
  id: string
  generateToTodo: boolean
}

/**
 * 切换“生成到清单”
 */
export function toggleHabitGenerateApi(data: HabitToggleGenerateRequest) {
  return request.post('/habits/toggle-generate', data)
}

/* =========================
 * 四、历史记录查看接口
 * ========================= */

/**
 * 习惯历史记录项
 * 说明：
 * 仅用于习惯页展示历史，不在习惯页执行动作
 */
export interface HabitRecordItemResponse {
  id: string
  habitId: string
  status: HabitRecordStatus
  date: string
  time?: string
  source: HabitRecordSource
  note?: string
  createdAt?: string
}

/**
 * 查询习惯历史记录请求
 */
export interface HabitRecordPageRequest {
  habitId: string
  pageNum?: number
  pageSize?: number
  status?: HabitRecordStatus
  startDate?: string
  endDate?: string
}

/**
 * 分页查询习惯历史记录
 */
export function pageHabitRecordsApi(data: HabitRecordPageRequest) {
  return request.post('/habits/records/page', data)
}