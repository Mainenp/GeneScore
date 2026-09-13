<template>
  <div class="space-y-6">
    <div class="text-center mb-8">
      <h3 class="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-2">
        Prognosis Analysis
      </h3>
      <p class="text-gray-600">
        Survival prognosis associated with {{ gene?.symbol || gene?.geneSymbol }} expression
      </p>
    </div>

    <div v-if="!data" class="text-center py-12 text-gray-500">
      <p class="text-lg">No prognosis data available</p>
    </div>

    <div v-else class="space-y-6">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
          <h4 class="text-xl font-semibold text-gray-800 mb-4">Hazard Ratio</h4>
          <div ref="gaugeChartRef" style="width: 100%; height: 300px;"></div>
        </div>

        <div class="space-y-4">
          <div class="bg-gradient-to-br from-blue-50 to-blue-100 p-6 rounded-2xl border border-blue-200">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-semibold text-blue-800">Hazard Ratio</span>
            </div>
            <p :class="getHRClass(data.hazard_ratio)" class="text-5xl font-bold">
              {{ data.hazard_ratio.toFixed(2) }}
            </p>
          </div>

          <div class="bg-gradient-to-br from-green-50 to-green-100 p-6 rounded-2xl border border-green-200">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-semibold text-green-800">P-value</span>
            </div>
            <p :class="getPValueClass(data.p_value)" class="text-5xl font-bold">
              {{ formatPValue(data.p_value) }}
            </p>
          </div>

          <div class="bg-gradient-to-br from-purple-50 to-purple-100 p-6 rounded-2xl border border-purple-200">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-semibold text-purple-800">Risk Level</span>
            </div>
            <p :class="getRiskClass(data.risk)" class="text-4xl font-bold">
              {{ data.risk }}
            </p>
          </div>

          <div class="bg-gradient-to-br from-orange-50 to-orange-100 p-6 rounded-2xl border border-orange-200">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm font-semibold text-orange-800">Sample Size</span>
            </div>
            <p class="text-5xl font-bold text-gray-700">
              {{ data.sample_size }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-gradient-to-r from-indigo-50 to-purple-50 p-8 rounded-2xl border border-indigo-100">
        <h4 class="text-xl font-bold text-gray-800 mb-4">Clinical Interpretation</h4>
        <p class="text-gray-700 text-lg leading-relaxed mb-6">
          {{ getInterpretation(data.hazard_ratio, data.p_value, data.risk) }}
        </p>
      </div>
    </div>

    <div class="mt-8 p-6 bg-blue-50 rounded-2xl border border-blue-200">
      <h4 class="font-semibold text-blue-800 mb-2">Note</h4>
      <p class="text-blue-700">
        Hazard Ratio > 1 indicates poor prognosis (higher expression associated with worse survival). Hazard Ratio < 1 indicates good prognosis (higher expression associated with better survival).
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { PrognosisAnalysis } from '../../types/gene'

interface Props {
  gene: any
  data: PrognosisAnalysis | null
}

const props = defineProps<Props>()
const gaugeChartRef = ref<HTMLDivElement>()
let gaugeChart: echarts.ECharts | null = null

const resizeCharts = () => {
  console.log('Resizing prognosis chart')
  if (gaugeChart) {
    gaugeChart.resize()
  }
}

defineExpose({
  resizeCharts
})

const getHRClass = (hr: number) => {
  if (hr > 2) return 'text-red-600'
  if (hr > 1.5) return 'text-orange-600'
  if (hr < 0.7) return 'text-green-600'
  return 'text-gray-700'
}

const getPValueClass = (p: number) => {
  if (p < 0.01) return 'text-green-600'
  if (p < 0.05) return 'text-blue-600'
  return 'text-gray-500'
}

const getRiskClass = (risk: string) => {
  if (risk.includes('High')) return 'text-red-600'
  if (risk.includes('Protective') || risk.includes('Low')) return 'text-green-600'
  return 'text-gray-600'
}

const formatPValue = (p: number) => {
  if (p < 0.0001) return p.toExponential(2)
  return p.toFixed(4)
}

const getInterpretation = (hr: number, p: number, risk: string) => {
  if (p >= 0.05) {
    return 'The association between gene expression and patient survival is not statistically significant. Further validation with larger cohorts may be needed.'
  }

  if ((risk === 'High Risk' || risk === 'High') && hr > 1.5) {
    return 'This gene is strongly associated with poor prognosis. High expression is significantly correlated with worse patient survival, suggesting it may be a potential therapeutic target or prognostic biomarker.'
  }

  if ((risk === 'High Risk' || risk === 'High')) {
    return 'Elevated expression of this gene is associated with worse clinical outcomes. It may serve as a prognostic marker for patient risk stratification in clinical settings.'
  }

  if ((risk === 'Protective' || risk === 'Low') && hr < 0.7) {
    return 'High expression of this gene shows a strong protective effect. Patients with higher expression tend to have significantly better survival outcomes, indicating potential tumor suppressor functions.'
  }

  if ((risk === 'Protective' || risk === 'Low')) {
    return 'This gene appears to have a protective effect. Higher expression is associated with improved patient survival, suggesting potential beneficial roles in tumor suppression or treatment response.'
  }

  return 'The prognostic significance of this gene requires further investigation in additional patient cohorts to determine its clinical utility.'
}

const initCharts = async (attempt = 0) => {
  console.log('initCharts called, attempt:', attempt)
  
  if (!props.data) {
    console.log('No data available')
    return
  }
  
  await nextTick()
  
  // 检查DOM元素是否存在
  if (!gaugeChartRef.value) {
    console.log('Gauge ref not ready')
    // 如果重试次数少于3次，延迟后再试
    if (attempt < 3) {
      setTimeout(() => initCharts(attempt + 1), 200 * (attempt + 1))
    }
    return
  }
  
  if (gaugeChart) {
    gaugeChart.dispose()
  }

  gaugeChart = echarts.init(gaugeChartRef.value)
  const gaugeOption = {
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 3,
      splitNumber: 6,
      radius: '70%',
      center: ['50%', '60%'],
      axisLine: {
        lineStyle: {
          width: 20,
          color: [
            [0.33, '#10b981'],
            [0.67, '#f59e0b'],
            [1, '#ef4444']
          ]
        }
      },
      pointer: {
        itemStyle: { color: '#1f2937' },
        offsetCenter: [0, '-15%'],
        width: 8
      },
      axisTick: {
        distance: -20,
        length: 6,
        lineStyle: { color: '#fff', width: 2 }
      },
      splitLine: {
        distance: -25,
        length: 12,
        lineStyle: { color: '#fff', width: 3 }
      },
      axisLabel: {
        color: '#374151',
        distance: 25,
        fontSize: 12,
        fontWeight: 'bold'
      },
      detail: {
        valueAnimation: true,
        formatter: '{value:.2f}',
        color: '#1f2937',
        fontSize: 28,
        fontWeight: 'bold',
        offsetCenter: [0, '20%']
      },
      title: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#374151',
        offsetCenter: [0, '80%']
      },
      data: [{
        value: props.data.hazard_ratio,
        name: 'Hazard Ratio'
      }]
    }]
  }
  gaugeChart.setOption(gaugeOption)
  console.log('Gauge chart initialized successfully')
}

const handleResize = () => {
  gaugeChart?.resize()
}

onMounted(() => {
  console.log('Component mounted')
  window.addEventListener('resize', handleResize)
})

watch(() => props.data, (newData) => {
  console.log('Data changed:', newData)
  if (newData) {
    initCharts()
  }
}, { immediate: true })

onUnmounted(() => {
  gaugeChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
</style>
