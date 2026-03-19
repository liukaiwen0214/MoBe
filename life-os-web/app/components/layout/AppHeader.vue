<script setup lang="ts">
const route = useRoute()
const colorMode = useColorMode()

const titleMap: Record<string, string> = {
  '/': '首页',
  '/login': '登录',
  '/register': '注册',
  '/profile': '个人中心',
  '/sessions': '会话管理',
  '/settings/preferences': '偏好设置',
  '/settings/password': '修改密码'
}

const pageTitle = computed(() => {
  return titleMap[route.path] || 'MoBe'
})

const isDark = computed({
  get: () => colorMode.value === 'dark',
  set: (value: boolean) => {
    colorMode.preference = value ? 'dark' : 'light'
  }
})
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

      <UDropdownMenu
        :items="[
          [{ label: '个人中心', icon: 'i-lucide-user-round', to: '/profile' }],
          [{ label: '会话管理', icon: 'i-lucide-monitor-smartphone', to: '/sessions' }],
          [{ label: '退出登录', icon: 'i-lucide-log-out', color: 'error' }]
        ]"
      >
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
  background: rgba(255, 255, 255, 0.82);
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
