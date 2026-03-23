<script setup lang="ts">
import { computed, ref } from 'vue'
import { useWindowSize } from '@vueuse/core'
import AppSidebar from '~/components/layout/AppSidebar.vue'
import AppHeader from '~/components/layout/AppHeader.vue'
import AppFooter from '~/components/layout/AppFooter.vue'

const sidebarCollapsed = ref(false)
const mobileSidebarOpen = ref(false)

const { width } = useWindowSize()
const isMobile = computed(() => width.value < 1024)

function toggleSidebar() {
  if (isMobile.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
    return
  }

  sidebarCollapsed.value = !sidebarCollapsed.value
}

function closeMobileSidebar() {
  mobileSidebarOpen.value = false
}
</script>

<template>
  <div class="app-shell">
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

    <Transition name="fade">
      <div
        v-if="isMobile && mobileSidebarOpen"
        class="mobile-overlay"
        @click="closeMobileSidebar"
      />
    </Transition>

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

    <div class="app-main">
      <header class="app-header">
        <AppHeader
          :show-menu-trigger="isMobile"
          @toggle-sidebar="toggleSidebar"
        />
      </header>

      <main class="app-content">
        <slot />
      </main>

      <footer class="app-footer">
        <AppFooter />
      </footer>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  height: 100vh;
  overflow: hidden;
  display: flex;
  background: var(--mobe-bg);
}

.app-sidebar {
  width: 248px;
  min-width: 248px;
  flex-shrink: 0;
  border-right: 1px solid var(--mobe-divider);
  background: transparent;
  transition: width 0.22s ease, min-width 0.22s ease;
}

.app-sidebar.collapsed {
  width: 76px;
  min-width: 76px;
}

.app-main {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: grid;
  grid-template-rows: 64px minmax(0, 1fr) 40px;
  background: transparent;
}

.app-header {
  min-height: 0;
  padding: 0 24px;
  border-bottom: 1px solid var(--mobe-divider);
  background: color-mix(in srgb, var(--mobe-bg) 92%, transparent);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.app-content {
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
  background: transparent;
}

.app-footer {
  min-height: 0;
  padding: 0 24px;
  border-top: 1px solid var(--mobe-divider);
  background: transparent;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.18);
  z-index: 40;
}

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
