<script setup lang="ts">
definePageMeta({
  middleware: 'auth'
})

const overviewStats = [
  {
    label: '今日任务',
    value: '12'
  },
  {
    label: '今日日程',
    value: '3'
  },
  {
    label: '本周习惯',
    value: '5 / 7'
  },
  {
    label: '本月支出',
    value: '¥1,268'
  }
]

const quickLinks = [
  {
    title: '新建任务',
    desc: '快速添加待办事项',
    icon: 'i-lucide-circle-plus',
    to: '/tasks'
  },
  {
    title: '写一条笔记',
    desc: '记录灵感与想法',
    icon: 'i-lucide-square-pen',
    to: '/notes'
  },
  {
    title: '查看账单',
    desc: '管理每日消费流水',
    icon: 'i-lucide-wallet-cards',
    to: '/bills'
  },
  {
    title: '日程安排',
    desc: '整理今天的计划',
    icon: 'i-lucide-calendar-days',
    to: '/calendar'
  }
]

const recentNotes = [
  { title: '首页框架调整想法', time: '今天 10:16' },
  { title: '账单页分组展示草稿', time: '昨天 21:02' },
  { title: '偏好设置字段整理', time: '昨天 18:30' }
]
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div class="page-header__left">
        <div class="page-header__eyebrow">
          Dashboard
        </div>
        <h1 class="page-header__title">
          欢迎回来
        </h1>
        <p class="page-header__desc">
          从这里开始整理今天的任务、日程、习惯和生活记录。
        </p>
      </div>

      <div class="page-header__right">
        <UButton icon="i-lucide-plus">
          新建内容
        </UButton>
      </div>
    </div>

    <div class="mobe-stats">
      <div
        v-for="item in overviewStats"
        :key="item.label"
        class="mobe-stat"
      >
        <span class="mobe-stat__label">{{ item.label }}</span>
        <span class="mobe-stat__value">{{ item.value }}</span>
      </div>
    </div>

    <section class="home-section">
      <div class="home-section__head">
        <div>
          <div class="home-section__title">
            快捷入口
          </div>
          <div class="home-section__desc">
            常用功能从这里快速进入
          </div>
        </div>
      </div>

      <div class="quick-grid">
        <NuxtLink
          v-for="item in quickLinks"
          :key="item.title"
          :to="item.to"
          class="quick-item"
        >
          <div class="quick-item__icon">
            <UIcon :name="item.icon" />
          </div>
          <div class="quick-item__body">
            <div class="quick-item__title">
              {{ item.title }}
            </div>
            <div class="quick-item__desc">
              {{ item.desc }}
            </div>
          </div>
        </NuxtLink>
      </div>
    </section>

    <section class="home-section">
      <div class="home-section__head">
        <div>
          <div class="home-section__title">
            最近笔记
          </div>
          <div class="home-section__desc">
            最近记录下来的内容
          </div>
        </div>
      </div>

      <div class="recent-list">
        <div
          v-for="item in recentNotes"
          :key="item.title"
          class="recent-item"
        >
          <div class="recent-item__title">
            {{ item.title }}
          </div>
          <div class="recent-item__time">
            {{ item.time }}
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-section {
  display: flex;
  flex-direction: column;
  gap: var(--mobe-space-4);
}

.home-section__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mobe-space-4);
}

.home-section__title {
  font-size: var(--mobe-font-lg);
  font-weight: 700;
  color: var(--mobe-text);
}

.home-section__desc {
  margin-top: var(--mobe-space-1);
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--mobe-space-4);
}

.quick-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid var(--mobe-divider);
  text-decoration: none;
}

.quick-item__icon {
  width: 34px;
  height: 34px;
  border-radius: var(--mobe-radius-md);
  background: var(--mobe-primary-soft);
  color: var(--mobe-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-item__body {
  min-width: 0;
}

.quick-item__title {
  font-size: var(--mobe-font-md);
  font-weight: 600;
  color: var(--mobe-text);
}

.quick-item__desc {
  margin-top: 4px;
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
  line-height: 1.5;
}

.recent-list {
  display: flex;
  flex-direction: column;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--mobe-space-4);
  padding: 14px 0;
  border-top: 1px solid var(--mobe-divider);
}

.recent-item__title {
  font-size: var(--mobe-font-md);
  color: var(--mobe-text);
}

.recent-item__time {
  font-size: var(--mobe-font-sm);
  color: var(--mobe-text-soft);
  white-space: nowrap;
}

@media (max-width: 1023px) {
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }

  .recent-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
