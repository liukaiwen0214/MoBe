// eslint.config.mjs
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

// 定义 .nuxt 下 eslint 配置的路径
const __dirname = path.dirname(fileURLToPath(import.meta.url))
const nuxtEslintConfigPath = path.join(__dirname, '.nuxt/eslint.config.mjs')

// 核心配置：只在文件存在时才导入
const nuxtEslintConfig = fs.existsSync(nuxtEslintConfigPath)
  ? (await import(nuxtEslintConfigPath)).default
  : []

// 最终导出 ESLint 配置
export default [
  // 先导入 Nuxt 生成的配置（如果存在）
  ...nuxtEslintConfig,
  // 再添加自定义的 ESLint 规则
  {
    rules: {
      // 示例：关闭 console 警告
      'no-console': 'off',
      // 可添加其他自定义规则
    }
  }
]
