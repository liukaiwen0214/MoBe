/**
 * Nuxt 配置文件
 * <p>
 * 功能：配置 Nuxt 应用的各种选项和模块
 * 说明：使用 defineNuxtConfig 函数定义配置
 */
export default defineNuxtConfig({
  /**
   * 模块配置
   * <p>
   * 说明：配置 Nuxt 应用使用的模块
   */
  modules: [
    '@nuxt/ui',  // Nuxt 官方 UI 库
    '@pinia/nuxt'  // Pinia 状态管理
  ],

  /**
   * 开发工具配置
   * <p>
   * 说明：配置 Nuxt 开发工具
   */
  devtools: {
    enabled: false  // 禁用开发工具
  },

  /**
   * CSS 配置
   * <p>
   * 说明：配置全局 CSS 文件
   */
  css: ['~/assets/css/main.css'],

  /**
   * 兼容性日期
   * <p>
   * 说明：指定 Nuxt 兼容性日期
   */
  compatibilityDate: '2025-01-15',

  /**
   * UI 配置
   * <p>
   * 说明：配置 @nuxt/ui 的选项
   */
  ui: {
    fonts: false  // 禁用默认字体
  }
})