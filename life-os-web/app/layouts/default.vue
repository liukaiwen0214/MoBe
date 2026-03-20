<script setup lang="ts">
/**
 * 导入布局组件
 * @import LayoutAppSidebar 侧边栏组件
 * @import LayoutAppHeader 头部组件
 */
import LayoutAppSidebar from '@/components/layout/AppSidebar.vue'
import LayoutAppHeader from '@/components/layout/AppHeader.vue'
</script>

<template>
  <!--
    默认布局组件
    功能：为应用的主要页面提供标准布局结构
    说明：使用 Flex 布局实现响应式布局，包含侧边栏、头部和内容区域
    结构：
    - 侧边栏：位于左侧，显示导航菜单
    - 头部：位于右侧顶部，显示用户信息和操作
    - 内容区域：位于右侧中部，显示页面内容
    -->
  <div class="app-shell">
    <!-- 侧边栏区域 -->
    <aside class="app-sidebar">
      <LayoutAppSidebar />
    </aside>

    <!-- 主内容区域 -->
    <div class="app-main">
      <!-- 头部区域 -->
      <header class="app-header">
        <LayoutAppHeader />
      </header>

      <!-- 页面内容区域 -->
      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
/**
 * 应用外壳样式
 * 说明：设置整体布局容器，使用 Flex 布局
 * - min-height: 100vh 确保布局占满整个视口高度
 * - background: #f8f5f1 设置背景颜色
 */
.app-shell {
  min-height: 100vh;
  display: flex;
}

/**
 * 侧边栏样式
 * 说明：设置侧边栏的宽度和内边距
 * - width: 236px 固定侧边栏宽度
 * - padding: 16px 设置内边距
 * - box-sizing: border-box 确保内边距不影响宽度
 */
.app-sidebar {
  width: 236px;
  padding: 16px;
  box-sizing: border-box;
}

/**
 * 主内容区域样式
 * 说明：设置主内容区域的布局
 * - flex: 1 占据剩余空间
 * - min-width: 0 避免 flex 子元素溢出
 * - display: flex 设置为 Flex 容器
 * - flex-direction: column 垂直排列子元素
 * - padding: 16px 16px 16px 0 设置内边距
 */
.app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 16px 16px 0;
  box-sizing: border-box;
}

/**
 * 头部样式
 * 说明：设置头部的高度和下边距
 * - height: 72px 固定头部高度
 * - margin-bottom: 16px 设置下边距
 */
.app-header {
  height: 72px;
  margin-bottom: 16px;
}

/**
 * 内容区域样式
 * 说明：设置内容区域的样式
 * - flex: 1 占据剩余空间
 * - min-height: 0 避免 flex 子元素溢出
 * - border-radius: 24px 设置圆角
 * - background: rgba(255, 255, 255, 0.72) 设置背景颜色
 * - border: 1px solid rgba(120, 90, 60, 0.08) 设置边框
 * - box-shadow: 0 10px 30px rgba(95, 71, 47, 0.06) 设置阴影
 * - padding: 24px 设置内边距
 */
.app-content {
  flex: 1;
  min-height: 0;
  border-radius: 24px;
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 10px 30px rgba(95, 71, 47, 0.06);
  padding: 24px;
  box-sizing: border-box;
}
</style>
