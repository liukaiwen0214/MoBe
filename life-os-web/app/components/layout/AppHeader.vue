<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useColorMode } from '#imports'
import { useUserStore } from '~/stores/user'
/**
 * 应用头部组件
 * <p>
 * 功能：显示应用的头部信息，包括页面标题、搜索框、深色模式切换和用户菜单
 */
const route = useRoute()
/**
 * 用户状态管理
 */
const userStore = useUserStore()
/**
 * 颜色模式管理
 */
const colorMode = useColorMode()
/**
 * 页面标题映射
 */
const titleMap: Record<string, string> = {
  '/': '首页',
  '/login': '登录',
  '/register': '注册',
  '/profile': '个人中心',
  '/sessions': '会话管理',
  '/settings/preferences': '偏好设置',
  '/settings/password': '修改密码'
}
/**
 * 页面标题
 */
const pageTitle = computed(() => {
  return titleMap[route.path] || 'MoBe'
})
/**
 * 是否深色模式
 */
const isDark = computed({
  get: () => colorMode.value === 'dark',
  set: (value: boolean) => {
    colorMode.preference = value ? 'dark' : 'light'
  }
})
/**
 * 用户菜单项
 */
const menuItems = computed(() => [
  [
    {
      label: '个人中心',
      icon: 'i-lucide-user-round',
      to: '/profile'
    }
  ],
  [
    {
      label: '会话管理',
      icon: 'i-lucide-monitor-smartphone',
      to: '/sessions'
    }
  ],
  [
    {
      label: '退出登录',
      icon: 'i-lucide-log-out',
      click: async () => {
        userStore.logout()
        await navigateTo('/login')
      }
    }
  ]
])
</script>

<template>
  <div class="header-card">
    <div class="header-left">
      <div class="header-title">
        {{ pageTitle }}
      </div>
      <div class="header-subtitle">
        欢迎回来，开始今天的整理吧
      </div>
    </div>

    <div class="header-right">
      <UInput
        icon="i-lucide-search"
        placeholder="搜索内容..."
        size="lg"
        class="header-search"
      />

      <UTooltip text="深色模式">
        <USwitch v-model="isDark" />
      </UTooltip>

      <UDropdownMenu :items="menuItems">
        <UButton
          color="neutral"
          variant="soft"
          class="user-btn"
        >
          <template #leading>
            <UAvatar
              text="M"
              size="sm"
            />
          </template>
          茉白
        </UButton>
      </UDropdownMenu>
    </div>
  </div>
</template>

<style scoped>
.header-card {
  height: 100%;
  border-radius: 24px;
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 10px 30px rgba(95, 71, 47, 0.05);
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  box-sizing: border-box;
}

.header-left {
  min-width: 0;
}

.header-title {
  font-size: 22px;
  font-weight: 700;
  color: #5f4730;
}

.header-subtitle {
  font-size: 13px;
  color: #9a8370;
  margin-top: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-search {
  width: 240px;
}

.user-btn {
  border-radius: 14px;
}
</style>