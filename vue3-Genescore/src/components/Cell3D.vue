<template>
  <div class="cell-container" ref="containerRef">
    <canvas ref="canvasRef" class="cell-canvas"></canvas>
    <div class="cell-info" v-if="showInfo">
      <span class="cell-name">{{ cellName }}</span>
      <span class="cell-score" v-if="score > 0">评分: {{ score.toFixed(1) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as PIXI from 'pixi.js'

interface Props {
  x?: number
  y?: number
  radius?: number
  color?: number
  cellName?: string
  score?: number
  showInfo?: boolean
  animated?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  x: 0,
  y: 0,
  radius: 50,
  color: 0x00ff88,
  cellName: '细胞',
  score: 0,
  showInfo: true,
  animated: true
})

const containerRef = ref<HTMLElement>()
const canvasRef = ref<HTMLCanvasElement>()
let app: PIXI.Application | null = null
let cellContainer: PIXI.Container | null = null
let animationFrame: number | null = null

const initPixi = () => {
  if (!canvasRef.value || !containerRef.value) return

  // 确保容器有尺寸
  const width = containerRef.value.clientWidth || 300
  const height = containerRef.value.clientHeight || 300

  // 检查PIXI版本，v7使用同步初始化，v8+使用异步init
  const PIXI_VERSION = (PIXI.VERSION || '7.3.3').split('.')[0]
  console.log('[Cell3D] PIXI version:', PIXI_VERSION)

  try {
    // 统一使用v7 API初始化
    app = new PIXI.Application({
      view: canvasRef.value,
      width: width,
      height: height,
      backgroundColor: 0x0a0a0a,
      antialias: true,
      resolution: window.devicePixelRatio || 1
    })
    console.log('[Cell3D] initPixi initialized')
    createCell()
    if (props.animated) {
      startAnimation()
    }
  } catch (error) {
    console.error('[Cell3D] initPixi error:', error)
  }
}

const createCell = () => {
  if (!app) return

  cellContainer = new PIXI.Container()
  // 使用app.screen获取尺寸
  const screenWidth = app.screen.width || 300
  const screenHeight = app.screen.height || 300
  cellContainer.x = screenWidth / 2
  cellContainer.y = screenHeight / 2

  // 创建多层细胞膜效果 (使用v7 API)
  for (let i = 3; i >= 0; i--) {
    const radius = props.radius + i * 8
    const membrane = new PIXI.Graphics()
    
    // 绘制不规则圆形细胞膜 (v7 API)
    membrane.lineStyle(2, props.color, 0.3 - i * 0.05)
    membrane.beginFill(props.color, 0.1 + i * 0.05)
    membrane.drawCircle(0, 0, radius)
    membrane.endFill()
    
    cellContainer.addChild(membrane)
  }

  // 创建细胞核 (v7 API)
  const nucleus = new PIXI.Graphics()
  nucleus.beginFill(0xffffff, 0.8)
  nucleus.drawCircle(0, 0, props.radius * 0.3)
  nucleus.endFill()
  cellContainer.addChild(nucleus)

  // 创建细胞内部的颗粒效果 (v7 API)
  for (let i = 0; i < 20; i++) {
    const particle = new PIXI.Graphics()
    const angle = Math.random() * Math.PI * 2
    const distance = Math.random() * props.radius * 0.6
    particle.beginFill(0x88ffcc, 0.6)
    particle.drawCircle(
      Math.cos(angle) * distance,
      Math.sin(angle) * distance,
      2 + Math.random() * 3
    )
    particle.endFill()
    // 使用Object.assign添加自定义属性
    Object.assign(particle, { angle, distance, speed: 0.01 + Math.random() * 0.02 })
    cellContainer.addChild(particle)
  }

  // 创建发光效果 (v7 API)
  const glow = new PIXI.Graphics()
  glow.beginFill(props.color, 0.1)
  glow.drawCircle(0, 0, props.radius * 1.5)
  glow.endFill()
  // 移除BlurFilter以避免类型错误
  cellContainer.addChildAt(glow, 0)

  app.stage.addChild(cellContainer)
}

const startAnimation = () => {
  const animate = () => {
    if (!cellContainer || !app) return

    // 细胞浮动效果
    const time = Date.now() * 0.001
    cellContainer.scale.set(1 + Math.sin(time) * 0.05)
    cellContainer.rotation += 0.002

    // 脉动效果
    const children = cellContainer.children
    children.forEach((child, index) => {
      if (child instanceof PIXI.Graphics && index < 4) {
        const baseScale = 1 + index * 0.1
        const pulse = Math.sin(time * 2 + index) * 0.02
        child.scale.set(baseScale + pulse)
      }
      
      // 粒子运动
      if (child instanceof PIXI.Graphics && 'angle' in child && 'distance' in child && 'speed' in child) {
        const { angle, distance, speed } = child as any
        (child as any).angle += speed
        child.x = Math.cos((child as any).angle) * (child as any).distance
        child.y = Math.sin((child as any).angle) * (child as any).distance
      }
    })

    animationFrame = requestAnimationFrame(animate)
  }
  
  animate()
}

onMounted(() => {
  initPixi()
  
  // 响应窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  console.log('[Cell3D] onUnmounted, destroying app')
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
  if (app) {
    app.destroy(true, { children: true, texture: true })
    app = null
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  if (!app || !containerRef.value) return
  app.renderer.resize(containerRef.value.clientWidth, containerRef.value.clientHeight)
  if (cellContainer) {
    // 使用app.screen获取尺寸
    const screenWidth = app.screen.width || 300
    const screenHeight = app.screen.height || 300
    cellContainer.x = screenWidth / 2
    cellContainer.y = screenHeight / 2
  }
}
</script>

<style scoped>
.cell-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 100%);
}

.cell-canvas {
  width: 100%;
  height: 100%;
}

.cell-info {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-family: 'Arial', sans-serif;
  text-shadow: 0 0 10px rgba(0, 255, 136, 0.8);
}

.cell-name {
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 1px;
}

.cell-score {
  font-size: 12px;
  color: #00ff88;
  background: rgba(0, 255, 136, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}
</style>
