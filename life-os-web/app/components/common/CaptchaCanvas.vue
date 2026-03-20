<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

/**
 * 验证码画布组件
 * <p>
 * 功能：显示验证码并允许用户点击刷新
 * 说明：使用 Canvas 绘制验证码，包含背景、噪点、干扰线和字符
 */

/**
 * 组件属性
 */
const props = defineProps<{
  /**
   * 验证码字符串
   */
  code: string
  /**
   * 画布宽度（可选），默认 120px
   */
  width?: number
  /**
   * 画布高度（可选），默认 40px
   */
  height?: number
}>()

/**
 * 组件事件
 */
const emit = defineEmits<{
  /**
   * 刷新验证码事件
   */
  refresh: []
}>()

/**
 * Canvas 元素引用
 */
const canvasRef = ref<HTMLCanvasElement | null>(null)

/**
 * 生成指定范围内的随机整数
 * @param min 最小值
 * @param max 最大值
 * @returns 随机整数
 */
function rand(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

/**
 * 绘制验证码
 * <p>
 * 功能：在 Canvas 上绘制验证码，包括背景、噪点、干扰线和字符
 */
function drawCaptcha() {
  const canvas = canvasRef.value
  if (!canvas || !props.code) return

  const width = props.width ?? 120
  const height = props.height ?? 40

  // 设置 Canvas 尺寸，使用 2x 分辨率以获得清晰的显示效果
  canvas.width = width * 2
  canvas.height = height * 2
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 缩放 Canvas 上下文以适应 2x 分辨率
  ctx.scale(2, 2)

  // 绘制背景
  ctx.clearRect(0, 0, width, height)
  ctx.fillStyle = 'rgba(255,255,255,0.22)'
  ctx.fillRect(0, 0, width, height)

  // 绘制噪点背景
  for (let i = 0; i < 18; i++) {
    ctx.fillStyle = `rgba(120, 125, 145, ${Math.random() * 0.12 + 0.05})`
    ctx.beginPath()
    ctx.arc(rand(0, width), rand(0, height), rand(1, 2), 0, Math.PI * 2)
    ctx.fill()
  }

  // 绘制干扰线
  for (let i = 0; i < 2; i++) {
    ctx.strokeStyle = `rgba(140, 150, 175, ${Math.random() * 0.25 + 0.18})`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(rand(0, 12), rand(6, height - 6))
    ctx.bezierCurveTo(
      rand(width / 4, width / 2),
      rand(0, height),
      rand(width / 2, (width * 3) / 4),
      rand(0, height),
      rand(width - 12, width),
      rand(6, height - 6)
    )
    ctx.stroke()
  }

  // 绘制字符
  const chars = props.code.split('')
  const gap = width / (chars.length + 1)

  chars.forEach((char, index) => {
    const x = gap * (index + 1)
    const y = rand(height / 2 + 2, height / 2 + 6)
    const rotate = (Math.random() * 30 - 15) * (Math.PI / 180)
    const fontSize = rand(22, 26)

    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(rotate)
    ctx.font = `600 ${fontSize}px Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`
    ctx.fillStyle = ['#6f6974', '#7b7682', '#6d7484', '#7e7a88'][rand(0, 3)] as string
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(char, 0, 0)
    ctx.restore()
  })
}

/**
 * 监听验证码变化，重新绘制
 */
watch(
  () => props.code,
  () => {
    drawCaptcha()
  },
  { immediate: true }
)

/**
 * 组件挂载后绘制验证码
 */
onMounted(() => {
  drawCaptcha()
})
</script>

<template>
  <!-- 验证码画布容器 -->
  <button
    type="button"
    class="captcha-canvas-wrap"
    @click="emit('refresh')"
  >
    <!-- Canvas 元素，用于绘制验证码 -->
    <canvas ref="canvasRef" class="captcha-canvas" />
  </button>
</template>

<style scoped>
/**
 * 验证码画布容器样式
 */
.captcha-canvas-wrap {
  min-width: 120px;
  height: 40px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  overflow: hidden;
  cursor: pointer;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
  transition: all 0.2s ease;
}
/**
 * 鼠标悬停时的样式
 */
.captcha-canvas-wrap:hover {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.4);
}

/**
 * Canvas 元素样式
 */
.captcha-canvas {
  display: block;
  width: 120px;
  height: 40px;
}
</style>