<script setup lang="ts">

</script>

<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <LayoutAppSidebar />
    </aside>

    <div class="app-main">
      <header class="app-header">
        <LayoutAppHeader />
      </header>

      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  background: #f8f5f1;
}

.app-sidebar {
  width: 236px;
  padding: 16px;
  box-sizing: border-box;
}

.app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 16px 16px 0;
  box-sizing: border-box;
}

.app-header {
  height: 72px;
  margin-bottom: 16px;
}

.app-content {
  flex: 1;
  min-height: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(120, 90, 60, 0.08);
  box-shadow: 0 10px 30px rgba(95, 71, 47, 0.06);
  padding: 24px;
  box-sizing: border-box;
}
</style>
