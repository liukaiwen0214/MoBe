<!--
 * default.vue
 * 文件用途：应用的默认布局，包含侧边栏、头部和底部等通用布局元素
 * 所属模块：life-os-web（前端模块）
 * 核心职责：
 * 1. 提供应用的整体布局结构
 * 2. 处理侧边栏的展开/收起逻辑
 * 3. 适配移动端和桌面端的布局
 * 与其他模块的关联：
 * - 引用AppSidebar、AppHeader、AppFooter组件
 * - 使用@vueuse/core的useWindowSize钩子监测窗口大小
 * 在整体业务流程中的位置：位于布局层，为大部分页面提供通用布局
-->

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useWindowSize } from '@vueuse/core'
import AppSidebar from '~/components/layout/AppSidebar.vue'
import AppHeader from '~/components/layout/AppHeader.vue'
import AppFooter from '~/components/layout/AppFooter.vue'

// 侧边栏收起状态（桌面端）
const sidebarCollapsed = ref(false)
// 移动端侧边栏打开状态
const mobileSidebarOpen = ref(false)

// 使用useWindowSize钩子获取窗口大小
const { width } = useWindowSize()
// 计算属性：是否为移动端
// 当窗口宽度小于1024px时认为是移动端
const isMobile = computed(() => width.value < 1024)

/**
 * 切换侧边栏状态
 * 功能：根据设备类型切换侧边栏的展开/收起状态
 * 调用时机：用户点击侧边栏切换按钮时
 * 核心流程：
 * 1. 如果是移动端，切换移动端侧边栏的打开/关闭状态
 * 2. 如果是桌面端，切换侧边栏的展开/收起状态
 */
function toggleSidebar() {
  if (isMobile.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
    return
  }

  sidebarCollapsed.value = !sidebarCollapsed.value
}

/**
 * 关闭移动端侧边栏
 * 功能：关闭移动端的侧边栏
 * 调用时机：用户点击移动端遮罩层或侧边栏导航项时
 */
function closeMobileSidebar() {
  mobileSidebarOpen.value = false
}
</script>

<template>
  <!-- 应用布局容器 -->
  <div class="app-shell">
    <!-- 桌面端侧边栏 -->
    <!-- 当不是移动端时显示，支持展开/收起状态 -->
    <aside
      v-if="!isMobile"
      class="app-sidebar"
      :class="{ collapsed: sidebarCollapsed }"
    >
      <AppSidebar
        :collapsed="sidebarCollapsed"
        @toggle="toggleSidebar"
      />
    </aside>

    <!-- 移动端遮罩层 -->
    <!-- 当移动端侧边栏打开时显示，点击可关闭侧边栏 -->
    <Transition name="fade">
      <div
        v-if="isMobile && mobileSidebarOpen"
        class="mobile-overlay"
        @click="closeMobileSidebar"
      />
    </Transition>

    <!-- 移动端侧边栏 -->
    <!-- 当移动端且侧边栏打开时显示，从左侧滑入 -->
    <Transition name="slide-left">
      <aside
        v-if="isMobile && mobileSidebarOpen"
        class="mobile-sidebar"
      >
        <AppSidebar
          :collapsed="false"
          :mobile="true"
          @toggle="closeMobileSidebar"
          @navigate="closeMobileSidebar"
        />
      </aside>
    </Transition>

    <!-- 应用主内容区域 -->
    <!-- 包含头部、内容和底部 -->
    <div class="app-main">
      <!-- 应用头部 -->
      <!-- 显示应用标题、用户信息等 -->
      <header class="app-header">
        <AppHeader
          :show-menu-trigger="isMobile"
          @toggle-sidebar="toggleSidebar"
        />
      </header>

      <!-- 应用内容区域 -->
      <!-- 显示页面的主要内容 -->
      <main class="app-content">
        <slot />
      </main>

      <!-- 应用底部 -->
      <!-- 显示版权信息等 -->
      <footer class="app-footer">
        <AppFooter />
      </footer>
    </div>
  </div>
</template>

<style scoped>
/* 应用布局容器 */
/* 100vh高度，flex布局，使用自定义背景色 */
.app-shell {
  height: 100vh;
  overflow: hidden;
  display: flex;
  background: var(--mobe-bg);
}

/* 桌面端侧边栏 */
/* 固定宽度248px，支持过渡动画 */
.app-sidebar {
  width: 248px;
  min-width: 248px;
  flex-shrink: 0;
  border-right: 1px solid var(--mobe-divider);
  background: transparent;
  transition: width 0.22s ease, min-width 0.22s ease;
}

/* 收起状态的侧边栏 */
/* 宽度缩小为76px */
.app-sidebar.collapsed {
  width: 76px;
  min-width: 76px;
}

/* 应用主内容区域 */
/* 占满剩余空间，使用grid布局 */
.app-main {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: grid;
  grid-template-rows: 64px minmax(0, 1fr) 40px;
  background: transparent;
}

/* 应用头部 */
/* 高度64px，带边框和模糊效果 */
.app-header {
  min-height: 0;
  padding: 0 24px;
  border-bottom: 1px solid var(--mobe-divider);
  background: color-mix(in srgb, var(--mobe-bg) 92%, transparent);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* 应用内容区域 */
/* 可滚动，带内边距 */
.app-content {
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  background: transparent;
}

/* 应用底部 */
/* 高度40px，带边框 */
.app-footer {
  min-height: 0;
  padding: 0 24px;
  border-top: 1px solid var(--mobe-divider);
  background: transparent;
}

/* 移动端遮罩层 */
/* 固定定位，半透明背景 */
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.18);
  z-index: 40;
}

/* 移动端侧边栏 */
/* 固定定位，从左侧滑入 */
.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: min(84vw, 320px);
  z-index: 50;
  background: var(--mobe-surface-elevated);
  border-right: 1px solid var(--mobe-divider);
}

/* 移动端响应式调整 */
/* 调整布局参数以适应移动端 */
@media (max-width: 1023px) {
  .app-main {
    grid-template-rows: 56px minmax(0, 1fr) 40px;
  }

  .app-header,
  .app-content,
  .app-footer {
    padding-left: 16px;
    padding-right: 16px;
  }

  .app-content {
    padding-top: 16px;
    padding-bottom: 16px;
  }
}
</style>
