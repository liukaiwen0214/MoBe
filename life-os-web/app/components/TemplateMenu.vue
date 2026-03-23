<script setup lang="ts">
/**
 * 模板菜单组件
 * <p>
 * 功能：显示可选择的模板列表，点击可跳转到对应的模板页面
 * 说明：使用 UDropdownMenu 组件实现下拉菜单功能
 */
</script>

<template>
  <UDropdownMenu
    v-slot="{ open }"
    :modal="false"
    :items="[
      {
        label: 'Starter',
        to: 'https://starter-template.nuxt.dev/',
        color: 'primary',
        checked: true,
        type: 'checkbox'
      },
      {
        label: 'Landing',
        to: 'https://landing-template.nuxt.dev/'
      },
      {
        label: 'Docs',
        to: 'https://docs-template.nuxt.dev/'
      },
      {
        label: 'SaaS',
        to: 'https://saas-template.nuxt.dev/'
      },
      {
        label: 'Dashboard',
        to: 'https://dashboard-template.nuxt.dev/'
      },
      {
        label: 'Chat',
        to: 'https://chat-template.nuxt.dev/'
      },
      {
        label: 'Portfolio',
        to: 'https://portfolio-template.nuxt.dev/'
      },
      {
        label: 'Changelog',
        to: 'https://changelog-template.nuxt.dev/'
      }
    ]"
    :content="{ align: 'start' }"
    :ui="{ content: 'min-w-fit' }"
    size="xs"
  >
    <UButton
      label="Starter"
      variant="subtle"
      trailing-icon="i-lucide-chevron-down"
      size="xs"
      class="-mb-[6px] font-semibold rounded-full truncate"
      :class="[open && 'bg-primary/15']"
      :ui="{
        trailingIcon: ['transition-transform duration-200', open ? 'rotate-180' : undefined].filter(Boolean).join(' ')
      }"
    />
  </UDropdownMenu>
</template>
