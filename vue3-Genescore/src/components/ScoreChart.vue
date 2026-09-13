<template>
  <div class="chart-container" ref="chartRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as PIXI from 'pixi.js'

interface GeneScore {
  cancerType: string
  geneSymbol: string
  id: number
  score: number
}

interface Props {
  data: GeneScore[]
  chartType?: 'bar' | 'line' | 'radar' | 'pie'
  height?: number
  animated?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  chartType: 'bar',
  height: 300,
  animated: true
})

const chartRef = ref<HTMLElement>()
let app: PIXI.Application | null = null
let chartContainer: PIXI.Container | null = null
const animationDuration = 1000
let startTime: number | null = null

const chartColors = [
  0x00ff88, // 绿色
  0x00aaff, // 蓝色
  0xff6b6b, // 红色
  0xffd93d, // 黄色
  0x6bcb77, // 浅绿
  0x4d96ff, // 浅蓝
  0xff85a1, // 粉色
  0xa8e6cf  // 薄荷绿
]

const initChart = () => {
  console.log('[Chart] initChart called, data length:', props.data.length, 'chartType:', props.chartType)
  if (!chartRef.value || !props.data.length) {
    console.log('[Chart] Abort: no chartRef or no data')
    return
  }

  // 清理旧图表
  if (app) {
    console.log('[Chart] Destroying old app')
    app.destroy(true, { children: true, texture: true })
    app = null
  }

  console.log('[Chart] Creating new PIXI Application')

  // PIXI.js v7 使用不同的初始化方式
  try {
    // 统一使用v7 API初始化
    console.log('[Chart] Using PIXI v7 sync init')
    app = new PIXI.Application({
      width: chartRef.value.clientWidth || 400,
      height: props.height,
      backgroundColor: 0x0a0a1a,
      antialias: true,
      resolution: window.devicePixelRatio || 1
    })
    console.log('[Chart] PIXI Application created successfully')
    
    // 将PIXI创建的canvas添加到我们的容器中
    if (app.view instanceof HTMLElement) {
      chartRef.value.appendChild(app.view)
    } else if (app.view) {
      chartRef.value.appendChild(app.view as Node)
    }
    
    createChart()
    if (props.animated) {
      startAnimation()
    }
  } catch (error) {
    console.error('[Chart] Error creating PIXI app:', error)
  }
}

const createChart = () => {
  if (!app) {
    console.error('[Chart] createChart called but app is null')
    return
  }

  console.log('[Chart] Creating chart, app.screen:', app.screen)
  
  chartContainer = new PIXI.Container()
  chartContainer.x = 60
  chartContainer.y = props.height - 40

  const margin = { top: 30, right: 30, bottom: 60, left: 60 }
  // 使用app.screen获取尺寸
  const screenWidth = app.screen.width || 400
  const screenHeight = app.screen.height || props.height
  const chartWidth = screenWidth - margin.left - margin.right
  const chartHeight = screenHeight - margin.top - margin.bottom

  // 绘制背景网格
  drawGrid(chartContainer, chartWidth, chartHeight, margin)

  // 绘制坐标轴
  drawAxes(chartContainer, chartWidth, chartHeight)

  // 根据图表类型绘制数据
  if (props.chartType === 'bar') {
    drawBarChart(chartContainer, chartWidth, chartHeight, margin)
  } else if (props.chartType === 'line') {
    drawLineChart(chartContainer, chartWidth, chartHeight, margin)
  } else if (props.chartType === 'radar') {
    drawRadarChart(chartContainer, chartWidth, chartHeight, margin)
  } else if (props.chartType === 'pie') {
    drawPieChart(chartContainer, chartWidth, chartHeight)
  }

  // 绘制标题
  drawTitle(chartContainer, chartWidth, margin)

  app.stage.addChild(chartContainer)
}

const drawGrid = (container: PIXI.Container, width: number, height: number, margin: any) => {
  const grid = new PIXI.Graphics()
  
  // 绘制水平网格线 (v7 API)
  for (let i = 0; i <= 5; i++) {
    const y = (height / 5) * i
    grid.moveTo(0, y)
    grid.lineTo(width, y)
    grid.lineStyle(1, 0x2a2a4a, 0.5)
  }

  // 绘制垂直网格线 (v7 API)
  for (let i = 0; i <= props.data.length; i++) {
    const x = (width / props.data.length) * i
    grid.moveTo(x, 0)
    grid.lineTo(x, height)
    grid.lineStyle(1, 0x2a2a4a, 0.3)
  }

  container.addChild(grid)
}

const drawAxes = (container: PIXI.Container, width: number, height: number) => {
  const axes = new PIXI.Graphics()
  
  // X轴 (v7 API)
  axes.lineStyle(2, 0x4a4a6a)
  axes.moveTo(0, height)
  axes.lineTo(width, height)

  // Y轴 (v7 API)
  axes.moveTo(0, 0)
  axes.lineTo(0, height)

  // Y轴标签
  for (let i = 0; i <= 5; i++) {
    const y = (height / 5) * i
    const value = 100 - i * 20
    const label = new PIXI.Text(value.toString(), {
      fontFamily: 'Arial',
      fontSize: 12,
      fill: 0x888888
    })
    label.x = -40
    label.y = y - 6
    container.addChild(label)
  }

  // X轴标签
  props.data.forEach((item, index) => {
    const x = (width / props.data.length) * index + (width / props.data.length) / 2
    const label = new PIXI.Text(item.geneSymbol.length > 8 ? item.geneSymbol.substring(0, 8) + '...' : item.geneSymbol, {
      fontFamily: 'Arial',
      fontSize: 11,
      fill: 0xaaaaaa
    })
    label.anchor.set(0.5, 0)
    label.x = x
    label.y = height + 10
    label.rotation = Math.PI / 6
    container.addChild(label)
  })

  container.addChild(axes)
}

const drawBarChart = (container: PIXI.Container, width: number, height: number, margin: any) => {
  const barWidth = width / props.data.length * 0.6
  const maxScore = Math.max(...props.data.map(d => d.score), 100)

  props.data.forEach((item, index) => {
    const x = (width / props.data.length) * index + (width / props.data.length) / 2 - barWidth / 2
    const barHeight = (item.score / maxScore) * height
    const color = chartColors[index % chartColors.length] || 0x00ff88

    // 创建渐变效果 (v7 API)
    for (let i = 0; i < barHeight; i += 4) {
      const gradientBar = new PIXI.Graphics()
      const alpha = 0.5 + (i / barHeight) * 0.5
      gradientBar.beginFill(color, alpha)
      gradientBar.drawRect(x, -i - 4, barWidth, 4)
      gradientBar.endFill()
      // 使用Object.assign添加自定义属性
      Object.assign(gradientBar, { 
        targetY: -i - 4, 
        delay: i * 2,
        index,
        originalY: 0
      })
      container.addChild(gradientBar)
    }

    // 顶部圆角 (v7 API)
    const topCap = new PIXI.Graphics()
    topCap.beginFill(color, 0.9)
    topCap.drawRoundedRect(x - 2, -barHeight - 6, barWidth + 4, 10, 5)
    topCap.endFill()
    // 使用Object.assign添加自定义属性
    Object.assign(topCap, { targetY: -barHeight - 6, delay: barHeight * 2, index, originalY: 0 })
    container.addChild(topCap)

    // 评分标签
    const scoreLabel = new PIXI.Text(item.score.toFixed(1), {
      fontFamily: 'Arial',
      fontSize: 12,
      fill: color,
      fontWeight: 'bold'
    })
    scoreLabel.anchor.set(0.5, 1)
    scoreLabel.x = x + barWidth / 2
    scoreLabel.y = -barHeight - 12
    // 使用Object.assign添加自定义属性
    Object.assign(scoreLabel, { targetY: -barHeight - 12, delay: barHeight * 2, index, type: 'text', originalY: 0 })
    container.addChild(scoreLabel)

    // 保存初始状态用于动画
    container.children.forEach(child => {
      if (child instanceof PIXI.Graphics || (child as any).type === 'text') {
        const originalY = child.y
        child.y = 0
        // 确保自定义属性已初始化
        if (!(child as any).originalY) {
          (child as any).originalY = originalY
        }
      }
    })
  })
}

const drawLineChart = (container: PIXI.Container, width: number, height: number, margin: any) => {
  const points: { x: number; y: number; color: number; score: number; symbol: string }[] = []
  const maxScore = Math.max(...props.data.map(d => d.score), 100)

  props.data.forEach((item, index) => {
    const x = (width / props.data.length) * index + (width / props.data.length) / 2
    const y = height - (item.score / maxScore) * height
    const color = chartColors[index % chartColors.length] || 0x00ff88
    points.push({ x, y, color, score: item.score, symbol: item.geneSymbol })
  })

  // 绘制连接线 (v7 API)
  const line = new PIXI.Graphics()
  line.lineStyle(3, 0x00ff88, 0.8)
  points.forEach((point, index) => {
    if (index === 0) {
      line.moveTo(point.x, point.y)
    } else {
      line.lineTo(point.x, point.y)
    }
  })
  container.addChild(line)

  // 绘制数据点和发光效果 (v7 API)
  points.forEach((point, index) => {
    // 发光效果
    const glow = new PIXI.Graphics()
    glow.beginFill(point.color, 0.3)
    glow.drawCircle(point.x, point.y, 15)
    glow.endFill()
    container.addChild(glow)

    // 数据点
    const dot = new PIXI.Graphics()
    dot.lineStyle(2, 0xffffff)
    dot.beginFill(point.color)
    dot.drawCircle(point.x, point.y, 8)
    dot.endFill()
    container.addChild(dot)

    // 评分标签
    const label = new PIXI.Text(point.score.toFixed(1), {
      fontFamily: 'Arial',
      fontSize: 11,
      fill: point.color,
      fontWeight: 'bold'
    })
    label.anchor.set(0.5, 1)
    label.x = point.x
    label.y = point.y - 15
    container.addChild(label)
  })
}

const drawRadarChart = (container: PIXI.Container, width: number, height: number, margin: any) => {
  const centerX = width / 2
  const centerY = height / 2
  const radius = Math.min(width, height) / 2 - 40
  const maxScore = Math.max(...props.data.map(d => d.score), 100)
  const sides = props.data.length

  // 绘制背景网格 (v7 API)
  for (let i = 1; i <= 5; i++) {
    const levelRadius = (radius / 5) * i
    const level = new PIXI.Graphics()
    for (let j = 0; j <= sides; j++) {
      const angle = (j / sides) * Math.PI * 2 - Math.PI / 2
      const x = centerX + Math.cos(angle) * levelRadius
      const y = centerY + Math.sin(angle) * levelRadius
      if (j === 0) {
        level.moveTo(x, y)
      } else {
        level.lineTo(x, y)
      }
    }
    level.lineStyle(1, 0x00ff88, 0.2)
    container.addChild(level)
  }

  // 绘制轴线 (v7 API)
  for (let i = 0; i < sides; i++) {
    const angle = (i / sides) * Math.PI * 2 - Math.PI / 2
    const axis = new PIXI.Graphics()
    axis.moveTo(centerX, centerY)
    axis.lineTo(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius)
    axis.lineStyle(1, 0x00ff88, 0.3)
    container.addChild(axis)

    // 标签
    const color = chartColors[i % chartColors.length] || 0x00ff88
    const geneData = props.data[i]
    if (!geneData) return
    const label = new PIXI.Text(geneData.geneSymbol, {
      fontFamily: 'Arial',
      fontSize: 12,
      fill: color,
      fontWeight: 'bold'
    })
    label.anchor.set(0.5, 0.5)
    label.x = centerX + Math.cos(angle) * (radius + 25)
    label.y = centerY + Math.sin(angle) * (radius + 25)
    container.addChild(label)
  }

  // 绘制数据区域 (v7 API)
  const dataPoints = props.data.map((item, i) => {
    const angle = (i / sides) * Math.PI * 2 - Math.PI / 2
    const levelRadius = (item.score / maxScore) * radius
    return {
      x: centerX + Math.cos(angle) * levelRadius,
      y: centerY + Math.sin(angle) * levelRadius,
      color: chartColors[i % chartColors.length]
    }
  })

  const dataPolygon = new PIXI.Graphics()
  dataPolygon.beginFill(0x00ff88, 0.3)
  dataPolygon.lineStyle(2, 0x00ff88, 0.8)
  dataPoints.forEach((point, index) => {
    if (index === 0) {
      dataPolygon.moveTo(point.x, point.y)
    } else {
      dataPolygon.lineTo(point.x, point.y)
    }
  })
  if (dataPoints.length > 0 && dataPoints[0]) {
    dataPolygon.lineTo(dataPoints[0].x, dataPoints[0].y)
  }
  dataPolygon.endFill()
  container.addChild(dataPolygon)

  // 绘制数据点 (v7 API)
  dataPoints.forEach((point, index) => {
    const dot = new PIXI.Graphics()
    const color = point.color || 0x00ff88
    dot.beginFill(color)
    dot.drawCircle(point.x, point.y, 6)
    dot.endFill()
    container.addChild(dot)
  })
}

const drawPieChart = (container: PIXI.Container, width: number, height: number) => {
  const centerX = width / 2
  const centerY = height / 2 - 20
  const radius = Math.min(width, height) / 2 - 60
  
  // 计算总分用于计算百分比
  const totalScore = props.data.reduce((sum, item) => sum + item.score, 0)
  
  let startAngle = -Math.PI / 2 // 从顶部开始
  
  props.data.forEach((item, index) => {
    const sliceAngle = (item.score / totalScore) * Math.PI * 2
    const color = chartColors[index % chartColors.length] || 0x00ff88
    
    // 绘制饼图扇形
    const slice = new PIXI.Graphics()
    
    // 绘制扇形 (v7 API: drawPolygon)
    slice.beginFill(color, 0.8)
    slice.lineStyle(2, 0x1a1a2e)
    slice.drawPolygon([
      centerX, centerY,
      centerX + Math.cos(startAngle) * radius,
      centerY + Math.sin(startAngle) * radius,
      centerX + Math.cos(startAngle + sliceAngle) * radius,
      centerY + Math.sin(startAngle + sliceAngle) * radius
    ])
    slice.endFill()
    // 使用Object.assign添加自定义属性
    Object.assign(slice, { 
      targetScale: 1, 
      delay: index * 100,
      index,
      originalScale: 0.5
    })
    container.addChild(slice)
    
    // 添加高亮效果（鼠标悬停效果）
    const highlight = new PIXI.Graphics()
    highlight.beginFill(color, 0)
    highlight.drawPolygon([
      centerX, centerY,
      centerX + Math.cos(startAngle) * (radius + 10),
      centerY + Math.sin(startAngle) * (radius + 10),
      centerX + Math.cos(startAngle + sliceAngle) * (radius + 10),
      centerY + Math.sin(startAngle + sliceAngle) * (radius + 10)
    ])
    highlight.endFill()
    // 使用Object.assign添加自定义属性
    Object.assign(highlight, { 
      targetAlpha: 0,
      delay: index * 100,
      index 
    })
    container.addChild(highlight)
    
    // 百分比标签
    const percentage = ((item.score / totalScore) * 100).toFixed(1)
    const midAngle = startAngle + sliceAngle / 2
    const labelRadius = radius * 0.7
    const labelX = centerX + Math.cos(midAngle) * labelRadius
    const labelY = centerY + Math.sin(midAngle) * labelRadius
    
    if (percentage > '5') { // 只显示大于5%的标签
      const label = new PIXI.Text(percentage + '%', {
        fontFamily: 'Arial',
        fontSize: 12,
        fill: 0xffffff,
        fontWeight: 'bold'
      })
      label.anchor.set(0.5, 0.5)
      label.x = labelX
      label.y = labelY
      label.alpha = 0
      // 使用Object.assign添加自定义属性
      Object.assign(label, { targetAlpha: 1, delay: index * 100 + 500, type: 'text' })
      container.addChild(label)
    }
    
    // 基因名称标签
    const nameLabel = new PIXI.Text(item.geneSymbol, {
      fontFamily: 'Arial',
      fontSize: 10,
      fill: 0xaaaaaa
    })
    nameLabel.anchor.set(0.5, 0.5)
    nameLabel.x = centerX + Math.cos(midAngle) * (radius + 25)
    nameLabel.y = centerY + Math.sin(midAngle) * (radius + 25)
    container.addChild(nameLabel)
    
    // 绘制图例
    const legendY = height - 15
    const legendItemWidth = width / Math.min(props.data.length, 6)
    const legendIndex = index % Math.min(props.data.length, 6)
    const legendX = legendItemWidth * legendIndex + legendItemWidth / 2
    
    const legendDot = new PIXI.Graphics()
    legendDot.beginFill(color)
    legendDot.drawCircle(legendX - 20, legendY, 6)
    legendDot.endFill()
    container.addChild(legendDot)
    
    const legendLabel = new PIXI.Text(item.geneSymbol.substring(0, 6), {
      fontFamily: 'Arial',
      fontSize: 10,
      fill: 0xaaaaaa
    })
    legendLabel.anchor.set(0, 0.5)
    legendLabel.x = legendX - 10
    legendLabel.y = legendY
    container.addChild(legendLabel)
    
    startAngle += sliceAngle
  })
  
  // 初始化缩放状态
  container.children.forEach((child: any) => {
    if (child.originalScale !== undefined) {
      child.scale.set(child.originalScale)
    }
  })
}

const drawTitle = (container: PIXI.Container, width: number, margin: any) => {
  const title = new PIXI.Text(props.chartType === 'bar' ? '📊 基因评分分布' : 
          props.chartType === 'line' ? '📈 评分趋势分析' : 
          props.chartType === 'pie' ? '🥧 评分占比分析' : '🕸️ 多维度评分对比', {
    fontFamily: 'Arial',
    fontSize: 18,
    fill: 0xffffff,
    fontWeight: 'bold'
  })
  title.anchor.set(0.5, 0)
  title.x = width / 2
  title.y = 5
  container.addChild(title)
}

const startAnimation = () => {
  if (!chartContainer) return

  startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime!
    const progress = Math.min(elapsed / animationDuration, 1)
    const easedProgress = easeOutCubic(progress)

    chartContainer!.children.forEach((child: any) => {
      if (child.delay !== undefined) {
        const delayProgress = Math.max(0, (progress * animationDuration - child.delay) / (animationDuration - child.delay))
        const easedDelayProgress = easeOutCubic(Math.min(1, delayProgress))
        
        if (child.type === 'text') {
          child.alpha = easedDelayProgress
        } else if (child.originalScale !== undefined && child.targetScale !== undefined) {
          // 饼图缩放动画
          const scale = child.originalScale + (child.targetScale - child.originalScale) * easedDelayProgress
          child.scale.set(scale)
        } else if (child.originalY !== undefined) {
          child.y = child.originalY * easedDelayProgress
        }
      }
    })

    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  animate()
}

const easeOutCubic = (x: number): number => {
  return 1 - Math.pow(1 - x, 3)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  console.log('[Chart] onUnmounted, destroying app')
  if (app) {
    app.destroy(true, { children: true, texture: true })
    app = null
  }
  window.removeEventListener('resize', handleResize)
})

watch(() => props.data, () => {
  initChart()
}, { deep: true })

const handleResize = () => {
  initChart()
}
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: v-bind('props.height + "px"');
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(180deg, #0a0a1a 0%, #1a1a2e 100%);
}
</style>
