<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const pageBooting = ref(true)
const routeLoading = ref(false)

const showGlobalLoader = computed(() => pageBooting.value || routeLoading.value)
import { useHead, useSeoMeta } from '#app'

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

const title = 'MoBe'
const description = 'MoBe 个人生活与任务管理系统'

useSeoMeta({
  title,
  description,
  ogTitle: title,
  ogDescription: description,
  ogImage: '/favicon.ico',
  twitterImage: '/favicon.ico',
  twitterCard: 'summary_large_image'
})

onMounted(() => {
  window.setTimeout(() => {
    pageBooting.value = false
  }, 450)
})

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
  <UApp :toaster="{ position: 'top-right' }">
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

    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>

<style scoped>
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

.global-loader__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.global-loader__ring {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: 2px solid rgba(145, 132, 126, 0.16);
  border-top-color: #8c8791;
  animation: mobe-spin 0.85s linear infinite;
}

.global-loader__text {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: #7f7a84;
}

.global-loader-fade-enter-active,
.global-loader-fade-leave-active {
  transition: opacity 0.28s ease;
}

.global-loader-fade-enter-from,
.global-loader-fade-leave-to {
  opacity: 0;
}

@keyframes mobe-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>