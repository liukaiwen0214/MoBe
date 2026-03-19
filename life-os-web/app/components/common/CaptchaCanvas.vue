<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  code: string
  width?: number
  height?: number
}>()

const emit = defineEmits<{
  refresh: []
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)

function rand(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function drawCaptcha() {
  const canvas = canvasRef.value
  if (!canvas || !props.code) return

  const width = props.width ?? 120
  const height = props.height ?? 40

  canvas.width = width * 2
  canvas.height = height * 2
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.scale(2, 2)

  // 背景
  ctx.clearRect(0, 0, width, height)
  ctx.fillStyle = 'rgba(255,255,255,0.22)'
  ctx.fillRect(0, 0, width, height)

  // 噪点背景
  for (let i = 0; i < 18; i++) {
    ctx.fillStyle = `rgba(120, 125, 145, ${Math.random() * 0.12 + 0.05})`
    ctx.beginPath()
    ctx.arc(rand(0, width), rand(0, height), rand(1, 2), 0, Math.PI * 2)
    ctx.fill()
  }

  // 干扰线
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

  // 字符
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

watch(
  () => props.code,
  () => {
    drawCaptcha()
  },
  { immediate: true }
)

onMounted(() => {
  drawCaptcha()
})
</script>

<template>
  <button
    type="button"
    class="captcha-canvas-wrap"
    @click="emit('refresh')"
  >
    <canvas ref="canvasRef" class="captcha-canvas" />
  </button>
</template>

<style scoped>
.captcha-canvas-wrap {
  min-width: 120px;
  height: 40px;
  padding: 0;
  border: 1px solid rgba(0, 0, 0, 0.4);
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.captcha-canvas-wrap:hover {
  background: rgba(255, 255, 255, 0.28);
}

.captcha-canvas {
  display: block;
  width: 120px;
  height: 40px;
}
</style>