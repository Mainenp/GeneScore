<template>
  <div class="space-y-6">
    <div class="text-center mb-8">
      <h3 class="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-2">
        Drug Sensitivity Analysis
      </h3>
      <p class="text-gray-600">
        Potential therapeutic agents targeting {{ gene?.symbol || gene?.geneSymbol }}
      </p>
    </div>

    <div v-if="!data || data.length === 0" class="text-center py-12 text-gray-500">
      <p class="text-lg">No drug sensitivity data available</p>
    </div>

    <div v-else class="space-y-6">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
          <h4 class="text-xl font-semibold text-gray-800 mb-4">Drug Potency (IC50)</h4>
          <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
          <h4 class="text-xl font-semibold text-gray-800 mb-4">Coverage vs Potency</h4>
          <div ref="scatterChartRef" style="width: 100%; height: 300px;"></div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
        <div class="flex items-center justify-between mb-6">
          <h4 class="text-xl font-semibold text-gray-800">Therapeutic Candidates</h4>
          <span class="text-sm font-normal text-gray-500">{{ data.length }} drugs identified</span>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="(drug, index) in data" 
            :key="index"
            class="group bg-gradient-to-br from-gray-50 to-white p-6 rounded-xl border border-gray-200 hover:shadow-lg transition-all duration-300 cursor-pointer"
          >
            <div class="flex items-start justify-between mb-4">
              <div>
                <h5 class="text-xl font-bold text-gray-800 group-hover:text-indigo-600 transition-colors">
                  {{ drug.drug_name }}
                </h5>
                <p class="text-sm text-purple-600 font-medium mt-1">
                  {{ drug.target_pathway }}
                </p>
              </div>
              <div 
                class="w-12 h-12 rounded-xl flex items-center justify-center shadow-md"
                :class="getDrugColorClass(drug.mean_ic50)"
              >
                <div class="w-4 h-6 rounded-full"></div>
              </div>
            </div>

            <div class="space-y-3 mb-4">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600">IC50</span>
                <span 
                  class="font-bold text-lg"
                  :class="getIC50Class(drug.mean_ic50)"
                >
                  {{ drug.mean_ic50.toFixed(2) }} µM
                </span>
              </div>

              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600">Tested</span>
                <span class="font-semibold text-gray-800">
                  {{ drug.tested_cell_lines }} cell lines
                </span>
              </div>

              <div>
                <span class="text-xs text-gray-500">Targets:</span>
                <p class="text-sm text-gray-700 mt-1 leading-relaxed">
                  {{ drug.target_genes }}
                </p>
              </div>
            </div>

            <div class="pt-4 border-t border-gray-200">
              <span 
                class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-sm font-semibold"
                :class="getSensitivityClass(drug.mean_ic50)"
              >
                {{ getSensitivityLabel(drug.mean_ic50) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-gradient-to-br from-green-50 to-green-100 p-6 rounded-2xl border border-green-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-green-800">Highly Sensitive</span>
          </div>
          <p class="text-4xl font-bold text-green-700">
            {{ data.filter(d => d.mean_ic50 < 1).length }}
          </p>
        </div>

        <div class="bg-gradient-to-br from-yellow-50 to-yellow-100 p-6 rounded-2xl border border-yellow-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-yellow-800">Moderately Sensitive</span>
          </div>
          <p class="text-4xl font-bold text-yellow-700">
            {{ data.filter(d => d.mean_ic50 >= 1 && d.mean_ic50 < 5).length }}
          </p>
        </div>

        <div class="bg-gradient-to-br from-orange-50 to-orange-100 p-6 rounded-2xl border border-orange-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-orange-800">Low Sensitivity</span>
          </div>
          <p class="text-4xl font-bold text-orange-700">
            {{ data.filter(d => d.mean_ic50 >= 5 && d.mean_ic50 < 10).length }}
          </p>
        </div>

        <div class="bg-gradient-to-br from-blue-50 to-blue-100 p-6 rounded-2xl border border-blue-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm font-semibold text-blue-800">Total Tested</span>
          </div>
          <p class="text-4xl font-bold text-blue-700">
            {{ data.reduce((sum, d) => sum + d.tested_cell_lines, 0) }}
          </p>
        </div>
      </div>

      <div class="bg-white p-6 rounded-2xl shadow-xl border border-gray-100">
        <h4 class="text-xl font-semibold text-gray-800 mb-4">Sensitivity Distribution</h4>
        <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
      </div>
    </div>

    <div class="mt-8 p-6 bg-blue-50 rounded-2xl border border-blue-200">
      <h4 class="font-semibold text-blue-800 mb-2">Note</h4>
      <p class="text-blue-700">
        IC50 values indicate the drug concentration required for 50% inhibition. Lower IC50 values indicate higher drug sensitivity. This data is for research purposes only and should not be used for clinical decisions.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { DrugSensitivityData } from '../../types/gene'

interface Props {
  gene: any
  data: DrugSensitivityData[] | null
}

const props = defineProps<Props>()
const barChartRef = ref<HTMLDivElement>()
const scatterChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
let barChart: echarts.ECharts | null = null
let scatterChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const resizeCharts = () => {
  console.log('Resizing drug sensitivity charts')
  if (barChart) {
    barChart.resize()
  }
  if (scatterChart) {
    scatterChart.resize()
  }
  if (pieChart) {
    pieChart.resize()
  }
}

defineExpose({
  resizeCharts
})

const getDrugColorClass = (ic50: number) => {
  if (ic50 < 1) return 'bg-gradient-to-br from-green-400 to-green-600'
  if (ic50 < 5) return 'bg-gradient-to-br from-yellow-400 to-yellow-600'
  if (ic50 < 10) return 'bg-gradient-to-br from-orange-400 to-orange-600'
  return 'bg-gradient-to-br from-red-400 to-red-600'
}

const getIC50Class = (ic50: number) => {
  if (ic50 < 1) return 'text-green-600'
  if (ic50 < 5) return 'text-yellow-600'
  if (ic50 < 10) return 'text-orange-600'
  return 'text-red-600'
}

const getSensitivityClass = (ic50: number) => {
  if (ic50 < 1) return 'bg-green-100 text-green-800'
  if (ic50 < 5) return 'bg-yellow-100 text-yellow-800'
  if (ic50 < 10) return 'bg-orange-100 text-orange-800'
  return 'bg-red-100 text-red-800'
}

const getSensitivityLabel = (ic50: number) => {
  if (ic50 < 1) return 'Highly Sensitive'
  if (ic50 < 5) return 'Moderately Sensitive'
  if (ic50 < 10) return 'Low Sensitivity'
  return 'Resistant'
}

const initCharts = async (attempt = 0) => {
  console.log('initCharts called, attempt:', attempt, 'data:', props.data)
  
  if (!props.data || props.data.length === 0) {
    console.log('No drug data available')
    return
  }
  
  await nextTick()
  
  // 检查所有 DOM 元素是否存在
  if (!barChartRef.value || !scatterChartRef.value || !pieChartRef.value) {
    console.log('Some refs not ready:', { 
      barChartRef: !!barChartRef.value, 
      scatterChartRef: !!scatterChartRef.value, 
      pieChartRef: !!pieChartRef.value 
    })
    // 如果重试次数少于3次，延迟后再试
    if (attempt < 3) {
      setTimeout(() => initCharts(attempt + 1), 200 * (attempt + 1))
    }
    return
  }
  
  if (barChart) {
    barChart.dispose()
  }
  if (scatterChart) {
    scatterChart.dispose()
  }
  if (pieChart) {
    pieChart.dispose()
  }

  const sortedData = [...props.data].sort((a, b) => a.mean_ic50 - b.mean_ic50)

  barChart = echarts.init(barChartRef.value)
  const barOption = {
    tooltip: { 
      trigger: 'axis', 
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        return `${p.name}<br/>IC50: ${p.value.toFixed(2)} µM`
      }
    },
    grid: { left: '10%', right: '10%', bottom: '15%', top: '15%' },
    xAxis: {
      type: 'category',
      data: sortedData.map(d => d.drug_name),
      axisLabel: { 
        fontSize: 10, 
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: 'IC50 (µM)',
      nameTextStyle: { fontWeight: 'bold' }
    },
    series: [{
      type: 'bar',
      data: sortedData.map(d => ({
        value: d.mean_ic50,
        itemStyle: {
          color: d.mean_ic50 < 1 ? '#10b981' : 
                 d.mean_ic50 < 5 ? '#f59e0b' : 
                 d.mean_ic50 < 10 ? '#f97316' : '#ef4444',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: '50%'
    }]
  }
  barChart.setOption(barOption)

  scatterChart = echarts.init(scatterChartRef.value)
  const scatterOption = {
    tooltip: {
      formatter: (params: any) => {
        return `${params.data[2]}<br/>IC50: ${params.data[0].toFixed(2)} µM<br/>Cell Lines: ${params.data[1]}`
      }
    },
    grid: { left: '10%', right: '10%', bottom: '10%', top: '15%' },
    xAxis: {
      type: 'value',
      name: 'IC50 (µM)',
      nameTextStyle: { fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      name: 'Tested Cell Lines',
      nameTextStyle: { fontWeight: 'bold' }
    },
    series: [{
      type: 'scatter',
      symbolSize: (data: number[]) => Math.max(10, Math.min(35, data[1] / 20)),
      data: sortedData.map(d => [
        d.mean_ic50, 
        d.tested_cell_lines,
        d.drug_name
      ]),
      itemStyle: {
        color: (params: any) => {
          const ic50 = params.data[0]
          if (ic50 < 1) return '#10b981'
          if (ic50 < 5) return '#f59e0b'
          if (ic50 < 10) return '#f97316'
          return '#ef4444'
        },
        opacity: 0.75,
        shadowBlur: 6,
        shadowColor: 'rgba(0,0,0,0.15)'
      }
    }]
  }
  scatterChart.setOption(scatterOption)

  pieChart = echarts.init(pieChartRef.value)
  const highlySensitive = sortedData.filter(d => d.mean_ic50 < 1).length
  const moderatelySensitive = sortedData.filter(d => d.mean_ic50 >= 1 && d.mean_ic50 < 5).length
  const lowSensitivity = sortedData.filter(d => d.mean_ic50 >= 5 && d.mean_ic50 < 10).length
  const resistant = sortedData.filter(d => d.mean_ic50 >= 10).length

  const pieOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: highlySensitive, name: 'Highly Sensitive', itemStyle: { color: '#10b981' } },
        { value: moderatelySensitive, name: 'Moderately Sensitive', itemStyle: { color: '#f59e0b' } },
        { value: lowSensitivity, name: 'Low Sensitivity', itemStyle: { color: '#f97316' } },
        { value: resistant, name: 'Resistant', itemStyle: { color: '#ef4444' } }
      ]
    }]
  }
  pieChart.setOption(pieOption)
  console.log('All charts initialized successfully')
}

const handleResize = () => {
  barChart?.resize()
  scatterChart?.resize()
  pieChart?.resize()
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
  pieChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
</style>
