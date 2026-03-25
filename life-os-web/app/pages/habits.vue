<script setup lang="ts">
import { computed, reactive, ref, useTemplateRef, watch } from 'vue'
import { parseDate, Time, parseTime, type CalendarDate } from '@internationalized/date'

definePageMeta({
  middleware: 'auth'
})

type HabitFrequencyType = 'DAILY' | 'WEEKLY' | 'MONTHLY'
type HabitStatus = 'ENABLED' | 'DISABLED'
type HabitRecordStatus = 'DONE' | 'MISSED' | 'SKIPPED'
type HabitRecordSource = 'MANUAL' | 'SYSTEM' | 'LIST'

type HabitRecord = {
  id: string
  status: HabitRecordStatus
  date: string
  time: string
  source: HabitRecordSource
  note?: string
}

type HabitItem = {
  id: string
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
  records: HabitRecord[]
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


const habits = ref<HabitItem[]>([
  {
    id: '1',
    name: '晨间饮水',
    description: '起床后先喝一杯温水，让身体慢慢醒过来。',
    icon: 'i-lucide-cup-soda',
    color: 'green',
    frequencyType: 'DAILY',
    frequencyText: '每天',
    startDate: '2026-03-01',
    reminderTime: '08:00',
    generateToTodo: true,
    status: 'ENABLED',
    totalCheckInCount: 22,
    streakCount: 7,
    longestStreakCount: 12,
    lastCheckInAt: '2026-03-25 08:06',
    sort: 1,
    records: [
      { id: '1-1', status: 'DONE', date: '2026-03-25', time: '08:06', source: 'MANUAL', note: '今天起得早。' },
      { id: '1-2', status: 'DONE', date: '2026-03-24', time: '08:14', source: 'LIST' },
      { id: '1-3', status: 'DONE', date: '2026-03-23', time: '08:11', source: 'MANUAL' },
      { id: '1-4', status: 'MISSED', date: '2026-03-22', time: '--', source: 'SYSTEM', note: '当天忘记执行。' }
    ]
  },
  {
    id: '2',
    name: '晚间阅读',
    description: '睡前至少阅读 20 分钟，降低碎片化输入。',
    icon: 'i-lucide-book-open-text',
    color: 'blue',
    frequencyType: 'DAILY',
    frequencyText: '每天',
    startDate: '2026-03-05',
    reminderTime: '22:30',
    generateToTodo: false,
    status: 'ENABLED',
    totalCheckInCount: 15,
    streakCount: 3,
    longestStreakCount: 6,
    lastCheckInAt: '2026-03-24 22:42',
    sort: 2,
    records: [
      { id: '2-1', status: 'DONE', date: '2026-03-24', time: '22:42', source: 'MANUAL' },
      { id: '2-2', status: 'DONE', date: '2026-03-23', time: '22:28', source: 'MANUAL' },
      { id: '2-3', status: 'DONE', date: '2026-03-22', time: '22:36', source: 'LIST' },
      { id: '2-4', status: 'SKIPPED', date: '2026-03-21', time: '--', source: 'SYSTEM', note: '外出，可跳过。' }
    ]
  },
  {
    id: '3',
    name: '周末整理账单',
    description: '检查本周消费记录，补齐分类和备注。',
    icon: 'i-lucide-wallet-cards',
    color: 'orange',
    frequencyType: 'WEEKLY',
    frequencyText: '每周日',
    startDate: '2026-02-20',
    reminderTime: '20:00',
    generateToTodo: true,
    status: 'DISABLED',
    totalCheckInCount: 8,
    streakCount: 0,
    longestStreakCount: 4,
    lastCheckInAt: '2026-03-16 20:09',
    sort: 3,
    records: [
      { id: '3-1', status: 'MISSED', date: '2026-03-23', time: '--', source: 'SYSTEM' },
      { id: '3-2', status: 'DONE', date: '2026-03-16', time: '20:09', source: 'LIST' },
      { id: '3-3', status: 'DONE', date: '2026-03-09', time: '19:48', source: 'MANUAL' }
    ]
  }
])

const filters = reactive({
  keyword: '',
  status: 'ALL',
  frequencyType: 'ALL',
  generateToTodo: 'ALL'
})

const detailOpen = ref(false)
const drawerMode = ref<'create' | 'detail'>('detail')
const detailMode = ref<'view' | 'edit'>('view')
const activeHabitId = ref<string>('1')

const habitStartDateInput = useTemplateRef('habitStartDateInput')
const habitHistoryScrollRef = ref<HTMLElement | null>(null)

const editForm = reactive({
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

const filteredHabits = computed(() => {
  return habits.value.filter((item) => {
    const keyword = filters.keyword.trim().toLowerCase()
    const matchKeyword = !keyword
      || item.name.toLowerCase().includes(keyword)
      || item.description.toLowerCase().includes(keyword)

    const matchStatus = filters.status === 'ALL' || item.status === filters.status
    const matchFrequency = filters.frequencyType === 'ALL' || item.frequencyType === filters.frequencyType
    const matchGenerate = filters.generateToTodo === 'ALL'
      || (filters.generateToTodo === 'ON' && item.generateToTodo)
      || (filters.generateToTodo === 'OFF' && !item.generateToTodo)

    return matchKeyword && matchStatus && matchFrequency && matchGenerate
  })
})

const timelineItems = computed(() => {
  if (!activeHabit.value) return []

  return [...activeHabit.value.records]
    .sort((a, b) => `${b.date} ${b.time}`.localeCompare(`${a.date} ${a.time}`))
    .map((record) => ({
      ...record,
      date: `${record.date}${record.time && record.time !== '--' ? ` ${record.time}` : ''}`,
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

function getIconLabel(icon: string) {
  return iconOptions.find(item => item.value === icon)?.label || '未选择'
}

function resetEditForm(habit: HabitItem) {
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
  editForm.name = ''
  editForm.description = ''
  editForm.icon = 'i-lucide-sparkles'
  editForm.color = 'green'
  editForm.frequencyType = 'DAILY'
  editForm.frequencyText = '每天'
  editForm.startDate = ''
  editForm.reminderTime = ''
  editForm.generateToTodo = true
  editForm.status = 'ENABLED'
}

function openDetail(habit: HabitItem) {
  activeHabitId.value = habit.id
  resetEditForm(habit)
  drawerMode.value = 'detail'
  detailMode.value = 'view'
  detailOpen.value = true
}

function openCreateDrawer() {
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
}

function saveHabit() {
  if (!activeHabit.value) return

  const target = habits.value.find(item => item.id === activeHabit.value?.id)
  if (!target) return

  target.name = editForm.name.trim()
  target.description = editForm.description.trim()
  target.icon = editForm.icon.trim() || 'i-lucide-sparkles'
  target.color = editForm.color
  target.frequencyType = editForm.frequencyType
  target.frequencyText = editForm.frequencyText.trim()
  target.startDate = editForm.startDate.trim()
  target.reminderTime = editForm.reminderTime.trim()
  target.generateToTodo = editForm.generateToTodo
  target.status = editForm.status

  detailMode.value = 'view'
}

function createHabit() {
  const name = editForm.name.trim()
  if (!name) return

  const newHabit: HabitItem = {
    id: String(Date.now()),
    name,
    description: editForm.description.trim(),
    icon: editForm.icon.trim() || 'i-lucide-sparkles',
    color: editForm.color,
    frequencyType: editForm.frequencyType,
    frequencyText: editForm.frequencyText.trim() || getFrequencyLabel(editForm.frequencyType),
    startDate: editForm.startDate.trim(),
    reminderTime: editForm.reminderTime.trim(),
    generateToTodo: editForm.generateToTodo,
    status: editForm.status,
    totalCheckInCount: 0,
    streakCount: 0,
    longestStreakCount: 0,
    lastCheckInAt: '',
    sort: habits.value.length + 1,
    records: []
  }

  habits.value.unshift(newHabit)
  activeHabitId.value = newHabit.id
  detailOpen.value = false
}

function toggleGenerateToTodo(habit: HabitItem, value: boolean) {
  habit.generateToTodo = value
  if (activeHabit.value?.id === habit.id) {
    editForm.generateToTodo = value
  }
}

function toggleStatus(habit: HabitItem, value: boolean) {
  habit.status = value ? 'ENABLED' : 'DISABLED'
  if (activeHabit.value?.id === habit.id) {
    editForm.status = habit.status
  }
}

function getStatusLabel(status: HabitStatus) {
  return status === 'ENABLED' ? '启用中' : '已停用'
}

function getTimelineIcon(status: HabitRecordStatus) {
  if (status === 'DONE') return 'i-lucide-check'
  if (status === 'SKIPPED') return 'i-lucide-forward'
  return 'i-lucide-x'
}

function getTimelineColor(status: HabitRecordStatus) {
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

        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw">
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

    <div class="habit-card-grid">
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

          <UBadge :color="habit.status === 'ENABLED' ? 'primary' : 'neutral'" variant="soft">
            {{ getStatusLabel(habit.status) }}
          </UBadge>
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
              <USwitch :model-value="habit.generateToTodo" @update:model-value="toggleGenerateToTodo(habit, $event)" />
            </div>

            <div class="habit-card__switch-item">
              <span class="habit-card__switch-label">启用</span>
              <USwitch :model-value="habit.status === 'ENABLED'" @update:model-value="toggleStatus(habit, $event)" />
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
            <UButton icon="i-lucide-save" @click="createHabit">
              保存
            </UButton>
          </div>
        </div>

        <div v-else-if="activeHabit" class="habit-detail-drawer">
          <div class="habit-detail__header">
            <div class="habit-detail__title-wrap">
              <div class="habit-detail__title-row">
                <h2 class="habit-detail__title">{{ activeHabit.name }}</h2>
                <UBadge :color="activeHabit.status === 'ENABLED' ? 'primary' : 'neutral'" variant="soft">
                  {{ getStatusLabel(activeHabit.status) }}
                </UBadge>
              </div>
              <p class="habit-detail__subtitle">查看完整规则配置与历史打卡记录。</p>
            </div>

            <div class="habit-detail__actions">
              <template v-if="detailMode === 'view'">
                <UButton color="neutral" variant="soft" icon="i-lucide-pencil" @click="startEdit">
                  编辑
                </UButton>
              </template>

              <template v-else>
                <UButton color="neutral" variant="soft" @click="cancelEdit">
                  取消
                </UButton>
                <UButton icon="i-lucide-check" @click="saveHabit">
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

                  <div class="habit-form-item">
                    <label class="habit-form-label">开始日期</label>
                    <UInputDate ref="habitStartDateInput" v-model="habitStartDateValue" class="w-full" :format-options="{
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit'
                    }" :ui="{
                      base: 'pr-32'
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

                  <div class="habit-form-item">
                    <label class="habit-form-label">提醒时间</label>
                    <UInputTime ref="habitReminderTimeInput" v-model="habitReminderTimeValue" :hour-cycle="24"
                      class="w-full" />
                  </div>

                  <div class="habit-form-item">
                    <label class="habit-form-label">提醒时间</label>
                    <UInput v-model="editForm.reminderTime" />
                  </div>

                  <div class="habit-form-item habit-form-item--full">
                    <label class="habit-form-label">生成到清单</label>
                    <div class="habit-inline-switch">
                      <span class="habit-inline-switch__text">{{ editForm.generateToTodo ? '已开启' : '未开启' }}</span>
                      <USwitch v-model="editForm.generateToTodo" />
                    </div>
                  </div>

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
                    <span class="habit-info-item__value">{{ activeHabit.name }}</span>
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
                <strong class="habit-detail__stats-inline-value">{{ activeHabit.totalCheckInCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">当前连续打卡</span>
                <strong class="habit-detail__stats-inline-value">{{ activeHabit.streakCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">最长连续打卡</span>
                <strong class="habit-detail__stats-inline-value">{{ activeHabit.longestStreakCount }}</strong>
              </div>

              <div class="habit-detail__stats-inline-item">
                <span class="habit-detail__stats-inline-label">最近一次打卡</span>
                <strong class="habit-detail__stats-inline-value habit-detail__stats-inline-value--time">
                  {{ activeHabit.lastCheckInAt || '--' }}
                </strong>
              </div>
            </div>
          </section>

          <section class="habit-detail__section habit-detail__section--timeline">
            <div class="habit-detail__section-head">
              <h3 class="habit-detail__section-title">历史打卡记录</h3>
            </div>

            <div ref="habitHistoryScrollRef" class="habit-history-scroll">
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
  flex-wrap: nowrap;
}

.mobe-page-title-wrap {
  min-width: 0;
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
  gap: var(--mobe-space-6);
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
.source-label{
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

@media (max-width: 1279px) {

  .habit-card-grid,
  .habit-detail__section-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {

  .mobe-toolbar-search,
  .mobe-toolbar-select,
  .habit-card-grid,
  .habit-detail__section-grid,
  .habit-form-grid,
  .habit-card__info-panel,
  .habit-create-form__grid,
  .habit-detail__stats-inline {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .mobe-toolbar,
  .habit-detail__header,
  .habit-create-drawer__header {
    align-items: stretch;
  }

  .habit-card__top,
  .habit-card__switch-row,
  .habit-detail__header,
  .habit-create-drawer__header {
    flex-direction: column;
  }

  .habit-create-form__switch {
    flex-direction: column;
    align-items: flex-start;
  }

  .habit-create-drawer,
  .habit-detail-drawer {
    padding: 18px;
  }

  .habit-info-item {
    grid-template-columns: 1fr;
    gap: var(--mobe-space-2);
  }
}
</style>