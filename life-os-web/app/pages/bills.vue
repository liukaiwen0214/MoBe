<script setup lang="ts">

import { computed, onMounted, reactive, ref, shallowRef, useTemplateRef, watch } from 'vue'
import { CalendarDate, Time, parseDate, parseTime } from '@internationalized/date'
import { createBillApi, pageBillsApi, deleteRecordApi, updateRecordApi, batchCreateBillsApi } from '~/api/bills'


definePageMeta({
  middleware: 'auth'
})

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
type BillGroup = {
  id: string
  day: string
  weekday: string
  count: number
  amount: number
  items: BillRecord[]
}
type BillColumnKey = 'remark'

const systemToast = useSystemToast()

const loading = ref(true)
const creatingBill = ref(false)
const editingBill = ref(false)
const deletingBill = ref(false)
const importingBills = ref(false)

const createBillOpen = ref(false)
const editBillOpen = ref(false)
const deleteConfirmOpen = ref(false)
const importOverlayOpen = ref(false)
const filterPanelOpen = ref(false)

const dragOver = ref(false)
const selectedImportFile = ref<File | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const deletingBillId = ref('')
const editingBillId = ref('')

const billRecords = ref<BillRecord[]>([])
const collapsedGroups = ref<Record<string, boolean>>({})

const columnVisibility = reactive<Record<BillColumnKey, boolean>>({
  remark: true
})

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
const rangePopoverOpen = ref(false)
const monthPopoverOpen = ref(false)

const categorySearchTerm = ref('')
const editCategorySearchTerm = ref('')

const billForm = reactive({
  date: '',
  time: '',
  type: 'EXPENSE' as BillType,
  category: '餐饮',
  amountInput: '',
  remark: ''
})

const editBillForm = reactive({
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

const editFormErrors = reactive({
  date: '',
  time: '',
  type: '',
  category: '',
  amountInput: ''
})

const rangeCalendarValue = ref<any>({
  start: undefined,
  end: undefined
})

const createDateInputRef = useTemplateRef('createDateInput')
const editDateInputRef = useTemplateRef('editDateInput')

const createDatePopoverOpen = ref(false)
const editDatePopoverOpen = ref(false)

watch(rangeCalendarValue, (newValue) => {
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

const activeFilterCount = computed(() => {
  let count = 0

  if (filters.keyword.trim()) count += 1
  if (filters.type !== 'ALL') count += 1
  if (filters.dateMode === 'QUICK' && filters.quickDate !== 'ALL') count += 1
  if (filters.dateMode === 'RANGE' && (filters.customDateRange.start || filters.customDateRange.end)) count += 1
  if (filters.dateMode === 'MONTH' && filters.month) count += 1

  return count
})

const hasActiveFilters = computed(() => activeFilterCount.value > 0)

const visibleColumnOptions = computed(() => [
  { key: 'remark', label: '备注' }
] as Array<{ key: BillColumnKey; label: string }>)

const billDateValue = computed({
  get() {
    return billForm.date ? parseDate(billForm.date) : undefined
  },
  set(value: CalendarDate | undefined) {
    billForm.date = value ? value.toString() : ''
  }
})

const editBillDateValue = computed({
  get() {
    return editBillForm.date ? parseDate(editBillForm.date) : undefined
  },
  set(value: CalendarDate | undefined) {
    editBillForm.date = value ? value.toString() : ''
  }
})

const billTimeValue = computed({
  get() {
    return billForm.time ? parseTime(billForm.time) : undefined
  },
  set(value: Time | undefined) {
    billForm.time = value
      ? `${pad2(value.hour)}:${pad2(value.minute)}`
      : ''
  }
})

const editBillTimeValue = computed({
  get() {
    return editBillForm.time ? parseTime(editBillForm.time) : undefined
  },
  set(value: Time | undefined) {
    editBillForm.time = value
      ? `${pad2(value.hour)}:${pad2(value.minute)}`
      : ''
  }
})

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

function formatDateTime(value: string) {
  const date = new Date(value)
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}`
}

function formatTimeOnly(value: string) {
  const date = new Date(value)
  return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`
}

function formatDay(value: string) {
  const date = new Date(value)
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`
}

function formatWeekday(value: string) {
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

function getTypeBadgeClass(type: BillType) {
  return type === 'INCOME'
    ? 'bill-type-badge bill-type-badge--income'
    : 'bill-type-badge bill-type-badge--expense'
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
const normalizedEditCategorySearchTerm = computed(() => editCategorySearchTerm.value.trim())

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
  () => billForm.category,
  value => {
    if (typeof value === 'string' && value.startsWith('__create__')) {
      addCategoryFromSearch(value.replace('__create__', ''))
    }
  }
)

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

    await updateRecordApi({ ...updated })

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

  if (!nextCategories.length) return

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

function parseCsvTextToBills(text: string): BillRecord[] {
  const lines = text
    .split(/\r?\n/)
    .map(line => line.trim())
    .filter(Boolean)

  if (lines.length <= 1) return []

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
  return {
    pageNum: 1,
    pageSize: 500,
    type: filters.type !== 'ALL' ? filters.type : undefined,
    keyword: filters.keyword.trim() || undefined,
    dateMode: filters.dateMode !== 'NONE' ? filters.dateMode : undefined,
    quickDate: filters.dateMode === 'QUICK' ? filters.quickDate : undefined,
    startDate:
      filters.dateMode === 'RANGE' && filters.customDateRange.start
        ? `${filters.customDateRange.start.toString()} 00:00:00`
        : undefined,
    endDate:
      filters.dateMode === 'RANGE' && filters.customDateRange.end
        ? `${filters.customDateRange.end.toString()} 23:59:59`
        : undefined,
    month:
      filters.dateMode === 'MONTH' && filters.month
        ? `${filters.month.year}-${pad2(filters.month.month)}`
        : undefined
  }
}

async function fetchBillRecords(showSuccess = false) {
  loading.value = true

  try {
    const request = buildBillPageRequest()
    const res: any = await pageBillsApi(request)
    const records = res?.data?.records || []

    billRecords.value = records
    mergeCategoriesFromBills(records)

    const days = Array.from(new Set(records.map((item: BillRecord) => formatDay(item.recordDate))))
    collapsedGroups.value = Object.fromEntries(days.map((day, index) => [day, index !== 0]))

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
  if (!keyword) return true
  return buildSearchableText(item).includes(keyword)
}

function matchesDateFilter(item: BillRecord) {
  const { dateMode, quickDate, customDateRange, month } = filters

  if (dateMode === 'NONE') return true

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
    if (customDateRange.start && recordDay < customDateRange.start.toString()) return false
    if (customDateRange.end && recordDay > customDateRange.end.toString()) return false
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

  rangeCalendarValue.value = {
    start: undefined,
    end: undefined
  }

  rangePopoverOpen.value = false
  monthPopoverOpen.value = false

  systemToast.success('已清空筛选', '所有筛选条件已重置', 'bill-clear-filters-success')
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

watch(
  () => ({
    keyword: filters.keyword,
    type: filters.type,
    quickDate: filters.quickDate,
    dateMode: filters.dateMode,
    rangeStart: filters.customDateRange.start?.toString() || '',
    rangeEnd: filters.customDateRange.end?.toString() || '',
    month: filters.month ? `${filters.month.year}-${filters.month.month}` : ''
  }),
  () => {
    pagination.page = 1
  },
  { deep: true }
)

const filteredLeafRows = computed<BillRecord[]>(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return billRecords.value.filter(item => {
    const matchType = filters.type === 'ALL' || item.type === filters.type
    const matchKeyword = matchesKeyword(item, keyword)
    const matchDate = matchesDateFilter(item)
    return matchType && matchKeyword && matchDate
  })
})

const groupedTableData = computed<BillGroup[]>(() => {
  const groupedMap = new Map<string, BillRecord[]>()

  for (const item of filteredLeafRows.value) {
    const day = formatDay(item.recordDate)
    if (!groupedMap.has(day)) {
      groupedMap.set(day, [])
    }
    groupedMap.get(day)?.push(item)
  }

  return Array.from(groupedMap.entries())
    .sort((a, b) => new Date(b[0]).getTime() - new Date(a[0]).getTime())
    .map(([day, items]) => {
      const amount = items.reduce((sum, item) => {
        return item.type === 'EXPENSE' ? sum - item.amount : sum + item.amount
      }, 0)

      return {
        id: day,
        day,
        weekday: formatWeekday(day),
        count: items.length,
        amount,
        items: [...items].sort((a, b) => new Date(b.recordDate).getTime() - new Date(a.recordDate).getTime())
      }
    })
})

const totalGroupCount = computed(() => groupedTableData.value.length)
const totalPageCount = computed(() => Math.max(1, Math.ceil(totalGroupCount.value / pagination.pageSize)))

watch(totalPageCount, value => {
  if (pagination.page > value) pagination.page = value
})

const pagedTableData = computed<BillGroup[]>(() => {
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

function toggleGroup(day: string) {
  collapsedGroups.value = {
    ...collapsedGroups.value,
    [day]: !collapsedGroups.value[day]
  }
}

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
})
</script>
<template>
  <div class="page-shell">
    <div class="bills-workspace">
      <div class="bills-workspace__title-wrap">
        <div class="bills-workspace__eyebrow">
          Finance
        </div>
        <div class="bills-workspace__title-row">
          <h1 class="bills-workspace__title">
            账单
          </h1>
          <span class="bills-workspace__desc">当知足凌驾于欲望之上，幸福将贯穿一生。</span>
        </div>
      </div>

      <div class="bills-workspace__actions">
        <UInput
  v-model="filters.keyword"
  icon="i-lucide-search"
  placeholder="搜索分类、金额、日期、备注"
  class="checklist-toolbar__search"
/>

        <UButton color="neutral" variant="soft" icon="i-lucide-refresh-cw" @click="fetchBillRecords(true)">
          刷新
        </UButton>

        <UButton color="neutral" variant="soft" icon="i-lucide-upload"  @click="openImportOverlay">
          导入
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

        <UButton icon="i-lucide-plus" @click="openCreateBillSlideover">
          新增账单
        </UButton>
      </div>
    </div>

    <div class="bills-main-panel page-section">
      <div class="bills-summary-bar">
        <div class="bills-summary-item">
          <span class="bills-summary-item__label">收入</span>
          <span class="bills-summary-item__value bills-summary-item__value--income">
            {{ formatCurrency(summary.income) }}
          </span>
        </div>
        <div class="bills-summary-item">
          <span class="bills-summary-item__label">支出</span>
          <span class="bills-summary-item__value bills-summary-item__value--expense">
            {{ formatCurrency(-summary.expense) }}
          </span>
        </div>
        <div class="bills-summary-item">
          <span class="bills-summary-item__label">结余</span>
          <span class="bills-summary-item__value"
            :class="summary.balance >= 0 ? 'bills-summary-item__value--income' : 'bills-summary-item__value--expense'">
            {{ formatCurrency(summary.balance) }}
          </span>
        </div>
        <div class="bills-summary-item">
          <span class="bills-summary-item__label">记录数</span>
          <span class="bills-summary-item__value">{{ summary.count }}</span>
        </div>
      </div>

      <div v-if="loading" class="bill-loading">
        <div class="bill-loading__ring" />
        <div class="bill-loading__text">
          正在加载账单
        </div>
      </div>

      <template v-else>
        <div v-if="!pagedTableData.length" class="bill-empty">
          <div class="bill-empty__title">
            暂无账单
          </div>
          <div class="bill-empty__desc">
            当前筛选条件下没有匹配结果，试试调整筛选或新增一条账单。
          </div>
        </div>

        <template v-else>
          <div class="bill-grid-shell">
            <div class="bill-grid-head">
              <div class="bill-col bill-col--time">时间</div>
              <div class="bill-col bill-col--type">类型</div>
              <div class="bill-col bill-col--category">分类</div>
              <div class="bill-col bill-col--amount">金额</div>
              <div v-if="columnVisibility.remark" class="bill-col bill-col--remark">备注</div>
              <div class="bill-col bill-col--operations">操作</div>
            </div>

            <div class="bill-groups">
              <section v-for="group in pagedTableData" :key="group.id" class="bill-group">
                <button type="button" class="bill-group__header" @click="toggleGroup(group.day)">
                  <div class="bill-group__left">
                    <UIcon name="i-lucide-chevron-down" class="size-4 bill-group__arrow"
                      :class="{ 'bill-group__arrow--collapsed': collapsedGroups[group.day] }" />
                    <span class="bill-group__title">{{ group.day }}</span>
                    <span class="bill-group__weekday">{{ group.weekday }}</span>
                    <span class="bill-group__count">{{ group.count }} 笔</span>
                  </div>

                  <div class="bill-group__amount"
                    :class="group.amount >= 0 ? 'bill-group__amount--income' : 'bill-group__amount--expense'">
                    {{ formatCurrency(group.amount) }}
                  </div>
                </button>

                <div v-show="!collapsedGroups[group.day]" class="bill-group__body">
                  <div v-for="item in group.items" :key="item.id" class="bill-row">
                    <div class="bill-col bill-col--time">
                      <div class="bill-time-wrap">
                        <div class="bill-time-wrap__main">{{ formatTimeOnly(item.recordDate) }}</div>
                        <div class="bill-time-wrap__sub">{{ formatDateTime(item.recordDate) }}</div>
                      </div>
                    </div>

                    <div class="bill-col bill-col--type">
                      <span :class="getTypeBadgeClass(item.type)">
                        {{ getTypeLabel(item.type) }}
                      </span>
                    </div>

                    <div class="bill-col bill-col--category">
                      <span class="bill-meta-text">{{ item.category }}</span>
                    </div>

                    <div class="bill-col bill-col--amount">
                      <span class="bill-amount-text"
                        :class="item.type === 'INCOME' ? 'bill-amount-text--income' : 'bill-amount-text--expense'">
                        {{ formatCurrency(item.type === 'EXPENSE' ? -item.amount : item.amount) }}
                      </span>
                    </div>

                    <div v-if="columnVisibility.remark" class="bill-col bill-col--remark">
                      <span class="bill-meta-text">{{ item.remark || '—' }}</span>
                    </div>

                    <div class="bill-col bill-col--operations">
                      <div class="bill-op-group">
                        <button type="button" class="bill-op-btn" @click="openEditBillSlideover(item.id)">
                          编辑
                        </button>
                        <button type="button" class="bill-op-btn bill-op-btn--danger"
                          @click="openDeleteConfirm(item.id)">
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

    <div class="bills-pagination-bar">
      <div class="bills-pagination-bar__meta">
        共 {{ totalGroupCount }} 个日期分组，当前第 {{ pagination.page }} / {{ totalPageCount }} 页
      </div>
      <UPagination v-model:page="pagination.page" :items-per-page="pagination.pageSize" :total="totalGroupCount" />
    </div>

    <USlideover v-model:open="createBillOpen" side="right" :ui="{ content: 'w-full max-w-[520px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">Finance</div>
              <h2 class="bill-create-panel__title">新增账单</h2>
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

  <UInputDate
    ref="createDateInput"
    v-model="billDateValue"
    size="lg"
    class="bill-native-control"
    :format-options="{
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }"
    locale="en-CA"
  >
    <template #trailing>
      <UPopover
        v-model:open="createDatePopoverOpen"
        :reference="createDateInputRef?.inputsRef?.[3]?.$el"
      >
        <UButton
          color="neutral"
          variant="link"
          size="sm"
          icon="i-lucide-calendar"
          aria-label="Select a date"
          class="bill-date-trigger-btn"
        />

        <template #content>
          <UCalendar
            v-model="billDateValue"
            class="bill-date-calendar"
            :month-controls="true"
            :year-controls="true"
            @update:model-value="createDatePopoverOpen = false"
          />
        </template>
      </UPopover>
    </template>
  </UInputDate>

  <div v-if="formErrors.date" class="bill-form-field__error">{{ formErrors.date }}</div>
</div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">时间</label>
                <UInputTime v-model="billTimeValue" size="lg" class="bill-native-control" locale="en-GB" :hour-cycle="24"
                  :format-options="{
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                  }" />
                <div v-if="formErrors.time" class="bill-form-field__error">{{ formErrors.time }}</div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">类型</label>
                <USelect v-model="billForm.type" :items="billTypeOptions" size="lg" />
                <div v-if="formErrors.type" class="bill-form-field__error">{{ formErrors.type }}</div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">分类</label>
                <USelectMenu v-model="billForm.category" v-model:search-term="categorySearchTerm"
                  :items="categorySelectMenuItems" value-key="value" label-key="label" searchable
                  placeholder="请选择或搜索分类">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>

                <div class="bill-form-field__hint">搜索不存在的分类时，可直接回车添加。</div>
                <div v-if="formErrors.category" class="bill-form-field__error">{{ formErrors.category }}</div>
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
              <div class="bill-form-field__hint">当前金额：{{ amountPreviewText }}</div>
              <div v-if="formErrors.amountInput" class="bill-form-field__error">{{ formErrors.amountInput }}</div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="billForm.remark" :rows="4" placeholder="补充备注信息（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeCreateBillSlideover">取消</UButton>
            <UButton icon="i-lucide-save" :loading="creatingBill" @click="submitCreateBill">保存</UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <USlideover v-model:open="filterPanelOpen" side="right" :ui="{ content: 'w-full max-w-[440px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">Finance</div>
              <h2 class="bill-create-panel__title">筛选账单</h2>
              <p class="bill-create-panel__desc">选择条件后立即生效，关闭面板不会清空当前筛选。</p>
            </div>
            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="filterPanelOpen = false" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-field">
              <label class="bill-form-field__label">关键词</label>
              <UInput v-model="filters.keyword" placeholder="模糊搜索类型、分类、金额、日期、备注" icon="i-lucide-search" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">类型</label>
              <USelect v-model="filters.type" :items="typeOptions" />
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">快捷时间</label>
              <div class="filter-quick-actions filter-quick-actions--panel">
                <UButton v-for="option in quickDateOptions" :key="option.value"
                  :color="filters.dateMode === 'QUICK' && filters.quickDate === option.value ? 'primary' : 'neutral'"
                  :variant="filters.dateMode === 'QUICK' && filters.quickDate === option.value ? 'solid' : 'soft'"
                  size="sm" @click="selectQuickDate(option.value)">
                  {{ option.label }}
                </UButton>
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">自定义时间</label>
              <UPopover v-model:open="rangePopoverOpen">
                <UButton color="neutral" variant="outline"
                  class="toolbar-date-trigger toolbar-date-trigger--range toolbar-date-trigger--panel">
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
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">按月份</label>
              <UPopover v-model:open="monthPopoverOpen">
                <UButton color="neutral" variant="outline"
                  class="toolbar-date-trigger toolbar-date-trigger--month toolbar-date-trigger--panel">
                  <span class="toolbar-date-trigger__text">{{ formatMonthSummary(filters.month) }}</span>
                  <UIcon name="i-lucide-calendar-range" class="size-4" />
                </UButton>
                <template #content>
                  <div class="month-picker-popover">
                    <div class="month-picker-popover__header">
                      <UButton color="neutral" variant="ghost" icon="i-lucide-chevron-left"
                        @click="previousMonthPickerYear" />
                      <span class="month-picker-popover__year">{{ monthPickerYear }}</span>
                      <UButton color="neutral" variant="ghost" icon="i-lucide-chevron-right"
                        @click="nextMonthPickerYear" />
                    </div>

                    <div class="month-picker-grid">
                      <button v-for="item in monthPickerItems" :key="`${item.value.year}-${item.value.month}`"
                        type="button" class="month-picker-grid__item" :class="{
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
            <UButton color="neutral" variant="soft" @click="filterPanelOpen = false">关闭</UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <USlideover v-model:open="editBillOpen" side="right" :ui="{ content: 'w-full max-w-[520px]' }">
      <template #content>
        <div class="bill-create-panel">
          <div class="bill-create-panel__header">
            <div>
              <div class="bill-create-panel__eyebrow">Finance</div>
              <h2 class="bill-create-panel__title">编辑账单</h2>
              <p class="bill-create-panel__desc">修改这笔账单的日期、时间、类型、分类、金额和备注。</p>
            </div>
            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeEditBillSlideover" />
          </div>

          <div class="bill-create-panel__body">
            <div class="bill-form-grid">
              <div class="bill-form-field">
  <label class="bill-form-field__label">日期</label>

  <UInputDate
    ref="editDateInput"
    v-model="editBillDateValue"
    size="lg"
    class="bill-native-control"
    :format-options="{
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }"
    locale="en-CA"
  >
    <template #trailing>
      <UPopover
        v-model:open="editDatePopoverOpen"
        :reference="editDateInputRef?.inputsRef?.[3]?.$el"
      >
        <UButton
          color="neutral"
          variant="link"
          size="sm"
          icon="i-lucide-calendar"
          aria-label="Select a date"
          class="bill-date-trigger-btn"
        />

        <template #content>
          <UCalendar
            v-model="editBillDateValue"
            class="bill-date-calendar"
            :month-controls="true"
            :year-controls="true"
            @update:model-value="editDatePopoverOpen = false"
          />
        </template>
      </UPopover>
    </template>
  </UInputDate>

  <div v-if="editFormErrors.date" class="bill-form-field__error">{{ editFormErrors.date }}</div>
</div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">时间</label>
                <UInputTime v-model="editBillTimeValue" size="lg" class="bill-native-control" locale="en-GB"
                  :hour-cycle="24" :format-options="{
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                  }" />
                <div v-if="editFormErrors.time" class="bill-form-field__error">{{ editFormErrors.time }}</div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">类型</label>
                <USelect v-model="editBillForm.type" :items="billTypeOptions" size="lg" />
                <div v-if="editFormErrors.type" class="bill-form-field__error">{{ editFormErrors.type }}</div>
              </div>

              <div class="bill-form-field">
                <label class="bill-form-field__label">分类</label>
                <USelectMenu v-model="editBillForm.category" v-model:search-term="editCategorySearchTerm"
                  :items="editCategorySelectMenuItems" value-key="value" label-key="label" searchable size="lg"
                  placeholder="请选择或搜索分类">
                  <template #empty>
                    <div class="bill-selectmenu-empty">没有匹配结果</div>
                  </template>
                </USelectMenu>

                <div class="bill-form-field__hint">搜索不存在的分类时，可直接回车添加。</div>
                <div v-if="editFormErrors.category" class="bill-form-field__error">{{ editFormErrors.category }}</div>
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
              <div v-if="editFormErrors.amountInput" class="bill-form-field__error">{{ editFormErrors.amountInput }}
              </div>
            </div>

            <div class="bill-form-field">
              <label class="bill-form-field__label">备注</label>
              <UTextarea v-model="editBillForm.remark" :rows="4" placeholder="补充备注信息（可选）" autoresize />
            </div>
          </div>

          <div class="bill-create-panel__footer">
            <UButton color="neutral" variant="soft" @click="closeEditBillSlideover">取消</UButton>
            <UButton icon="i-lucide-save" :loading="editingBill" @click="submitEditBill">保存修改</UButton>
          </div>
        </div>
      </template>
    </USlideover>

    <input ref="fileInputRef" type="file" accept=".csv" class="bill-import-input" @change="handleImportFileSelect">

    <UModal v-model:open="importOverlayOpen" :ui="{ content: 'max-w-[560px]' }">
      <template #content>
        <div class="bill-import-modal">
          <div class="bill-import-modal__header">
            <div>
              <div class="bill-import-modal__eyebrow">Finance</div>
              <h2 class="bill-import-modal__title">导入账单</h2>
              <p class="bill-import-modal__desc">支持拖拽或选择 CSV 文件导入账单数据，也可以先下载模板填写后再导入。</p>
            </div>
            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeImportOverlay" />
          </div>

          <div class="bill-import-modal__body">
            <div class="bill-import-dropzone" :class="{ 'bill-import-dropzone--active': dragOver }"
              @dragover="handleDragOver" @dragleave="handleDragLeave" @drop="handleDrop" @click="openImportFilePicker">
              <UIcon name="i-lucide-file-up" class="bill-import-dropzone__icon" />
              <div class="bill-import-dropzone__title">拖动文件到这里，或点击选择文件</div>
              <div class="bill-import-dropzone__desc">当前仅支持 .csv 文件</div>

              <div v-if="selectedImportFile" class="bill-import-dropzone__file">
                已选择：{{ selectedImportFile.name }}
              </div>
            </div>

            <div class="bill-import-template">
              <div class="bill-import-template__text">没有模板？先下载模板再填写会更稳。</div>
              <UButton color="neutral" variant="soft" icon="i-lucide-download" @click="downloadImportTemplate">
                下载模板
              </UButton>
            </div>
          </div>

          <div class="bill-import-modal__footer">
            <UButton color="neutral" variant="soft" @click="closeImportOverlay">取消</UButton>
            <UButton icon="i-lucide-upload" :loading="importingBills" @click="submitImportBills">开始导入</UButton>
          </div>
        </div>
      </template>
    </UModal>

    <UModal v-model:open="deleteConfirmOpen" :ui="{ content: 'max-w-[420px]' }">
      <template #content>
        <div class="bill-import-modal">
          <div class="bill-import-modal__header">
            <div>
              <div class="bill-import-modal__eyebrow">Finance</div>
              <h2 class="bill-import-modal__title">删除账单</h2>
              <p class="bill-import-modal__desc">删除后将无法恢复，确认删除这笔账单吗？</p>
            </div>
            <UButton color="neutral" variant="ghost" icon="i-lucide-x" @click="closeDeleteConfirm" />
          </div>

          <div class="bill-import-modal__footer">
            <UButton color="neutral" variant="soft" @click="closeDeleteConfirm">取消</UButton>
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

.bills-main-panel {
  padding: 0;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--mobe-border-soft) 72%, transparent);
  border-radius: 12px;
  background: color-mix(in srgb, var(--mobe-surface, #fff) 96%, var(--mobe-surface-soft, #f7f8fa));
}

.bills-summary-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  padding: 14px 16px 12px;
  border-bottom: 1px solid color-mix(in srgb, var(--mobe-divider) 64%, transparent);
}

.bills-summary-item {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}

.bills-summary-item__label {
  font-size: 13px;
  color: var(--mobe-text-soft);
}

.bills-summary-item__value {
  font-size: 14px;
  font-weight: 700;
  color: var(--mobe-text);
}

.bills-summary-item__value--income {
  color: #059669;
}

.bills-summary-item__value--expense {
  color: #dc2626;
}

.bill-loading,
.bill-empty {
  display: flex;
  min-height: 320px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  text-align: center;
  padding: 24px;
}

.bill-loading__ring {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 2px solid rgba(145, 132, 126, 0.16);
  border-top-color: #8c8791;
  animation: bill-spin 0.85s linear infinite;
}

@keyframes bill-spin {
  to {
    transform: rotate(360deg);
  }
}

.bill-loading__text,
.bill-empty__desc {
  font-size: 13px;
  color: var(--mobe-text-soft);
}

.bill-empty__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--mobe-text);
}

.bill-grid-shell {
  overflow-x: auto;
}

.bill-grid-head,
.bill-row {
  display: grid;
  grid-template-columns:
    minmax(140px, 1.1fr) minmax(90px, 0.7fr) minmax(120px, 0.9fr) minmax(120px, 0.9fr) minmax(180px, 1.4fr) minmax(120px, 0.9fr);
  align-items: center;
  gap: 12px;
}

.bill-grid-head {
  min-height: 40px;
  padding: 8px 22px 6px;
  font-size: 12px;
  color: var(--mobe-text-soft);
  border-bottom: 1px solid color-mix(in srgb, var(--mobe-divider) 64%, transparent);
  min-width: 1000px;
}

.bill-groups {
  padding: 6px 0 12px;
  min-width: 1000px;
}

.bill-group+.bill-group {
  margin-top: 8px;
}

.bill-group__header {
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

.bill-group__left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.bill-group__arrow {
  color: var(--mobe-text-soft);
  transition: transform 0.18s ease;
}

.bill-group__arrow--collapsed {
  transform: rotate(-90deg);
}

.bill-group__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--mobe-text);
}

.bill-group__weekday,
.bill-group__count {
  font-size: 12px;
  color: var(--mobe-text-soft);
}

.bill-group__amount {
  font-size: 14px;
  font-weight: 700;
}

.bill-group__amount--income {
  color: #059669;
}

.bill-group__amount--expense {
  color: #dc2626;
}

.bill-group__body {
  padding: 0 12px;
}

.bill-row {
  min-height: 62px;
  padding: 0 10px;
  border-top: 1px solid color-mix(in srgb, var(--mobe-divider) 58%, transparent);
  transition: background-color 0.18s ease;
}

.bill-row:hover {
  background: color-mix(in srgb, var(--mobe-surface-soft, #f7f8fa) 72%, transparent);
}

.bill-col {
  min-width: 0;
}

.bill-time-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bill-time-wrap__main {
  font-size: 14px;
  font-weight: 600;
  color: var(--mobe-text);
}

.bill-time-wrap__sub,
.bill-meta-text {
  font-size: 12px;
  color: var(--mobe-text-soft);
}

.bill-type-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.bill-type-badge--income {
  background: color-mix(in srgb, #10b981 12%, white);
  color: #059669;
}

.bill-type-badge--expense {
  background: color-mix(in srgb, #ef4444 10%, white);
  color: #dc2626;
}

.bill-amount-text {
  font-size: 14px;
  font-weight: 700;
}

.bill-amount-text--income {
  color: #059669;
}

.bill-amount-text--expense {
  color: #dc2626;
}

.bill-op-group {
  display: inline-flex;
  gap: 10px;
  align-items: center;
}

.bill-op-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: var(--mobe-text-soft);
  cursor: pointer;
}

.bill-op-btn:hover {
  color: var(--mobe-text);
}

.bill-op-btn--danger:hover {
  color: #dc2626;
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

.bill-native-control {
  width: 100%;
}

.bill-native-control :deep(input) {
  min-height: 44px;
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
  border-color: var(--mobe-primary, #10b981);
  background: color-mix(in srgb, var(--mobe-primary, #10b981) 6%, white);
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
  color: var(--mobe-primary, #10b981);
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

.filter-quick-actions {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
}

.filter-quick-actions--panel {
  flex-wrap: wrap;
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

.toolbar-date-trigger--panel {
  width: 100%;
  min-width: 0;
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
  border-color: var(--mobe-primary, #10b981);
  background: color-mix(in srgb, var(--mobe-primary, #10b981) 8%, white);
  color: var(--mobe-text, #10b981);
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

  .bills-summary-bar {
    gap: 14px;
  }

  .bill-form-grid {
    grid-template-columns: 1fr;
  }

  .bill-column-list {
    grid-template-columns: 1fr;
  }

  .bills-pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .month-picker-popover {
    width: min(280px, calc(100vw - 32px));
  }
}
</style>
