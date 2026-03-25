<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, useTemplateRef } from 'vue'
import { parseDate, parseTime, type CalendarDate, type Time } from '@internationalized/date'
import {
  createChecklistApi,
  pageChecklistsApi,
  updateChecklistApi,
  completeChecklistApi,
  restoreChecklistApi,
  skipChecklistApi,
  deleteChecklistApi
} from '~/api/lists'
import { listSimpleTasksApi, createTaskApi, type TaskSimpleItem } from '~/api/tasks'
import { listSimpleHabitsApi, createHabitApi, type HabitSimpleItem } from '~/api/habits'
definePageMeta({
  middleware: 'auth'
})

const systemToast = useSystemToast()

type ChecklistExecutionStatus = 'PENDING' | 'DONE' | 'SKIPPED' | 'MISSED'

type ChecklistExecutionRecord = {
  id: string
  taskId: string
  taskName: string
  habitId?: string
  habitName?: string
  checklistId: string
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

type ChecklistGroup = {
  taskId: string
  taskName: string
  pendingCount: number
  items: ChecklistExecutionRecord[]
}

type ChecklistColumnKey =
  | 'description'
  | 'habit'
  | 'executeDate'
  | 'note'
  | 'operations'

const loading = ref(true)
const filterPanelOpen = ref(false)
const creatingChecklist = ref(false)
const createOpen = ref(false)
const editOpen = ref(false)
const deleteConfirmOpen = ref(false)

const checklistRecords = ref<ChecklistExecutionRecord[]>([])
const collapsedGroups = ref<Record<string, boolean>>({})
const undoArmedMap = ref<Record<string, number>>({})
const shakingIds = ref<Record<string, boolean>>({})

const deletingChecklist = ref(false)
const deletingChecklistItem = ref<ChecklistExecutionRecord | null>(null)
const editingChecklist = ref(false)
const editingChecklistItem = ref<ChecklistExecutionRecord | null>(null)

const columnVisibility = reactive<Record<ChecklistColumnKey, boolean>>({
  description: true,
  habit: true,
  executeDate: true,
  note: true,
  operations: true
})

const filters = reactive({
  keyword: '',
  status: 'ALL' as 'ALL' | ChecklistExecutionStatus,
  executeDate: ''
})

const checklistForm = reactive({
  taskId: '',
  taskName: '',
  habitId: '',
  habitName: '',
  title: '',
  description: '',
  executeDate: '',
  executeTime: '',
  note: '',
  status: 'PENDING' as ChecklistExecutionStatus
})
const editChecklistForm = reactive({
  taskId: '',
  taskName: '',
  habitId: '',
  habitName: '',
  title: '',
  description: '',
  executeDate: '',
  executeTime: '',
  note: ''
})

const formErrors = reactive({
  taskName: '',
  title: '',
  executeDate: ''
})
const editFormErrors = reactive({
  taskName: '',
  title: '',
  executeDate: ''
})
const taskOptions = ref<TaskSimpleItem[]>([])
const habitOptions = ref<HabitSimpleItem[]>([])
const taskKeyword = ref('')
const habitKeyword = ref('')
const editTaskKeyword = ref('')
const editHabitKeyword = ref('')
const normalizedTaskKeyword = computed(() => taskKeyword.value.trim())
const normalizedHabitKeyword = computed(() => habitKeyword.value.trim())
const normalizedEditTaskKeyword = computed(() => editTaskKeyword.value.trim())
const normalizedEditHabitKeyword = computed(() => editHabitKeyword.value.trim())
const DEFAULT_TEMP_TASK_NAME = '临时清单'
const filterExecuteDateInput = useTemplateRef('filterExecuteDateInput')
const filterDatePopoverOpen = ref(false)
const createExecuteDateInput = useTemplateRef('createExecuteDateInput')
const createExecuteDateValue = computed<CalendarDate | undefined>({
  get() {
    if (!checklistForm.executeDate) return undefined
    try {
      return parseDate(checklistForm.executeDate)
    } catch {
      return undefined
    }
  },
  set(value) {
    checklistForm.executeDate = value ? value.toString() : ''
  }
})

const createExecuteTimeValue = computed<Time | undefined>({
  get() {
    if (!checklistForm.executeTime) return undefined
    try {
      return parseTime(checklistForm.executeTime)
    } catch {
      return undefined
    }
  },
  set(value) {
    checklistForm.executeTime = value ? value.toString().slice(0, 5) : ''
  }
})
const editExecuteDateValue = computed<CalendarDate | undefined>({
  get() {
    if (!editChecklistForm.executeDate) return undefined
    try {
      return parseDate(editChecklistForm.executeDate)
    } catch {
      return undefined
    }
  },
  set(value) {
    editChecklistForm.executeDate = value ? value.toString() : ''
  }
})

const editExecuteTimeValue = computed<Time | undefined>({
  get() {
    if (!editChecklistForm.executeTime) return undefined
    try {
      return parseTime(editChecklistForm.executeTime)
    } catch {
      return undefined
    }
  },
  set(value) {
    editChecklistForm.executeTime = value ? value.toString().slice(0, 5) : ''
  }
})

const filterExecuteDateValue = computed<CalendarDate | undefined>({
  get() {
    if (!filters.executeDate) return undefined
    try {
      return parseDate(filters.executeDate)
    } catch {
      return undefined
    }
  },
  set(value) {
    filters.executeDate = value ? value.toString() : ''
  }
})

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '待执行', value: 'PENDING' },
  { label: '已完成', value: 'DONE' },
  { label: '已跳过', value: 'SKIPPED' },
  { label: '已错过', value: 'MISSED' }
]


const visibleColumnOptions = computed(() => [
  { key: 'description', label: '说明' },
  { key: 'habit', label: '习惯' },
  { key: 'executeDate', label: '执行时间' },
  { key: 'note', label: '备注' },
  { key: 'operations', label: '操作' }
] as Array<{ key: ChecklistColumnKey; label: string }>)

const activeFilterCount = computed(() => {
  let count = 0
  if (filters.keyword.trim()) count += 1
  if (filters.status !== 'ALL') count += 1
  if (filters.executeDate) count += 1
  return count
})

const hasActiveFilters = computed(() => activeFilterCount.value > 0)

const taskSelectMenuItems = computed(() => {
  const keyword = normalizedTaskKeyword.value.toLowerCase()

  const filtered = keyword
    ? taskOptions.value
      .filter(item => item.name.toLowerCase().includes(keyword))
      .map(item => ({
        label: item.name,
        value: item.id
      }))
    : taskOptions.value.map(item => ({
      label: item.name,
      value: item.id
    }))

  const hasExactMatch = taskOptions.value.some(
    item => item.name.trim().toLowerCase() === keyword && keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedTaskKeyword.value}」任务`,
        value: `__create__${normalizedTaskKeyword.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

const editTaskSelectMenuItems = computed(() => {
  const keyword = normalizedEditTaskKeyword.value.toLowerCase()

  const filtered = keyword
    ? taskOptions.value
      .filter(item => item.name.toLowerCase().includes(keyword))
      .map(item => ({
        label: item.name,
        value: item.id
      }))
    : taskOptions.value.map(item => ({
      label: item.name,
      value: item.id
    }))

  const hasExactMatch = taskOptions.value.some(
    item => item.name.trim().toLowerCase() === keyword && keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedEditTaskKeyword.value}」任务`,
        value: `__create__${normalizedEditTaskKeyword.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

const habitSelectMenuItems = computed(() => {
  if (!checklistForm.taskId) return []

  const keyword = normalizedHabitKeyword.value.toLowerCase()

  const filtered = habitOptions.value
    .filter(item => item.taskId === checklistForm.taskId)
    .filter(item => !keyword || item.name.toLowerCase().includes(keyword))
    .map(item => ({
      label: item.name,
      value: item.id
    }))

  const hasExactMatch = habitOptions.value.some(
    item =>
      item.taskId === checklistForm.taskId &&
      item.name.trim().toLowerCase() === keyword &&
      keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedHabitKeyword.value}」习惯`,
        value: `__create__${normalizedHabitKeyword.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

const editHabitSelectMenuItems = computed(() => {
  if (!editChecklistForm.taskId) return []

  const keyword = normalizedEditHabitKeyword.value.toLowerCase()

  const filtered = habitOptions.value
    .filter(item => item.taskId === editChecklistForm.taskId)
    .filter(item => !keyword || item.name.toLowerCase().includes(keyword))
    .map(item => ({
      label: item.name,
      value: item.id
    }))

  const hasExactMatch = habitOptions.value.some(
    item =>
      item.taskId === editChecklistForm.taskId &&
      item.name.trim().toLowerCase() === keyword &&
      keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedEditHabitKeyword.value}」习惯`,
        value: `__create__${normalizedEditHabitKeyword.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

function formatNowDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatNowTime() {
  const now = new Date()
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}
async function fetchTaskOptions() {
  try {
    const res: any = await listSimpleTasksApi()
    taskOptions.value = res?.data || []
  } catch (error: any) {
    systemToast.error('获取任务失败', error?.message || '请稍后重试', 'task-simple-list-error')
  }
}
async function ensureDefaultTempTaskSelected() {
  await fetchTaskOptions()

  let tempTask = taskOptions.value.find(item => item.name === DEFAULT_TEMP_TASK_NAME)

  if (!tempTask) {
    try {
      await createTaskApi({ name: DEFAULT_TEMP_TASK_NAME })
      await fetchTaskOptions()
      tempTask = taskOptions.value.find(item => item.name === DEFAULT_TEMP_TASK_NAME)
    } catch (error: any) {
      systemToast.error('初始化默认任务失败', error?.message || '请稍后重试', 'task-temp-init-error')
      return
    }
  }

  if (tempTask) {
    await handleTaskSelect(tempTask.id)
  }
}
async function handleTaskSelect(taskId?: string) {
  if (!taskId) {
    checklistForm.taskId = ''
    checklistForm.taskName = ''
    checklistForm.habitId = ''
    checklistForm.habitName = ''
    habitKeyword.value = ''
    habitOptions.value = []
    return
  }

  const selected = taskOptions.value.find(item => item.id === taskId)
  if (!selected) return

  checklistForm.taskId = selected.id
  checklistForm.taskName = selected.name

  checklistForm.habitId = ''
  checklistForm.habitName = ''
  habitKeyword.value = ''

  await fetchHabitOptions(checklistForm.taskId)
}

async function handleEditTaskSelect(taskId?: string) {
  if (!taskId) {
    editChecklistForm.taskId = ''
    editChecklistForm.taskName = ''
    editChecklistForm.habitId = ''
    editChecklistForm.habitName = ''
    editHabitKeyword.value = ''
    habitOptions.value = []
    return
  }

  const selected = taskOptions.value.find(item => item.id === taskId)
  if (!selected) return

  editChecklistForm.taskId = selected.id
  editChecklistForm.taskName = selected.name

  editChecklistForm.habitId = ''
  editChecklistForm.habitName = ''
  editHabitKeyword.value = ''

  await fetchHabitOptions(editChecklistForm.taskId)
}

function handleHabitSelect(habitId?: string) {
  if (!habitId) {
    checklistForm.habitId = ''
    checklistForm.habitName = ''
    return
  }

  const selected = habitOptions.value.find(item => item.id === habitId)
  if (!selected) return

  checklistForm.habitId = selected.id
  checklistForm.habitName = selected.name
}

function handleEditHabitSelect(habitId?: string) {
  if (!habitId) {
    editChecklistForm.habitId = ''
    editChecklistForm.habitName = ''
    return
  }

  const selected = habitOptions.value.find(item => item.id === habitId)
  if (!selected) return

  editChecklistForm.habitId = selected.id
  editChecklistForm.habitName = selected.name
}

async function fetchHabitOptions(taskId?: string) {
  if (!taskId) {
    habitOptions.value = []
    return
  }

  try {
    const res: any = await listSimpleHabitsApi({ taskId })
    habitOptions.value = res?.data || []
  } catch (error: any) {
    systemToast.error('获取习惯失败', error?.message || '请稍后重试', 'habit-simple-list-error')
  }
}

async function createTaskFromKeyword(nameArg?: string) {
  const name = (nameArg || taskKeyword.value).trim()
  if (!name) return

  const existed = taskOptions.value.find(
    item => item.name.trim().toLowerCase() === name.toLowerCase()
  )
  if (existed) {
    taskKeyword.value = ''
    await handleTaskSelect(existed.id)
    return
  }

  try {
    await createTaskApi({ name })
    await fetchTaskOptions()

    const created = taskOptions.value.find(
      item => item.name.trim().toLowerCase() === name.toLowerCase()
    )
    if (created) {
      taskKeyword.value = ''
      await handleTaskSelect(created.id)
    }

    systemToast.success('新增成功', `任务「${name}」已创建`, 'task-create-success')
  } catch (error: any) {
    systemToast.error('新增失败', error?.message || '请稍后重试', 'task-create-error')
  }
}
async function createEditTaskFromKeyword(nameArg?: string) {
  const name = (nameArg || editTaskKeyword.value).trim()
  if (!name) return

  const existed = taskOptions.value.find(
    item => item.name.trim().toLowerCase() === name.toLowerCase()
  )
  if (existed) {
    editTaskKeyword.value = ''
    await handleEditTaskSelect(existed.id)
    return
  }

  try {
    await createTaskApi({ name })
    await fetchTaskOptions()

    const created = taskOptions.value.find(
      item => item.name.trim().toLowerCase() === name.toLowerCase()
    )
    if (created) {
      editTaskKeyword.value = ''
      await handleEditTaskSelect(created.id)
    }

    systemToast.success('新增成功', `任务「${name}」已创建`, 'edit-task-create-success')
  } catch (error: any) {
    systemToast.error('新增失败', error?.message || '请稍后重试', 'edit-task-create-error')
  }
}

async function createHabitFromKeyword(nameArg?: string) {
  const name = (nameArg || habitKeyword.value).trim()
  if (!name || !checklistForm.taskId) return

  const existed = habitOptions.value.find(
    item =>
      item.taskId === checklistForm.taskId &&
      item.name.trim().toLowerCase() === name.toLowerCase()
  )
  if (existed) {
    habitKeyword.value = ''
    handleHabitSelect(existed.id)
    return
  }

  try {
    await createHabitApi({
      taskId: checklistForm.taskId,
      name
    })

    await fetchHabitOptions(checklistForm.taskId)

    const created = habitOptions.value.find(
      item =>
        item.taskId === checklistForm.taskId &&
        item.name.trim().toLowerCase() === name.toLowerCase()
    )
    if (created) {
      habitKeyword.value = ''
      handleHabitSelect(created.id)
    }

    systemToast.success('新增成功', `习惯「${name}」已创建`, 'habit-create-success')
  } catch (error: any) {
    systemToast.error('新增失败', error?.message || '请稍后重试', 'habit-create-error')
  }
}
async function createEditHabitFromKeyword(nameArg?: string) {
  const name = (nameArg || editHabitKeyword.value).trim()
  if (!name || !editChecklistForm.taskId) return

  const existed = habitOptions.value.find(
    item =>
      item.taskId === editChecklistForm.taskId &&
      item.name.trim().toLowerCase() === name.toLowerCase()
  )
  if (existed) {
    editHabitKeyword.value = ''
    handleEditHabitSelect(existed.id)
    return
  }

  try {
    await createHabitApi({
      taskId: editChecklistForm.taskId,
      name
    })

    await fetchHabitOptions(editChecklistForm.taskId)

    const created = habitOptions.value.find(
      item =>
        item.taskId === editChecklistForm.taskId &&
        item.name.trim().toLowerCase() === name.toLowerCase()
    )
    if (created) {
      editHabitKeyword.value = ''
      handleEditHabitSelect(created.id)
    }

    systemToast.success('新增成功', `习惯「${name}」已创建`, 'edit-habit-create-success')
  } catch (error: any) {
    systemToast.error('新增失败', error?.message || '请稍后重试', 'edit-habit-create-error')
  }
}
watch(
  () => checklistForm.taskId,
  async value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      const name = value.replace('__create__', '')
      await createTaskFromKeyword(name)
    }
  }
)

watch(
  () => checklistForm.habitId,
  async value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      const name = value.replace('__create__', '')
      await createHabitFromKeyword(name)
    }
  }
)

watch(
  () => editChecklistForm.taskId,
  async value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      const name = value.replace('__create__', '')
      await createEditTaskFromKeyword(name)
    }
  }
)

watch(
  () => editChecklistForm.habitId,
  async value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      const name = value.replace('__create__', '')
      await createEditHabitFromKeyword(name)
    }
  }
)

function getStatusLabel(status: ChecklistExecutionStatus) {
  if (status === 'DONE') return '已完成'
  if (status === 'SKIPPED') return '已跳过'
  if (status === 'MISSED') return '已错过'
  return '待执行'
}

function statusBadgeClass(status: ChecklistExecutionStatus) {
  if (status === 'DONE') return 'check-status-badge check-status-badge--done'
  if (status === 'SKIPPED') return 'check-status-badge check-status-badge--skipped'
  if (status === 'MISSED') return 'check-status-badge check-status-badge--missed'
  return 'check-status-badge check-status-badge--pending'
}

function clearAllFilters() {
  filters.keyword = ''
  filters.status = 'ALL'
  filters.executeDate = ''
  systemToast.success('已清空筛选', '所有筛选条件已重置', 'checklist-clear-filters-success')
}

function matchesKeyword(item: ChecklistExecutionRecord, keyword: string) {
  if (!keyword) return true

  const text = [
    item.taskName,
    item.habitName || '',
    item.title,
    item.description || '',
    item.note || ''
  ].join(' ').toLowerCase()

  return text.includes(keyword)
}

async function fetchChecklistRecords(showSuccess = false) {
  loading.value = true

  try {
    const requestData = {
      pageNum: 1,
      pageSize: 500,
      keyword: filters.keyword.trim() || undefined,
      status: filters.status !== 'ALL' ? filters.status : undefined,
      executeDate: filters.executeDate || undefined
    }

    const res: any = await pageChecklistsApi(requestData)
    const records = (res?.data?.records || []) as ChecklistExecutionRecord[]

    checklistRecords.value = records

    const taskIds = Array.from(new Set(records.map(item => item.taskId || item.taskName)))
    collapsedGroups.value = Object.fromEntries(
      taskIds.map(taskId => [taskId as string, collapsedGroups.value[taskId as string] ?? false])
    )

    if (showSuccess) {
      systemToast.success('刷新成功', '清单执行列表已更新', 'checklist-refresh-success')
    }
  } catch (error: any) {
    systemToast.error('获取失败', error?.message || '请稍后重试', 'checklist-fetch-error')
  } finally {
    loading.value = false
  }
}

const filteredRecords = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return checklistRecords.value.filter(item => {
    const matchKeyword = matchesKeyword(item, keyword)
    const matchStatus = filters.status === 'ALL' || item.status === filters.status
    const matchExecuteDate = !filters.executeDate || item.executeDate === filters.executeDate
    return matchKeyword && matchStatus && matchExecuteDate
  })
})

const groupedChecklistData = computed<ChecklistGroup[]>(() => {
  const groupedMap = new Map<string, ChecklistExecutionRecord[]>()

  for (const item of filteredRecords.value) {
    const groupKey = item.taskId || item.taskName || 'default'
    if (!groupedMap.has(groupKey)) {
      groupedMap.set(groupKey, [])
    }
    groupedMap.get(groupKey)?.push(item)
  }

  return Array.from(groupedMap.entries()).map(([taskId, items]) => {
    const sortedItems = [...items].sort((a, b) => {
      const statusWeight = (status: ChecklistExecutionStatus) => {
        if (status === 'PENDING') return 0
        if (status === 'DONE') return 1
        if (status === 'SKIPPED') return 2
        return 3
      }

      const statusDiff = statusWeight(a.status) - statusWeight(b.status)
      if (statusDiff !== 0) return statusDiff

      const dateA = `${a.executeDate || ''} ${a.executeTime || ''}`
      const dateB = `${b.executeDate || ''} ${b.executeTime || ''}`
      const dateDiff = dateA.localeCompare(dateB)
      if (dateDiff !== 0) return dateDiff

      return (a.sort || 0) - (b.sort || 0)
    })

    return {
      taskId,
      taskName: items[0]?.taskName || '未分类',
      pendingCount: items.filter(item => item.status === 'PENDING').length,
      items: sortedItems
    }
  })
})

function toggleGroup(taskId: string) {
  collapsedGroups.value = {
    ...collapsedGroups.value,
    [taskId]: !collapsedGroups.value[taskId]
  }
}

function armUndoShake(id: string) {
  shakingIds.value = {
    ...shakingIds.value,
    [id]: true
  }

  setTimeout(() => {
    const next = { ...shakingIds.value }
    delete next[id]
    shakingIds.value = next
  }, 520)
}

async function toggleChecklistStatus(id: string) {
  const target = checklistRecords.value.find(item => item.id === id)
  if (!target) return

  if (target.status === 'PENDING') {
    try {
      await completeChecklistApi(id)
      target.status = 'DONE'
      target.completedAt = new Date().toISOString()

      const nextUndo = { ...undoArmedMap.value }
      delete nextUndo[id]
      undoArmedMap.value = nextUndo

      systemToast.success('已完成', `${target.title} 已标记为完成`, `checklist-status-${id}`)
    } catch (error: any) {
      systemToast.error('操作失败', error?.message || '请稍后重试', `checklist-complete-${id}`)
    }
    return
  }

  const now = Date.now()
  const armedAt = undoArmedMap.value[id]

  if (!armedAt || now - armedAt > 3000) {
    undoArmedMap.value = {
      ...undoArmedMap.value,
      [id]: now
    }
    armUndoShake(id)
    systemToast.info('再次点击可恢复', '3 秒内再次点击可恢复为待执行', `checklist-undo-arm-${id}`)
    return
  }

  try {
    await restoreChecklistApi(id)
    target.status = 'PENDING'
    target.completedAt = undefined

    const nextUndo = { ...undoArmedMap.value }
    delete nextUndo[id]
    undoArmedMap.value = nextUndo

    const nextShake = { ...shakingIds.value }
    delete nextShake[id]
    shakingIds.value = nextShake

    systemToast.success('已恢复', `${target.title} 已恢复为待执行`, `checklist-undo-done-${id}`)
  } catch (error: any) {
    systemToast.error('恢复失败', error?.message || '请稍后重试', `checklist-restore-${id}`)
  }
}

async function skipChecklist(item: ChecklistExecutionRecord) {
  if (item.status === 'DONE') return

  try {
    await skipChecklistApi(item.id)
    item.status = 'SKIPPED'
    systemToast.success('已跳过', `${item.title} 已标记为跳过`, `checklist-skip-${item.id}`)
  } catch (error: any) {
    systemToast.error('跳过失败', error?.message || '请稍后重试', `checklist-skip-error-${item.id}`)
  }
}

function isUndoArmed(id: string) {
  const armedAt = undoArmedMap.value[id]
  if (!armedAt) return false
  return Date.now() - armedAt <= 3000
}

function openCreateChecklist() {
  checklistForm.taskId = ''
  checklistForm.taskName = ''
  checklistForm.habitId = ''
  checklistForm.habitName = ''
  checklistForm.title = ''
  checklistForm.description = ''
  checklistForm.executeDate = formatNowDate()
  checklistForm.executeTime = formatNowTime()
  checklistForm.note = ''
  checklistForm.status = 'PENDING'

  taskKeyword.value = ''
  habitKeyword.value = ''
  habitOptions.value = []

  formErrors.taskName = ''
  formErrors.title = ''
  formErrors.executeDate = ''

  createOpen.value = true
  ensureDefaultTempTaskSelected()
}

function closeCreateChecklist() {
  createOpen.value = false
}
function closeEditChecklist() {
  editOpen.value = false
  editingChecklistItem.value = null
}

function validateChecklistForm() {
  formErrors.taskName = checklistForm.taskName.trim() ? '' : '请输入所属任务'
  formErrors.title = checklistForm.title.trim() ? '' : '请输入清单标题'
  formErrors.executeDate = checklistForm.executeDate ? '' : '请选择执行日期'
  return !formErrors.taskName && !formErrors.title && !formErrors.executeDate
}
function validateEditChecklistForm() {
  editFormErrors.taskName = editChecklistForm.taskName.trim() ? '' : '请输入所属任务'
  editFormErrors.title = editChecklistForm.title.trim() ? '' : '请输入清单标题'
  editFormErrors.executeDate = editChecklistForm.executeDate ? '' : '请选择执行日期'
  return !editFormErrors.taskName && !editFormErrors.title && !editFormErrors.executeDate
}

async function submitCreateChecklist() {
  if (!validateChecklistForm()) {
    systemToast.error('保存失败', '请先补全必填项', 'checklist-create-validate-error')
    return
  }

  creatingChecklist.value = true

  try {
    await createChecklistApi({
      taskId: checklistForm.taskId || undefined,
      taskName: checklistForm.taskName.trim(),
      habitId: checklistForm.habitId || undefined,
      habitName: checklistForm.habitName.trim() || undefined,
      title: checklistForm.title.trim(),
      description: checklistForm.description.trim() || undefined,
      executeDate: checklistForm.executeDate,
      executeTime: checklistForm.executeTime || undefined,
      note: checklistForm.note.trim() || undefined,
      status: 'PENDING',
      sort: 0
    })

    closeCreateChecklist()
    await fetchChecklistRecords()

    systemToast.success('保存成功', '执行项已新增', 'checklist-create-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'checklist-create-error')
  } finally {
    creatingChecklist.value = false
  }
}

async function submitEditChecklist() {
  if (!editingChecklistItem.value) return

  if (!validateEditChecklistForm()) {
    systemToast.error('保存失败', '请先补全必填项', 'checklist-edit-validate-error')
    return
  }

  editingChecklist.value = true

  try {
    await updateChecklistApi({
      id: editingChecklistItem.value.id,
      taskId: editChecklistForm.taskId || undefined,
      taskName: editChecklistForm.taskName.trim(),
      habitId: editChecklistForm.habitId || undefined,
      habitName: editChecklistForm.habitName.trim() || undefined,
      title: editChecklistForm.title.trim(),
      description: editChecklistForm.description.trim() || undefined,
      executeDate: editChecklistForm.executeDate,
      executeTime: editChecklistForm.executeTime || undefined,
      note: editChecklistForm.note.trim() || undefined
    })

    closeEditChecklist()
    await fetchChecklistRecords()

    systemToast.success('保存成功', '执行项已更新', 'checklist-edit-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'checklist-edit-error')
  } finally {
    editingChecklist.value = false
  }
}

async function editChecklist(item: ChecklistExecutionRecord) {
  if (item.status === 'DONE') return

  editingChecklistItem.value = item

  editChecklistForm.taskId = item.taskId || ''
  editChecklistForm.taskName = item.taskName || ''
  editChecklistForm.habitId = item.habitId || ''
  editChecklistForm.habitName = item.habitName || ''
  editChecklistForm.title = item.title || ''
  editChecklistForm.description = item.description || ''
  editChecklistForm.executeDate = item.executeDate || ''
  editChecklistForm.executeTime = item.executeTime || ''
  editChecklistForm.note = item.note || ''

  editTaskKeyword.value = ''
  editHabitKeyword.value = ''

  editFormErrors.taskName = ''
  editFormErrors.title = ''
  editFormErrors.executeDate = ''

  await fetchTaskOptions()

  if (editChecklistForm.taskId) {
    await fetchHabitOptions(editChecklistForm.taskId)
  } else {
    habitOptions.value = []
  }

  editOpen.value = true
}

function openDeleteChecklist(item: ChecklistExecutionRecord) {
  if (item.status === 'DONE') return
  deletingChecklistItem.value = item
  deleteConfirmOpen.value = true
}

function closeDeleteChecklist() {
  deleteConfirmOpen.value = false
}

function resetDeleteChecklistState() {
  deletingChecklistItem.value = null
}

function handleDeleteModalOpenChange(value: boolean) {
  deleteConfirmOpen.value = value

  if (!value) {
    setTimeout(() => {
      resetDeleteChecklistState()
    }, 180)
  }
}

async function confirmDeleteChecklist() {
  if (!deletingChecklistItem.value) return

  deletingChecklist.value = true

  try {
    const item = deletingChecklistItem.value
    await deleteChecklistApi(item.id)

    checklistRecords.value = checklistRecords.value.filter(record => record.id !== item.id)
    closeDeleteChecklist()
    systemToast.success('删除成功', `${item.title} 已删除`, `checklist-delete-${item.id}`)
  } catch (error: any) {
    systemToast.error('删除失败', error?.message || '请稍后重试', 'checklist-delete-error')
  } finally {
    deletingChecklist.value = false
  }
}

let undoTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await fetchChecklistRecords()

  undoTimer = setInterval(() => {
    const now = Date.now()
    const nextMap: Record<string, number> = {}

    Object.entries(undoArmedMap.value).forEach(([id, time]) => {
      if (now - time <= 3000) {
        nextMap[id] = time
      }
    })

    undoArmedMap.value = nextMap
  }, 500)
})

onBeforeUnmount(() => {
  if (undoTimer) {
    clearInterval(undoTimer)
    undoTimer = null
  }
})
</script>

<template>
  <div class="page-shell">
    <div class="bills-workspace">
      <div class="bills-workspace__title-wrap">
        <div class="bills-workspace__eyebrow">
          Checklist
        </div>
        <div class="bills-workspace__title-row">
          <h1 class="bills-workspace__title">
            清单
          </h1>
          <span class="bills-workspace__desc">按任务查看和管理具体执行项</span>
        </div>
      </div>

      <div class="bills-workspace__actions">
        <UInput v-model="filters.keyword" icon="i-lucide-search" placeholder="搜索标题、说明、习惯或备注"
          class="checklist-toolbar__search" />

        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw" @click="fetchChecklistRecords(true)">
          刷新
        </UButton>

        <div class="bill-filter-trigger-wrap">
          <UButton color="neutral" :variant="hasActiveFilters ? 'soft' : 'outline'" icon="i-lucide-sliders-horizontal"
            @click="filterPanelOpen = true">
            {{ hasActiveFilters ? `筛选(${activeFilterCount})` : '筛选' }}
          </UButton>

          <button v-if="hasActiveFilters" type="button" class="bill-filter-trigger-wrap__clear" aria-label="清空筛选"
            @click.stop="clearAllFilters">
            <UIcon name="i-lucide-x" class="size-3.5" />
          </button>
        </div>

        <UButton icon="i-lucide-plus" @click="openCreateChecklist">
          新增清单
        </UButton>
      </div>
    </div>

    <div class="checklist-main-panel page-section">
      <div v-if="loading" class="checklist-loading">
        <div class="checklist-loading__ring" />
        <div class="checklist-loading__text">
          正在加载清单
        </div>
      </div>

      <template v-else>
        <div v-if="!groupedChecklistData.length" class="checklist-empty">
          <div class="checklist-empty__title">
            暂无清单
          </div>
          <div class="checklist-empty__desc">
            当前筛选条件下没有匹配结果，试试调整筛选或新增一条清单。
          </div>
        </div>

        <template v-else>
          <div class="checklist-table-shell">
            <div class="checklist-table-head">
              <div class="checklist-col checklist-col--status" />
              <div class="checklist-col checklist-col--title">清单</div>
              <div v-if="columnVisibility.description" class="checklist-col checklist-col--desc">说明</div>
              <div v-if="columnVisibility.habit" class="checklist-col checklist-col--habit">习惯</div>
              <div v-if="columnVisibility.executeDate" class="checklist-col checklist-col--execute">执行时间</div>
              <div v-if="columnVisibility.note" class="checklist-col checklist-col--note">备注</div>
              <div v-if="columnVisibility.operations" class="checklist-col checklist-col--operations">操作</div>
            </div>

            <div class="checklist-groups">
              <section v-for="group in groupedChecklistData" :key="group.taskId" class="checklist-group">
                <button type="button" class="checklist-group__header" @click="toggleGroup(group.taskId)">
                  <div class="checklist-group__left">
                    <UIcon name="i-lucide-chevron-down" class="size-4 checklist-group__arrow"
                      :class="{ 'checklist-group__arrow--collapsed': collapsedGroups[group.taskId] }" />
                    <span class="checklist-group__title">{{ group.taskName }}</span>
                    <span class="checklist-group__count">待执行 {{ group.pendingCount }}</span>
                  </div>
                </button>

                <div v-show="!collapsedGroups[group.taskId]" class="checklist-group__body">
                  <div v-for="item in group.items" :key="item.id" class="checklist-row" :class="{
                    'checklist-row--pending': item.status === 'PENDING',
                    'checklist-row--done': item.status === 'DONE',
                    'checklist-row--skipped': item.status === 'SKIPPED',
                    'checklist-row--missed': item.status === 'MISSED',
                    'checklist-row--shake': shakingIds[item.id]
                  }">
                    <div class="checklist-col checklist-col--status">
                      <button type="button" class="check-status-btn" :class="{
                        'check-status-btn--pending': item.status === 'PENDING',
                        'check-status-btn--done': item.status === 'DONE',
                        'check-status-btn--skipped': item.status === 'SKIPPED',
                        'check-status-btn--missed': item.status === 'MISSED',
                        'check-status-btn--armed': isUndoArmed(item.id)
                      }" @click.stop="toggleChecklistStatus(item.id)">
                        <UIcon :name="item.status === 'DONE'
                          ? 'i-lucide-check'
                          : item.status === 'SKIPPED'
                            ? 'i-lucide-forward'
                            : item.status === 'MISSED'
                              ? 'i-lucide-clock-3'
                              : 'i-lucide-circle'
                          " class="size-4" />
                      </button>
                    </div>

                    <div class="checklist-col checklist-col--title">
                      <div class="checklist-title-wrap">
                        <div class="checklist-title-line">
                          <span class="checklist-title" :class="{ 'checklist-title--done': item.status === 'DONE' }">
                            {{ item.title }}
                          </span>

                          <span :class="statusBadgeClass(item.status)">
                            {{ getStatusLabel(item.status) }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div v-if="columnVisibility.description" class="checklist-col checklist-col--desc">
                      <div class="checklist-desc" :class="{ 'checklist-desc--done': item.status === 'DONE' }">
                        {{ item.description || '—' }}
                      </div>
                    </div>

                    <div v-if="columnVisibility.habit" class="checklist-col checklist-col--habit">
                      <span class="checklist-meta-text">
                        {{ item.habitName || '—' }}
                      </span>
                    </div>

                    <div v-if="columnVisibility.executeDate" class="checklist-col checklist-col--execute">
                      <span class="checklist-execute-text">
                        {{ item.executeDate || '—' }}<template v-if="item.executeTime"> &nbsp;{{ item.executeTime
                          }}</template>
                      </span>
                    </div>

                    <div v-if="columnVisibility.note" class="checklist-col checklist-col--note">
                      <div class="checklist-desc" :class="{ 'checklist-desc--done': item.status === 'DONE' }">
                        {{ item.note || '—' }}
                      </div>
                    </div>

                    <div v-if="columnVisibility.operations" class="checklist-col checklist-col--operations">
                      <div class="checklist-op-group">
                        <button v-if="item.status === 'PENDING'" type="button" class="checklist-op-btn"
                          @click.stop="skipChecklist(item)">
                          跳过
                        </button>

                        <button type="button" class="checklist-op-btn" :disabled="item.status === 'DONE'"
                          :class="{ 'checklist-op-btn--disabled': item.status === 'DONE' }"
                          @click.stop="editChecklist(item)">
                          编辑
                        </button>

                        <button type="button" class="checklist-op-btn checklist-op-btn--danger"
                          :disabled="item.status !== 'PENDING'"
                          :class="{ 'checklist-op-btn--disabled': item.status !== 'PENDING' }"
                          @click.stop="openDeleteChecklist(item)">
                          删除
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </template>
      </template>
    </div>

    <USlideover v-model:open="filterPanelOpen" side="right" :ui="{ content: 'w-full max-w-[420px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">
                Checklist
              </div>
              <h2 class="bill-create-panel__title">
                筛选清单
              </h2>
              <p class="bill-create-panel__desc">
                选择条件后立即生效。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="filterPanelOpen = false" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-field">
              <label class="bill-form-field__label">状态</label>
              <USelect v-model="filters.status" :items="statusOptions" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">执行日期</label>

              <UInputDate ref="filterExecuteDateInput" v-model="filterExecuteDateValue" class="w-full" locale="en-CA"
                :format-options="{ year: 'numeric', month: '2-digit', day: '2-digit' }" :ui="{ base: 'pr-10' }">
                <template #trailing>
                  <UPopover v-model:open="filterDatePopoverOpen"
                    :reference="filterExecuteDateInput?.inputsRef?.[3]?.$el">
                    <UButton color="neutral" variant="ghost" icon="i-lucide-calendar" />

                    <template #content>
                      <UCalendar v-model="filterExecuteDateValue" class="p-2" :month-controls="true"
                        :year-controls="true" @update:model-value="filterDatePopoverOpen = false" />
                    </template>
                  </UPopover>
                </template>
              </UInputDate>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">显示列</label>
              <div class="bill-column-list">
                <label v-for="item in visibleColumnOptions" :key="item.key" class="bill-column-list__item">
                  <input v-model="columnVisibility[item.key]" type="checkbox">
                  <span>{{ item.label }}</span>
                </label>
              </div>
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="filterPanelOpen = false">
              关闭
            </UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <USlideover v-model:open="createOpen" side="right" :ui="{ content: 'w-full max-w-[460px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">
                Checklist
              </div>
              <h2 class="bill-create-panel__title">
                新增清单
              </h2>
              <p class="bill-create-panel__desc">
                创建一条具体执行项，用于当天或某次执行。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeCreateChecklist" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-field">
              <label class="bill-form-field__label">所属任务</label>
              <div class="check-select-menu-wrap">
                <USelectMenu v-model="checklistForm.taskId" v-model:search-term="taskKeyword"
                  :items="taskSelectMenuItems" value-key="value" label-key="label" searchable placeholder="请选择或搜索任务"
                  @update:model-value="handleTaskSelect">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>
              </div>
              <div v-if="formErrors.taskName" class="bill-form-field__error">
                {{ formErrors.taskName }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">所属习惯</label>
              <div class="check-select-menu-wrap">
                <USelectMenu v-model="checklistForm.habitId" v-model:search-term="habitKeyword"
                  :items="habitSelectMenuItems" value-key="value" label-key="label" searchable
                  :disabled="!checklistForm.taskId" placeholder="请选择或搜索习惯" @update:model-value="handleHabitSelect">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">清单标题</label>
              <UInput v-model="checklistForm.title" placeholder="例如：背 20 个单词" />
              <div v-if="formErrors.title" class="bill-form-field__error">
                {{ formErrors.title }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">说明</label>
              <UTextarea v-model="checklistForm.description" :rows="3" placeholder="补充这条执行项的说明（可选）" autoresize />
            </div>

            <div class="bill-form-grid--compact">
              <div class="bill-form-field">
                <label class="bill-form-field__label">执行日期</label>
                <UInputDate ref="createExecuteDateInput" v-model="createExecuteDateValue" class="w-full" locale="en-CA"
                  :format-options="{ year: 'numeric', month: '2-digit', day: '2-digit' }" :ui="{ base: 'pr-10' }">
                  <template #trailing>
                    <UPopover :reference="createExecuteDateInput?.inputsRef?.[3]?.$el">
                      <UButton color="neutral" variant="ghost" icon="i-lucide-calendar" />
                      <template #content>
                        <UCalendar v-model="createExecuteDateValue" class="p-2" :month-controls="true"
                          :year-controls="true" />
                      </template>
                    </UPopover>
                  </template>
                </UInputDate>
                <div v-if="formErrors.executeDate" class="bill-form-field__error">
                  {{ formErrors.executeDate }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">执行时间</label>
                <UInputTime v-model="createExecuteTimeValue" class="w-full" locale="en-GB" :hour-cycle="24"
                  :format-options="{ hour: '2-digit', minute: '2-digit', hour12: false }" />
              </div>
            </div>
            <div v-if="filters.executeDate" class="bill-form-field__hint">
              当前筛选日期：{{ filters.executeDate }}
              <button type="button" class="check-filter-date-clear" @click="filters.executeDate = ''">
                清空
              </button>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="checklistForm.note" :rows="3" placeholder="补充执行备注（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeCreateChecklist">
              取消
            </UButton>

            <UButton icon="i-lucide-save" :loading="creatingChecklist" @click="submitCreateChecklist">
              保存
            </UButton>
          </div>
        </div>
      </template>
    </USlideover>
    <USlideover v-model:open="editOpen" side="right" :ui="{ content: 'w-full max-w-[460px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">
                Checklist
              </div>
              <h2 class="bill-create-panel__title">
                编辑清单
              </h2>
              <p class="bill-create-panel__desc">
                修改这条执行项的内容。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeEditChecklist" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-field">
              <label class="bill-form-field__label">所属任务</label>
              <div class="check-select-menu-wrap">
                <USelectMenu v-model="editChecklistForm.taskId" v-model:search-term="editTaskKeyword"
                  :items="editTaskSelectMenuItems" value-key="value" label-key="label" searchable placeholder="请选择或搜索任务"
                  @update:model-value="handleEditTaskSelect">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>
              </div>
              <div v-if="editFormErrors.taskName" class="bill-form-field__error">
                {{ editFormErrors.taskName }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">所属习惯</label>
              <div class="check-select-menu-wrap">
                <USelectMenu v-model="editChecklistForm.habitId" v-model:search-term="editHabitKeyword"
                  :items="editHabitSelectMenuItems" value-key="value" label-key="label" searchable
                  :disabled="!editChecklistForm.taskId" placeholder="请选择或搜索习惯"
                  @update:model-value="handleEditHabitSelect">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">清单标题</label>
              <UInput v-model="editChecklistForm.title" placeholder="例如：背 20 个单词" />
              <div v-if="editFormErrors.title" class="bill-form-field__error">
                {{ editFormErrors.title }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">说明</label>
              <UTextarea v-model="editChecklistForm.description" :rows="3" placeholder="补充这条执行项的说明（可选）" autoresize />
            </div>

            <div class="bill-form-grid--compact">
              <div class="bill-form-field">
                <label class="bill-form-field__label">执行日期</label>
                <UInputDate v-model="editExecuteDateValue" class="w-full" locale="en-CA"
                  :format-options="{ year: 'numeric', month: '2-digit', day: '2-digit' }" />
                <div v-if="editFormErrors.executeDate" class="bill-form-field__error">
                  {{ editFormErrors.executeDate }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">执行时间</label>
                <UInputTime v-model="editExecuteTimeValue" class="w-full" locale="en-GB" :hour-cycle="24"
                  :format-options="{ hour: '2-digit', minute: '2-digit', hour12: false }" />
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="editChecklistForm.note" :rows="3" placeholder="补充执行备注（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeEditChecklist">
              取消
            </UButton>

            <UButton icon="i-lucide-save" :loading="editingChecklist" @click="submitEditChecklist">
              保存
            </UButton>
          </div>
        </div>
      </template>
    </USlideover>
    <UModal v-model:open="deleteConfirmOpen" :ui="{ content: 'max-w-[440px]' }"
      @update:open="handleDeleteModalOpenChange">
      <template #content>
        <div class="bill-import-modal">
          <div class="bill-import-modal__header">
            <div>
              <div class="bill-import-modal__eyebrow">
                Checklist
              </div>
              <h2 class="bill-import-modal__title">
                删除清单
              </h2>
              <p class="bill-import-modal__desc">
                删除后将无法恢复，确认删除这条执行项吗？
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeDeleteChecklist" />
          </div>

          <div class="bill-import-modal__footer">
            <UButton color="neutral" variant="soft" @click="closeDeleteChecklist">
              取消
            </UButton>

            <UButton color="error" icon="i-lucide-trash-2" :loading="deletingChecklist" @click="confirmDeleteChecklist">
              确认删除
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>

<style scoped>
.bills-workspace {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.bills-workspace__title-wrap {
  min-width: 0;
}

.bills-workspace__eyebrow {
  font-size: 12px;
  color: var(--mobe-text-soft);
  margin-bottom: 4px;
}

.bills-workspace__title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}

.bills-workspace__title {
  margin: 0;
  font-size: 32px;
  line-height: 1.15;
  font-weight: 700;
  color: var(--mobe-text);
}

.bills-workspace__desc {
  font-size: 13px;
  color: var(--mobe-text-soft);
  white-space: nowrap;
}

.bills-workspace__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.checklist-toolbar__search {
  width: 320px;
}

.bill-filter-trigger-wrap {
  position: relative;
  display: inline-flex;
}

.bill-filter-trigger-wrap__clear {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border: 1px solid var(--mobe-border-soft);
  border-radius: 999px;
  background: var(--mobe-surface, #fff);
  color: var(--mobe-text-soft);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
  transition: all 0.18s ease;
  z-index: 2;
}

.bill-filter-trigger-wrap__clear:hover {
  color: var(--mobe-text);
  border-color: var(--mobe-border);
  background: var(--mobe-surface-soft, #f7f8fa);
}

.checklist-main-panel {
  padding: 0;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--mobe-border-soft) 72%, transparent);
  border-radius: 12px;
  background: color-mix(in srgb, var(--mobe-surface, #fff) 96%, var(--mobe-surface-soft, #f7f8fa));
}

.checklist-loading {
  display: flex;
  min-height: 360px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.checklist-loading__ring {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 2px solid rgba(145, 132, 126, 0.16);
  border-top-color: #8c8791;
  animation: checklist-spin 0.85s linear infinite;
}

.checklist-loading__text {
  font-size: 13px;
  color: #7f7a84;
  letter-spacing: 0.08em;
}

@keyframes checklist-spin {
  to {
    transform: rotate(360deg);
  }
}

.checklist-empty {
  display: flex;
  min-height: 320px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  text-align: center;
  padding: 24px;
}

.checklist-empty__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--mobe-text);
}

.checklist-empty__desc {
  max-width: 420px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--mobe-text-soft);
}

.checklist-table-shell {
  overflow-x: auto;
}

.checklist-table-head,
.checklist-row {
  display: grid;
  grid-template-columns:
    48px minmax(200px, 1.4fr) minmax(220px, 1.8fr) minmax(140px, 0.95fr) minmax(140px, 0.95fr) minmax(180px, 1.3fr) minmax(120px, 0.9fr);
  align-items: center;
  gap: 12px;
}

.checklist-table-head {
  min-height: 40px;
  padding: 8px 22px 6px;
  font-size: 12px;
  color: var(--mobe-text-soft);
  border-bottom: 1px solid color-mix(in srgb, var(--mobe-divider) 64%, transparent);
  min-width: 1160px;
}

.checklist-groups {
  padding: 6px 0 12px;
  min-width: 1160px;
}

.checklist-group+.checklist-group {
  margin-top: 8px;
}

.checklist-group__header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  text-align: left;
}

.checklist-group__left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.checklist-group__arrow {
  color: var(--mobe-text-soft);
  transition: transform 0.18s ease;
}

.checklist-group__arrow--collapsed {
  transform: rotate(-90deg);
}

.checklist-group__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--mobe-text);
}

.checklist-group__count {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--mobe-surface-soft, #f7f8fa);
  border: 1px solid color-mix(in srgb, var(--mobe-border-soft) 72%, transparent);
  font-size: 12px;
  color: var(--mobe-text-soft);
}

.checklist-group__body {
  padding: 0 12px;
}

.checklist-row {
  min-height: 62px;
  padding: 0 10px;
  border-top: 1px solid color-mix(in srgb, var(--mobe-divider) 58%, transparent);
  transition: background-color 0.18s ease, transform 0.18s ease;
}

.checklist-row:hover {
  background: color-mix(in srgb, var(--mobe-surface-soft, #f7f8fa) 72%, transparent);
}

.checklist-row--pending {
  background: transparent;
}



.checklist-row--shake {
  animation: checklist-row-shake 0.45s ease;
}

@keyframes checklist-row-shake {

  0%,
  100% {
    transform: translateX(0);
  }

  20% {
    transform: translateX(-4px);
  }

  40% {
    transform: translateX(4px);
  }

  60% {
    transform: translateX(-3px);
  }

  80% {
    transform: translateX(3px);
  }
}

.checklist-col {
  min-width: 0;
}

.check-status-btn {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--mobe-border-soft);
  border-radius: 999px;
  background: var(--mobe-surface, #fff);
  color: var(--mobe-text-soft);
  cursor: pointer;
  transition: all 0.18s ease;
}

.check-status-btn:hover {
  border-color: var(--mobe-border);
  color: var(--mobe-text);
}

.check-status-btn--pending {
  border-color: var(--mobe-border-soft);
  background: var(--mobe-surface, #fff);
  color: var(--mobe-text-soft);
}

.check-status-btn--done {
  border-color: color-mix(in srgb, #16a34a 58%, white);
  background: color-mix(in srgb, #16a34a 12%, white);
  color: #15803d;
}

.check-status-btn--skipped {
  border-color: color-mix(in srgb, #f59e0b 58%, white);
  background: color-mix(in srgb, #f59e0b 12%, white);
  color: #d97706;
}

.check-status-btn--missed {
  border-color: color-mix(in srgb, #ef4444 58%, white);
  background: color-mix(in srgb, #ef4444 10%, white);
  color: #dc2626;
}

.check-status-btn--armed {
  border-color: color-mix(in srgb, #f59e0b 58%, white);
  background: color-mix(in srgb, #f59e0b 10%, white);
  color: #d97706;
}

.checklist-title--done {
  text-decoration: line-through;
  text-decoration-thickness: 1px;
  text-decoration-color: color-mix(in srgb, var(--mobe-text-soft) 76%, transparent);
}

.checklist-row--skipped .checklist-title,
.checklist-row--skipped .checklist-desc,
.checklist-row--skipped .checklist-meta-text,
.checklist-row--skipped .checklist-execute-text {
  color: #b45309;
}

.checklist-row--missed .checklist-title,
.checklist-row--missed .checklist-desc,
.checklist-row--missed .checklist-meta-text,
.checklist-row--missed .checklist-execute-text {
  color: #b91c1c;
}

.checklist-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.checklist-title-line {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  width: 100%;
}

.checklist-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--mobe-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 0 1 auto;
  min-width: 0;
}

.checklist-title--done {
  text-decoration: line-through;
  text-decoration-thickness: 1px;
  text-decoration-color: color-mix(in srgb, var(--mobe-text-soft) 76%, transparent);
}

.check-status-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11px;
  border: 1px solid color-mix(in srgb, var(--mobe-border-soft) 72%, transparent);
  background: var(--mobe-surface-soft, #f7f8fa);
}

.check-status-badge--pending {
  color: var(--mobe-text-soft);
}

.check-status-badge--done {
  color: #15803d;
  background: color-mix(in srgb, #16a34a 12%, white);
}

.check-status-badge--skipped {
  color: #d97706;
  background: color-mix(in srgb, #f59e0b 12%, white);
}

.check-status-badge--missed {
  color: #dc2626;
  background: color-mix(in srgb, #ef4444 12%, white);
}

.checklist-desc,
.checklist-meta-text {
  font-size: 13px;
  color: var(--mobe-text-soft);
  line-height: 1.5;
}

.checklist-desc {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.checklist-desc--done {
  text-decoration: line-through;
  text-decoration-thickness: 1px;
  text-decoration-color: color-mix(in srgb, var(--mobe-text-soft) 66%, transparent);
}

.checklist-execute-text {
  display: inline-block;
  font-size: 13px;
  color: var(--mobe-text-soft);
  white-space: nowrap;
  line-height: 1.5;
}


.checklist-op-group {
  display: inline-flex;
  gap: 10px;
  align-items: center;
}

.checklist-op-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: var(--mobe-text-soft);
  cursor: pointer;
}

.checklist-op-btn:hover {
  color: var(--mobe-text);
}

.checklist-op-btn--danger:hover {
  color: #dc2626;
}

.checklist-op-btn--disabled {
  cursor: not-allowed;
  color: #b5bac3 !important;
}

.bill-create-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--mobe-surface, #fff);
}

.bill-create-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 24px 20px;
  border-bottom: 1px solid var(--mobe-divider);
}

.bill-create-panel__eyebrow {
  font-size: 12px;
  color: var(--mobe-text-soft);
  margin-bottom: 6px;
}

.bill-create-panel__title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--mobe-text);
}

.bill-create-panel__desc {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--mobe-text-soft);
}

.bill-create-panel__body {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.bill-create-panel__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px 24px;
  border-top: 1px solid var(--mobe-divider);
  background: var(--mobe-surface, #fff);
}

.bill-form-grid--compact {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.bill-form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}

.bill-form-field__label {
  font-size: 14px;
  font-weight: 600;
  color: var(--mobe-text);
}

.bill-form-field__error {
  font-size: 12px;
  color: #e11d48;
}

.bill-column-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
}

.bill-column-list__item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--mobe-text);
}

.bill-column-list__item input {
  width: 14px;
  height: 14px;
}

.bill-import-modal {
  display: flex;
  flex-direction: column;
  background: var(--mobe-surface, #fff);
}

.bill-import-modal__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 24px 20px;
  border-bottom: 1px solid var(--mobe-divider);
}

.bill-import-modal__eyebrow {
  font-size: 12px;
  color: var(--mobe-text-soft);
  margin-bottom: 6px;
}

.bill-import-modal__title {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--mobe-text);
}

.bill-import-modal__desc {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--mobe-text-soft);
}

.bill-import-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px 24px;
  border-top: 1px solid var(--mobe-divider);
  flex-wrap: wrap;
}

.check-select-menu-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.check-filter-date-clear {
  margin-left: 8px;
  border: none;
  background: transparent;
  padding: 0;
  font-size: 12px;
  color: var(--mobe-text-soft);
  cursor: pointer;
}

.check-filter-date-clear:hover {
  color: var(--mobe-text);
}

@media (max-width: 1023px) {
  .bills-workspace {
    flex-direction: column;
    align-items: stretch;
  }

  .bills-workspace__title {
    font-size: 28px;
  }

  .bills-workspace__desc {
    white-space: normal;
  }

  .bills-workspace__actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .checklist-toolbar__search {
    width: 100%;
  }

  .bill-form-grid--compact {
    grid-template-columns: 1fr;
  }

  .bill-column-list {
    grid-template-columns: 1fr;
  }
}
</style>