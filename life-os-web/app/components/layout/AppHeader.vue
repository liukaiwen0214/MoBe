<script setup lang="ts">
/**
 * 应用头部组件
 * <p>
 * 功能：显示应用的头部导航，包括页面标题、菜单触发器、用户菜单等
 * 说明：使用 Vue 3 组合式 API 构建
 */

/**
 * 组件属性
 */
const props = defineProps<{
  /**
   * 是否显示菜单触发器
   * <p>
   * 说明：控制是否显示侧边栏切换按钮
   */
  showMenuTrigger?: boolean
}>()

/**
 * 组件事件
 */
const emit = defineEmits<{
  /**
   * 切换侧边栏事件
   * <p>
   * 说明：当用户点击菜单触发器时触发
   */
  toggleSidebar: []
}>()

// 导入路由相关函数
const route = useRoute()

/**
 * 页面标题映射
 * <p>
 * 说明：将路由路径映射到对应的页面标题
 */
const pageTitleMap: Record<string, string> = {
  '/': '首页',
  '/tasks': '任务',
  '/calendar': '日程',
  '/notes': '笔记',
  '/files': '文件',
  '/lists': '清单',
  '/habits': '习惯',
  '/goals': '目标',
  '/bills': '账单',
  '/statistics': '统计',
  '/assistant': 'AI 助手'
}

/**
 * 当前页面标题
 * <p>
 * 说明：根据当前路由路径计算页面标题
 */
const pageTitle = computed(() => pageTitleMap[route.path] || 'MoBe')
</script>

<template>
  <div class="header-wrap">
    <div class="header-left">
      <button
        v-if="showMenuTrigger"
        class="menu-trigger"
        type="button"
        @click="emit('toggleSidebar')"
      >
        <UIcon name="i-lucide-menu" />
      </button>

      <div class="header-title-wrap">
        <div class="header-kicker">
          页面
        </div>
        <div class="header-title">
          {{ pageTitle }}
        </div>
      </div>
    </div>

    <div class="header-right">
      <UColorModeButton />

      <UButton
        color="neutral"
        variant="ghost"
        icon="i-lucide-bell"
      />

      <UDropdownMenu
        :items="[
          [{ label: '个人中心', icon: 'i-lucide-user-round', to: '/profile' }],
          [{ label: '会话管理', icon: 'i-lucide-monitor-smartphone', to: '/sessions' }],
          [{ label: '偏好设置', icon: 'i-lucide-sliders-horizontal', to: '/settings/preferences' }],
          [{ label: '修改密码', icon: 'i-lucide-key-round', to: '/settings/password' }]
        ]"
      >
        <UButton color="neutral" variant="ghost" class="account-btn">
          <template #leading>
            <UAvatar text="茉" size="sm" />
          </template>
          茉白
        </UButton>
      </UDropdownMenu>
    </div>
  </div>
</template>

<style scoped>
.header-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.menu-trigger {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  color: var(--mobe-text-soft);
  border-radius: var(--mobe-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.menu-trigger:hover {
  background: var(--mobe-surface-soft);
}

.header-title-wrap {
  min-width: 0;
}

.header-kicker {
  font-size: 11px;
  color: var(--mobe-text-mute);
  line-height: 1.2;
}

.header-title {
  margin-top: 2px;
  font-size: var(--mobe-font-lg);
  font-weight: 700;
  color: var(--mobe-text);
  line-height: 1.2;
}

.account-btn {
  border-radius: var(--mobe-radius-md);
}
</style>
