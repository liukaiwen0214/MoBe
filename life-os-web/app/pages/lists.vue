<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { createChecklistApi, pageChecklistsApi, updateChecklistApi, completeChecklistApi, restoreChecklistApi, deleteChecklistApi } from '~/api/checklists'
definePageMeta({
  middleware: 'auth'
})

const systemToast = useSystemToast()

type ChecklistPriority = 'LOW' | 'MEDIUM' | 'HIGH'
type ChecklistStatus = 'PENDING' | 'DONE'
type ChecklistFrequency = 'ONCE' | 'DAILY' | 'WEEKLY' | 'MONTHLY'
type ChecklistSourceType = 'CHECKLIST' | 'HABIT'

type ChecklistRecord = {
  id: string
  taskId: string
  taskName: string
  title: string
  description?: string
  status: ChecklistStatus
  priority: ChecklistPriority
  frequency: ChecklistFrequency
  reminderText?: string
  actionText?: string
  actionType?: string
  sourceType: ChecklistSourceType
  sort: number
  completedAt?: string
}

type ChecklistGroup = {
  taskId: string
  taskName: string
  pendingCount: number
  items: ChecklistRecord[]
}

type ChecklistColumnKey =
  | 'description'
  | 'reminder'
  | 'frequency'
  | 'priority'
  | 'action'
  | 'operations'

const loading = ref(true)
const filterPanelOpen = ref(false)
const creatingChecklist = ref(false)
const createOpen = ref(false)
const deleteConfirmOpen = ref(false)

const checklistRecords = ref<ChecklistRecord[]>([])
const collapsedGroups = ref<Record<string, boolean>>({})
const undoArmedMap = ref<Record<string, number>>({})
const shakingIds = ref<Record<string, boolean>>({})

const deletingChecklist = ref(false)
const deletingChecklistItem = ref<ChecklistRecord | null>(null)
const deleteHabitMode = ref<'DELETE_HABIT' | 'DELETE_ONLY_THIS_RENDER'>('DELETE_ONLY_THIS_RENDER')

const columnVisibility = reactive<Record<ChecklistColumnKey, boolean>>({
  description: true,
  reminder: true,
  frequency: true,
  priority: true,
  action: true,
  operations: true
})

const filters = reactive({
  keyword: '',
  status: 'ALL' as 'ALL' | ChecklistStatus,
  priority: 'ALL' as 'ALL' | ChecklistPriority,
  frequency: 'ALL' as 'ALL' | ChecklistFrequency,
  reminderOnly: false,
  actionOnly: false
})

const checklistForm = reactive({
  taskName: '',
  title: '',
  description: '',
  priority: 'MEDIUM' as ChecklistPriority,
  frequency: 'DAILY' as ChecklistFrequency,
  reminderText: '',
  actionText: ''
})

const formErrors = reactive({
  taskName: '',
  title: ''
})

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '待完成', value: 'PENDING' },
  { label: '已完成', value: 'DONE' }
]

const priorityOptions = [
  { label: '全部优先级', value: 'ALL' },
  { label: '高优先级', value: 'HIGH' },
  { label: '中优先级', value: 'MEDIUM' },
  { label: '低优先级', value: 'LOW' }
]

const frequencyOptions = [
  { label: '全部频率', value: 'ALL' },
  { label: '单次', value: 'ONCE' },
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const visibleColumnOptions = computed(() => [
  { key: 'description', label: '说明' },
  { key: 'reminder', label: '提醒' },
  { key: 'frequency', label: '重复' },
  { key: 'priority', label: '优先级' },
  { key: 'action', label: '动作' },
  { key: 'operations', label: '操作' }
] as Array<{ key: ChecklistColumnKey; label: string }>)

const activeFilterCount = computed(() => {
  let count = 0
  if (filters.keyword.trim()) count += 1
  if (filters.status !== 'ALL') count += 1
  if (filters.priority !== 'ALL') count += 1
  if (filters.frequency !== 'ALL') count += 1
  if (filters.reminderOnly) count += 1
  if (filters.actionOnly) count += 1

  return count
})

const hasActiveFilters = computed(() => activeFilterCount.value > 0)

function getPriorityLabel(priority: ChecklistPriority) {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return '低'
}

function getFrequencyLabel(frequency: ChecklistFrequency) {
  if (frequency === 'DAILY') return '每天'
  if (frequency === 'WEEKLY') return '每周'
  if (frequency === 'MONTHLY') return '每月'
  return '单次'
}

function getStatusLabel(status: ChecklistStatus) {
  return status === 'DONE' ? '已完成' : '待完成'
}

function priorityBadgeClass(priority: ChecklistPriority) {
  if (priority === 'HIGH') return 'check-priority check-priority--high'
  if (priority === 'MEDIUM') return 'check-priority check-priority--medium'
  return 'check-priority check-priority--low'
}

function priorityWeight(priority: ChecklistPriority) {
  if (priority === 'HIGH') return 3
  if (priority === 'MEDIUM') return 2
  return 1
}

/**
 * TODO:
 * 后续替换为真实 API
 */
function buildMockChecklistData(): ChecklistRecord[] {
  return [
    {
      id: '1',
      taskId: 'finance',
      taskName: '财务自律',
      title: '记账',
      description: '记录今天的收入和支出',
      status: 'PENDING',
      priority: 'HIGH',
      frequency: 'DAILY',
      reminderText: '今天 21:30',
      actionText: '新增账单',
      actionType: 'finance',
      sourceType: 'HABIT',
      sort: 1
    },
    {
      id: '2',
      taskId: 'finance',
      taskName: '财务自律',
      title: '预算检查',
      description: '看本周预算使用情况',
      status: 'PENDING',
      priority: 'MEDIUM',
      frequency: 'WEEKLY',
      reminderText: '周日 20:00',
      actionText: '查看任务',
      actionType: 'task',
      sourceType: 'HABIT',
      sort: 2
    },
    {
      id: '3',
      taskId: 'finance',
      taskName: '财务自律',
      title: '本周支出复盘',
      description: '汇总娱乐与饮食开销',
      status: 'DONE',
      priority: 'MEDIUM',
      frequency: 'WEEKLY',
      reminderText: '周六',
      actionText: '打开笔记',
      actionType: 'note',
      sourceType: 'HABIT',
      sort: 3
    },
    {
      id: '4',
      taskId: 'finance',
      taskName: '财务自律',
      title: '发薪日分配',
      description: '工资到账后做分配',
      status: 'PENDING',
      priority: 'HIGH',
      frequency: 'ONCE',
      reminderText: '发薪日',
      actionText: '新增账单',
      actionType: 'finance',
      sourceType: 'CHECKLIST',
      sort: 4
    },
    {
      id: '5',
      taskId: 'life',
      taskName: '生活秩序',
      title: '整理桌面',
      description: '睡前保持桌面整洁',
      status: 'DONE',
      priority: 'MEDIUM',
      frequency: 'DAILY',
      reminderText: '今晚 22:00',
      actionText: '打开笔记',
      actionType: 'note',
      sourceType: 'HABIT',
      sort: 1
    },
    {
      id: '6',
      taskId: 'life',
      taskName: '生活秩序',
      title: '拿快递',
      description: '取回驿站包裹',
      status: 'PENDING',
      priority: 'HIGH',
      frequency: 'ONCE',
      reminderText: '今天',
      actionText: '打开任务',
      actionType: 'task',
      sourceType: 'CHECKLIST',
      sort: 2
    },
    {
      id: '7',
      taskId: 'life',
      taskName: '生活秩序',
      title: '采购补货',
      description: '补水和水果',
      status: 'PENDING',
      priority: 'LOW',
      frequency: 'WEEKLY',
      reminderText: '周六上午',
      actionText: '打开清单',
      actionType: 'checklist',
      sourceType: 'CHECKLIST',
      sort: 3
    },
    {
      id: '8',
      taskId: 'growth',
      taskName: '学习成长',
      title: '阅读 30 分钟',
      description: '每天固定输入一点内容',
      status: 'PENDING',
      priority: 'MEDIUM',
      frequency: 'DAILY',
      reminderText: '23:00',
      actionText: '打开目标',
      actionType: 'goal',
      sourceType: 'HABIT',
      sort: 1
    },
    {
      id: '9',
      taskId: 'growth',
      taskName: '学习成长',
      title: '输出笔记',
      description: '对输入内容做整理',
      status: 'PENDING',
      priority: 'HIGH',
      frequency: 'WEEKLY',
      reminderText: '周三 / 周日',
      actionText: '打开笔记',
      actionType: 'note',
      sourceType: 'HABIT',
      sort: 2
    },
    {
      id: '10',
      taskId: 'growth',
      taskName: '学习成长',
      title: '知识复盘',
      description: '复盘本周吸收内容',
      status: 'DONE',
      priority: 'LOW',
      frequency: 'WEEKLY',
      reminderText: '周日',
      actionText: '打开目标',
      actionType: 'goal',
      sourceType: 'HABIT',
      sort: 3
    }
  ]
}

async function fetchChecklistRecords(showSuccess = false) {
  loading.value = true

  try {
    const requestData = {
      pageNum: 1,
      pageSize: 500,
      keyword: filters.keyword.trim() || undefined,
      status: filters.status !== 'ALL' ? filters.status : undefined,
      priority: filters.priority !== 'ALL' ? filters.priority : undefined,
      frequency: filters.frequency !== 'ALL' ? filters.frequency : undefined,
      reminderOnly: filters.reminderOnly ? 1 : 0,
      actionOnly: filters.actionOnly ? 1 : 0
    }

    const res: any = await pageChecklistsApi(requestData)
    const records = res?.data?.records || []

    checklistRecords.value = records as ChecklistRecord[]

    const taskIds = Array.from(new Set(records.map((item: any) => item.taskId || item.taskName)))
    collapsedGroups.value = Object.fromEntries(
      taskIds.map((taskId) => [taskId as string, collapsedGroups.value[taskId as string] ?? false])
    )

    if (showSuccess) {
      systemToast.success('刷新成功', '清单列表已更新', 'checklist-refresh-success')
    }
  } catch (error: any) {
    systemToast.error('获取失败', error?.message || '请稍后重试', 'checklist-fetch-error')
  } finally {
    loading.value = false
  }
}

function clearAllFilters() {
  filters.keyword = ''
  filters.status = 'ALL'
  filters.priority = 'ALL'
  filters.frequency = 'ALL'
  filters.reminderOnly = false
  filters.actionOnly = false

  systemToast.success('已清空筛选', '所有筛选条件已重置', 'checklist-clear-filters-success')
}

function matchesKeyword(item: ChecklistRecord, keyword: string) {
  if (!keyword) return true

  const text = [
    item.taskName,
    item.title,
    item.description || '',
    item.reminderText || '',
    item.actionText || ''
  ].join(' ').toLowerCase()

  return text.includes(keyword)
}

const filteredRecords = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return checklistRecords.value.filter(item => {
    const matchKeyword = matchesKeyword(item, keyword)
    const matchStatus = filters.status === 'ALL' || item.status === filters.status
    const matchPriority = filters.priority === 'ALL' || item.priority === filters.priority
    const matchFrequency = filters.frequency === 'ALL' || item.frequency === filters.frequency
    const matchReminder = !filters.reminderOnly || Boolean(item.reminderText)
    const matchAction = !filters.actionOnly || Boolean(item.actionText)

    return matchKeyword && matchStatus && matchPriority && matchFrequency && matchReminder && matchAction
  })
})

const groupedChecklistData = computed(() => {
  const groupedMap = new Map<string, any[]>()

  for (const item of checklistRecords.value) {
    const groupKey = item.taskId || item.taskName || 'default'

    if (!groupedMap.has(groupKey)) {
      groupedMap.set(groupKey, [])
    }

    groupedMap.get(groupKey)?.push(item)
  }

  return Array.from(groupedMap.entries()).map(([taskId, items]) => {
    const sortedItems = [...items].sort((a, b) => {
      if (a.status !== b.status) {
        return a.status === 'PENDING' ? -1 : 1
      }

      const priorityWeight = (priority: string) => {
        if (priority === 'HIGH') return 3
        if (priority === 'MEDIUM') return 2
        return 1
      }

      const priorityDiff = priorityWeight(b.priority) - priorityWeight(a.priority)
      if (priorityDiff !== 0) {
        return priorityDiff
      }

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
      // 字段用于前端展示完成时间，不在 ChecklistRecord 类型定义中
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
    systemToast.info('再次点击可恢复', '3 秒内再次点击可恢复为未完成', `checklist-undo-arm-${id}`)
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

    systemToast.success('已恢复', `${target.title} 已恢复为待完成`, `checklist-undo-done-${id}`)
  } catch (error: any) {
    systemToast.error('恢复失败', error?.message || '请稍后重试', `checklist-restore-${id}`)
  }
}

function isUndoArmed(id: string) {
  const armedAt = undoArmedMap.value[id]
  if (!armedAt) return false
  return Date.now() - armedAt <= 3000
}

function handleAction(item: ChecklistRecord) {
  if (item.status === 'DONE') return

  systemToast.info(
    '动作占位',
    item.actionText ? `后续这里执行：${item.actionText}` : '该清单暂无动作',
    `checklist-action-${item.id}`
  )
}

function openCreateChecklist() {
  checklistForm.taskName = ''
  checklistForm.title = ''
  checklistForm.description = ''
  checklistForm.priority = 'MEDIUM'
  checklistForm.frequency = 'DAILY'
  checklistForm.reminderText = ''
  checklistForm.actionText = ''

  formErrors.taskName = ''
  formErrors.title = ''

  createOpen.value = true
}

function closeCreateChecklist() {
  createOpen.value = false
}
async function confirmDeleteChecklistWithMode(mode: 'DELETE_HABIT' | 'DELETE_ONLY_THIS_RENDER') {
  deleteHabitMode.value = mode
  await confirmDeleteChecklist()
}
function validateChecklistForm() {
  formErrors.taskName = checklistForm.taskName.trim() ? '' : '请输入所属任务'
  formErrors.title = checklistForm.title.trim() ? '' : '请输入清单标题'

  return !formErrors.taskName && !formErrors.title
}

async function submitCreateChecklist() {
  if (!validateChecklistForm()) {
    systemToast.error('保存失败', '请先补全任务和清单标题', 'checklist-create-validate-error')
    return
  }

  creatingChecklist.value = true

  try {
    await createChecklistApi({
      taskName: checklistForm.taskName.trim(),
      title: checklistForm.title.trim(),
      description: checklistForm.description.trim() || undefined,
      priority: checklistForm.priority,
      reminderText: checklistForm.reminderText.trim() || undefined,
      actionText: checklistForm.actionText.trim() || undefined,
      actionType: checklistForm.actionText.trim() ? 'custom' : undefined,
      sort: 0
    })

    closeCreateChecklist()
    await fetchChecklistRecords()

    systemToast.success('保存成功', '清单已新增', 'checklist-create-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'checklist-create-error')
  } finally {
    creatingChecklist.value = false
  }
}

function editChecklist(item: ChecklistRecord) {
  if (item.status === 'DONE') return
  systemToast.info('编辑占位', `后续这里编辑：${item.title}`, `checklist-edit-${item.id}`)
}

function openDeleteChecklist(item: ChecklistRecord) {
  if (item.status === 'DONE') return

  deletingChecklistItem.value = item
  deleteHabitMode.value = item.sourceType === 'HABIT' ? 'DELETE_ONLY_THIS_RENDER' : 'DELETE_HABIT'
  deleteConfirmOpen.value = true
}

function closeDeleteChecklist() {
  deleteConfirmOpen.value = false
}
function resetDeleteChecklistState() {
  deletingChecklistItem.value = null
  deleteHabitMode.value = 'DELETE_ONLY_THIS_RENDER'
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

    if (item.sourceType === 'CHECKLIST') {
      await deleteChecklistApi(item.id)
    } else {
      /**
       * TODO:
       * HABIT 来源后续接习惯模块删除接口
       * 现在先只做前端占位
       */
      throw new Error('习惯删除接口暂未接入')
    }

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
          <span class="bills-workspace__desc">承接提醒、重复、动作入口的生活清单</span>
        </div>
      </div>

      <div class="bills-workspace__actions">
        <UInput v-model="filters.keyword" icon="i-lucide-search" placeholder="搜索标题、说明、提醒或动作"
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
              <div v-if="columnVisibility.reminder" class="checklist-col checklist-col--reminder">提醒</div>
              <div v-if="columnVisibility.frequency" class="checklist-col checklist-col--frequency">重复</div>
              <div v-if="columnVisibility.priority" class="checklist-col checklist-col--priority">优先级</div>
              <div v-if="columnVisibility.action" class="checklist-col checklist-col--action">动作</div>
              <div v-if="columnVisibility.operations" class="checklist-col checklist-col--operations">操作</div>
            </div>

            <div class="checklist-groups">
              <section v-for="group in groupedChecklistData" :key="group.taskId" class="checklist-group">
                <button type="button" class="checklist-group__header" @click="toggleGroup(group.taskId)">
                  <div class="checklist-group__left">
                    <UIcon name="i-lucide-chevron-down" class="size-4 checklist-group__arrow"
                      :class="{ 'checklist-group__arrow--collapsed': collapsedGroups[group.taskId] }" />
                    <span class="checklist-group__title">{{ group.taskName }}</span>
                    <span class="checklist-group__count">未完成 {{ group.pendingCount }}</span>
                  </div>
                </button>

                <div v-show="!collapsedGroups[group.taskId]" class="checklist-group__body">
                  <div v-for="item in group.items" :key="item.id" class="checklist-row" :class="{
                    'checklist-row--done': item.status === 'DONE',
                    'checklist-row--shake': shakingIds[item.id]
                  }">
                    <div class="checklist-col checklist-col--status">
                      <button type="button" class="check-status-btn" :class="{
                        'check-status-btn--done': item.status === 'DONE',
                        'check-status-btn--armed': isUndoArmed(item.id)
                      }" @click.stop="toggleChecklistStatus(item.id)">
                        <UIcon :name="item.status === 'DONE' ? 'i-lucide-check' : 'i-lucide-circle'" class="size-4" />
                      </button>
                    </div>

                    <div class="checklist-col checklist-col--title">
                      <div class="checklist-title-wrap">
                        <div class="checklist-title" :class="{ 'checklist-title--done': item.status === 'DONE' }">
                          {{ item.title }}
                        </div>
                        <div class="checklist-subline">
                          {{ getStatusLabel(item.status) }}
                          <span class="checklist-source-badge"
                            :class="`checklist-source-badge--${item.sourceType.toLowerCase()}`">
                            {{ item.sourceType === 'HABIT' ? '习惯' : '清单' }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div v-if="columnVisibility.description" class="checklist-col checklist-col--desc">
                      <div class="checklist-desc" :class="{ 'checklist-desc--done': item.status === 'DONE' }">
                        {{ item.description || '—' }}
                      </div>
                    </div>

                    <div v-if="columnVisibility.reminder" class="checklist-col checklist-col--reminder">
                      <span class="checklist-meta-text">
                        {{ item.reminderText || '—' }}
                      </span>
                    </div>

                    <div v-if="columnVisibility.frequency" class="checklist-col checklist-col--frequency">
                      <span class="checklist-meta-text">
                        {{ getFrequencyLabel(item.frequency) }}
                      </span>
                    </div>

                    <div v-if="columnVisibility.priority" class="checklist-col checklist-col--priority">
                      <span :class="priorityBadgeClass(item.priority)">
                        {{ getPriorityLabel(item.priority) }}
                      </span>
                    </div>

                    <div v-if="columnVisibility.action" class="checklist-col checklist-col--action">
                      <button v-if="item.actionText" type="button" class="checklist-action-link"
                        :disabled="item.status === 'DONE'"
                        :class="{ 'checklist-action-link--disabled': item.status === 'DONE' }"
                        @click.stop="handleAction(item)">
                        {{ item.actionText }}
                      </button>
                      <span v-else class="checklist-meta-text">—</span>
                    </div>

                    <div v-if="columnVisibility.operations" class="checklist-col checklist-col--operations">
                      <div class="checklist-op-group">
                        <button type="button" class="checklist-op-btn" :disabled="item.status === 'DONE'"
                          :class="{ 'checklist-op-btn--disabled': item.status === 'DONE' }"
                          @click.stop="editChecklist(item)">
                          编辑
                        </button>
                        <button type="button" class="checklist-op-btn checklist-op-btn--danger"
                          :disabled="item.status === 'DONE'"
                          :class="{ 'checklist-op-btn--disabled': item.status === 'DONE' }"
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

    <USlideover v-model:open="filterPanelOpen" side="right" :ui="{
      content: 'w-full max-w-[420px]'
    }">
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
              <label class="bill-form-field__label">优先级</label>
              <USelect v-model="filters.priority" :items="priorityOptions" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">重复频率</label>
              <USelect v-model="filters.frequency" :items="frequencyOptions" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">其他条件</label>
              <div class="check-filter-switches">
                <label class="check-filter-switch">
                  <input v-model="filters.reminderOnly" type="checkbox">
                  <span>仅看有提醒</span>
                </label>

                <label class="check-filter-switch">
                  <input v-model="filters.actionOnly" type="checkbox">
                  <span>仅看有动作</span>
                </label>
              </div>
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

    <USlideover v-model:open="createOpen" side="right" :ui="{
      content: 'w-full max-w-[460px]'
    }">
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
                先创建一条可提醒、可重复、可挂动作入口的生活清单。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeCreateChecklist" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-field">
              <label class="bill-form-field__label">所属任务</label>
              <UInput v-model="checklistForm.taskName" placeholder="例如：财务自律" />
              <div v-if="formErrors.taskName" class="bill-form-field__error">
                {{ formErrors.taskName }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">清单标题</label>
              <UInput v-model="checklistForm.title" placeholder="例如：每天记账" />
              <div v-if="formErrors.title" class="bill-form-field__error">
                {{ formErrors.title }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">说明</label>
              <UTextarea v-model="checklistForm.description" :rows="3" placeholder="补充这条清单的说明（可选）" autoresize />
            </div>

            <div class="bill-form-grid bill-form-grid--compact">
              <div class="bill-form-field">
                <label class="bill-form-field__label">优先级</label>
                <USelect v-model="checklistForm.priority" :items="[
                  { label: '高优先级', value: 'HIGH' },
                  { label: '中优先级', value: 'MEDIUM' },
                  { label: '低优先级', value: 'LOW' }
                ]" />
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">重复频率</label>
                <USelect v-model="checklistForm.frequency" :items="[
                  { label: '单次', value: 'ONCE' },
                  { label: '每天', value: 'DAILY' },
                  { label: '每周', value: 'WEEKLY' },
                  { label: '每月', value: 'MONTHLY' }
                ]" />
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">提醒文案</label>
              <UInput v-model="checklistForm.reminderText" placeholder="例如：今天 21:30" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">动作入口</label>
              <UInput v-model="checklistForm.actionText" placeholder="例如：新增账单 / 打开笔记" />
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

    <UModal v-model:open="deleteConfirmOpen" :ui="{
      content: 'max-w-[440px]'
    }" @update:open="handleDeleteModalOpenChange">
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
                <template v-if="deletingChecklistItem?.sourceType === 'HABIT'">
                  这条内容来自习惯模块，确认要删除吗？
                </template>
                <template v-else>
                  删除后将无法恢复，确认删除这条清单吗？
                </template>
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeDeleteChecklist" />
          </div>

          <div class="bill-import-modal__footer">
            <template v-if="deletingChecklistItem?.sourceType === 'HABIT'">
              <UButton color="neutral" variant="soft" @click="closeDeleteChecklist">
                取消
              </UButton>

              <UButton color="warning" variant="soft" :loading="deletingChecklist"
                @click="confirmDeleteChecklistWithMode('DELETE_ONLY_THIS_RENDER')">
                删除当前展示
              </UButton>

              <UButton color="error" icon="i-lucide-trash-2" :loading="deletingChecklist"
                @click="confirmDeleteChecklistWithMode('DELETE_HABIT')">
                删除整个习惯
              </UButton>
            </template>

            <template v-else>
              <UButton color="neutral" variant="soft" @click="closeDeleteChecklist">
                取消
              </UButton>

              <UButton color="error" icon="i-lucide-trash-2" :loading="deletingChecklist"
                @click="confirmDeleteChecklist">
                确认删除
              </UButton>
            </template>
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
    48px minmax(180px, 1.25fr) minmax(220px, 1.8fr) minmax(110px, 0.9fr) minmax(80px, 0.7fr) minmax(80px, 0.7fr) minmax(120px, 0.9fr) minmax(120px, 0.9fr);
  align-items: center;
  gap: 12px;
}

.checklist-table-head {
  min-height: 40px;
  padding: 8px 22px 6px;
  font-size: 12px;
  color: var(--mobe-text-soft);
  border-bottom: 1px solid color-mix(in srgb, var(--mobe-divider) 64%, transparent);
  min-width: 1120px;
}

.checklist-groups {
  padding: 6px 0 12px;
  min-width: 1120px;
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

.checklist-row--done {
  opacity: 0.72;
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

.check-status-btn--done {
  border-color: color-mix(in srgb, #16a34a 58%, white);
  background: color-mix(in srgb, #16a34a 12%, white);
  color: #15803d;
}

.check-status-btn--armed {
  border-color: color-mix(in srgb, #f59e0b 58%, white);
  background: color-mix(in srgb, #f59e0b 10%, white);
  color: #d97706;
}

.checklist-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.checklist-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--mobe-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.checklist-title--done {
  text-decoration: line-through;
  text-decoration-thickness: 1px;
  text-decoration-color: color-mix(in srgb, var(--mobe-text-soft) 76%, transparent);
}

.checklist-subline {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--mobe-text-soft);
}

.checklist-source-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11px;
  border: 1px solid color-mix(in srgb, var(--mobe-border-soft) 72%, transparent);
  background: var(--mobe-surface-soft, #f7f8fa);
}

.checklist-source-badge--habit {
  color: #7c3aed;
}

.checklist-source-badge--checklist {
  color: #0f766e;
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

.check-priority {
  display: inline-flex;
  min-width: 28px;
  height: 24px;
  padding: 0 8px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.check-priority--high {
  background: color-mix(in srgb, #ef4444 12%, white);
  color: #dc2626;
}

.check-priority--medium {
  background: color-mix(in srgb, #f59e0b 12%, white);
  color: #d97706;
}

.check-priority--low {
  background: color-mix(in srgb, #64748b 12%, white);
  color: #475569;
}

.checklist-action-link {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--ui-primary, #10b981);
  cursor: pointer;
  text-align: left;
}

.checklist-action-link:hover {
  text-decoration: underline;
}

.checklist-action-link--disabled {
  color: var(--mobe-text-soft);
  cursor: not-allowed;
  text-decoration: none !important;
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

.check-filter-switches {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-filter-switch {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--mobe-text);
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

.bill-import-modal__body {
  padding: 24px;
}

.bill-import-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px 24px;
  border-top: 1px solid var(--mobe-divider);
  flex-wrap: wrap;
}

.check-delete-action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.check-delete-action-grid__btn {
  justify-content: center;
  min-height: 44px;
}

@media (max-width: 640px) {
  .check-delete-action-grid {
    grid-template-columns: 1fr;
  }
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