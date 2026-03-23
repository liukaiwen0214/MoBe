<script setup lang="ts">
/**
 * 应用侧边栏组件
 * <p>
 * 功能：显示应用的侧边栏导航菜单，包括品牌标识、菜单分组和菜单项
 * 说明：使用 Vue 3 组合式 API 构建，支持折叠和移动端适配
 */

/**
 * 组件属性
 */
const props = defineProps<{
  /**
   * 是否折叠
   * <p>
   * 说明：控制侧边栏是否处于折叠状态
   */
  collapsed?: boolean
  /**
   * 是否为移动端
   * <p>
   * 说明：控制侧边栏是否为移动端模式
   */
  mobile?: boolean
}>()

/**
 * 组件事件
 */
const emit = defineEmits<{
  /**
   * 切换侧边栏状态事件
   * <p>
   * 说明：当用户点击切换按钮时触发
   */
  toggle: []
  /**
   * 导航事件
   * <p>
   * 说明：当用户在移动端点击菜单项时触发
   */
  navigate: []
}>()

// 导入路由相关函数
const route = useRoute()

/**
 * 菜单分组配置
 * <p>
 * 说明：定义侧边栏的菜单分组和菜单项
 */
const menuGroups = [
  {
    title: '核心',
    items: [
      { label: '首页', icon: 'i-lucide-house', to: '/' },
      { label: '任务', icon: 'i-lucide-list-todo', to: '/tasks' },
      { label: '日程', icon: 'i-lucide-calendar-days', to: '/calendar' },
      { label: '笔记', icon: 'i-lucide-notebook-text', to: '/notes' },
      { label: '文件', icon: 'i-lucide-folder', to: '/files' }
    ]
  },
  {
    title: '生活',
    items: [
      { label: '清单', icon: 'i-lucide-check-check', to: '/lists' },
      { label: '习惯', icon: 'i-lucide-repeat', to: '/habits' },
      { label: '目标', icon: 'i-lucide-flag', to: '/goals' },
      { label: '账单', icon: 'i-lucide-wallet-cards', to: '/bills' }
    ]
  },
  {
    title: '智能',
    items: [
      { label: '统计', icon: 'i-lucide-chart-column', to: '/statistics' },
      { label: 'AI 助手', icon: 'i-lucide-sparkles', to: '/assistant' }
    ]
  }
]

/**
 * 处理导航事件
 * <p>
 * 功能：在移动端模式下，点击菜单项后触发导航事件
 */
function handleNavigate() {
  if (props.mobile) {
    emit('navigate')
  }
}
</script>

<template>
  <div class="sidebar-wrap" :class="{ collapsed: collapsed }">
    <div class="sidebar-top">
      <NuxtLink to="/" class="brand" @click="handleNavigate">
        <div class="brand-logo">
          茉
        </div>

        <div v-if="!collapsed" class="brand-text">
          <div class="brand-name">MoBe</div>
          <div class="brand-sub">Life OS</div>
        </div>
      </NuxtLink>

      <button
        class="sidebar-toggle"
        type="button"
        @click="emit('toggle')"
      >
        <UIcon :name="collapsed ? 'i-lucide-panel-left-open' : 'i-lucide-panel-left-close'" />
      </button>
    </div>

    <div class="sidebar-body">
      <div
        v-for="group in menuGroups"
        :key="group.title"
        class="menu-group"
      >
        <div v-if="!collapsed" class="menu-group-title">
          {{ group.title }}
        </div>

        <NuxtLink
          v-for="item in group.items"
          :key="item.to"
          :to="item.to"
          class="menu-item"
          :class="{ active: route.path === item.to, collapsed: collapsed }"
          @click="handleNavigate"
        >
          <UIcon :name="item.icon" class="menu-icon" />
          <span v-if="!collapsed" class="menu-label">{{ item.label }}</span>
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 18px 14px;
  background: transparent;
}

.sidebar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.brand {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: var(--mobe-radius-md);
  background: var(--mobe-primary-soft);
  color: var(--mobe-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.brand-text {
  min-width: 0;
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--mobe-text);
  line-height: 1.2;
}

.brand-sub {
  margin-top: 2px;
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-soft);
}

.sidebar-toggle {
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: var(--mobe-text-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--mobe-radius-sm);
  cursor: pointer;
  flex-shrink: 0;
}

.sidebar-toggle:hover {
  background: var(--mobe-surface-soft);
}

.sidebar-body {
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-group-title {
  padding: 0 10px;
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
  margin-bottom: 4px;
}

.menu-item {
  height: 40px;
  border-radius: var(--mobe-radius-md);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  text-decoration: none;
  color: var(--mobe-text-soft);
  transition: background 0.18s ease, color 0.18s ease;
}

.menu-item:hover {
  background: color-mix(in srgb, var(--mobe-text) 6%, transparent);
  color: var(--mobe-text);
}

.menu-item.active {
  background: var(--mobe-primary-soft);
  color: var(--mobe-text);
}

.menu-item.collapsed {
  justify-content: center;
  padding: 0;
}

.menu-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.menu-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collapsed .brand {
  justify-content: center;
}

.collapsed .sidebar-top {
  flex-direction: column;
  align-items: center;
}
</style>
