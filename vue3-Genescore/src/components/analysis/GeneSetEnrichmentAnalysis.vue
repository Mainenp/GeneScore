<template>
  <div class="p-6">
    <h3 class="text-xl font-bold text-gray-800 mb-4">Gene Set Enrichment Analysis</h3>
    <p class="text-gray-600 mb-6">
      This analysis shows pathways and biological processes that are significantly enriched with genes co-expressed or functionally related to {{ gene?.symbol || gene?.geneSymbol }}.
    </p>
    
    <div v-if="!data || data.length === 0" class="text-center py-8 text-gray-500">
      <p>No enrichment data available</p>
    </div>
    
    <div v-else>
      <!-- Visualization Chart -->
      <div class="mb-8">
        <h4 class="text-lg font-semibold text-gray-700 mb-4">Enrichment Score by Pathway</h4>
        <div ref="chartRef" class="w-full h-80 bg-white rounded-lg shadow"></div>
      </div>
      
      <!-- Data Table -->
      <div class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-gray-50">
              <th class="text-left p-3 border-b font-semibold text-gray-700">Pathway</th>
              <th class="text-left p-3 border-b font-semibold text-gray-700">Gene Count</th>
              <th class="text-left p-3 border-b font-semibold text-gray-700">Enrichment Score</th>
              <th class="text-left p-3 border-b font-semibold text-gray-700">p-value</th>
              <th class="text-left p-3 border-b font-semibold text-gray-700">Significance</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in data" :key="index" class="hover:bg-gray-50">
              <td class="p-3 border-b font-medium text-blue-700">{{ item.pathway }}</td>
              <td class="p-3 border-b">{{ item.geneCount }}</td>
              <td class="p-3 border-b">
                <span :class="getEnrichmentScoreClass(item.enrichmentScore)" class="font-medium">
                  {{ item.enrichmentScore.toFixed(2) }}
                </span>
              </td>
              <td class="p-3 border-b">
                <span :class="getPValueClass(item.pValue)" class="font-medium">
                  {{ formatPValue(item.pValue) }}
                </span>
              </td>
              <td class="p-3 border-b">
                <span :class="getSignificanceClass(getSignificance(item.pValue))" class="px-2 py-1 rounded-full text-sm font-medium">
                  {{ getSignificance(item.pValue) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <div class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
      <h4 class="font-semibold text-blue-800 mb-2">Note</h4>
      <p class="text-blue-700 text-sm">
        Higher enrichment scores indicate stronger association between {{ gene?.symbol || gene?.geneSymbol }} and the pathway. 
        Lower p-values indicate higher statistical significance.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue';
import * as echarts from 'echarts';

interface Props {
  gene: any;
  data: any[] | null;
}

const props = defineProps<Props>();
const chartRef = ref<HTMLElement>();
let chartInstance: echarts.ECharts | null = null;

const initChart = () => {
  if (!chartRef.value || !props.data || props.data.length === 0) return;
  
  if (chartInstance) {
    chartInstance.dispose();
  }
  
  chartInstance = echarts.init(chartRef.value);
  
  const pathways = props.data.map(item => item.pathway);
  const enrichmentScores = props.data.map(item => item.enrichmentScore);
  const colors = props.data.map(item => {
    if (item.pValue < 0.0001) return '#8b5cf6';
    if (item.pValue < 0.001) return '#22c55e';
    if (item.pValue < 0.01) return '#3b82f6';
    if (item.pValue < 0.05) return '#eab308';
    return '#6b7280';
  });
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const item = props.data![params[0].dataIndex];
        return `
          <div style="font-weight: bold;">${item.pathway}</div>
          <div>Enrichment Score: ${item.enrichmentScore.toFixed(2)}</div>
          <div>Gene Count: ${item.geneCount}</div>
          <div>p-value: ${formatPValue(item.pValue)}</div>
        `;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: 'Enrichment Score',
      nameLocation: 'middle',
      nameGap: 40
    },
    yAxis: {
      type: 'category',
      data: pathways,
      axisLabel: {
        interval: 0,
        rotate: 0,
        fontSize: 11
      }
    },
    series: [
      {
        name: 'Enrichment Score',
        type: 'bar',
        data: enrichmentScores.map((score, idx) => ({
          value: score,
          itemStyle: { color: colors[idx] }
        })),
        label: {
          show: true,
          position: 'right',
          formatter: '{c}'
        },
        markPoint: {
          data: [
            { type: 'max', name: 'Max' }
          ]
        }
      }
    ]
  };
  
  chartInstance.setOption(option);
};

const getEnrichmentScoreClass = (score: number) => {
  if (score > 2) return 'text-purple-600';
  if (score > 1.5) return 'text-blue-600';
  return 'text-gray-700';
};

const getPValueClass = (p: number) => {
  if (p < 0.0001) return 'text-purple-600';
  if (p < 0.001) return 'text-green-600';
  if (p < 0.01) return 'text-blue-600';
  return 'text-gray-500';
};

const formatPValue = (p: number) => {
  if (p < 0.0001) return p.toExponential(2);
  return p.toFixed(6);
};

const getSignificance = (p: number) => {
  if (p < 0.0001) return '****';
  if (p < 0.001) return '***';
  if (p < 0.01) return '**';
  if (p < 0.05) return '*';
  return 'ns';
};

const getSignificanceClass = (significance: string) => {
  switch (significance) {
    case '****':
      return 'bg-purple-100 text-purple-800';
    case '***':
      return 'bg-green-100 text-green-800';
    case '**':
      return 'bg-blue-100 text-blue-800';
    case '*':
      return 'bg-yellow-100 text-yellow-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', handleResize);
});

watch(() => props.data, () => {
  initChart();
}, { deep: true });
</script>

<style scoped>
</style>
