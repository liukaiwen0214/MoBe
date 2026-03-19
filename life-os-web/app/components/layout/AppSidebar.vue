<script setup lang="ts">
const route = useRoute()

const menuGroups = [
  {
    title: '核心',
    items: [
      { label: '首页', to: '/', icon: 'i-lucide-house' },
      { label: '任务', to: '/tasks', icon: 'i-lucide-list-todo' },
      { label: '日程', to: '/calendar', icon: 'i-lucide-calendar-days' },
      { label: '笔记', to: '/notes', icon: 'i-lucide-notebook-text' },
      { label: '文件', to: '/files', icon: 'i-lucide-folder' }
    ]
  },
  {
    title: '用户',
    items: [
      { label: '个人中心', to: '/profile', icon: 'i-lucide-user-round' },
      { label: '会话管理', to: '/sessions', icon: 'i-lucide-monitor-smartphone' },
      { label: '偏好设置', to: '/settings/preferences', icon: 'i-lucide-sliders-horizontal' },
      { label: '修改密码', to: '/settings/password', icon: 'i-lucide-key-round' }
    ]
  }
]

function isActive(path: string) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="sidebar-card">
    <div class="brand">
      <div class="brand-badge">
        M
      </div>
      <div class="brand-text">
        <div class="brand-title">
          MoBe
        </div>
        <div class="brand-subtitle">
          Life OS
        </div>
      </div>
    </div>

    <div class="menu-wrap">
      <div
        v-for="group in menuGroups"
        :key="group.title"
        class="menu-group"
      >
        <div class="menu-group-title">
          {{ group.title }}
        </div>

        <NuxtLink
          v-for="item in group.items"
          :key="item.to"
          :to="item.to"
          class="menu-item"
          :class="{ active: isActive(item.to) }"
        >
          <UIcon
            :name="item.icon"
            class="menu-item-icon"
          />
          <span>{{ item.label }}</span>
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar-card {
  height: calc(100vh - 32px);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 12px 36px rgba(95, 71, 47, 0.07);
  padding: 18px 14px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px 18px;
}

.brand-badge {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #8f6d56, #b89d87);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #5f4730;
}

.brand-subtitle {
  font-size: 12px;
  color: #9a8370;
  margin-top: 2px;
}

.menu-wrap {
  flex: 1;
  overflow: auto;
  padding: 4px 4px 0;
}

.menu-group + .menu-group {
  margin-top: 20px;
}

.menu-group-title {
  font-size: 12px;
  color: #a08b77;
  padding: 0 10px 8px;
}

.menu-item {
  height: 42px;
  border-radius: 14px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6d5745;
  text-decoration: none;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: rgba(184, 157, 135, 0.12);
}

.menu-item.active {
  background: linear-gradient(135deg, rgba(184, 157, 135, 0.22), rgba(143, 109, 86, 0.16));
  color: #4f3928;
  font-weight: 600;
}

.menu-item-icon {
  font-size: 18px;
}
</style>
