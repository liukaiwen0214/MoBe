<script setup lang="ts">
import { computed, h, nextTick, onMounted, reactive, ref, resolveComponent, watch } from 'vue'
import { CalendarDate, parseDate } from '@internationalized/date'
import type { TableColumn } from '@nuxt/ui'
import type { ExpandedState, RowSelectionState, SortingState, VisibilityState } from '@tanstack/vue-table'
import { createBillApi, pageBillsApi, deleteRecordApi, updateRecordApi, batchCreateBillsApi } from '~/api/bills'
definePageMeta({
  middleware: 'auth'
})

const systemToast = useSystemToast()

/**
 * 行为标识常量
 * 用于 script 动态创建的 DOM 节点，避免把样式类名同时当作逻辑选择器使用
 */
const DOM_ACTIONS = {
  TOGGLE_GROUP_DATE: 'toggle-group-date'
} as const

/**
 * 样式类常量
 * 仅用于统一 className，避免散落字符串，同时降低动态 DOM 场景下的维护成本
 */
const UI_CLASSES = {
  GROUP_DATE_BTN: 'group-date-btn',
  GROUP_DATE_CONTENT: 'group-date-content',
  GROUP_DATE_ARROW_WRAP: 'group-date-arrow-wrap',
  GROUP_DATE_ARROW: 'group-date-arrow',
  GROUP_DATE_TEXT: 'group-date-text',
  GROUP_DATE_SEP: 'group-date-sep',
  GROUP_DATE_WEEKDAY: 'group-date-weekday',
  GROUP_DATE_COUNT: 'group-date-count'
} as const

type BillType = 'INCOME' | 'EXPENSE'
type QuickDateFilter = 'ALL' | 'TODAY' | 'THIS_WEEK' | 'THIS_MONTH' | 'LAST_30_DAYS'
type DateFilterMode = 'NONE' | 'QUICK' | 'RANGE' | 'MONTH'

type CalendarDateRangeValue = {
  start?: CalendarDate
  end?: CalendarDate
}

type BillRecord = {
  id: string
  type: BillType
  category: string
  amount: number
  recordDate: string
  remark?: string
}

type BillLeafRow = BillRecord & {
  kind: 'item'
}

type BillGroupRow = {
  id: string
  kind: 'group'
  day: string
  label: string
  weekday: string
  amount: number
  count: number
  children: BillLeafRow[]
}

type BillTableRow = BillGroupRow | BillLeafRow

const UButton = resolveComponent('UButton')
const UBadge = resolveComponent('UBadge')
const UCheckbox = resolveComponent('UCheckbox')
const UDropdownMenu = resolveComponent('UDropdownMenu')
const UInput = resolveComponent('UInput')
const USelect = resolveComponent('USelect')
const USelectMenu = resolveComponent('USelectMenu')
const UTextarea = resolveComponent('UTextarea')
const UIcon = resolveComponent('UIcon')

const loading = ref(true)
const creatingBill = ref(false)
const importingBills = ref(false)

const createBillOpen = ref(false)
const importOverlayOpen = ref(false)
const dragOver = ref(false)
const selectedImportFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const billRecords = ref<BillRecord[]>([])

const sorting = ref<SortingState>([
  { id: 'recordDate', desc: true }
])

const rowSelection = ref<RowSelectionState>({})
const columnVisibility = ref<VisibilityState>({
  remark: true
})
const expanded = ref<ExpandedState>({})

const datePickerOpen = ref(false)
const timePickerOpen = ref(false)
const categorySearchTerm = ref('')

const rangePopoverOpen = ref(false)
const monthPopoverOpen = ref(false)

const hourListRef = ref<HTMLElement | null>(null)
const minuteListRef = ref<HTMLElement | null>(null)
const activeHourItemRef = ref<HTMLElement | null>(null)
const activeMinuteItemRef = ref<HTMLElement | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10
})

const typeOptions = [
  { label: '全部类型', value: 'ALL' },
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]

const billTypeOptions = [
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]

const quickDateOptions: Array<{ label: string; value: QuickDateFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '今天', value: 'TODAY' },
  { label: '本周', value: 'THIS_WEEK' },
  { label: '本月', value: 'THIS_MONTH' },
  { label: '近30天', value: 'LAST_30_DAYS' }
]

const categoryOptions = ref([
  { label: '餐饮', value: '餐饮' },
  { label: '交通', value: '交通' },
  { label: '购物', value: '购物' },
  { label: '娱乐', value: '娱乐' },
  { label: '饮品', value: '饮品' },
  { label: '工资', value: '工资' },
  { label: '红包', value: '红包' }
])

const filters = reactive({
  keyword: '',
  type: 'ALL' as 'ALL' | BillType,
  quickDate: 'ALL' as QuickDateFilter,
  dateMode: 'NONE' as DateFilterMode,
  customDateRange: {} as CalendarDateRangeValue,
  month: undefined as CalendarDate | undefined
})

const monthPickerYear = ref(new Date().getFullYear())

const billForm = reactive({
  date: '',
  time: '',
  type: 'EXPENSE' as BillType,
  category: '餐饮',
  amountInput: '',
  remark: ''
})

const formErrors = reactive({
  date: '',
  time: '',
  type: '',
  category: '',
  amountInput: ''
})
const editingBill = ref(false)
const deletingBill = ref(false)

const editBillOpen = ref(false)
const deleteConfirmOpen = ref(false)

const editingBillId = ref<string>('')
const deletingBillId = ref<string>('')

const editCategorySearchTerm = ref('')

const editBillForm = reactive({
  date: '',
  time: '',
  type: 'EXPENSE' as BillType,
  category: '餐饮',
  amountInput: '',
  remark: ''
})

const editFormErrors = reactive({
  date: '',
  time: '',
  type: '',
  category: '',
  amountInput: ''
})

function isBillLeafRow(row: BillTableRow): row is BillLeafRow {
  return row.kind === 'item'
}

function pad2(value: number) {
  return `${value}`.padStart(2, '0')
}

function getTodayDateString() {
  const now = new Date()
  return `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())}`
}

function getCurrentTimeString() {
  const now = new Date()
  return `${pad2(now.getHours())}:${pad2(now.getMinutes())}`
}

function formatDateDisplay(value: string) {
  if (!value) return '请选择日期'

  const date = new Date(`${value}T00:00:00`)
  const yyyy = date.getFullYear()
  const mm = pad2(date.getMonth() + 1)
  const dd = pad2(date.getDate())
  return `${yyyy} / ${mm} / ${dd}`
}

function formatTimeDisplay(value: string) {
  return value || '请选择时间'
}

function formatDateTime(value: string) {
  const date = new Date(value)
  const yyyy = date.getFullYear()
  const mm = pad2(date.getMonth() + 1)
  const dd = pad2(date.getDate())
  const hh = pad2(date.getHours())
  const mi = pad2(date.getMinutes())
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

function formatDay(value: string) {
  const date = new Date(value)
  const yyyy = date.getFullYear()
  const mm = pad2(date.getMonth() + 1)
  const dd = pad2(date.getDate())
  return `${yyyy}-${mm}-${dd}`
}

function formatWeekday(value: string): string {
  const date = new Date(value)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[date.getDay()] || '周日'
}

function formatCurrency(amount: number) {
  const sign = amount >= 0 ? '' : '-'
  return `${sign}¥${Math.abs(amount).toFixed(2)}`
}

function getTypeLabel(type: BillType) {
  return type === 'INCOME' ? '收入' : '支出'
}

function formatRangeSummary(range: any) {
  if (!range.start && !range.end) {
    return '自定义时间'
  }

  const start = range.start ? range.start.toString() : '开始'
  const end = range.end ? range.end.toString() : '结束'
  return `${start} － ${end}`
}

function formatMonthSummary(value?: any) {
  if (!value) {
    return '按月份'
  }

  return `${value.year} / ${pad2(value.month)}`
}

function normalizeAmountInput(value: string) {
  return value
    .replace(/[^\d.]/g, '')
    .replace(/^(\d*\.\d{0,2}).*$/, '$1')
}

function parseAmount(value: string) {
  const normalized = normalizeAmountInput(value)
  if (!normalized) return NaN
  return Number(normalized)
}

function buildRecordDate(date: string, time: string) {
  return `${date}T${time}:00`
}

function resetBillForm() {
  billForm.date = getTodayDateString()
  billForm.time = getCurrentTimeString()
  billForm.type = 'EXPENSE'
  billForm.category = '餐饮'
  billForm.amountInput = ''
  billForm.remark = ''

  categorySearchTerm.value = ''

  formErrors.date = ''
  formErrors.time = ''
  formErrors.type = ''
  formErrors.category = ''
  formErrors.amountInput = ''
}

function openCreateBillSlideover() {
  resetBillForm()
  createBillOpen.value = true
}

function closeCreateBillSlideover() {
  createBillOpen.value = false
}

const calendarDateValue = computed({
  get() {
    return billForm.date ? parseDate(billForm.date) : parseDate(getTodayDateString())
  },
  set(value: CalendarDate) {
    billForm.date = value.toString()
    datePickerOpen.value = false
  }
})

const rangeCalendarValue = ref<any>({
  start: undefined,
  end: undefined
})

watch(rangeCalendarValue, (newValue) => {
  // 避免循环更新：只有当值真正变化时才更新
  const startChanged = filters.customDateRange.start !== newValue?.start
  const endChanged = filters.customDateRange.end !== newValue?.end

  if (startChanged || endChanged) {
    filters.customDateRange = {
      start: newValue?.start,
      end: newValue?.end
    }
    filters.dateMode = newValue?.start || newValue?.end ? 'RANGE' : 'NONE'
    filters.quickDate = 'ALL'
    filters.month = undefined
  }
}, { deep: true })

// 初始化rangeCalendarValue，避免循环更新
if (!rangeCalendarValue.value.start && !rangeCalendarValue.value.end) {
  rangeCalendarValue.value = { ...filters.customDateRange }
}

const hours = Array.from({ length: 24 }, (_, index) => pad2(index))
const minutes = Array.from({ length: 60 }, (_, index) => pad2(index))

const selectedHour = computed(() => billForm.time.split(':')[0] || '00')
const selectedMinute = computed(() => billForm.time.split(':')[1] || '00')

function updateBillTime(hour?: string, minute?: string) {
  const nextHour = hour ?? selectedHour.value
  const nextMinute = minute ?? selectedMinute.value
  billForm.time = `${nextHour}:${nextMinute}`
}

async function selectHour(hour: string) {
  updateBillTime(hour, undefined)
  await scrollTimePickerToSelected()
}

async function selectMinute(minute: string) {
  updateBillTime(undefined, minute)
  await scrollTimePickerToSelected()
}

function setActiveHourItemRef(el: unknown, hour: string) {
  if (selectedHour.value === hour) {
    activeHourItemRef.value = el as HTMLElement | null
  }
}

function setActiveMinuteItemRef(el: unknown, minute: string) {
  if (selectedMinute.value === minute) {
    activeMinuteItemRef.value = el as HTMLElement | null
  }
}

function scrollActiveItemIntoView(container: HTMLElement | null, target: HTMLElement | null) {
  if (!container || !target) return

  const containerHeight = container.clientHeight
  const targetTop = target.offsetTop
  const targetHeight = target.offsetHeight
  const nextScrollTop = targetTop - containerHeight / 2 + targetHeight / 2

  container.scrollTo({
    top: Math.max(0, nextScrollTop),
    behavior: 'smooth'
  })
}

async function scrollTimePickerToSelected() {
  await nextTick()
  scrollActiveItemIntoView(hourListRef.value, activeHourItemRef.value)
  scrollActiveItemIntoView(minuteListRef.value, activeMinuteItemRef.value)
}

watch(timePickerOpen, async (open) => {
  if (!open) return
  await scrollTimePickerToSelected()
})

const parsedFormAmount = computed(() => parseAmount(billForm.amountInput))

const amountPreviewText = computed(() => {
  if (!Number.isFinite(parsedFormAmount.value) || parsedFormAmount.value <= 0) {
    return '¥0.00'
  }

  return formatCurrency(parsedFormAmount.value)
})

function validateBillForm() {
  formErrors.date = billForm.date ? '' : '请选择日期'
  formErrors.time = billForm.time ? '' : '请选择时间'
  formErrors.type = billForm.type ? '' : '请选择类型'
  formErrors.category = billForm.category ? '' : '请选择分类'

  const amount = parseAmount(billForm.amountInput)
  if (!billForm.amountInput.trim()) {
    formErrors.amountInput = '请输入金额'
  } else if (!Number.isFinite(amount) || amount <= 0) {
    formErrors.amountInput = '金额必须大于 0'
  } else {
    formErrors.amountInput = ''
  }

  return !Object.values(formErrors).some(Boolean)
}

function findBillById(id: string) {
  return billRecords.value.find(item => item.id === id)
}

function fillEditBillForm(record: BillRecord) {
  const [date = '', timeWithSecond = '00:00:00'] = record.recordDate.split('T')
  const time = timeWithSecond.slice(0, 5)

  editBillForm.date = date
  editBillForm.time = time
  editBillForm.type = record.type
  editBillForm.category = record.category
  editBillForm.amountInput = `${record.amount}`
  editBillForm.remark = record.remark || ''

  editCategorySearchTerm.value = ''

  editFormErrors.date = ''
  editFormErrors.time = ''
  editFormErrors.type = ''
  editFormErrors.category = ''
  editFormErrors.amountInput = ''
}

function openEditBillSlideover(id: string) {
  const record = findBillById(id)

  if (!record) {
    systemToast.error('打开失败', '未找到要编辑的账单', 'bill-edit-open-error')
    return
  }

  editingBillId.value = id
  fillEditBillForm(record)
  editBillOpen.value = true
}

function closeEditBillSlideover() {
  editBillOpen.value = false
  editingBillId.value = ''
}

function openDeleteConfirm(id: string) {
  const record = findBillById(id)

  if (!record) {
    systemToast.error('删除失败', '未找到要删除的账单', 'bill-delete-open-error')
    return
  }

  deletingBillId.value = id
  deleteConfirmOpen.value = true
}

function closeDeleteConfirm() {
  deleteConfirmOpen.value = false
  deletingBillId.value = ''
}
function validateEditBillForm() {
  editFormErrors.date = editBillForm.date ? '' : '请选择日期'
  editFormErrors.time = editBillForm.time ? '' : '请选择时间'
  editFormErrors.type = editBillForm.type ? '' : '请选择类型'
  editFormErrors.category = editBillForm.category ? '' : '请选择分类'

  const amount = parseAmount(editBillForm.amountInput)
  if (!editBillForm.amountInput.trim()) {
    editFormErrors.amountInput = '请输入金额'
  } else if (!Number.isFinite(amount) || amount <= 0) {
    editFormErrors.amountInput = '金额必须大于 0'
  } else {
    editFormErrors.amountInput = ''
  }

  return !Object.values(editFormErrors).some(Boolean)
}


function addCategoryFromSearch(term?: string) {
  const name = (term ?? categorySearchTerm.value).trim()
  if (!name) return

  const exists = categoryOptions.value.some(item => item.value === name)
  if (!exists) {
    categoryOptions.value = [{ label: name, value: name }, ...categoryOptions.value]
  }

  billForm.category = name
  categorySearchTerm.value = ''
}

const normalizedCategorySearchTerm = computed(() => categorySearchTerm.value.trim())

const categorySelectMenuItems = computed(() => {
  const keyword = normalizedCategorySearchTerm.value.toLowerCase()

  const filtered = keyword
    ? categoryOptions.value.filter(item => item.label.toLowerCase().includes(keyword))
    : categoryOptions.value

  const hasExactMatch = categoryOptions.value.some(
    item => item.label.toLowerCase() === keyword && keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedCategorySearchTerm.value}」分类`,
        value: `__create__${normalizedCategorySearchTerm.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

watch(
  () => billForm.category,
  value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      addCategoryFromSearch(value.replace('__create__', ''))
    }
  }
)
const normalizedEditCategorySearchTerm = computed(() => editCategorySearchTerm.value.trim())

const editCategorySelectMenuItems = computed(() => {
  const keyword = normalizedEditCategorySearchTerm.value.toLowerCase()

  const filtered = keyword
    ? categoryOptions.value.filter(item => item.label.toLowerCase().includes(keyword))
    : categoryOptions.value

  const hasExactMatch = categoryOptions.value.some(
    item => item.label.toLowerCase() === keyword && keyword
  )

  if (keyword && !hasExactMatch) {
    return [
      {
        label: `继续回车添加「${normalizedEditCategorySearchTerm.value}」分类`,
        value: `__create__${normalizedEditCategorySearchTerm.value}`,
        create: true
      },
      ...filtered
    ]
  }

  return filtered
})

watch(
  () => editBillForm.category,
  value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      const name = value.replace('__create__', '')
      addCategoryFromSearch(name)
      editBillForm.category = name
      editCategorySearchTerm.value = ''
    }
  }
)
async function submitEditBill() {
  if (!validateEditBillForm()) {
    systemToast.error('保存失败', '请检查日期、时间、分类和金额后再试', 'bill-edit-validate-error')
    return
  }

  editingBill.value = true

  try {
    const index = billRecords.value.findIndex(item => item.id === editingBillId.value)

    if (index < 0) {
      systemToast.error('保存失败', '未找到要编辑的账单', 'bill-edit-not-found')
      return
    }

    const original = billRecords.value[index] as BillRecord

    const updated: BillRecord = {
      ...original,
      type: editBillForm.type,
      category: editBillForm.category,
      amount: Number(parseAmount(editBillForm.amountInput).toFixed(2)),
      recordDate: buildRecordDate(editBillForm.date, editBillForm.time),
      ...(editBillForm.remark.trim() ? { remark: editBillForm.remark.trim() } : { remark: undefined })
    }

    /**
     * TODO:
     * 后续这里替换为真实编辑账单 API。
     * 建议改为：
     * 1. 调用编辑接口
     * 2. 成功后重新拉取列表或按后端返回更新当前记录
     */
    await updateRecordApi({
      ...updated
    })

    billRecords.value.splice(index, 1, updated)
    mergeCategoriesFromBills([updated])

    closeEditBillSlideover()
    systemToast.success('保存成功', '账单已更新', 'bill-edit-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'bill-edit-error')
  } finally {
    editingBill.value = false
  }
}

function mergeCategoriesFromBills(records: BillRecord[]) {
  const nextCategories = Array.from(
    new Set(records.map(item => item.category.trim()).filter(Boolean))
  )

  if (!nextCategories.length) {
    return
  }

  const existing = new Set(categoryOptions.value.map(item => item.value))
  const missing = nextCategories.filter(category => !existing.has(category))

  if (missing.length) {
    categoryOptions.value = [
      ...missing.map(category => ({
        label: category,
        value: category
      })),
      ...categoryOptions.value
    ]
  }
}

async function submitCreateBill() {
  if (!validateBillForm()) {
    systemToast.error('保存失败', '请检查日期、时间、分类和金额后再试', 'bill-create-validate-error')
    return
  }

  creatingBill.value = true

  try {
    const request = {
      type: billForm.type,
      category: billForm.category,
      amount: Number(parseAmount(billForm.amountInput).toFixed(2)),
      recordDate: buildRecordDate(billForm.date, billForm.time),
      ...(billForm.remark.trim() ? { remark: billForm.remark.trim() } : {})
    }

    await createBillApi(request)

    closeCreateBillSlideover()
    pagination.page = 1
    await fetchBillRecords()

    systemToast.success('保存成功', '账单已新增', 'bill-create-success')
  } catch (error: any) {
    systemToast.error('保存失败', error?.message || '请稍后重试', 'bill-create-error')
  } finally {
    creatingBill.value = false
  }
}

function openImportFilePicker() {
  fileInputRef.value?.click()
}

function generateImportId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function parseImportedBillType(value: string): BillType {
  const normalized = value.trim().toUpperCase()

  if (normalized === 'INCOME' || value.trim() === '收入') {
    return 'INCOME'
  }

  return 'EXPENSE'
}
function normalizeImportedRecordDate(value: string) {
  const raw = value.trim()
  if (!raw) return ''

  const normalized = raw.replace(/\//g, '-').replace(/\s+/, 'T')
  const [datePart = '', timePart = ''] = normalized.split('T')

  const dateSegments = datePart.split('-').map(item => item.trim())
  if (dateSegments.length !== 3) return ''

  const [year = '', month = '', day = ''] = dateSegments
  const paddedDate = [
    year,
    month.padStart(2, '0'),
    day.padStart(2, '0')
  ].join('-')

  if (!timePart) {
    return `${paddedDate}T00:00:00`
  }

  const timeSegments = timePart.split(':').map(item => item.trim())
  const hour = (timeSegments[0] || '0').padStart(2, '0')
  const minute = (timeSegments[1] || '0').padStart(2, '0')
  const second = (timeSegments[2] || '0').padStart(2, '0')

  return `${paddedDate}T${hour}:${minute}:${second}`
}
/**
 * 当前仅支持最基础的 CSV 模板导入，且解析逻辑在前端本地完成。
 * 后续接入真实 API 后，建议改为：
 * 1. 前端只做基础校验
 * 2. 上传文件到后端
 * 3. 由后端解析、校验、入库并返回导入结果
 */
function parseCsvTextToBills(text: string): BillRecord[] {
  const lines = text
    .split(/\r?\n/)
    .map(line => line.trim())
    .filter(Boolean)

  if (lines.length <= 1) {
    return []
  }

  const rows = lines.slice(1)

  return rows
    .map((line): BillRecord | null => {
      const [recordDateRaw = '', typeRaw = '', categoryRaw = '', amountRaw = '', remarkRaw = ''] = line.split(',')

      const recordDate = normalizeImportedRecordDate(recordDateRaw)
      const amount = Number(amountRaw.trim())
      const remark = remarkRaw.trim()

      if (!recordDate || !categoryRaw.trim() || !Number.isFinite(amount) || amount <= 0) {
        return null
      }

      const bill: BillRecord = {
        id: generateImportId(),
        type: parseImportedBillType(typeRaw),
        category: categoryRaw.trim(),
        amount,
        recordDate: recordDate.length === 16 ? `${recordDate}:00` : recordDate
      }

      if (remark) {
        bill.remark = remark
      }

      return bill
    })
    .filter((item): item is BillRecord => item !== null)
}

function openImportOverlay() {
  selectedImportFile.value = null
  dragOver.value = false
  importOverlayOpen.value = true
}

function closeImportOverlay() {
  importOverlayOpen.value = false
  dragOver.value = false
  selectedImportFile.value = null
}

function handleImportFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0] || null
  selectedImportFile.value = file
  target.value = ''
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
  dragOver.value = true
}

function handleDragLeave(event: DragEvent) {
  event.preventDefault()
  dragOver.value = false
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  dragOver.value = false

  const file = event.dataTransfer?.files?.[0] || null
  if (file) {
    selectedImportFile.value = file
  }
}

async function submitImportBills() {
  if (!selectedImportFile.value) {
    systemToast.warning('导入失败', '请先选择文件', 'bill-import-empty-file')
    return
  }

  const fileName = selectedImportFile.value.name.toLowerCase()

  if (!fileName.endsWith('.csv')) {
    systemToast.warning('导入失败', '当前只支持 CSV 文件导入', 'bill-import-invalid-file-type')
    return
  }

  importingBills.value = true

  try {
    const text = await selectedImportFile.value.text()
    const importedBills = parseCsvTextToBills(text)

    if (!importedBills.length) {
      systemToast.error('导入失败', '没有解析到可导入的数据，请检查文件内容', 'bill-import-empty-data')
      return
    }

    const requestData = {
      records: importedBills.map(item => ({
        type: item.type,
        category: item.category,
        amount: item.amount,
        recordDate: item.recordDate,
        ...(item.remark ? { remark: item.remark } : {})
      }))
    }

    await batchCreateBillsApi(requestData)

    closeImportOverlay()
    pagination.page = 1
    await fetchBillRecords()

    systemToast.success('导入成功', `成功导入 ${importedBills.length} 条账单`, 'bill-import-success')
  } catch (error: any) {
    systemToast.error('导入失败', error?.message || '请稍后重试', 'bill-import-error')
  } finally {
    importingBills.value = false
  }
}

function downloadImportTemplate() {
  try {
    /**
     * TODO:
     * 当前模板在前端静态生成。
     * 后续如果导入字段规则变复杂，建议改为从后端获取统一模板文件。
     */
    const template = [
      '日期时间,类型,分类,金额,备注',
      '2026-03-23 08:30,支出,餐饮,28,早餐',
      '2026-03-23 09:10,支出,交通,6,公交',
      '2026-03-23 12:00,收入,红包,88,朋友转账'
    ].join('\n')

    const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = url
    link.download = '账单导入模板.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    URL.revokeObjectURL(url)

    systemToast.success('下载成功', '模板已开始下载', 'bill-template-download-success')
  } catch (error: any) {
    systemToast.error('下载失败', error?.message || '请稍后重试', 'bill-template-download-error')
  }
}
function buildBillPageRequest() {
  const request = {
    pageNum: 1,
    pageSize: 500,
    type: filters.type !== 'ALL' ? filters.type : undefined,
    keyword: filters.keyword.trim() || undefined,
    dateMode: filters.dateMode !== 'NONE' ? filters.dateMode : undefined,
    quickDate: filters.dateMode === 'QUICK' ? filters.quickDate : undefined,
    startDate: filters.dateMode === 'RANGE' && filters.customDateRange.start ? `${filters.customDateRange.start.toString()} 00:00:00` : undefined,
    endDate: filters.dateMode === 'RANGE' && filters.customDateRange.end ? `${filters.customDateRange.end.toString()} 23:59:59` : undefined,
    month: filters.dateMode === 'MONTH' && filters.month ? `${filters.month.year}-${pad2(filters.month.month)}` : undefined
  }

  return request
}
/**
 * 获取账单列表
 */
async function fetchBillRecords(showSuccess = false) {
  loading.value = true

  try {
    const request = buildBillPageRequest()
    const res: any = await pageBillsApi(request)

    const records = res?.data?.records || []

    billRecords.value = records
    mergeCategoriesFromBills(records)

    if (showSuccess) {
      systemToast.success('刷新成功', '账单列表已更新', 'bill-fetch-success')
    }
  } catch (error: any) {
    systemToast.error('获取账单失败', error?.message || '请稍后重试', 'bill-fetch-error')
  } finally {
    loading.value = false
  }
}

function startOfDay(date: Date) {
  const next = new Date(date)
  next.setHours(0, 0, 0, 0)
  return next
}

function endOfDay(date: Date) {
  const next = new Date(date)
  next.setHours(23, 59, 59, 999)
  return next
}

function getWeekStart(date: Date) {
  const next = startOfDay(date)
  const day = next.getDay()
  const diff = day === 0 ? -6 : 1 - day
  next.setDate(next.getDate() + diff)
  return startOfDay(next)
}

function getWeekEnd(date: Date) {
  const next = getWeekStart(date)
  next.setDate(next.getDate() + 6)
  return endOfDay(next)
}

function buildSearchableText(item: BillRecord) {
  return [
    getTypeLabel(item.type),
    item.type,
    item.category,
    `${item.amount}`,
    item.amount.toFixed(2),
    formatDay(item.recordDate),
    formatDateTime(item.recordDate),
    item.remark || ''
  ]
    .join(' ')
    .toLowerCase()
}

function matchesKeyword(item: BillRecord, keyword: string) {
  if (!keyword) {
    return true
  }

  return buildSearchableText(item).includes(keyword)
}

function matchesDateFilter(item: BillRecord) {
  const { dateMode, quickDate, customDateRange, month } = filters

  if (dateMode === 'NONE') {
    return true
  }

  const recordDate = new Date(item.recordDate)
  const recordDay = formatDay(item.recordDate)

  if (dateMode === 'QUICK') {
    const now = new Date()

    switch (quickDate) {
      case 'TODAY': {
        const start = startOfDay(now)
        const end = endOfDay(now)
        return recordDate >= start && recordDate <= end
      }
      case 'THIS_WEEK': {
        const start = getWeekStart(now)
        const end = getWeekEnd(now)
        return recordDate >= start && recordDate <= end
      }
      case 'THIS_MONTH': {
        const start = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0)
        const end = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999)
        return recordDate >= start && recordDate <= end
      }
      case 'LAST_30_DAYS': {
        const end = endOfDay(now)
        const start = startOfDay(now)
        start.setDate(start.getDate() - 29)
        return recordDate >= start && recordDate <= end
      }
      default:
        return true
    }
  }

  if (dateMode === 'RANGE') {
    if (customDateRange.start && recordDay < customDateRange.start.toString()) {
      return false
    }

    if (customDateRange.end && recordDay > customDateRange.end.toString()) {
      return false
    }

    return true
  }

  if (dateMode === 'MONTH' && month) {
    const monthPrefix = `${month.year}-${pad2(month.month)}`
    return recordDay.startsWith(monthPrefix)
  }

  return true
}

function selectQuickDate(value: QuickDateFilter) {
  filters.quickDate = value
  filters.dateMode = value === 'ALL' ? 'NONE' : 'QUICK'
  filters.customDateRange = {}
  filters.month = undefined
}

function clearAllFilters() {
  filters.keyword = ''
  filters.type = 'ALL'
  filters.quickDate = 'ALL'
  filters.dateMode = 'NONE'
  filters.customDateRange = {}
  filters.month = undefined
  rangePopoverOpen.value = false
  monthPopoverOpen.value = false
  systemToast.success('已清空筛选', '所有筛选条件已重置', 'bill-clear-filters-success')
}

function clearDateFilters() {
  filters.quickDate = 'ALL'
  filters.dateMode = 'NONE'
  filters.customDateRange = {}
  filters.month = undefined
  rangePopoverOpen.value = false
  monthPopoverOpen.value = false
}

function onRangeCalendarUpdate(value: any) {
  filters.customDateRange = {
    start: value?.start,
    end: value?.end
  }
  filters.dateMode = value?.start || value?.end ? 'RANGE' : 'NONE'
  filters.quickDate = 'ALL'
  filters.month = undefined
}

function previousMonthPickerYear() {
  monthPickerYear.value -= 1
}

function nextMonthPickerYear() {
  monthPickerYear.value += 1
}

const monthPickerItems = computed(() => {
  return Array.from({ length: 12 }, (_, index) => ({
    label: `${index + 1}月`,
    value: new CalendarDate(monthPickerYear.value, index + 1, 1)
  }))
})

function selectMonthFilter(value: CalendarDate) {
  filters.month = value
  filters.dateMode = 'MONTH'
  filters.quickDate = 'ALL'
  filters.customDateRange = {}
  monthPopoverOpen.value = false
}

const filterWatchSource = computed(() => ({
  keyword: filters.keyword,
  type: filters.type,
  quickDate: filters.quickDate,
  dateMode: filters.dateMode,
  rangeStart: filters.customDateRange.start?.toString() || '',
  rangeEnd: filters.customDateRange.end?.toString() || '',
  month: filters.month ? `${filters.month.year}-${filters.month.month}` : ''
}))

watch(filterWatchSource, () => {
  pagination.page = 1
  expanded.value = {}
}, { deep: true })

const filteredLeafRows = computed<BillRecord[]>(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return billRecords.value.filter(item => {
    const matchType = filters.type === 'ALL' || item.type === filters.type
    const matchKeyword = matchesKeyword(item, keyword)
    const matchDate = matchesDateFilter(item)

    return matchType && matchKeyword && matchDate
  })
})

const groupedTableData = computed<BillGroupRow[]>(() => {
  const groupedMap = new Map<string, BillLeafRow[]>()

  for (const item of filteredLeafRows.value) {
    const day = formatDay(item.recordDate)
    const leaf: BillLeafRow = {
      ...item,
      kind: 'item'
    }

    if (!groupedMap.has(day)) {
      groupedMap.set(day, [])
    }

    groupedMap.get(day)?.push(leaf)
  }

  return Array.from(groupedMap.entries())
    .sort((a, b) => new Date(b[0]).getTime() - new Date(a[0]).getTime())
    .map(([day, children]) => {
      const amount = children.reduce((sum, item) => {
        return item.type === 'EXPENSE' ? sum - item.amount : sum + item.amount
      }, 0)

      return {
        id: `group-${day}`,
        kind: 'group',
        day,
        label: day,
        weekday: formatWeekday(day),
        amount,
        count: children.length,
        children: children.sort((a, b) => new Date(b.recordDate).getTime() - new Date(a.recordDate).getTime())
      }
    })
})

const totalGroupCount = computed(() => groupedTableData.value.length)
const totalPageCount = computed(() => Math.max(1, Math.ceil(totalGroupCount.value / pagination.pageSize)))

watch(totalPageCount, value => {
  if (pagination.page > value) {
    pagination.page = value
  }
})

const pagedTableData = computed<BillGroupRow[]>(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return groupedTableData.value.slice(start, end)
})

const summary = computed(() => {
  const income = filteredLeafRows.value
    .filter(item => item.type === 'INCOME')
    .reduce((sum, item) => sum + item.amount, 0)

  const expense = filteredLeafRows.value
    .filter(item => item.type === 'EXPENSE')
    .reduce((sum, item) => sum + item.amount, 0)

  return {
    income,
    expense,
    balance: income - expense,
    count: filteredLeafRows.value.length
  }
})

const columns = computed<TableColumn<BillTableRow>[]>(() => [
  {
    id: 'select',
    header: ({ table }) =>
      h(UCheckbox, {
        modelValue: table.getIsAllPageRowsSelected(),
        'onUpdate:modelValue': (value: boolean) => table.toggleAllPageRowsSelected(value),
        'aria-label': '全选'
      }),
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('div')
      }

      return h(UCheckbox, {
        modelValue: row.getIsSelected(),
        'onUpdate:modelValue': (value: boolean) => row.toggleSelected(value),
        'aria-label': '选择当前行'
      })
    },
    enableSorting: false,
    enableHiding: false,
    meta: {
      class: {
        th: 'w-10',
        td: 'w-10'
      }
    }
  },
  {
    id: 'recordDate',
    accessorKey: 'recordDate',
    header: ({ column }) =>
      h(
        UButton,
        {
          color: 'neutral',
          variant: 'ghost',
          class: 'px-0 font-medium',
          onClick: () => column.toggleSorting(column.getIsSorted() === 'asc')
        },
        () => [
          '日期',
          h(UIcon, {
            name:
              column.getIsSorted() === 'asc'
                ? 'i-lucide-arrow-up'
                : column.getIsSorted() === 'desc'
                  ? 'i-lucide-arrow-down'
                  : 'i-lucide-arrow-up-down',
            class: 'ml-1 size-4'
          })
        ]
      ),
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h(
          'button',
          {
            type: 'button',
            class: UI_CLASSES.GROUP_DATE_BTN,
            'data-action': DOM_ACTIONS.TOGGLE_GROUP_DATE,
            'aria-expanded': row.getIsExpanded(),
            onClick: () => row.toggleExpanded()
          },
          [
            h('div', { class: UI_CLASSES.GROUP_DATE_CONTENT }, [
              h('span', { class: UI_CLASSES.GROUP_DATE_ARROW_WRAP }, [
                h(UIcon, {
                  name: row.getIsExpanded() ? 'i-lucide-chevron-down' : 'i-lucide-chevron-right',
                  class: UI_CLASSES.GROUP_DATE_ARROW
                })
              ]),
              h('span', { class: UI_CLASSES.GROUP_DATE_TEXT }, currentRow.label),
              h('span', { class: UI_CLASSES.GROUP_DATE_SEP }, '｜'),
              h('span', { class: UI_CLASSES.GROUP_DATE_WEEKDAY }, currentRow.weekday),
              h('span', { class: UI_CLASSES.GROUP_DATE_SEP }, '｜'),
              h('span', { class: UI_CLASSES.GROUP_DATE_COUNT }, `${currentRow.count} 笔`)
            ])
          ]
        )
      }

      return h('span', { class: 'text-sm' }, formatDateTime(currentRow.recordDate))
    },
    footer: () => h('span', { class: 'font-medium' }, '合计'),
    meta: {
      class: {
        th: 'min-w-[240px]',
        td: 'min-w-[240px]'
      }
    }
  },
  {
    id: 'type',
    accessorKey: 'type',
    header: '类型',
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('span')
      }

      return h(
        UBadge,
        {
          color: currentRow.type === 'INCOME' ? 'success' : 'error',
          variant: 'soft'
        },
        () => getTypeLabel(currentRow.type)
      )
    },
    footer: () =>
      h('span', { class: 'text-xs text-[var(--mobe-text-soft)]' }, `${summary.value.count} 笔`)
  },
  {
    id: 'category',
    accessorKey: 'category',
    header: '分类',
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('span')
      }

      return h('span', {}, currentRow.category)
    },
    footer: () => h('span')
  },
  {
    id: 'amount',
    accessorKey: 'amount',
    header: ({ column }) =>
      h(
        UButton,
        {
          color: 'neutral',
          variant: 'ghost',
          class: 'px-0 font-medium',
          onClick: () => column.toggleSorting(column.getIsSorted() === 'asc')
        },
        () => [
          '金额',
          h(UIcon, {
            name:
              column.getIsSorted() === 'asc'
                ? 'i-lucide-arrow-up'
                : column.getIsSorted() === 'desc'
                  ? 'i-lucide-arrow-down'
                  : 'i-lucide-arrow-up-down',
            class: 'ml-1 size-4'
          })
        ]
      ),
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h(
          'span',
          {
            class: currentRow.amount >= 0 ? 'font-semibold text-emerald-600' : 'font-semibold text-rose-600'
          },
          formatCurrency(currentRow.amount)
        )
      }

      const signedAmount = currentRow.type === 'EXPENSE' ? -currentRow.amount : currentRow.amount

      return h(
        'span',
        {
          class: signedAmount >= 0 ? 'font-semibold text-emerald-600' : 'font-semibold text-rose-600'
        },
        formatCurrency(signedAmount)
      )
    },
    footer: () =>
      h(
        'span',
        {
          class: summary.value.balance >= 0 ? 'font-semibold text-emerald-600' : 'font-semibold text-rose-600'
        },
        formatCurrency(summary.value.balance)
      ),
    meta: {
      class: {
        th: 'text-right',
        td: 'text-right'
      }
    }
  },
  {
    id: 'remark',
    accessorKey: 'remark',
    header: '备注',
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('span')
      }

      return h('span', { class: 'text-sm text-[var(--mobe-text-soft)]' }, currentRow.remark || '-')
    },
    footer: () => h('span')
  },
  {
    id: 'actions',
    header: '操作',
    enableSorting: false,
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('span')
      }

      return h('div', { class: 'flex justify-end gap-2' }, [
        h(
          UButton,
          {
            size: 'xs',
            color: 'neutral',
            variant: 'ghost',
            onClick: () => openEditBillSlideover(currentRow.id)
          },
          () => '编辑'
        ),
        h(
          UButton,
          {
            size: 'xs',
            color: 'error',
            variant: 'ghost',
            onClick: () => openDeleteConfirm(currentRow.id)
          },
          () => '删除'
        )
      ])
    },
    footer: () =>
      h(
        'div',
        {
          class: 'text-right font-medium whitespace-nowrap'
        },
        [
          h('span', { class: 'text-[var(--mobe-text)]' }, '收入 '),
          h('span', { class: 'text-emerald-600' }, formatCurrency(summary.value.income)),
          h('span', { class: 'text-[var(--mobe-text)]' }, ' / 支出 '),
          h('span', { class: 'text-rose-600' }, formatCurrency(-summary.value.expense))
        ]
      ),
    meta: {
      class: {
        th: 'text-right w-[220px]',
        td: 'text-right w-[220px]'
      }
    }
  }
])

const visibleColumnMenuItems = computed(() => {
  return columns.value
    .filter(column => !['select', 'actions'].includes(column.id || ''))
    .map(column => ({
      label:
        typeof column.header === 'string'
          ? column.header
          : column.id === 'recordDate'
            ? '日期'
            : column.id === 'type'
              ? '类型'
              : column.id === 'category'
                ? '分类'
                : column.id === 'amount'
                  ? '金额'
                  : column.id === 'remark'
                    ? '备注'
                    : column.id || '',
      type: 'checkbox' as const,
      checked: columnVisibility.value[column.id || ''] !== false,
      onUpdateChecked(checked: boolean) {
        columnVisibility.value = {
          ...columnVisibility.value,
          [column.id || '']: checked
        }
      }
    }))
})
async function confirmDeleteBill() {
  deletingBill.value = true

  try {
    if (!deletingBillId.value) {
      systemToast.error('删除失败', '未找到要删除的账单', 'bill-delete-not-found')
      return
    }

    await deleteRecordApi({ id: deletingBillId.value })

    closeDeleteConfirm()

    if (pagedTableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }

    await fetchBillRecords()
    systemToast.success('删除成功', '账单已删除', 'bill-delete-success')
  } catch (error: any) {
    systemToast.error('删除失败', error?.message || '请稍后重试', 'bill-delete-error')
  } finally {
    deletingBill.value = false
  }
}

onMounted(async () => {
  resetBillForm()
  await fetchBillRecords()
  expanded.value = {}
})
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
<div class="page-header__left page-header__left--hero">
  <div class="page-header__eyebrow">
    Finance
  </div>

  <div class="page-header__title-row">
    <h1 class="page-header__title">
      账单
    </h1>
  </div>

  <p class="page-header__desc">
    当知足凌驾于欲望之上，幸福将贯穿一生。
  </p>

  <div class="page-header__meta">
    <span class="page-header__meta-tag">收支记录</span>
    <span class="page-header__meta-tag">日期分组</span>
    <span class="page-header__meta-tag">支持导入</span>
  </div>
</div>

      <div class="page-header__right">
        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw" @click="fetchBillRecords(true)">
          刷新
        </UButton>

        <UButton color="neutral" variant="soft" icon="i-lucide-upload" @click="openImportOverlay">
          导入
        </UButton>

        <UButton icon="i-lucide-plus" @click="openCreateBillSlideover">
          新增账单
        </UButton>
      </div>
    </div>

    <div class="page-toolbar page-toolbar--main">
      <div class="page-toolbar__left page-toolbar__left--grow">
        <UInput v-model="filters.keyword" placeholder="模糊搜索类型、分类、金额、日期、备注" icon="i-lucide-search"
          class="toolbar-search toolbar-search--wide" />

        <USelect v-model="filters.type" :items="typeOptions" class="toolbar-select" />
      </div>

      <div class="page-toolbar__right page-toolbar__right--wrap">
        <div class="filter-quick-actions">
          <UButton v-for="option in quickDateOptions" :key="option.value"
            :color="filters.dateMode === 'QUICK' && filters.quickDate === option.value ? 'primary' : 'neutral'"
            :variant="filters.dateMode === 'QUICK' && filters.quickDate === option.value ? 'solid' : 'soft'" size="sm"
            @click="selectQuickDate(option.value)">
            {{ option.label }}
          </UButton>
        </div>

        <UPopover v-model:open="rangePopoverOpen">
          <UButton color="neutral" variant="outline" class="toolbar-date-trigger toolbar-date-trigger--range">
            <span class="toolbar-date-trigger__text">{{ formatRangeSummary(filters.customDateRange) }}</span>
            <UIcon name="i-lucide-calendar" class="size-4" />
          </UButton>

          <template #content>
            <div class="toolbar-range-popover">
              <UCalendar v-model="rangeCalendarValue" range :number-of-months="2" :month-controls="true"
                :year-controls="true" />
            </div>
          </template>
        </UPopover>

        <UPopover v-model:open="monthPopoverOpen">
          <UButton color="neutral" variant="outline" class="toolbar-date-trigger toolbar-date-trigger--month">
            <span class="toolbar-date-trigger__text">{{ formatMonthSummary(filters.month) }}</span>
            <UIcon name="i-lucide-calendar-range" class="size-4" />
          </UButton>

          <template #content>
            <div class="month-picker-popover">
              <div class="month-picker-popover__header">
                <UButton color="neutral" variant="ghost" icon="i-lucide-chevron-left"
                  @click="previousMonthPickerYear" />
                <span class="month-picker-popover__year">{{ monthPickerYear }}</span>
                <UButton color="neutral" variant="ghost" icon="i-lucide-chevron-right" @click="nextMonthPickerYear" />
              </div>

              <div class="month-picker-grid">
                <button v-for="item in monthPickerItems" :key="`${item.value.year}-${item.value.month}`" type="button"
                  class="month-picker-grid__item" :class="{
                    'month-picker-grid__item--active':
                      filters.month &&
                      filters.month.year === item.value.year &&
                      filters.month.month === item.value.month
                  }" @click="selectMonthFilter(item.value)">
                  {{ item.label }}
                </button>
              </div>
            </div>
          </template>
        </UPopover>

        <UButton color="neutral" variant="ghost" icon="i-lucide-eraser" @click="clearAllFilters">
          清空筛选
        </UButton>

        <UDropdownMenu :items="[visibleColumnMenuItems]">
          <UButton color="neutral" variant="soft" icon="i-lucide-columns-3">
            显示列
          </UButton>
        </UDropdownMenu>
      </div>
    </div>

    <div class="mobe-stats">
      <div class="mobe-stat">
        <span class="mobe-stat__label">收入</span>
        <span class="mobe-stat__value mobe-stat__value--success">{{ formatCurrency(summary.income) }}</span>
      </div>
      <div class="mobe-stat">
        <span class="mobe-stat__label">支出</span>
        <span class="mobe-stat__value mobe-stat__value--danger">{{ formatCurrency(-summary.expense) }}</span>
      </div>
      <div class="mobe-stat">
        <span class="mobe-stat__label">结余</span>
        <span class="mobe-stat__value"
          :class="summary.balance >= 0 ? 'mobe-stat__value--success' : 'mobe-stat__value--danger'">
          {{ formatCurrency(summary.balance) }}
        </span>
      </div>
      <div class="mobe-stat">
        <span class="mobe-stat__label">记录数</span>
        <span class="mobe-stat__value">{{ summary.count }}</span>
      </div>
    </div>

    <div class="page-section bills-table-wrap">
      <UTable v-model:expanded="expanded" v-model:row-selection="rowSelection" v-model:sorting="sorting"
        v-model:column-visibility="columnVisibility" :data="pagedTableData" :columns="columns"
        :get-sub-rows="(row) => row.kind === 'group' ? row.children : undefined" :loading="loading"
        loading-color="primary" loading-animation="carousel" sticky class="bills-table" :ui="{
          base: 'border-separate border-spacing-0',
          thead: 'bg-[var(--mobe-table-surface-soft)]',
          tbody: '[&>tr]:last:[&>td]:border-b-0',
          tr: 'group',
          th: 'bg-[var(--mobe-table-surface-soft)]',
          td: 'align-middle empty:p-0 group-has-[td:not(:empty)]:border-b border-[var(--mobe-divider)]'
        }" />
    </div>

    <div class="bills-pagination-bar">
      <div class="bills-pagination-bar__meta">
        共 {{ totalGroupCount }} 个日期分组，当前第 {{ pagination.page }} / {{ totalPageCount }} 页
      </div>

      <UPagination v-model:page="pagination.page" :items-per-page="pagination.pageSize" :total="totalGroupCount" />
    </div>

    <USlideover v-model:open="createBillOpen" side="right" :ui="{
      content: 'w-full max-w-[520px]'
    }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">
                Finance
              </div>
              <h2 class="bill-create-panel__title">
                新增账单
              </h2>
              <p class="bill-create-panel__desc">
                记录一笔实际发生的收入或支出，时间以你选择的日期和时刻为准。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeCreateBillSlideover" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-grid">
              <div class="bill-form-field">
                <label class="bill-form-field__label">日期</label>

                <UPopover v-model:open="datePickerOpen">
                  <UButton color="neutral" variant="outline" size="lg" class="bill-picker-trigger">
                    <span>{{ formatDateDisplay(billForm.date) }}</span>
                    <UIcon name="i-lucide-calendar" class="size-4" />
                  </UButton>

                  <template #content>
                    <div class="bill-date-popover">
                      <UCalendar v-model="calendarDateValue" :month-controls="true" :year-controls="true" />
                    </div>
                  </template>
                </UPopover>

                <div v-if="formErrors.date" class="bill-form-field__error">
                  {{ formErrors.date }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">时间</label>

                <UPopover v-model:open="timePickerOpen">
                  <UButton color="neutral" variant="outline" size="lg" class="bill-picker-trigger">
                    <span>{{ formatTimeDisplay(billForm.time) }}</span>
                    <UIcon name="i-lucide-clock-3" class="size-4" />
                  </UButton>

                  <template #content>
                    <div class="bill-time-popover">
                      <div class="bill-time-columns">
                        <div class="bill-time-column">
                          <div class="bill-time-column__title">
                            小时
                          </div>
                          <div ref="hourListRef" class="bill-time-list">
                            <button v-for="hour in hours" :key="hour" :ref="(el) => setActiveHourItemRef(el, hour)"
                              type="button" class="bill-time-item"
                              :class="{ 'bill-time-item--active': selectedHour === hour }" @click="selectHour(hour)">
                              {{ hour }}
                            </button>
                          </div>
                        </div>

                        <div class="bill-time-column">
                          <div class="bill-time-column__title">
                            分钟
                          </div>
                          <div ref="minuteListRef" class="bill-time-list">
                            <button v-for="minute in minutes" :key="minute"
                              :ref="(el) => setActiveMinuteItemRef(el, minute)" type="button" class="bill-time-item"
                              :class="{ 'bill-time-item--active': selectedMinute === minute }"
                              @click="selectMinute(minute)">
                              {{ minute }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </template>
                </UPopover>

                <div v-if="formErrors.time" class="bill-form-field__error">
                  {{ formErrors.time }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">类型</label>
                <USelect v-model="billForm.type" :items="billTypeOptions" size="lg" />
                <div v-if="formErrors.type" class="bill-form-field__error">
                  {{ formErrors.type }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">分类</label>

                <USelectMenu v-model="billForm.category" v-model:search-term="categorySearchTerm"
                  :items="categorySelectMenuItems" value-key="value" label-key="label" searchable size="lg"
                  placeholder="请选择或搜索分类">
                  <template #empty>
                    <div class="bill-selectmenu-empty">
                      没有匹配结果
                    </div>
                  </template>
                </USelectMenu>

                <div class="bill-form-field__hint">
                  搜索不存在的分类时，可直接回车添加。
                </div>

                <div v-if="formErrors.category" class="bill-form-field__error">
                  {{ formErrors.category }}
                </div>
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">金额</label>
              <div class="bill-amount-input">
                <span class="bill-amount-input__prefix">¥</span>
                <UInput :model-value="billForm.amountInput" placeholder="0.00" size="lg" inputmode="decimal"
                  class="bill-amount-input__control"
                  @update:model-value="billForm.amountInput = normalizeAmountInput(String($event || ''))" />
              </div>
              <div class="bill-form-field__hint">
                当前金额：{{ amountPreviewText }}
              </div>
              <div v-if="formErrors.amountInput" class="bill-form-field__error">
                {{ formErrors.amountInput }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="billForm.remark" :rows="4" placeholder="补充备注信息（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeCreateBillSlideover">
              取消
            </UButton>

            <UButton icon="i-lucide-save" :loading="creatingBill" @click="submitCreateBill">
              保存
            </UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <USlideover v-model:open="editBillOpen" side="right" :ui="{
      content: 'w-full max-w-[520px]'
    }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">
                Finance
              </div>
              <h2 class="bill-create-panel__title">
                编辑账单
              </h2>
              <p class="bill-create-panel__desc">
                修改这笔账单的日期、时间、类型、分类、金额和备注。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeEditBillSlideover" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-grid">
              <div class="bill-form-field">
                <label class="bill-form-field__label">日期</label>
                <UInput v-model="editBillForm.date" type="date" size="lg" />
                <div v-if="editFormErrors.date" class="bill-form-field__error">
                  {{ editFormErrors.date }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">时间</label>
                <UInput v-model="editBillForm.time" type="time" size="lg" />
                <div v-if="editFormErrors.time" class="bill-form-field__error">
                  {{ editFormErrors.time }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">类型</label>
                <USelect v-model="editBillForm.type" :items="billTypeOptions" size="lg" />
                <div v-if="editFormErrors.type" class="bill-form-field__error">
                  {{ editFormErrors.type }}
                </div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">分类</label>
                <USelectMenu v-model="editBillForm.category" v-model:search-term="editCategorySearchTerm"
                  :items="editCategorySelectMenuItems" value-key="value" label-key="label" searchable size="lg"
                  placeholder="请选择或搜索分类">
                  <template #empty>
                    <div class="bill-selectmenu-empty">
                      没有匹配结果
                    </div>
                  </template>
                </USelectMenu>
                <div class="bill-form-field__hint">
                  搜索不存在的分类时，可直接回车添加。
                </div>
                <div v-if="editFormErrors.category" class="bill-form-field__error">
                  {{ editFormErrors.category }}
                </div>
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">金额</label>
              <div class="bill-amount-input">
                <span class="bill-amount-input__prefix">¥</span>
                <UInput :model-value="editBillForm.amountInput" placeholder="0.00" size="lg" inputmode="decimal"
                  class="bill-amount-input__control"
                  @update:model-value="editBillForm.amountInput = normalizeAmountInput(String($event || ''))" />
              </div>
              <div v-if="editFormErrors.amountInput" class="bill-form-field__error">
                {{ editFormErrors.amountInput }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="editBillForm.remark" :rows="4" placeholder="补充备注信息（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeEditBillSlideover">
              取消
            </UButton>

            <UButton icon="i-lucide-save" :loading="editingBill" @click="submitEditBill">
              保存修改
            </UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <input ref="fileInputRef" type="file" accept=".csv" class="bill-import-input" @change="handleImportFileSelect">

    <UModal v-model:open="importOverlayOpen" :ui="{
      content: 'max-w-[560px]'
    }">
      <template #content>
        <div class="bill-import-modal">
          <div class="bill-import-modal__header">
            <div>
              <div class="bill-import-modal__eyebrow">
                Finance
              </div>
              <h2 class="bill-import-modal__title">
                导入账单
              </h2>
              <p class="bill-import-modal__desc">
                支持拖拽或选择 CSV 文件导入账单数据，也可以先下载模板填写后再导入。
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeImportOverlay" />
          </div>

          <div class="bill-import-modal__body">
            <div class="bill-import-dropzone" :class="{ 'bill-import-dropzone--active': dragOver }"
              @dragover="handleDragOver" @dragleave="handleDragLeave" @drop="handleDrop" @click="openImportFilePicker">
              <UIcon name="i-lucide-file-up" class="bill-import-dropzone__icon" />
              <div class="bill-import-dropzone__title">
                拖动文件到这里，或点击选择文件
              </div>
              <div class="bill-import-dropzone__desc">
                当前仅支持 .csv 文件
              </div>

              <div v-if="selectedImportFile" class="bill-import-dropzone__file">
                已选择：{{ selectedImportFile.name }}
              </div>
            </div>

            <div class="bill-import-template">
              <div class="bill-import-template__text">
                没有模板？先下载模板再填写会更稳。
              </div>

              <UButton color="neutral" variant="soft" icon="i-lucide-download" @click="downloadImportTemplate">
                下载模板
              </UButton>
            </div>
          </div>

          <div class="bill-import-modal__footer">
            <UButton color="neutral" variant="soft" @click="closeImportOverlay">
              取消
            </UButton>

            <UButton icon="i-lucide-upload" :loading="importingBills" @click="submitImportBills">
              开始导入
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
    <UModal v-model:open="deleteConfirmOpen" :ui="{
      content: 'max-w-[420px]'
    }">
      <template #content>
        <div class="bill-import-modal">
          <div class="bill-import-modal__header">
            <div>
              <div class="bill-import-modal__eyebrow">
                Finance
              </div>
              <h2 class="bill-import-modal__title">
                删除账单
              </h2>
              <p class="bill-import-modal__desc">
                删除后将无法恢复，确认删除这笔账单吗？
              </p>
            </div>

            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeDeleteConfirm" />
          </div>

          <div class="bill-import-modal__footer">
            <UButton color="neutral" variant="soft" @click="closeDeleteConfirm">
              取消
            </UButton>

            <UButton color="error" icon="i-lucide-trash-2" :loading="deletingBill" @click="confirmDeleteBill">
              确认删除
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>

<style scoped>
.bill-import-input {
  display: none;
}

.toolbar-search {
  width: 260px;
}

.toolbar-search--wide {
  width: 320px;
}

.toolbar-select {
  width: 140px;
}

.page-toolbar--main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: nowrap;
  width: 100%;
  overflow-x: auto;
  padding-bottom: 8px;
}

.page-toolbar__left--grow {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  gap: 12px;
  align-items: center;
}

.page-toolbar__right--wrap {
  display: flex;
  flex: 0 0 auto;
  min-width: 0;
  flex-wrap: nowrap;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;
}

.filter-quick-actions {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
}

.toolbar-date-trigger {
  min-width: 190px;
  justify-content: space-between;
  border-radius: 10px;
}

.toolbar-date-trigger--range {
  min-width: 260px;
}

.toolbar-date-trigger--month {
  min-width: 160px;
}

.toolbar-date-trigger__text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-range-popover {
  padding: 8px;
}

.month-picker-popover {
  width: 280px;
  padding: 12px;
}

.month-picker-popover__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.month-picker-popover__year {
  font-size: 14px;
  font-weight: 700;
  color: var(--mobe-text);
}

.month-picker-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.month-picker-grid__item {
  height: 38px;
  border: 1px solid var(--mobe-border-soft);
  border-radius: 10px;
  background: var(--mobe-surface, #fff);
  color: var(--mobe-text);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}

.month-picker-grid__item:hover {
  border-color: var(--mobe-border);
  background: var(--mobe-surface-soft, #f6f7f8);
}

.month-picker-grid__item--active {
  border-color: var(--ui-primary, #10b981);
  background: color-mix(in srgb, var(--ui-primary, #10b981) 8%, white);
  color: var(--ui-primary, #10b981);
}

.bills-table-wrap {
  overflow: auto;
  border: 1px solid var(--mobe-border-soft);
  border-radius: var(--mobe-radius-md);
  background: var(--mobe-table-surface-soft);
  box-shadow: none;
}

.bills-table {
  min-width: 1080px;
}

.bills-pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
}

.bills-pagination-bar__meta {
  font-size: 13px;
  color: var(--mobe-text-soft);
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

.bill-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 16px;
  margin-bottom: 18px;
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

.bill-form-field__hint {
  font-size: 12px;
  color: var(--mobe-text-soft);
}

.bill-form-field__error {
  font-size: 12px;
  color: #e11d48;
}

.bill-amount-input {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  border: 1px solid var(--mobe-border-soft);
  border-radius: 10px;
  overflow: hidden;
  background: var(--mobe-surface, #fff);
}

.bill-amount-input__prefix {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 42px;
  font-size: 16px;
  font-weight: 700;
  color: var(--mobe-text);
  background: var(--mobe-surface-soft, #f6f7f8);
  border-right: 1px solid var(--mobe-border-soft);
}

.bill-amount-input__control :deep(input) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

.bill-picker-trigger {
  width: 100%;
  min-height: 44px;
  justify-content: space-between;
  border-radius: 10px;
}

.bill-date-popover {
  padding: 8px;
}

.bill-time-popover {
  width: 225px;
  padding: 8px 8px 10px;
}

.bill-time-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.bill-time-column {
  min-width: 0;
}

.bill-time-column__title {
  margin-bottom: 8px;
  padding: 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--mobe-text-soft);
}

.bill-time-list {
  max-height: 220px;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 0;
  padding-right: 2px;
  scroll-behavior: auto;
}

.bill-time-item {
  height: 54px;
  border: none;
  border-radius: 5px;
  background: transparent;
  color: var(--mobe-text);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  width: 48px;
  align-self: center;
  transition: background-color 0.16s ease, color 0.16s ease;
}

.bill-time-item:hover {
  background: #f1f1f3;
  color: var(--mobe-text);
}

.bill-time-item--active {
  background: #f1f1f3;
  color: var(--mobe-text);
  font-weight: 600;
}

.bill-time-list::-webkit-scrollbar {
  width: 6px;
}

.bill-time-list::-webkit-scrollbar-track {
  background: transparent;
}

.bill-time-list::-webkit-scrollbar-thumb {
  background: #c9c9ce;
  border-radius: 999px;
}

.bill-selectmenu-empty {
  padding: 10px 12px;
  font-size: 13px;
  color: var(--mobe-text-soft);
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

.bill-import-dropzone {
  border: 1.5px dashed var(--mobe-border-soft);
  border-radius: 14px;
  padding: 32px 20px;
  text-align: center;
  background: var(--mobe-surface-soft, #f7f8fa);
  cursor: pointer;
  transition: all 0.2s ease;
}

.bill-import-dropzone:hover,
.bill-import-dropzone--active {
  border-color: var(--ui-primary, #10b981);
  background: color-mix(in srgb, var(--ui-primary, #10b981) 6%, white);
}

.bill-import-dropzone__icon {
  width: 28px;
  height: 28px;
  margin-bottom: 12px;
  color: var(--mobe-text-soft);
}

.bill-import-dropzone__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--mobe-text);
}

.bill-import-dropzone__desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--mobe-text-soft);
}

.bill-import-dropzone__file {
  margin-top: 14px;
  font-size: 13px;
  color: var(--ui-primary, #10b981);
  word-break: break-all;
}

.bill-import-template {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  padding: 14px 16px;
  border: 1px solid var(--mobe-border-soft);
  border-radius: 12px;
  background: var(--mobe-surface, #fff);
}

.bill-import-template__text {
  font-size: 13px;
  color: var(--mobe-text-soft);
}

.bill-import-modal__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px 24px;
  border-top: 1px solid var(--mobe-divider);
}

.page-header__left--hero {
  padding: 4px 0;
}

.page-header__title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.page-header__badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-soft, #f7f8fa);
  font-size: 12px;
  font-weight: 600;
  color: var(--mobe-text-soft);
}

.page-header__desc {
  margin-top: 10px;
  max-width: 680px;
  color: var(--mobe-text-soft);
  line-height: 1.8;
}

.page-header__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.page-header__meta-tag {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--mobe-border-soft);
  background: var(--mobe-surface-soft, #f7f8fa);
  font-size: 12px;
  color: var(--mobe-text-soft);
}

@media (max-width: 1280px) {

  .page-toolbar,
  .page-toolbar__left,
  .page-toolbar__right {
    flex-wrap: wrap;
  }
}

@media (max-width: 1023px) {

  .toolbar-search,
  .toolbar-search--wide,
  .toolbar-select,
  .toolbar-date-trigger,
  .toolbar-date-trigger--range,
  .toolbar-date-trigger--month {
    width: 100%;
    min-width: 0;
  }

  .page-toolbar__right--wrap {
    justify-content: flex-start;
  }

  .bills-pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .bill-time-popover {
    width: min(248px, calc(100vw - 32px));
  }

  .bill-form-grid {
    grid-template-columns: 1fr;
  }

  .month-picker-popover {
    width: min(280px, calc(100vw - 32px));
  }
}
</style>