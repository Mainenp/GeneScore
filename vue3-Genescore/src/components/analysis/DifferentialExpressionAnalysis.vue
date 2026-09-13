<template>
  <div class="space-y-6">
    <div class="text-center mb-8">
      <h3 class="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-2">
        Differential Expression Analysis
      </h3>
      <p class="text-gray-600">
        Compare {{ gene?.symbol || gene?.geneSymbol }} expression between normal and tumor tissues
      </p>
    </div>

    <div v-if="!data" class="text-center py-12 text-gray-500">
      <p class="text-lg">No expression data available</p>
    </div>

    <div v-else class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="group bg-gradient-to-br from-blue-50 to-blue-100 p-6 rounded-2xl border border-blue-200 hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-blue-800">log2 Fold Change</span>
          </div>
          <p :class="getLog2FCClass(data.log2FC)" class="text-4xl font-bold">
            {{ data.log2FC.toFixed(2) }}
          </p>
        </div>
        
        <div class="group bg-gradient-to-br from-green-50 to-green-100 p-6 rounded-2xl border border-green-200 hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-green-800">P-value</span>
          </div>
          <p :class="getPValueClass(data.p_value)" class="text-4xl font-bold">
            {{ formatPValue(data.p_value) }}
          </p>
        </div>
        
        <div class="group bg-gradient-to-br from-purple-50 to-purple-100 p-6 rounded-2xl border border-purple-200 hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-purple-800">Status</span>
          </div>
          <p :class="getStatusClass(data.status)" class="text-4xl font-bold">
            {{ data.status }}
          </p>
        </div>
        
        <div class="group bg-gradient-to-br from-orange-50 to-orange-100 p-6 rounded-2xl border border-orange-200 hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-orange-800">Total Samples</span>
          </div>
          <p class="text-4xl font-bold text-gray-700">
            {{ data.tumor_count + data.normal_count }}
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
          <h4 class="text-xl font-semibold text-gray-800 mb-4">Expression Levels</h4>
          <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
          <h4 class="text-xl font-semibold text-gray-800 mb-4">Significance Plot</h4>
          <div ref="scatterChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-green-100 rounded-full -mr-16 -mt-16"></div>
          <div class="relative z-10">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-14 h-14 bg-green-500 rounded-xl flex items-center justify-center shadow-lg">
                <div class="w-8 h-8 bg-green-300 rounded-full"></div>
              </div>
              <div>
                <h4 class="text-lg font-bold text-green-800">Normal Tissue</h4>
                <p class="text-gray-500 text-sm">{{ data.normal_count }} samples</p>
              </div>
            </div>
            <div class="mt-4">
              <div class="text-5xl font-bold text-green-600 mb-2">
                {{ data.normal_mean.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-red-100 rounded-full -mr-16 -mt-16"></div>
          <div class="relative z-10">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-14 h-14 bg-red-500 rounded-xl flex items-center justify-center shadow-lg">
                <div class="w-8 h-8 bg-red-300 rounded-full"></div>
              </div>
              <div>
                <h4 class="text-lg font-bold text-red-800">Tumor Tissue</h4>
                <p class="text-gray-500 text-sm">{{ data.tumor_count }} samples</p>
              </div>
            </div>
            <div class="mt-4">
              <div class="text-5xl font-bold text-red-600 mb-2">
                {{ data.tumor_mean.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-gradient-to-r from-indigo-50 to-purple-50 p-8 rounded-2xl border border-indigo-100">
        <h4 class="text-xl font-bold text-gray-800 mb-4">Biological Interpretation</h4>
        <p class="text-gray-700 text-lg leading-relaxed mb-6">
          {{ getInterpretation(data.log2FC, data.p_value, data.status) }}
        </p>
      </div>
    </div>

    <div class="mt-8 p-6 bg-blue-50 rounded-2xl border border-blue-200">
      <h4 class="font-semibold text-blue-800 mb-2">Note</h4>
      <p class="text-blue-700">
        Positive log2 fold change indicates up-regulation in tumor. Negative log2 fold change indicates down-regulation in tumor.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ExpressionAnalysis } from '../../types/gene'

interface Props {
  gene: any
  data: ExpressionAnalysis | null
}

const props = defineProps<Props>()
const barChartRef = ref<HTMLDivElement>()
const scatterChartRef = ref<HTMLDivElement>()
let barChart: echarts.ECharts | null = null
let scatterChart: echarts.ECharts | null = null

const resizeCharts = () => {
  console.log('Resizing differential expression charts')
  if (barChart) {
    barChart.resize()
  }
  if (scatterChart) {
    scatterChart.resize()
  }
}

defineExpose({
  resizeCharts
})

const getLog2FCClass = (fc: number) => {
  if (fc > 1) return 'text-red-600'
  if (fc < -1) return 'text-green-600'
  return 'text-gray-700'
}

const getPValueClass = (p: number) => {
  if (p < 0.01) return 'text-green-600'
  if (p < 0.05) return 'text-blue-600'
  return 'text-gray-500'
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'Up':
      return 'text-red-600'
    case 'Down':
      return 'text-green-600'
    default:
      return 'text-gray-600'
  }
}

const formatPValue = (p: number) => {
  if (p < 0.0001) return p.toExponential(2)
  return p.toFixed(4)
}

const getInterpretation = (fc: number, p: number, status: string) => {
  if (p >= 0.05) {
    return 'No significant differential expression was observed between normal and tumor tissues. The gene expression pattern may be context-dependent or require larger sample sizes for detection.'
  }

  if (status === 'Up' && fc > 1) {
    return 'This gene is significantly up-regulated in tumor tissues. The elevated expression suggests it may function as an oncogene, promoting tumor growth and progression.'
  }

  if (status === 'Down' && fc < -1) {
    return 'This gene is significantly down-regulated in tumor tissues. Reduced expression suggests potential tumor suppressor functions, with loss of expression contributing to oncogenesis.'
  }

  return 'The expression pattern of this gene in cancer requires additional functional studies to determine its biological significance.'
}

const initCharts = async () => {
  console.log('initCharts called')
  
  if (!props.data) {
    console.log('No data')
    return
  }
  
  await nextTick()
  
  console.log('barChartRef:', barChartRef.value)
  console.log('scatterChartRef:', scatterChartRef.value)
  
  if (!barChartRef.value || !scatterChartRef.value) {
    console.log('Refs not ready')
    return
  }

  if (barChart) {
    barChart.dispose()
  }
  if (scatterChart) {
    scatterChart.dispose()
  }

  barChart = echarts.init(barChartRef.value)
  const barOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '10%', right: '10%', bottom: '10%', top: '15%' },
    xAxis: {
      type: 'category',
      data: ['Normal', 'Tumor'],
      axisLabel: { fontSize: 14, fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      name: 'Expression Level',
      nameTextStyle: { fontWeight: 'bold' }
    },
    series: [{
      type: 'bar',
      data: [
        { value: props.data.normal_mean, itemStyle: { color: '#10b981' } },
        { value: props.data.tumor_mean, itemStyle: { color: '#ef4444' } }
      ],
      barWidth: '40%',
      itemStyle: {
        borderRadius: [8, 8, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 14,
        fontWeight: 'bold'
      }
    }]
  }
  barChart.setOption(barOption)
  console.log('Bar chart initialized')

  scatterChart = echarts.init(scatterChartRef.value)
  const scatterOption = {
    tooltip: {
      formatter: (params: any) => {
        return `log2FC: ${params.data[0].toFixed(2)}<br/>-log10(p): ${params.data[1].toFixed(2)}`
      }
    },
    grid: { left: '10%', right: '10%', bottom: '10%', top: '15%' },
    xAxis: {
      type: 'value',
      name: 'log2 Fold Change',
      nameTextStyle: { fontWeight: 'bold' },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '-log10(P-value)',
      nameTextStyle: { fontWeight: 'bold' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'scatter',
      data: [[props.data.log2FC, -Math.log10(Math.max(props.data.p_value, 0.00001))]],
      symbolSize: 30,
      itemStyle: {
        color: props.data.p_value < 0.05 ? (props.data.log2FC > 0 ? '#ef4444' : '#10b981') : '#9ca3af',
        shadowBlur: 10,
        shadowColor: 'rgba(0,0,0,0.2)'
      },
      label: {
        show: true,
        position: 'right',
        formatter: props.data.status,
        fontWeight: 'bold'
      }
    }]
  }
  scatterChart.setOption(scatterOption)
  console.log('Scatter chart initialized')
}

const handleResize = () => {
  barChart?.resize()
  scatterChart?.resize()
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
  barChart?.dispose()
  scatterChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
</style>
