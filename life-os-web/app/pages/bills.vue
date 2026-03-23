<script setup lang="ts">
import { computed, h, onMounted, ref, resolveComponent } from 'vue'
import type { TableColumn } from '@nuxt/ui'
import type { ExpandedState, RowSelectionState, SortingState, VisibilityState } from '@tanstack/vue-table'

definePageMeta({
  middleware: 'auth'
})

// 选择器常量定义
const SELECTORS = {
  GROUP_DATE_BTN: 'group-date-btn',
  GROUP_DATE_CONTENT: 'group-date-content',
  GROUP_DATE_ARROW_WRAP: 'group-date-arrow-wrap',
  GROUP_DATE_ARROW: 'group-date-arrow',
  GROUP_DATE_TEXT: 'group-date-text',
  GROUP_DATE_SEP: 'group-date-sep',
  GROUP_DATE_WEEKDAY: 'group-date-weekday',
  GROUP_DATE_COUNT: 'group-date-count'
}

type BillType = 'INCOME' | 'EXPENSE'

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
const UIcon = resolveComponent('UIcon')

const loading = ref(true)
const billRecords = ref<BillRecord[]>([])

const searchKeyword = ref('')
const typeFilter = ref<'ALL' | BillType>('ALL')

const sorting = ref<SortingState>([
  { id: 'recordDate', desc: true }
])

const rowSelection = ref<RowSelectionState>({})
const columnVisibility = ref<VisibilityState>({
  remark: true
})
const expanded = ref<ExpandedState>({})

const typeOptions = [
  { label: '全部类型', value: 'ALL' },
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]

function isBillLeafRow(row: BillTableRow): row is BillLeafRow {
  return row.kind === 'item'
}

function formatDateTime(value: string) {
  const date = new Date(value)
  const yyyy = date.getFullYear()
  const mm = `${date.getMonth() + 1}`.padStart(2, '0')
  const dd = `${date.getDate()}`.padStart(2, '0')
  const hh = `${date.getHours()}`.padStart(2, '0')
  const mi = `${date.getMinutes()}`.padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

function formatDay(value: string) {
  const date = new Date(value)
  const yyyy = date.getFullYear()
  const mm = `${date.getMonth() + 1}`.padStart(2, '0')
  const dd = `${date.getDate()}`.padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function formatWeekday(value: string): string {
  const date = new Date(value)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const dayIndex = date.getDay()
  return weekdays[dayIndex] || '周日'
}

function formatCurrency(amount: number) {
  const sign = amount >= 0 ? '' : '-'
  return `${sign}¥${Math.abs(amount).toFixed(2)}`
}

function getTypeLabel(type: BillType) {
  return type === 'INCOME' ? '收入' : '支出'
}

function buildMockData(): BillRecord[] {
  return [
    { id: '1', type: 'EXPENSE', category: '餐饮', amount: 28, recordDate: '2026-03-22T12:15:00', remark: '午餐' },
    { id: '2', type: 'EXPENSE', category: '交通', amount: 16.5, recordDate: '2026-03-22T08:42:00', remark: '打车' },
    { id: '3', type: 'EXPENSE', category: '饮品', amount: 18, recordDate: '2026-03-22T10:10:00', remark: '咖啡' },
    { id: '4', type: 'INCOME', category: '工资', amount: 5200, recordDate: '2026-03-21T09:00:00', remark: '工资到账' },
    { id: '5', type: 'EXPENSE', category: '购物', amount: 129, recordDate: '2026-03-21T20:13:00', remark: '日用品' },
    { id: '6', type: 'EXPENSE', category: '娱乐', amount: 58, recordDate: '2026-03-21T22:05:00', remark: '电影票' },
    { id: '7', type: 'EXPENSE', category: '餐饮', amount: 42, recordDate: '2026-03-20T19:20:00', remark: '晚餐' },
    { id: '8', type: 'INCOME', category: '红包', amount: 88, recordDate: '2026-03-20T11:30:00', remark: '朋友转账' },
    { id: '9', type: 'EXPENSE', category: '交通', amount: 6, recordDate: '2026-03-20T08:10:00', remark: '公交' }
  ]
}

/**
 * 后续把这里替换成真实 API
 */
async function fetchBillRecords() {
  loading.value = true

  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    billRecords.value = buildMockData()
  } finally {
    loading.value = false
  }
}

const filteredRecords = computed(() => {
  let records = [...billRecords.value]

  if (typeFilter.value !== 'ALL') {
    records = records.filter(item => item.type === typeFilter.value)
  }

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    records = records.filter(item =>
      item.category.toLowerCase().includes(keyword) ||
      (item.remark || '').toLowerCase().includes(keyword)
    )
  }

  return records
})

const tableData = computed<BillTableRow[]>(() => {
  const groupedMap = new Map<string, BillLeafRow[]>()

  for (const item of filteredRecords.value) {
    const day = formatDay(item.recordDate)
    const leaf: BillLeafRow = {
      ...item,
      kind: 'item'
    }

    if (!groupedMap.has(day)) {
      groupedMap.set(day, [])
    }

    groupedMap.get(day)!.push(leaf)
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

const flatLeafRows = computed(() => filteredRecords.value)

const summary = computed(() => {
  const income = flatLeafRows.value
    .filter(item => item.type === 'INCOME')
    .reduce((sum, item) => sum + item.amount, 0)

  const expense = flatLeafRows.value
    .filter(item => item.type === 'EXPENSE')
    .reduce((sum, item) => sum + item.amount, 0)

  return {
    income,
    expense,
    balance: income - expense,
    count: flatLeafRows.value.length
  }
})

const columns = computed<TableColumn<BillTableRow>[]>(() => [
  {
    id: 'select',
    header: ({ table }) =>
      h(UCheckbox, {
        modelValue: table.getIsAllPageRowsSelected(),
        'onUpdate:modelValue': (value: boolean) => table.toggleAllPageRowsSelected(!!value),
        'aria-label': '全选'
      }),
    cell: ({ row }) => {
      const currentRow = row.original

      if (!isBillLeafRow(currentRow)) {
        return h('div')
      }

      return h(UCheckbox, {
        modelValue: row.getIsSelected(),
        'onUpdate:modelValue': (value: boolean) => row.toggleSelected(!!value),
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
            class: SELECTORS.GROUP_DATE_BTN,
            onClick: () => row.toggleExpanded()
          },
          [
            h('div', { class: SELECTORS.GROUP_DATE_CONTENT }, [
              h('span', { class: SELECTORS.GROUP_DATE_ARROW_WRAP }, [
                h(UIcon, {
                  name: row.getIsExpanded() ? 'i-lucide-chevron-down' : 'i-lucide-chevron-right',
                  class: SELECTORS.GROUP_DATE_ARROW
                })
              ]),
              h('span', { class: SELECTORS.GROUP_DATE_TEXT }, currentRow.label),
              h('span', { class: SELECTORS.GROUP_DATE_SEP }, '｜'),
              h('span', { class: SELECTORS.GROUP_DATE_WEEKDAY }, currentRow.weekday),
              h('span', { class: SELECTORS.GROUP_DATE_SEP }, '｜'),
              h('span', { class: SELECTORS.GROUP_DATE_COUNT }, `${currentRow.count} 笔`)
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
            variant: 'ghost'
          },
          () => '编辑'
        ),
        h(
          UButton,
          {
            size: 'xs',
            color: 'error',
            variant: 'ghost'
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

onMounted(async () => {
  await fetchBillRecords()
  expanded.value = {}
})
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div class="page-header__left">
        <div class="page-header__eyebrow">
          Finance
        </div>
        <h1 class="page-header__title">
          账单
        </h1>
        <p class="page-header__desc">
          记录每天的消费和收入，先用伪造数据搭结构，后续直接接入接口。
        </p>
      </div>

      <div class="page-header__right">
        <UButton
          color="neutral"
          variant="soft"
          icon="i-lucide-refresh-cw"
          @click="fetchBillRecords"
        >
          刷新
        </UButton>

        <UButton
          icon="i-lucide-plus"
        >
          新增账单
        </UButton>
      </div>
    </div>

    <div class="page-toolbar">
      <div class="page-toolbar__left">
        <UInput
          v-model="searchKeyword"
          placeholder="搜索分类或备注"
          icon="i-lucide-search"
          class="toolbar-search"
        />

        <USelect
          v-model="typeFilter"
          :items="typeOptions"
          class="toolbar-select"
        />
      </div>

      <div class="page-toolbar__right">
        <UDropdownMenu :items="[visibleColumnMenuItems]">
          <UButton
            color="neutral"
            variant="soft"
            icon="i-lucide-columns-3"
          >
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
        <span
          class="mobe-stat__value"
          :class="summary.balance >= 0 ? 'mobe-stat__value--success' : 'mobe-stat__value--danger'"
        >
          {{ formatCurrency(summary.balance) }}
        </span>
      </div>
      <div class="mobe-stat">
        <span class="mobe-stat__label">记录数</span>
        <span class="mobe-stat__value">{{ summary.count }}</span>
      </div>
    </div>

    <div class="page-section bills-table-wrap">
      <UTable
        v-model:expanded="expanded"
        v-model:row-selection="rowSelection"
        v-model:sorting="sorting"
        v-model:column-visibility="columnVisibility"
        :data="tableData"
        :columns="columns"
        :get-sub-rows="(row) => row.kind === 'group' ? row.children : undefined"
        :loading="loading"
        loading-color="primary"
        loading-animation="carousel"
        sticky
        class="bills-table"
        :ui="{
          base: 'border-separate border-spacing-0',
          thead: 'bg-[var(--mobe-surface-elevated)]',
          tbody: '[&>tr]:last:[&>td]:border-b-0',
          tr: 'group',
          th: 'bg-[var(--mobe-surface-elevated)]',
          td: 'align-middle empty:p-0 group-has-[td:not(:empty)]:border-b border-[var(--mobe-divider)]'
        }"
      />
    </div>
  </div>
</template>

<style scoped>
.toolbar-search {
  width: 260px;
}

.toolbar-select {
  width: 140px;
}

.bills-table-wrap {
  overflow: auto;
}

.bills-table {
  min-width: 1080px;
}

/* 以下类名在JavaScript中通过SELECTORS常量定义和使用 */

.group-date-btn {
  width: 100%;
  display: flex;
  align-items: center;
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  text-align: left;
}

.group-date-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 22px;
  white-space: nowrap;
}

.group-date-arrow-wrap {
  width: 16px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 16px;
}

.group-date-arrow {
  width: 16px;
  height: 16px;
  display: block;
  line-height: 0;
  font-size: 0;
  color: var(--mobe-text);
}

.group-date-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--mobe-text);
}

.group-date-weekday,
.group-date-count {
  font-size: 13px;
  color: var(--mobe-text-soft);
}

.group-date-sep {
  font-size: 12px;
  color: var(--mobe-text-soft);
  opacity: 0.7;
}

@media (max-width: 1023px) {
  .toolbar-search,
  .toolbar-select {
    width: 100%;
  }
}
</style>
