<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { parseDate, Time, parseTime, type CalendarDate } from '@internationalized/date'
import {
  pageHabitsApi,
  createHabitDetailApi,
  updateHabitApi,
  deleteHabitApi,
  enableHabitApi,
  disableHabitApi,
  toggleHabitGenerateApi,
  getHabitStatsApi,
  pageHabitTimelineApi,
  type HabitPageItemResponse,
  type HabitStatsResponse,
  type HabitTimelineItemResponse
} from '~/api/habits'
import { listSimpleTasksApi, type TaskSimpleItem } from '~/api/tasks'

definePageMeta({
  middleware: 'auth'
})
const systemToast = useSystemToast()
type HabitFrequencyType = 'DAILY' | 'WEEKLY' | 'MONTHLY'
type HabitStatus = 'ENABLED' | 'DISABLED'
type HabitTimelineStatus = 'PENDING' | 'DONE' | 'MISSED' | 'SKIPPED'
type HabitTimelineSource = 'LIST'

type HabitTimelineRecord = {
  id: string
  status: HabitTimelineStatus
  recordDate: string
  recordTime: string
  source: HabitTimelineSource
  note?: string
}

type HabitItem = {
  id: string
  taskId?: string
  taskName?: string
  checklistId?: string
  checklistTitle?: string
  name: string
  description: string
  icon: string
  color: string
  frequencyType: HabitFrequencyType
  frequencyText: string
  startDate: string
  reminderTime: string
  generateToTodo: boolean
  status: HabitStatus
  totalCheckInCount: number
  streakCount: number
  longestStreakCount: number
  lastCheckInAt: string
  sort: number
}

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' }
]

const frequencyOptions = [
  { label: '全部频率', value: 'ALL' },
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const frequencyTypeEditOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const generateOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已开启', value: 'ON' },
  { label: '未开启', value: 'OFF' }
]

const colorOptions = [
  { label: '绿色', value: 'green' },
  { label: '蓝色', value: 'blue' },
  { label: '橙色', value: 'orange' },
  { label: '紫色', value: 'purple' }
]


const loading = ref(false)
const habits = ref<HabitItem[]>([])
const creatingHabit = ref(false)
const updatingHabit = ref(false)
const togglingGenerateIds = ref<string[]>([])
const togglingStatusIds = ref<string[]>([])
const deletingHabit = ref(false)
const habitDetailLoading = ref(false)

const habitStats = ref<HabitStatsResponse>({
  habitId: '',
  totalCheckInCount: 0,
  streakCount: 0,
  longestStreakCount: 0,
  lastCheckInAt: ''
})

const habitTimelineRecords = ref<HabitTimelineRecord[]>([])
const filters = reactive({
  keyword: '',
  status: 'ALL' as 'ALL' | HabitStatus,
  frequencyType: 'ALL' as 'ALL' | HabitFrequencyType,
  generateToTodo: 'ALL' as 'ALL' | 'ON' | 'OFF'
})
const debouncedKeyword = ref('')
let habitKeywordTimer: ReturnType<typeof setTimeout> | null = null

const detailOpen = ref(false)
const drawerMode = ref<'create' | 'detail'>('detail')
const detailMode = ref<'view' | 'edit'>('view')
const activeHabitId = ref<string>('1')

const habitStartDateInput = useTemplateRef('habitStartDateInput')
const habitReminderTimeInput = useTemplateRef('habitReminderTimeInput')
const habitHistoryScrollRef = ref<HTMLElement | null>(null)
const taskOptions = ref<TaskSimpleItem[]>([])
const taskLoading = ref(false)

const editForm = reactive({
  taskId: '',
  name: '',
  description: '',
  icon: '',
  color: 'green',
  frequencyType: 'DAILY' as HabitFrequencyType,
  frequencyText: '',
  startDate: '',
  reminderTime: '',
  generateToTodo: true,
  status: 'ENABLED' as HabitStatus
})

const activeHabit = computed(() => {
  return habits.value.find(item => item.id === activeHabitId.value) || habits.value[0]
})

const habitStartDateValue = computed<CalendarDate | undefined>({
  get() {
    if (!editForm.startDate) return undefined
    try {
      return parseDate(editForm.startDate)
    } catch {
      return undefined
    }
  },
  set(value) {
    editForm.startDate = value ? value.toString() : ''
  }
})

const habitReminderTimeValue = computed<Time | undefined>({
  get() {
    if (!editForm.reminderTime) return undefined
    try {
      return parseTime(editForm.reminderTime)
    } catch {
      return undefined
    }
  },
  set(value) {
    editForm.reminderTime = value ? value.toString().slice(0, 5) : ''
  }
})

const filteredHabits = computed(() => habits.value)

const timelineItems = computed(() => {
  return [...habitTimelineRecords.value]
    .sort((a, b) => `${b.recordDate} ${b.recordTime}`.localeCompare(`${a.recordDate} ${a.recordTime}`))
    .map((record) => ({
      ...record,
      date: `${record.recordDate}${record.recordTime && record.recordTime !== '--' ? ` ${record.recordTime}` : ''}`,
      icon: getTimelineIcon(record.status)
    }))
})

const iconOptions = [
  { label: '喝水', value: 'i-lucide-cup-soda' },
  { label: '阅读', value: 'i-lucide-book-open-text' },
  { label: '账单', value: 'i-lucide-wallet-cards' },
  { label: '目标', value: 'i-lucide-target' },
  { label: '运动', value: 'i-lucide-dumbbell' },
  { label: '睡眠', value: 'i-lucide-moon-star' },
  { label: '学习', value: 'i-lucide-graduation-cap' },
  { label: '清单', value: 'i-lucide-list-todo' },
  { label: '提醒', value: 'i-lucide-bell' },
  { label: '星标', value: 'i-lucide-star' }
]
function formatDateTimeText(value?: string) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

function mapHabitItem(item: HabitPageItemResponse): HabitItem {
  return {
    id: item.id,
    taskId: item.taskId || undefined,
    taskName: item.taskName || undefined,
    name: item.name || '',
    checklistId: item.checklistId || undefined,
    checklistTitle: item.checklistTitle || undefined,
    description: item.description || '',
    icon: item.icon || 'i-lucide-sparkles',
    color: item.color || 'green',
    frequencyType: (item.frequencyType || 'DAILY') as HabitFrequencyType,
    frequencyText: item.frequencyText || getFrequencyLabel((item.frequencyType || 'DAILY') as HabitFrequencyType),
    startDate: item.startDate || '',
    reminderTime: item.reminderTime ? item.reminderTime.slice(0, 5) : '',
    generateToTodo: !!item.generateToTodo,
    status: (item.status || 'ENABLED') as HabitStatus,
    totalCheckInCount: item.totalCheckInCount || 0,
    streakCount: item.streakCount || 0,
    longestStreakCount: item.longestStreakCount || 0,
    lastCheckInAt: formatDateTimeText(item.lastCheckInAt),
    sort: item.sort || 0
  }
}
function mapHabitTimelineItem(item: HabitTimelineItemResponse): HabitTimelineRecord {
  return {
    id: item.id,
    status: item.status as HabitTimelineStatus,
    recordDate: item.recordDate || '',
    recordTime: item.recordTime ? item.recordTime.slice(0, 5) : '--',
    source: item.source as HabitTimelineSource,
    note: item.note || undefined
  }
}
function pad2(value: number) {
  return String(value).padStart(2, '0')
}

function getNowDateString() {
  const now = new Date()
  return `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())}`
}

function getNowTimeString() {
  const now = new Date()
  return `${pad2(now.getHours())}:${pad2(now.getMinutes())}`
}
async function fetchTaskOptions() {
  taskLoading.value = true

  try {
    const res: any = await listSimpleTasksApi()
    taskOptions.value = res?.data || []
  } catch (error: any) {
    systemToast.error('获取任务失败', error?.message || '请稍后重试', 'habit-task-options-fetch-error')
  } finally {
    taskLoading.value = false
  }
}

async function fetchHabitDetailData(habitId: string) {
  if (!habitId) return

  habitDetailLoading.value = true

  try {
    const [statsRes, timelineRes]: any = await Promise.all([
      getHabitStatsApi(habitId),
      pageHabitTimelineApi({
        habitId,
        pageNum: 1,
        pageSize: 100
      })
    ])

    const stats = statsRes?.data as HabitStatsResponse | undefined
    const records = (timelineRes?.data?.records || []) as HabitTimelineItemResponse[]

    habitStats.value = {
      habitId: stats?.habitId || habitId,
      totalCheckInCount: stats?.totalCheckInCount || 0,
      streakCount: stats?.streakCount || 0,
      longestStreakCount: stats?.longestStreakCount || 0,
      lastCheckInAt: formatDateTimeText(stats?.lastCheckInAt) || ''
    }

    habitTimelineRecords.value = records.map(mapHabitTimelineItem)
  } catch (error: any) {
    systemToast.error('获取详情失败', error?.message || '请稍后重试', `habit-detail-fetch-${habitId}`)
  } finally {
    habitDetailLoading.value = false
  }
}
async function fetchHabits(showSuccess = false) {
  loading.value = true

  try {
    const res: any = await pageHabitsApi({
      pageNum: 1,
      pageSize: 200,
      keyword: debouncedKeyword.value.trim() || undefined,
      status: filters.status !== 'ALL' ? (filters.status as HabitStatus) : undefined,
      frequencyType: filters.frequencyType !== 'ALL' ? (filters.frequencyType as HabitFrequencyType) : undefined,
      generateToTodo:
        filters.generateToTodo === 'ALL'
          ? undefined
          : filters.generateToTodo === 'ON'
    })

    const records = (res?.data?.records || []) as HabitPageItemResponse[]
    habits.value = records.map(mapHabitItem)

    if (!activeHabitId.value && habits.value.length > 0) {
      activeHabitId.value = habits.value[0]?.id || ''
    }

    if (activeHabitId.value && !habits.value.some(item => item.id === activeHabitId.value)) {
      activeHabitId.value = habits.value[0]?.id || ''
    }
    if (detailOpen.value && activeHabitId.value) {
      await fetchHabitDetailData(activeHabitId.value)
    }

    if (showSuccess) {
      systemToast.success('刷新成功', '习惯列表已更新', 'habit-page-refresh-success')
    }
  } catch (error: any) {
    systemToast.error('获取失败', error?.message || '请稍后重试', 'habit-page-fetch-error')
  } finally {
    loading.value = false
  }
}
function getIconLabel(icon: string) {
  return iconOptions.find(item => item.value === icon)?.label || '未选择'
}

function resetEditForm(habit: HabitItem) {
  editForm.taskId = habit.taskId || ''
  editForm.name = habit.name
  editForm.description = habit.description
  editForm.icon = habit.icon
  editForm.color = habit.color
  editForm.frequencyType = habit.frequencyType
  editForm.frequencyText = habit.frequencyText
  editForm.startDate = habit.startDate
  editForm.reminderTime = habit.reminderTime
  editForm.generateToTodo = habit.generateToTodo
  editForm.status = habit.status
}

function resetCreateForm() {
  editForm.taskId = ''
  editForm.name = ''
  editForm.description = ''
  editForm.icon = 'i-lucide-sparkles'
  editForm.color = 'green'
  editForm.frequencyType = 'DAILY'
  editForm.frequencyText = '每天'
  editForm.startDate = getNowDateString()
  editForm.reminderTime = getNowTimeString()
  editForm.generateToTodo = true
  editForm.status = 'ENABLED'
}

async function openDetail(habit: HabitItem) {
  await fetchTaskOptions()
  activeHabitId.value = habit.id
  resetEditForm(habit)
  drawerMode.value = 'detail'
  detailMode.value = 'view'
  detailOpen.value = true

  await fetchHabitDetailData(habit.id)
}

async function openCreateDrawer() {
  await fetchTaskOptions()
  resetCreateForm()
  drawerMode.value = 'create'
  detailMode.value = 'view'
  detailOpen.value = true
}

function startEdit() {
  if (!activeHabit.value) return
  resetEditForm(activeHabit.value)
  detailMode.value = 'edit'
}

function cancelEdit() {
  if (!activeHabit.value) return
  resetEditForm(activeHabit.value)
  detailMode.value = 'view'
}

function closeDrawer() {
  detailOpen.value = false
  habitTimelineRecords.value = []
  habitStats.value = {
    habitId: '',
    totalCheckInCount: 0,
    streakCount: 0,
    longestStreakCount: 0,
    lastCheckInAt: ''
  }
}
async function removeHabit() {
  if (!activeHabit.value) return

  deletingHabit.value = true

  try {
    await deleteHabitApi(activeHabit.value.id)

    const deletedId = activeHabit.value.id

    await fetchHabits()
    detailOpen.value = false

    if (activeHabitId.value === deletedId) {
      activeHabitId.value = habits.value[0]?.id || ''
    }

    systemToast.success('删除成功', '习惯已删除', `habit-delete-${deletedId}`)
  } catch (error: any) {
    systemToast.error('删除失败', error?.message || '请稍后重试', 'habit-delete-error')
  } finally {
    deletingHabit.value = false
  }
}

async function saveHabit() {
  if (!activeHabit.value) return

  const name = editForm.name.trim()
  if (!name) {
    systemToast.error('保存失败', '请输入习惯名称', 'habit-update-name-empty')
    return
  }

  updatingHabit.value = true

  try {
    await updateHabitApi({
      id: activeHabit.value.id,
      taskId: editForm.taskId || undefined,
      name,
      description: editForm.description.trim() || undefined,
      icon: editForm.icon.trim() || 'i-lucide-sparkles',
      color: editForm.color,
      frequencyType: editForm.frequencyType,
      frequencyText: editForm.frequencyText.trim() || getFrequencyLabel(editForm.frequencyType),
      startDate: editForm.startDate || undefined,
      reminderTime: editForm.reminderTime || undefined,
      generateToTodo: editForm.generateToTodo,
      status: editForm.status,
      sort: activeHabit.value.sort
    })

    await fetchHabits()
    detailMode.value = 'view'

    const latestHabit = habits.value.find(item => item.id === activeHabit.value?.id)
    if (latestHabit) {
      resetEditForm(latestHabit)
    }

    systemToast.success('保存成功', '习惯已更新', 'habit-update-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'habit-update-error')
  } finally {
    updatingHabit.value = false
  }
}

async function createHabit() {
  const name = editForm.name.trim()
  if (!name) {
    systemToast.error('保存失败', '请输入习惯名称', 'habit-create-name-empty')
    return
  }

  creatingHabit.value = true

  try {
    await createHabitDetailApi({
      taskId: editForm.taskId || undefined,
      name,
      description: editForm.description.trim() || undefined,
      icon: editForm.icon.trim() || 'i-lucide-sparkles',
      color: editForm.color,
      frequencyType: editForm.frequencyType,
      frequencyText: editForm.frequencyText.trim() || getFrequencyLabel(editForm.frequencyType),
      startDate: editForm.startDate || undefined,
      reminderTime: editForm.reminderTime || undefined,
      generateToTodo: editForm.generateToTodo,
      status: editForm.status,
      sort: habits.value.length + 1
    })

    await fetchHabits()
    detailOpen.value = false

    systemToast.success('保存成功', '习惯已新增', 'habit-create-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'habit-create-error')
  } finally {
    creatingHabit.value = false
  }
}

async function toggleGenerateToTodo(habit: HabitItem, value: boolean) {
  if (togglingGenerateIds.value.includes(habit.id)) return

  const previousValue = habit.generateToTodo
  habit.generateToTodo = value

  if (activeHabit.value?.id === habit.id) {
    editForm.generateToTodo = value
  }

  togglingGenerateIds.value = [...togglingGenerateIds.value, habit.id]

  try {
    await toggleHabitGenerateApi({
      id: habit.id,
      generateToTodo: value
    })

    systemToast.success(
      '更新成功',
      value ? '已开启生成到清单' : '已关闭生成到清单',
      `habit-toggle-generate-${habit.id}`
    )
  } catch (error: any) {
    habit.generateToTodo = previousValue

    if (activeHabit.value?.id === habit.id) {
      editForm.generateToTodo = previousValue
    }

    systemToast.error(
      '更新失败',
      error?.message || '请稍后重试',
      `habit-toggle-generate-error-${habit.id}`
    )
  } finally {
    togglingGenerateIds.value = togglingGenerateIds.value.filter(id => id !== habit.id)
  }
}

async function toggleStatus(habit: HabitItem, value: boolean) {
  if (togglingStatusIds.value.includes(habit.id)) return

  const previousStatus = habit.status
  const nextStatus: HabitStatus = value ? 'ENABLED' : 'DISABLED'

  habit.status = nextStatus

  if (activeHabit.value?.id === habit.id) {
    editForm.status = nextStatus
  }

  togglingStatusIds.value = [...togglingStatusIds.value, habit.id]

  try {
    if (value) {
      await enableHabitApi(habit.id)
    } else {
      await disableHabitApi(habit.id)
    }

    systemToast.success(
      '更新成功',
      value ? '习惯已启用' : '习惯已停用',
      `habit-toggle-status-${habit.id}`
    )
  } catch (error: any) {
    habit.status = previousStatus

    if (activeHabit.value?.id === habit.id) {
      editForm.status = previousStatus
    }

    systemToast.error(
      '更新失败',
      error?.message || '请稍后重试',
      `habit-toggle-status-error-${habit.id}`
    )
  } finally {
    togglingStatusIds.value = togglingStatusIds.value.filter(id => id !== habit.id)
  }
}

function getStatusLabel(status: HabitStatus) {
  return status === 'ENABLED' ? '启用中' : '已停用'
}

function getTimelineIcon(status: HabitTimelineStatus) {
  if (status === 'DONE') return 'i-lucide-check'
  if (status === 'SKIPPED') return 'i-lucide-forward'
  return 'i-lucide-x'
}

function getTimelineColor(status: HabitTimelineStatus) {
  if (status === 'DONE') return 'primary'
  if (status === 'SKIPPED') return 'warning'
  return 'error'
}

function getFrequencyLabel(type: HabitFrequencyType) {
  if (type === 'DAILY') return '每天'
  if (type === 'WEEKLY') return '每周'
  return '每月'
}

function getColorLabel(color: string) {
  const found = colorOptions.find(item => item.value === color)
  return found?.label || color || '--'
}
function getHabitColorClass(color: string) {
  if (color === 'green') return 'habit-color--green'
  if (color === 'blue') return 'habit-color--blue'
  if (color === 'orange') return 'habit-color--orange'
  if (color === 'purple') return 'habit-color--purple'
  return 'habit-color--green'
}

watch(detailOpen, (open) => {
  if (!open || drawerMode.value !== 'detail') return

  requestAnimationFrame(() => {
    const el = habitHistoryScrollRef.value
    if (!el) return

    el.scrollTo({
      left: el.scrollWidth,
      behavior: 'smooth'
    })
  })
})
watch(
  () => ({
    keyword: debouncedKeyword.value,
    status: filters.status,
    frequencyType: filters.frequencyType,
    generateToTodo: filters.generateToTodo
  }),
  () => {
    fetchHabits()
  },
  { deep: true }
)
watch(
  () => filters.keyword,
  value => {
    if (habitKeywordTimer) {
      clearTimeout(habitKeywordTimer)
    }

    habitKeywordTimer = setTimeout(() => {
      debouncedKeyword.value = value
    }, 300)
  },
  { immediate: true }
)
onMounted(() => {
  fetchHabits()
})
onBeforeUnmount(() => {
  if (habitKeywordTimer) {
    clearTimeout(habitKeywordTimer)
    habitKeywordTimer = null
  }
})
</script>

<template>
  <div class="mobe-page-shell">
    <div class="mobe-page-header">
      <div class="mobe-page-title-wrap">
        <div class="mobe-page-kicker">页面</div>

        <div class="mobe-page-title-row">
          <h1 class="mobe-page-title">习惯</h1>
          <p class="mobe-page-subtitle">管理长期规则，沉淀执行记录，并控制是否生成到清单。</p>
        </div>
      </div>

      <div class="mobe-toolbar habit-toolbar">
        <UInput v-model="filters.keyword" icon="i-lucide-search" placeholder="搜索习惯名称或描述" class="mobe-toolbar-search" />

        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw" :loading="loading"
          @click="fetchHabits(true)">
          刷新
        </UButton>

        <USelect v-model="filters.status" :items="statusOptions" value-key="value" label-key="label"
          class="mobe-toolbar-select" />

        <USelect v-model="filters.frequencyType" :items="frequencyOptions" value-key="value" label-key="label"
          class="mobe-toolbar-select" />

        <USelect v-model="filters.generateToTodo" :items="generateOptions" value-key="value" label-key="label"
          class="mobe-toolbar-select" />

        <UButton icon="i-lucide-plus" @click="openCreateDrawer">
          新增习惯
        </UButton>
      </div>
    </div>

    <div v-if="loading" class="habit-list-state">
      正在加载习惯...
    </div>

    <div v-else-if="!filteredHabits.length" class="habit-list-state">
      暂无习惯，去新增一个吧
    </div>

    <div v-else class="habit-card-grid">
      <div v-for="habit in filteredHabits" :key="habit.id" class="habit-card" :class="{
        'habit-card--generate-on': habit.generateToTodo,
        'habit-card--disabled': habit.status === 'DISABLED'
      }" role="button" tabindex="0" @click="openDetail(habit)" @keydown.enter="openDetail(habit)"
        @keydown.space.prevent="openDetail(habit)">
        <div class="habit-card__top">
          <div class="habit-card__identity">
            <div class="habit-card__icon-wrap" :class="getHabitColorClass(habit.color)">
              <UIcon :name="habit.icon" class="habit-card__icon" />
            </div>

            <div class="habit-card__title-wrap">
              <div class="habit-card__title-row">
                <h3 class="habit-card__title">{{ habit.name }}</h3>

                <UTooltip :text="`已持续打卡 ${habit.streakCount} 天`">
                  <span class="habit-card__streak">{{ habit.streakCount }}</span>
                </UTooltip>
              </div>

              <p class="habit-card__desc">{{ habit.description || '暂无描述' }}</p>
            </div>
          </div>

          <div class="habit-card__badges">
            <UBadge v-if="habit.taskName" color="neutral" variant="soft">
              {{ habit.taskName }}
            </UBadge>

            <!-- <UTooltip v-if="habit.checklistTitle" :text="habit.checklistTitle">
              <UBadge color="neutral" variant="soft">
                关联清单
              </UBadge>
            </UTooltip> -->

            <UBadge :color="habit.status === 'ENABLED' ? 'primary' : 'neutral'" variant="soft">
              {{ getStatusLabel(habit.status) }}
            </UBadge>
          </div>
        </div>

        <div class="habit-card__middle">
          <div class="habit-card__info-panel">
            <div class="habit-card__field">
              <span class="habit-card__field-label">频率</span>
              <span class="habit-card__field-value">{{ habit.frequencyText }}</span>
            </div>

            <div class="habit-card__field">
              <span class="habit-card__field-label">最近打卡</span>
              <span class="habit-card__field-value habit-card__field-value--single-line">
                {{ habit.lastCheckInAt || '--' }}
              </span>
            </div>

            <div class="habit-card__field">
              <span class="habit-card__field-label">累计打卡</span>
              <span class="habit-card__field-value">{{ habit.totalCheckInCount }}</span>
            </div>
          </div>
        </div>

        <div class="habit-card__bottom" @click.stop>
          <div class="habit-card__switch-row">
            <div class="habit-card__switch-item">
              <span class="habit-card__switch-label">生成到清单</span>
              <USwitch :model-value="habit.generateToTodo" :loading="togglingGenerateIds.includes(habit.id)"
                :disabled="togglingGenerateIds.includes(habit.id)"
                @update:model-value="toggleGenerateToTodo(habit, $event)" />
            </div>

            <div class="habit-card__switch-item">
              <span class="habit-card__switch-label">启用</span>
              <USwitch :model-value="habit.status === 'ENABLED'" :loading="togglingStatusIds.includes(habit.id)"
                :disabled="togglingStatusIds.includes(habit.id)" @update:model-value="toggleStatus(habit, $event)" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <USlideover v-model:open="detailOpen" :ui="{ content: 'sm:max-w-2xl' }">
      <template #content>
        <div v-if="drawerMode === 'create'" class="habit-create-drawer">
          <div class="habit-create-drawer__header">
            <div class="habit-create-drawer__title-wrap">
              <div class="habit-create-drawer__eyebrow">Habits</div>
              <h2 class="habit-create-drawer__title">新增习惯</h2>
              <p class="habit-create-drawer__subtitle">先创建一个长期规则，再决定是否生成到清单。</p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeDrawer" />
          </div>

          <div class="habit-create-drawer__body">
            <div class="habit-create-form">
              <div class="habit-create-form__item">
                <label class="habit-create-form__label">所属任务</label>
                <USelect v-model="editForm.taskId"
                  :items="taskOptions.map(item => ({ label: item.name, value: item.id }))" value-key="value"
                  label-key="label" :loading="taskLoading" placeholder="可选，不选择则不绑定任务" />
              </div>
              <div class="habit-create-form__item">
                <label class="habit-create-form__label">习惯名称</label>
                <UInput v-model="editForm.name" placeholder="例如：晨间饮水" />
              </div>

              <div class="habit-create-form__item">
                <label class="habit-create-form__label">描述</label>
                <UTextarea v-model="editForm.description" :rows="4" placeholder="补充这个习惯的说明（可选）" />
              </div>

              <div class="habit-create-form__grid">
                <div class="habit-create-form__item habit-create-form__item--full">
                  <label class="habit-create-form__label">图标</label>
                  <div class="habit-create-icon-field">
                    <div class="habit-create-icon-field__preview" :class="getHabitColorClass(editForm.color)">
                      <UIcon :name="editForm.icon" class="habit-create-icon-field__icon" />
                    </div>

                    <USelect v-model="editForm.icon" :items="iconOptions" value-key="value" label-key="label"
                      class="habit-create-icon-field__select" />
                  </div>
                </div>

                <div class="habit-create-form__item">
                  <label class="habit-create-form__label">颜色</label>
                  <USelect v-model="editForm.color" :items="colorOptions" value-key="value" label-key="label" />
                </div>
              </div>

              <div class="habit-create-form__grid">
                <div class="habit-create-form__item">
                  <label class="habit-create-form__label">频率类型</label>
                  <USelect v-model="editForm.frequencyType" :items="frequencyTypeEditOptions" value-key="value"
                    label-key="label" />
                </div>

                <div class="habit-create-form__item">
                  <label class="habit-create-form__label">频率明细</label>
                  <UInput v-model="editForm.frequencyText" placeholder="例如：每天 / 每周一三五 / 每月 1 号" />
                </div>
              </div>

              <div class="habit-create-form__grid">
                <div class="habit-create-form__item">
                  <label class="habit-create-form__label">开始日期</label>
                  <UInputDate ref="habitStartDateInput" v-model="habitStartDateValue" class="w-full" :format-options="{
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit'
                  }" locale="en-CA">
                    <template #trailing>
                      <UPopover :reference="habitStartDateInput?.inputsRef?.[3]?.$el">
                        <UButton color="neutral" variant="ghost" icon="i-lucide-calendar" />
                        <template #content>
                          <UCalendar v-model="habitStartDateValue" class="p-2" />
                        </template>
                      </UPopover>
                    </template>
                  </UInputDate>
                </div>

                <div class="habit-create-form__item">
                  <label class="habit-create-form__label">提醒时间</label>
                  <UInputTime ref="habitReminderTimeInput" v-model="habitReminderTimeValue" :hour-cycle="24"
                    class="w-full" />
                </div>
              </div>

              <div class="habit-create-form__switch">
                <div class="habit-create-form__switch-text">
                  <span class="habit-create-form__switch-label">生成到清单</span>
                  <span class="habit-create-form__switch-desc">开启后，习惯可作为清单来源</span>
                </div>
                <USwitch v-model="editForm.generateToTodo" />
              </div>

              <div class="habit-create-form__switch">
                <div class="habit-create-form__switch-text">
                  <span class="habit-create-form__switch-label">启用状态</span>
                  <span class="habit-create-form__switch-desc">关闭后，该习惯不会继续生效</span>
                </div>
                <USwitch :model-value="editForm.status === 'ENABLED'"
                  @update:model-value="editForm.status = $event ? 'ENABLED' : 'DISABLED'" />
              </div>
            </div>
          </div>

          <div class="habit-create-drawer__footer">
            <UButton color="neutral" variant="soft" @click="closeDrawer">
              取消
            </UButton>
            <UButton icon="i-lucide-save" :loading="creatingHabit" @click="createHabit">
              保存
            </UButton>
          </div>
        </div>

        <div v-else-if="activeHabit" class="habit-detail-drawer">
          <div class="habit-detail__header">
            <div class="habit-detail__title-wrap">
              <div class="habit-detail__title-row">
                <h2 class="habit-detail__title">{{ activeHabit.name }}</h2>

                <div class="habit-detail__title-badges">
                  <UBadge v-if="activeHabit.taskName" color="neutral" variant="soft">
                    {{ activeHabit.taskName }}
                  </UBadge>
<!-- 
                  <UTooltip v-if="activeHabit.checklistTitle" :text="activeHabit.checklistTitle">
                    <UBadge color="neutral" variant="soft">
                      关联清单
                    </UBadge>
                  </UTooltip> -->

                  <UBadge :color="activeHabit.status === 'ENABLED' ? 'primary' : 'neutral'" variant="soft">
                    {{ getStatusLabel(activeHabit.status) }}
                  </UBadge>
                </div>
              </div>
              <p class="habit-detail__subtitle">查看完整规则配置与历史打卡记录。</p>
            </div>

            <div class="habit-detail__actions">
              <template v-if="detailMode === 'view'">
                <UButton color="error" variant="soft" icon="i-lucide-trash-2" :loading="deletingHabit"
                  :disabled="deletingHabit" @click="removeHabit">
                  删除
                </UButton>

                <UButton color="neutral" variant="soft" icon="i-lucide-pencil" @click="startEdit">
                  编辑
                </UButton>
              </template>

              <template v-else>
                <UButton color="neutral" variant="soft" @click="cancelEdit">
                  取消
                </UButton>
                <UButton icon="i-lucide-check" :loading="updatingHabit" @click="saveHabit">
                  保存
                </UButton>
              </template>
            </div>
          </div>

          <template v-if="detailMode === 'edit'">
            <div class="habit-detail__section-grid">
              <section class="habit-detail__section">
                <div class="habit-detail__section-head">
                  <h3 class="habit-detail__section-title">基本信息</h3>
                </div>

                <div class="habit-form-grid">
                  <div class="habit-form-item">
                    <label class="habit-form-label">名称</label>
                    <UInput v-model="editForm.name" />
                  </div>
                  <div class="habit-form-item">
                    <label class="habit-form-label">所属任务</label>
                    <USelect v-model="editForm.taskId"
                      :items="taskOptions.map(item => ({ label: item.name, value: item.id }))" value-key="value"
                      label-key="label" :loading="taskLoading" placeholder="可选，不选择则不绑定任务" />
                  </div>

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">图标</label>
                    <div class="habit-edit-icon-field">
                      <div class="habit-edit-icon-field__preview" :class="getHabitColorClass(editForm.color)">
                        <UIcon :name="editForm.icon" class="habit-edit-icon-field__icon" />
                      </div>

                      <USelect v-model="editForm.icon" :items="iconOptions" value-key="value" label-key="label"
                        class="habit-edit-icon-field__select" />
                    </div>
                  </div>

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">描述</label>
                    <UTextarea v-model="editForm.description" :rows="3" />
                  </div>

                  <div class="habit-form-item">
                    <label class="habit-form-label">颜色</label>
                    <USelect v-model="editForm.color" :items="colorOptions" value-key="value" label-key="label" />
                  </div>

                  <div class="habit-form-item">
                    <label class="habit-form-label">排序</label>
                    <div class="habit-readonly-box">{{ activeHabit.sort }}</div>
                  </div>
                </div>
              </section>

              <section class="habit-detail__section">
                <div class="habit-detail__section-head">
                  <h3 class="habit-detail__section-title">规则信息</h3>
                </div>

                <div class="habit-form-grid">
                  <div class="habit-form-item">
                    <label class="habit-form-label">频率类型</label>
                    <USelect v-model="editForm.frequencyType" :items="frequencyTypeEditOptions" value-key="value"
                      label-key="label" />
                  </div>

                  <div class="habit-form-item">
                    <label class="habit-form-label">频率明细</label>
                    <UInput v-model="editForm.frequencyText" />
                  </div>

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">开始日期</label>
                    <UInputDate ref="habitStartDateInput" v-model="habitStartDateValue" class="w-full" :format-options="{
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit'
                    }" :ui="{ base: 'pr-32' }" locale="en-CA">
                      <template #trailing>
                        <UPopover :reference="habitStartDateInput?.inputsRef?.[3]?.$el">
                          <UButton color="neutral" variant="ghost" icon="i-lucide-calendar" />
                          <template #content>
                            <UCalendar v-model="habitStartDateValue" class="p-2" />
                          </template>
                        </UPopover>
                      </template>
                    </UInputDate>
                  </div>

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">提醒时间</label>
                    <UInputTime ref="habitReminderTimeInput" v-model="habitReminderTimeValue" :hour-cycle="24"
                      class="w-full" />
                  </div>


                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">生成到清单</label>
                    <div class="habit-inline-switch">
                      <span class="habit-inline-switch__text">{{ editForm.generateToTodo ? '已开启' : '未开启' }}</span>
                      <USwitch v-model="editForm.generateToTodo" />
                    </div>
                  </div>
                  <!-- <div class="habit-info-item">
                    <span class="habit-info-item__label">关联清单</span>
                    <span class="habit-info-item__value">{{ activeHabit.checklistTitle || '--' }}</span>
                  </div> -->

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">启用状态</label>
                    <div class="habit-inline-switch">
                      <span class="habit-inline-switch__text">{{ editForm.status === 'ENABLED' ? '启用中' : '已停用' }}</span>
                      <USwitch :model-value="editForm.status === 'ENABLED'"
                        @update:model-value="editForm.status = $event ? 'ENABLED' : 'DISABLED'" />
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </template>

          <template v-else>
            <div class="habit-detail__section-grid">
              <section class="habit-detail__section">
                <div class="habit-detail__section-head">
                  <h3 class="habit-detail__section-title">基本信息</h3>
                </div>

                <div class="habit-info-list">
                  <div class="habit-info-item">
                    <span class="habit-info-item__label">名称</span>
                    <span class="habit-info-item__value">{{ activeHabit.name || '--' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">所属任务</span>
                    <span class="habit-info-item__value habit-info-item__value--single-line">
                      {{ activeHabit.taskName || '--' }}
                    </span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">图标</span>
                    <div class="habit-info-icon">
                      <div class="habit-info-icon__preview" :class="getHabitColorClass(activeHabit.color)">
                        <UIcon :name="activeHabit.icon" class="habit-info-icon__icon" />
                      </div>
                      <span class="habit-info-icon__label">{{ getIconLabel(activeHabit.icon) }}</span>
                    </div>
                  </div>

                  <div class="habit-info-item habit-info-item--top">
                    <span class="habit-info-item__label">描述</span>
                    <span class="habit-info-item__value">{{ activeHabit.description || '--' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">颜色</span>
                    <span class="habit-info-item__value">{{ getColorLabel(activeHabit.color) }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">排序</span>
                    <span class="habit-info-item__value">{{ activeHabit.sort }}</span>
                  </div>
                </div>
              </section>

              <section class="habit-detail__section">
                <div class="habit-detail__section-head">
                  <h3 class="habit-detail__section-title">规则信息</h3>
                </div>

                <div class="habit-info-list">
                  <div class="habit-info-item">
                    <span class="habit-info-item__label">频率类型</span>
                    <span class="habit-info-item__value">{{ getFrequencyLabel(activeHabit.frequencyType) }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">频率明细</span>
                    <span class="habit-info-item__value">{{ activeHabit.frequencyText || '--' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">开始日期</span>
                    <span class="habit-info-item__value">{{ activeHabit.startDate || '--' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">提醒时间</span>
                    <span class="habit-info-item__value">{{ activeHabit.reminderTime || '--' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">生成到清单</span>
                    <span class="habit-info-item__value">{{ activeHabit.generateToTodo ? '已开启' : '未开启' }}</span>
                  </div>

                  <div class="habit-info-item">
                    <span class="habit-info-item__label">状态</span>
                    <span class="habit-info-item__value">{{ getStatusLabel(activeHabit.status) }}</span>
                  </div>
                </div>
              </section>
            </div>
          </template>

          <section class="habit-detail__section">
            <div class="habit-detail__stats-inline">
              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">累计打卡次数</span>
                <strong class="habit-detail__stats-inline-value">{{ habitStats.totalCheckInCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">当前连续打卡</span>
                <strong class="habit-detail__stats-inline-value">{{ habitStats.streakCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">最长连续打卡</span>
                <strong class="habit-detail__stats-inline-value">{{ habitStats.longestStreakCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">最近一次打卡</span>
                <strong class="habit-detail__stats-inline-value habit-detail__stats-inline-value--time">
                   {{ habitStats.lastCheckInAt || '--' }}
                </strong>
              </div>
            </div>
          </section>

          <section class="habit-detail__section habit-detail__section--timeline">
            <div class="habit-detail__section-head">
              <h3 class="habit-detail__section-title">历史打卡记录</h3>
            </div>

            <div v-if="habitDetailLoading" class="habit-history-empty">
  正在加载记录...
</div>

            <div v-else-if="!timelineItems.length" class="habit-history-empty">
              暂无历史记录
            </div>

            <div v-else ref="habitHistoryScrollRef" class="habit-history-scroll">
              <div class="habit-history-track">
                <div v-for="(item, index) in timelineItems" :key="item.id" class="habit-history-node"
                  :class="index % 2 === 0 ? 'habit-history-node--top' : 'habit-history-node--bottom'">
                  <div class="habit-history-node__content">
                    <template v-if="index % 2 === 0">
                      <div class="habit-history-node__body">
                        <div class="habit-history-node__date">{{ item.date }}</div>
                        <div v-if="item.note" class="habit-history-node__meta">备注：{{ item.note }}</div>
                      </div>
                    </template>
                  </div>

                  <div class="habit-history-node__axis">
                    <div class="habit-timeline-indicator"
                      :class="`habit-timeline-indicator--${getTimelineColor(item.status)}`">
                      <UIcon :name="item.icon" class="habit-timeline-indicator__icon" />
                    </div>
                  </div>

                  <div class="habit-history-node__content">
                    <template v-if="index % 2 !== 0">
                      <div class="habit-history-node__body">
                        <div class="habit-history-node__date">{{ item.date }}</div>
                        <div v-if="item.note" class="habit-history-node__meta">备注：{{ item.note }}</div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </template>
    </USlideover>
  </div>
</template>
<style scoped>
.mobe-page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mobe-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--mobe-space-4);
  flex-wrap: wrap;
}

.mobe-page-title-wrap {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: var(--mobe-space-1);
}

.mobe-page-kicker {
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
}

.mobe-page-title-row {
  display: flex;
  align-items: flex-end;
  gap: var(--mobe-space-3);
  flex-wrap: wrap;
}

.mobe-page-title {
  margin: 0;
  font-size: var(--mobe-font-2xl);
  line-height: 1.2;
  font-weight: 700;
  color: var(--mobe-text);
}

.mobe-page-subtitle {
  margin: 0 0 2px;
  font-size: var(--mobe-font-md);
  color: var(--mobe-text-soft);
}

.mobe-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--mobe-space-3);
  flex-shrink: 0;
  flex-wrap: wrap;
}

.mobe-toolbar-search {
  width: 260px;
}

.mobe-toolbar-select {
  width: 140px;
}

.habit-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--mobe-space-5);
  padding-top: var(--mobe-space-6);
  border-top: 1px solid var(--mobe-divider);
}

.habit-card {
  width: 100%;
  padding: var(--mobe-space-5);
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-5);
  text-align: left;
  cursor: pointer;
  border-radius: var(--mobe-radius-lg);
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-elevated);
  box-shadow: var(--mobe-shadow-xs);
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.habit-card:hover {
  transform: translateY(-2px);
  border-color: var(--mobe-border);
  box-shadow: var(--mobe-shadow-sm);
}

.habit-card--generate-on {
  border-color: var(--mobe-primary);
}

.habit-card--disabled {
  opacity: 0.78;
}

.habit-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--mobe-space-4);
  padding-bottom: var(--mobe-space-4);
  border-bottom: 1px solid var(--mobe-divider);
}

.habit-card__identity {
  display: flex;
  gap: var(--mobe-space-3);
  min-width: 0;
  flex: 1;
}

.habit-card__icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: var(--mobe-radius-lg);
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.habit-card__icon {
  font-size: 18px;
  color: var(--mobe-text);
}


.habit-card__title-wrap {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.habit-card__title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.habit-card__title {
  margin: 0;
  font-size: 18px;
  line-height: 1.25;
  font-weight: 700;
  color: var(--mobe-text);
}

.habit-card__streak {
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: var(--mobe-primary-soft);
  color: var(--mobe-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--mobe-font-xs);
  font-weight: 700;
  cursor: help;
}

.habit-card__desc {
  margin: 0;
  color: var(--mobe-text-soft);
  font-size: var(--mobe-font-sm);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.habit-card__middle {
  min-width: 0;
}

.habit-card__info-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--mobe-space-3);
  padding: var(--mobe-space-4);
  border-radius: var(--mobe-radius-lg);
  background: var(--mobe-surface-soft);
  border: 1px solid var(--mobe-border-soft);
}

.habit-card__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.habit-card__field-label,
.habit-card__switch-label,
.habit-form-label,
.habit-create-form__label {
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
}

.habit-card__field-value {
  font-size: var(--mobe-font-md);
  font-weight: 600;
  color: var(--mobe-text);
  line-height: 1.4;
}

.habit-card__field-value--single-line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.habit-card__bottom {
  padding-top: 2px;
}

.habit-card__switch-row {
  display: flex;
  justify-content: space-between;
  gap: var(--mobe-space-3);
  flex-wrap: wrap;
}

.habit-card__switch-item {
  display: inline-flex;
  align-items: center;
  gap: var(--mobe-space-2);
}

.habit-create-drawer,
.habit-detail-drawer {
  height: 100%;
  padding: var(--mobe-space-6);
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.habit-create-drawer {
  gap: var(--mobe-space-5);
}

.habit-detail-drawer {
  gap: var(--mobe-space-5);
}

.habit-create-drawer__header,
.habit-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--mobe-space-4);
  padding-bottom: var(--mobe-space-4);
  border-bottom: 1px solid var(--mobe-divider);
}

.habit-create-drawer__title-wrap,
.habit-detail__title-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.habit-create-drawer__eyebrow {
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
}

.habit-create-drawer__title,
.habit-detail__title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--mobe-text);
}

.habit-create-drawer__subtitle,
.habit-detail__subtitle {
  margin: 0;
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
}

.habit-detail__title-row {
  display: flex;
  align-items: center;
  gap: var(--mobe-space-2);
  flex-wrap: wrap;
}

.habit-detail__actions {
  display: flex;
  gap: var(--mobe-space-2);
}

.habit-create-drawer__body {
  flex: 1;
  min-height: 0;
}

.habit-create-form {
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-4);
}

.habit-create-form__item {
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-2);
}

.habit-create-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--mobe-space-4);
}

.habit-create-form__switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mobe-space-4);
  padding: var(--mobe-space-4);
  border: 1px solid var(--mobe-border-soft);
  border-radius: var(--mobe-radius-md);
  background: var(--mobe-surface-soft);
}

.habit-create-form__switch-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.habit-create-form__switch-label {
  font-size: var(--mobe-font-md);
  font-weight: 600;
  color: var(--mobe-text);
}

.habit-create-form__switch-desc {
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
}

.habit-create-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--mobe-space-3);
  padding-top: var(--mobe-space-4);
  border-top: 1px solid var(--mobe-divider);
}

.habit-detail__section-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--mobe-space-4);
}

.habit-detail__section {
  padding: var(--mobe-space-5);
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-4);
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-elevated);
  border-radius: var(--mobe-radius-lg);
  box-shadow: var(--mobe-shadow-xs);
}

.source-label {
  display: flex;
  align-items: center;
  gap: 4px;
}

.habit-detail__section--timeline {
  padding-bottom: var(--mobe-space-3);
}

.habit-detail__section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mobe-space-3);
}

.habit-detail__section-title {
  margin: 0;
  font-size: var(--mobe-font-lg);
  font-weight: 700;
  color: var(--mobe-text);
}

.habit-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--mobe-space-4);
}

.habit-form-item {
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-2);
  min-width: 0;
}

.habit-form-item--full {
  grid-column: 1 / -1;
}

.habit-readonly-box {
  min-height: 0;
  height: auto;
  width: 100%;
  padding: 0 12px;
  border-radius: var(--mobe-radius-md);
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-soft);
  color: var(--mobe-text);
  font-size: var(--mobe-font-md);
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.habit-form-item .habit-readonly-box {
  height: 100%;
}

.habit-inline-switch {
  min-height: 40px;
  padding: 10px 12px;
  border-radius: var(--mobe-radius-md);
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mobe-space-3);
}

.habit-inline-switch__text {
  font-size: var(--mobe-font-md);
  color: var(--mobe-text);
}

.habit-info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  border-top: 1px solid var(--mobe-divider);
}

.habit-info-item {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: var(--mobe-space-4);
  align-items: center;
  padding: var(--mobe-space-3) 0;
  border-bottom: 1px solid var(--mobe-divider);
}

.habit-info-item--top {
  align-items: flex-start;
}

.habit-info-item__label {
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-mute);
}

.habit-info-item__value {
  font-size: var(--mobe-font-md);
  color: var(--mobe-text);
  line-height: 1.6;
  word-break: break-word;
  display: flex;
  align-items: center;
  min-height: 32px;
}

.habit-detail__stats-inline {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--mobe-space-3);
  padding-top: var(--mobe-space-2);
}

.habit-detail__stats-inline-item {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.habit-detail__stats-inline-label {
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
}

.habit-detail__stats-inline-value {
  font-size: var(--mobe-font-lg);
  font-weight: 700;
  color: var(--mobe-text);
  line-height: 1.3;
}

.habit-detail__stats-inline-value--time {
  white-space: nowrap;
  overflow: visible;
  text-overflow: unset;
}

.habit-timeline-indicator {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.habit-timeline-indicator--primary {
  background: var(--mobe-primary);
}

.habit-timeline-indicator--warning {
  background: var(--mobe-warning);
}

.habit-timeline-indicator--error {
  background: var(--mobe-danger);
}

.habit-timeline-indicator__icon {
  font-size: 14px;
}

.habit-history-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  overscroll-behavior-x: contain;
  padding: var(--mobe-space-4) 0;
  scrollbar-width: thin;
}

.habit-history-track {
  display: flex;
  align-items: stretch;
  gap: 0;
  min-width: max-content;
  padding: 0 var(--mobe-space-2);
}

.habit-history-node {
  width: 156px;
  display: grid;
  grid-template-rows: 1fr 32px 1fr;
  flex: 0 0 156px;
}

.habit-history-node__content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--mobe-space-2);
  min-height: 72px;
}

.habit-history-node__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-align: center;
  max-width: 132px;
}

.habit-card__badges {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.habit-detail__title-badges {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.habit-history-node__date {
  font-size: var(--mobe-font-sm);
  font-weight: 600;
  color: var(--mobe-text);
  line-height: 1.25;
  white-space: nowrap;
}

.habit-history-node__axis {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.habit-history-node__axis::before,
.habit-history-node__axis::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 18px);
  height: 1px;
  background: var(--mobe-divider);
}

.habit-history-node__axis::before {
  left: 0;
}

.habit-history-node__axis::after {
  right: 0;
}

.habit-history-node:first-child .habit-history-node__axis::before {
  display: none;
}

.habit-history-node:last-child .habit-history-node__axis::after {
  display: none;
}

.habit-history-node__title {
  font-size: var(--mobe-font-md);
  font-weight: 700;
  color: var(--mobe-text);
  line-height: 1.4;
}

.habit-history-node__meta {
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-soft);
  line-height: 1.3;
  max-width: 132px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.habit-history-node--top .habit-history-node__body {
  transform: translateY(6px);
}

.habit-history-node--bottom .habit-history-node__body {
  transform: translateY(-6px);
}

.habit-timeline-indicator {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
  z-index: 1;
}

.habit-timeline-indicator--primary {
  background: var(--mobe-primary);
}

.habit-timeline-indicator--warning {
  background: var(--mobe-warning);
}

.habit-timeline-indicator--error {
  background: var(--mobe-danger);
}

.habit-timeline-indicator__icon {
  font-size: 14px;
}

.habit-history-timeline {
  padding: 12px 8px 28px;
}

.habit-history-item--top {
  transform: translateY(-18px);
}

.habit-history-item--bottom {
  transform: translateY(18px);
}

.habit-timeline-title-row.habit-history-item--top,
.habit-timeline-description.habit-history-item--top,
.habit-history-item--top {
  text-align: center;
}

.habit-timeline-title-row.habit-history-item--bottom,
.habit-timeline-description.habit-history-item--bottom,
.habit-history-item--bottom {
  text-align: center;
}

.habit-timeline-title-row {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.habit-timeline-title {
  font-size: var(--mobe-font-md);
  font-weight: 600;
  color: var(--mobe-text);
}

.habit-timeline-description {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--mobe-text-soft);
  line-height: 1.5;
}

.habit-timeline-indicator {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto;
}

.habit-timeline-indicator--primary {
  background: var(--mobe-primary);
}

.habit-timeline-indicator--warning {
  background: var(--mobe-warning);
}

.habit-timeline-indicator--error {
  background: var(--mobe-danger);
}

.habit-timeline-indicator__icon {
  font-size: 14px;
}

.habit-color--green {
  background: color-mix(in srgb, var(--mobe-primary) 12%, white);
  color: var(--mobe-primary);
  border-color: color-mix(in srgb, var(--mobe-primary) 24%, transparent);
}

.habit-color--blue {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  border-color: rgba(59, 130, 246, 0.22);
}

.habit-color--orange {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
  border-color: rgba(245, 158, 11, 0.22);
}

.habit-color--purple {
  background: rgba(139, 92, 246, 0.12);
  color: #7c3aed;
  border-color: rgba(139, 92, 246, 0.22);
}

.habit-edit-icon-field,
.habit-create-icon-field {
  display: flex;
  align-items: stretch;
  gap: var(--mobe-space-3);
  width: 100%;
}

.habit-edit-icon-field__preview,
.habit-create-icon-field__preview {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  border-radius: var(--mobe-radius-md);
  border: 1px solid var(--mobe-border-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.habit-edit-icon-field__icon,
.habit-create-icon-field__icon {
  font-size: 18px;
}

.habit-edit-icon-field__select,
.habit-create-icon-field__select {
  flex: 1;
  min-width: 0;
}

.habit-create-form__item--full {
  grid-column: 1 / -1;
}

.habit-info-icon {
  display: inline-flex;
  align-items: center;
  gap: var(--mobe-space-3);
  min-height: 40px;
}

.habit-info-icon,
.habit-create-form__icon-preview,
.habit-edit-icon-preview {
  display: flex;
  align-items: center;
}

.habit-create-form__icon-box,
.habit-edit-icon-preview__box,
.habit-info-icon__preview {
  width: 33px;
  height: 33px;
  flex: 0 0 33px;
  border-radius: var(--mobe-radius-md);
  border: 1px solid var(--mobe-border-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.habit-info-icon__icon,
.habit-create-form__icon,
.habit-edit-icon-preview__icon {
  font-size: 18px;
}

.habit-create-form__icon-picker,
.habit-edit-icon-picker,
.habit-info-icon {
  display: flex;
  align-items: stretch;
  gap: var(--mobe-space-3);
  min-height: 40px;
}

.habit-create-form__icon-select,
.habit-edit-icon-picker__select {
  flex: 1;
  min-width: 0;
}

.habit-info-icon__label {
  height: 32px;
  display: inline-flex;
  align-items: center;
  font-size: var(--mobe-font-md);
  color: var(--mobe-text);
  line-height: 1;
}

.habit-list-state {
  padding: 36px 12px;
  text-align: center;
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
  border-top: 1px solid var(--mobe-divider);
}

@media (max-width: 1279px) {

  .habit-card-grid,
  .habit-detail__section-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .mobe-page-shell {
    gap: 16px;
  }

  .habit-card__badges,
  .habit-detail__title-badges {
    justify-content: flex-start;
  }

  .mobe-page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
  }

  .mobe-page-title-wrap {
    width: 100%;
  }

  .mobe-page-title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .mobe-page-title {
    font-size: 28px;
    line-height: 1.15;
  }

  .mobe-page-subtitle {
    font-size: 14px;
    line-height: 1.6;
    margin: 0;
    white-space: normal;
  }

  .mobe-toolbar {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    align-items: stretch;
    justify-content: initial;
  }

  .mobe-toolbar-search {
    width: 100%;
    min-width: 0;
    grid-column: 1 / -1;
  }

  .mobe-toolbar-select {
    width: 100%;
    min-width: 0;
  }

  .mobe-toolbar>.mobe-toolbar-select {
    grid-column: span 2;
  }

  .mobe-toolbar> :last-child {
    grid-column: span 2;
  }

  .habit-card-grid,
  .habit-detail__section-grid,
  .habit-form-grid,
  .habit-card__info-panel,
  .habit-create-form__grid,
  .habit-detail__stats-inline {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .habit-card {
    padding: 16px;
    gap: 16px;
  }

  .habit-card__top,
  .habit-card__switch-row,
  .habit-detail__header,
  .habit-create-drawer__header {
    flex-direction: column;
    align-items: stretch;
  }

  .habit-card__identity {
    align-items: flex-start;
  }

  .habit-card__title-row {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .habit-card__title {
    font-size: 18px;
    line-height: 1.35;
    word-break: break-word;
  }

  .habit-card__desc {
    line-height: 1.6;
  }

  .habit-card__info-panel {
    padding: 14px;
    gap: 12px;
  }

  .habit-card__field-value--single-line {
    white-space: normal;
    overflow: visible;
    text-overflow: unset;
    word-break: break-word;
  }

  .habit-card__switch-item {
    width: 100%;
    justify-content: space-between;
  }

  .habit-create-form__switch {
    flex-direction: column;
    align-items: flex-start;
  }

  .habit-create-drawer,
  .habit-detail-drawer {
    padding: 16px;
    gap: 16px;
  }

  .habit-detail__title,
  .habit-create-drawer__title {
    font-size: 22px;
  }

  .habit-detail__actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .habit-detail__actions>* {
    flex: 1;
    min-width: 0;
  }

  .habit-info-item {
    grid-template-columns: 1fr;
    gap: var(--mobe-space-2);
  }

  .habit-history-node {
    width: 132px;
    flex: 0 0 132px;
  }

  .habit-history-node__content {
    min-height: 64px;
  }

  .habit-history-node__body {
    max-width: 112px;
  }

  .habit-history-node__date {
    white-space: normal;
    word-break: break-word;
  }

  .habit-history-node__meta {
    max-width: 112px;
  }
}

@media (max-width: 480px) {
  .mobe-page-title {
    font-size: 24px;
  }

  .mobe-page-subtitle {
    font-size: 13px;
  }

  .mobe-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mobe-toolbar-search {
    grid-column: 1 / -1;
  }

  .mobe-toolbar>.mobe-toolbar-select {
    grid-column: 1 / -1;
  }

  .mobe-toolbar> :last-child {
    grid-column: 1 / -1;
  }

  .habit-card {
    padding: 14px;
  }

  .habit-card__icon-wrap {
    width: 42px;
    height: 42px;
  }

  .habit-card__title {
    font-size: 17px;
  }

  .habit-detail__stats-inline-value {
    font-size: 16px;
  }

  .habit-create-drawer,
  .habit-detail-drawer {
    padding: 14px;
  }
}
</style>