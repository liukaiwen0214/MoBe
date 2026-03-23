<script setup lang="ts">
/**
 * 应用页脚组件
 * <p>
 * 功能：显示应用的页脚信息，包括版权声明和标语
 * 说明：使用 Vue 3 组合式 API 构建
 */
</script>

<template>
  <footer class="footer-wrap">
    <div>© 2026 MoBe</div>
    <div>让生活慢慢归位</div>
  </footer>
</template>

<style scoped>
/**
 * 页脚样式
 * <p>
 * 说明：定义页脚的布局和样式
 */
.footer-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: var(--mobe-font-xs);
  color: var(--mobe-text-mute);
}
</style>
