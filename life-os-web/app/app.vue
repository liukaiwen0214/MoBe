<!--
 * app.vue
 * 文件用途：前端应用的根组件，负责应用的整体布局和全局状态管理
 * 所属模块：life-os-web（前端模块）
 * 核心职责：
 * 1. 管理应用的全局加载状态
 * 2. 配置应用的元信息（SEO、标题、图标等）
 * 3. 提供应用的整体布局结构
 * 4. 处理路由变化时的加载动画
 * 与其他模块的关联：
 * - 使用NuxtLayout和NuxtPage组件渲染页面内容
 * - 使用useRoute钩子监听路由变化
 * - 使用useHead和useSeoMeta配置页面元信息
 * 在整体业务流程中的位置：位于前端应用的最顶层，是所有页面的父组件
-->

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

// 路由实例，用于监听路由变化
const route = useRoute()

// 应用启动状态，控制启动加载动画
const pageBooting = ref(true)
// 路由加载状态，控制路由切换时的加载动画
const routeLoading = ref(false)

// 计算属性：是否显示全局加载动画
// 当应用启动或路由切换时显示
const showGlobalLoader = computed(() => pageBooting.value || routeLoading.value)
import { useHead, useSeoMeta } from '#app'

// 配置页面头部信息
// - 设置viewport元标签，确保响应式布局
// - 设置网站图标
// - 设置页面语言为中文
useHead({
  meta: [
    { name: 'viewport', content: 'width=device-width, initial-scale=1' }
  ],
  link: [
    { rel: 'icon', href: '/favicon.ico' }
  ],
  htmlAttrs: {
    lang: 'zh-CN'
  }
})

// 网站标题
const title = 'MoBe'
// 网站描述
const description = 'MoBe 个人生活与任务管理系统'

// 配置SEO元信息
// - 设置页面标题和描述
// - 设置Open Graph和Twitter卡片信息，用于社交媒体分享
useSeoMeta({
  title,
  description,
  ogTitle: title,
  ogDescription: description,
  ogImage: '/favicon.ico',
  twitterImage: '/favicon.ico',
  twitterCard: 'summary_large_image'
})

// 组件挂载后执行
// 设置450毫秒的延迟，然后关闭启动加载动画
onMounted(() => {
  window.setTimeout(() => {
    pageBooting.value = false
  }, 450)
})

// 监听路由变化
// 当路由路径变化时，显示路由加载动画，280毫秒后关闭
watch(
  () => route.fullPath,
  () => {
    routeLoading.value = true

    window.setTimeout(() => {
      routeLoading.value = false
    }, 280)
  }
)
</script>

<template>
  <!-- UApp组件：提供应用的基础结构和全局toast功能 -->
  <UApp :toaster="{ position: 'top-right' }">
    <!-- 全局加载动画 -->
    <!-- 当showGlobalLoader为true时显示，使用fade过渡效果 -->
    <Transition name="global-loader-fade">
      <div v-if="showGlobalLoader" class="global-loader">
        <div class="global-loader__inner">
          <div class="global-loader__ring" />
          <div class="global-loader__text">
            正在进入
          </div>
        </div>
      </div>
    </Transition>

    <!-- NuxtLayout组件：渲染页面布局 -->
    <!-- NuxtPage组件：渲染当前路由对应的页面内容 -->
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>

<style scoped>
/* 全局加载动画容器 */
/* 固定定位，覆盖整个屏幕，使用 backdrop-filter 实现模糊效果 */
.global-loader {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* 加载动画内部容器 */
/* 使用flex布局，垂直排列加载环和文字 */
.global-loader__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

/* 加载动画圆环 */
/* 使用border和animation实现旋转效果 */
.global-loader__ring {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: 2px solid rgba(145, 132, 126, 0.16);
  border-top-color: #8c8791;
  animation: mobe-spin 0.85s linear infinite;
}

/* 加载动画文字 */
/* 设置字体大小、字间距和颜色 */
.global-loader__text {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: #7f7a84;
}

/* 加载动画的过渡效果 */
/* 定义进入和离开时的过渡动画 */
.global-loader-fade-enter-active,
.global-loader-fade-leave-active {
  transition: opacity 0.28s ease;
}

.global-loader-fade-enter-from,
.global-loader-fade-leave-to {
  opacity: 0;
}

/* 旋转动画 */
/* 定义mobe-spin动画，实现360度旋转 */
@keyframes mobe-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>