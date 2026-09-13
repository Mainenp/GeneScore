<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-white/95 backdrop-blur-sm border-b border-gray-200 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <router-link to="/" class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            GeneScoreDB
          </router-link>
          <router-link to="/" class="text-gray-600 hover:text-blue-600 transition-colors flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Back to Home
          </router-link>
        </div>
      </div>
    </nav>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <!-- 标题区域 -->
      <div class="text-center mb-12">
        <div class="inline-flex items-center gap-2 px-4 py-2 bg-blue-50 border border-blue-200 rounded-full mb-6">
          <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
          </svg>
          <span class="text-blue-700 text-sm font-medium">Multi-Gene Knockout Analysis</span>
        </div>
        <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
          Synthetic Lethality Predictor
        </h1>
        <p class="text-xl text-gray-600 max-w-2xl mx-auto">
          Predict genetic interactions and synthetic lethality using advanced deep learning models
        </p>
      </div>

      <!-- 输入区域 -->
      <div class="bg-white rounded-3xl shadow-lg border border-gray-200 p-8 mb-8">
        <h2 class="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-3">
          <svg class="w-7 h-7 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"/>
          </svg>
          Enter Target Genes
        </h2>

        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-3">
            Gene Symbols (separate with commas, spaces, or newlines)
          </label>
          <textarea
            v-model="geneInput"
            placeholder="e.g., TP53, EGFR"
            class="w-full px-5 py-4 bg-gray-50 border-2 border-gray-200 rounded-2xl focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 text-gray-900 text-lg placeholder-gray-500 resize-none transition-all"
            rows="3"
            @keydown.ctrl.enter="runAnalysis"
          ></textarea>
        </div>

        <!-- 已输入的基因标签 -->
        <div v-if="parsedGenes.length > 0" class="mb-6">
          <p class="text-sm text-gray-600 mb-3">Parsed Genes:</p>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="(gene, index) in parsedGenes"
              :key="index"
              class="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-blue-100 to-purple-100 border border-blue-200 rounded-full text-blue-800 text-sm"
            >
              <span class="font-semibold">{{ gene }}</span>
              <button
                @click="removeGene(index)"
                class="hover:text-blue-600 transition-colors"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-4">
          <button
            @click="runAnalysis"
            :disabled="parsedGenes.length < 2 || isLoading"
            class="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 disabled:from-gray-400 disabled:to-gray-400 text-white font-bold py-4 px-8 rounded-2xl transition-all shadow-lg hover:shadow-blue-500/25 hover:scale-[1.01] disabled:cursor-not-allowed disabled:hover:scale-100"
          >
            <span v-if="!isLoading" class="flex items-center justify-center gap-3">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0a9 9 0 0118 0z"/>
              </svg>
              Run Prediction Analysis
            </span>
            <span v-else class="flex items-center justify-center gap-3">
              <svg class="animate-spin w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Analyzing...
            </span>
          </button>

          <button
            @click="clearInput"
            class="px-6 py-4 bg-gray-100 hover:bg-gray-200 border border-gray-200 text-gray-600 hover:text-gray-900 rounded-2xl transition-all"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 炫酷加载动画 -->
      <transition name="fade">
        <div v-if="isLoading" class="fixed inset-0 z-50 flex items-center justify-center bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
          <div class="text-center">
            <!-- DNA 双螺旋动画 -->
            <div class="dna-helix mb-8">
              <div v-for="i in 12" :key="i" class="dna-base" :style="getDnaBaseStyle(i)"></div>
            </div>

            <h2 class="text-3xl font-bold bg-gradient-to-r from-blue-400 via-purple-400 to-pink-400 bg-clip-text text-transparent mb-4">
              Analyzing Genetic Interactions
            </h2>
            <p class="text-gray-400 text-lg mb-2">
              This may take a few minutes...
            </p>
            <p class="text-gray-500 text-sm">
              {{ loadingMessage }}
            </p>

            <!-- 进度条 -->
            <div class="mt-8 w-80 mx-auto">
              <div class="h-2 bg-slate-700 rounded-full overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 rounded-full transition-all duration-300"
                  :style="{ width: `${progress}%` }"
                ></div>
              </div>
              <p class="text-gray-500 text-sm mt-2">{{ progress }}%</p>
            </div>
          </div>
        </div>
      </transition>

      <!-- 错误提示 -->
      <transition name="fade">
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-2xl p-6 mb-8">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0">
              <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0a9 9 0 0118 0z"/>
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-red-800 mb-1">Analysis Failed</h3>
              <p class="text-red-700">{{ error }}</p>
            </div>
            <button
              @click="error = ''"
              class="text-red-600 hover:text-red-800"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>
      </transition>

      <!-- 结果展示区域 -->
      <transition name="fade">
        <div v-if="analysisResult" class="space-y-8">
          <!-- 成功提示 -->
          <div class="bg-green-50 border border-green-200 rounded-2xl p-6">
            <div class="flex items-center gap-4">
              <div class="flex-shrink-0">
                <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0a9 9 0 0118 0z"/>
                </svg>
              </div>
              <div>
                <h3 class="text-xl font-bold text-green-800">Analysis Complete!</h3>
                <p class="text-green-700">Successfully analyzed {{ analysisResult.valid_genes?.length || 0 }} genes</p>
              </div>
            </div>
          </div>

          <!-- ECharts 热力图 -->
          <div v-if="analysisResult.scores" class="bg-white rounded-3xl shadow-lg border border-gray-200 p-6">
            <h3 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
              </svg>
              In-silico Double Knockout Lethality Score
            </h3>
            <div ref="heatmapRef" class="w-full" style="height: 700px;"></div>
          </div>

          <!-- 分数表格 -->
          <div v-if="analysisResult.scores" class="bg-white rounded-3xl shadow-lg border border-gray-200 p-6">
            <h3 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
              Interaction Scores
            </h3>
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead>
                  <tr class="border-b border-gray-200">
                    <th class="text-left py-4 px-4 text-gray-600 font-semibold text-sm uppercase tracking-wider">Gene</th>
                    <th v-for="gene in analysisResult.valid_genes" :key="gene" class="text-center py-4 px-4 text-gray-600 font-semibold text-sm uppercase tracking-wider">
                      {{ gene }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, gene1) in analysisResult.scores" :key="gene1" class="border-b border-gray-100 hover:bg-blue-50">
                    <td class="py-4 px-4 font-semibold text-gray-900">{{ gene1 }}</td>
                    <td v-for="gene2 in analysisResult.valid_genes" :key="gene2" class="text-center py-4 px-4">
                      <span :class="getScoreClass(row[gene2])" class="font-mono text-sm">
                        {{ formatTableNumber(row[gene2]) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="mt-4 flex flex-wrap gap-4 text-sm">
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full" style="background-color: #a50026;"></span>
                <span class="text-gray-600">Strong Negative (&lt; -0.5)</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full" style="background: linear-gradient(to right, #ffd3b6, #d4e5f7);"></span>
                <span class="text-gray-600">Near Zero</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full" style="background-color: #4575b4;"></span>
                <span class="text-gray-600">Strong Positive (&gt; 0.5)</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useGeneStore } from '../stores/gene';
import * as echarts from 'echarts';

const geneStore = useGeneStore();

const geneInput = ref('');
const isLoading = ref(false);
const error = ref('');
const analysisResult = ref<any>(null);
const progress = ref(0);
const loadingMessage = ref('Initializing deep learning model...');
const heatmapRef = ref<HTMLElement | null>(null);
let heatmapChart: echarts.ECharts | null = null;

const parsedGenes = computed(() => {
  if (!geneInput.value.trim()) return [];
  return geneInput.value
    .split(/[,\s\n]+/)
    .map(g => g.trim().toUpperCase())
    .filter(g => g.length > 0);
});

const getDnaBaseStyle = (i: number) => {
  const angle = (i / 12) * Math.PI * 2;
  const y = i * 20 - 100;
  return {
    '--i': i,
    transform: `translateY(${y}px) rotate(${angle}rad)`,
  };
};

const getScoreClass = (score: number) => {
  if (!score && score !== 0) return 'text-gray-500';
  if (score < -0.5) return 'text-red-700';
  if (score < 0) return 'text-red-500';
  if (score < 0.5) return 'text-blue-500';
  return 'text-blue-700';
};

const formatTableNumber = (num: number) => {
  if (num === undefined || num === null) return 'N/A';
  if (num === 0) return '0';
  const absNum = Math.abs(num);
  if (absNum < 0.01 && absNum > 0) {
    return num.toExponential(2);
  }
  return num.toFixed(4);
};

const removeGene = (index: number) => {
  const genes = [...parsedGenes.value];
  genes.splice(index, 1);
  geneInput.value = genes.join(', ');
};

const clearInput = () => {
  geneInput.value = '';
  analysisResult.value = null;
  error.value = '';
};

const startProgressAnimation = (geneCount: number) => {
  progress.value = 0;
  const totalTimeSeconds = geneCount * 30;
  const messages = [
    'Initializing deep learning model...',
    'Loading gene interaction network...',
    'Processing gene expression data...',
    'Calculating co-dependency scores...',
    'Analyzing synthetic lethality...',
    'Generating heatmap visualization...',
    'Finalizing results...',
  ];
  let messageIndex = 0;
  
  const intervalTime = 200; // 更新频率 200ms
  const totalSteps = (totalTimeSeconds * 1000) / intervalTime;
  const incrementPerStep = 100 / totalSteps;
  
  const interval = setInterval(() => {
    if (progress.value < 99) {
      const newProgress = progress.value + incrementPerStep;
      progress.value = Math.floor(newProgress);
      // 确保不超过99，最后保留一点给实际完成
      if (progress.value > 99) {
        progress.value = 99;
      }
      // 更新消息
      const progressPercent = progress.value;
      if (progressPercent > (messageIndex + 1) * 15 && messageIndex < messages.length - 1) {
        messageIndex++;
        loadingMessage.value = messages[messageIndex];
      }
    }
  }, intervalTime);
  
  return () => clearInterval(interval);
};

const formatNumber = (num: number) => {
  if (num === 0) return '0';
  const absNum = Math.abs(num);
  if (absNum < 0.01 && absNum > 0) {
    return num.toExponential(2);
  }
  return num.toFixed(4);
};

const renderHeatmap = () => {
  if (!heatmapRef.value || !analysisResult.value) return;
  
  if (heatmapChart) {
    heatmapChart.dispose();
  }
  
  heatmapChart = echarts.init(heatmapRef.value);
  
  const genes = analysisResult.value.valid_genes || [];
  const scores = analysisResult.value.scores || {};
  
  const data: [number, number, number][] = [];
  genes.forEach((gene1: string, i: number) => {
    genes.forEach((gene2: string, j: number) => {
      const value = scores[gene1]?.[gene2] ?? 0;
      data.push([i, j, value]);
    });
  });
  
  const option = {
    title: {
      text: `In-silico Double Knockout Lethality Score (Top ${genes.length} Genes)`,
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      position: 'top',
      formatter: (params: any) => {
        const gene1 = genes[params.data[0]];
        const gene2 = genes[params.data[1]];
        const score = params.data[2];
        return `${gene1} × ${gene2}<br/>Score: ${formatNumber(score)}`;
      }
    },
    grid: {
      height: '60%',
 top: '15%',
      left: '15%',
      right: '10%'
    },
    xAxis: {
      type: 'category',
      data: genes,
      splitArea: {
        show: true
      },
      axisLabel: {
        rotate: 45,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'category',
      data: genes,
      splitArea: {
        show: true
      },
      axisLabel: {
        fontSize: 12
      }
    },
    visualMap: {
      min: -1,
      max: 1,
      calculable: true,
      orient: 'vertical',
      right: '3%',
      top: '15%',
      inRange: {
        color: [
          '#a50026',
          '#d73027',
          '#f46d43',
          '#fdae61',
          '#ffd3b6',
          '#d4e5f7',
          '#abd9e9',
          '#74add1',
          '#4575b4',
          '#313695'
        ]
      }
    },
    series: [
      {
        name: 'Lethality Score',
        type: 'heatmap',
        data: data,
        label: {
          show: true,
          formatter: (params: any) => formatNumber(params.data[2]),
          fontSize: 9
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  heatmapChart.setOption(option);
};

const runAnalysis = async () => {
  if (parsedGenes.value.length < 2) {
    error.value = 'Please enter at least 2 genes';
    return;
  }

  isLoading.value = true;
  error.value = '';
  analysisResult.value = null;
  
  const stopProgress = startProgressAnimation(parsedGenes.value.length);

  try {
    const result = await geneStore.analyzeDKO(parsedGenes.value);
    if (result && result.status === 'success') {
      analysisResult.value = result;
      await nextTick();
      renderHeatmap();
    } else {
      error.value = result?.error || 'Analysis failed, please try again';
    }
  } catch (err: any) {
    error.value = err.message || 'An error occurred during analysis';
  } finally {
    stopProgress();
    progress.value = 100;
    isLoading.value = false;
  }
};

const handleResize = () => {
  if (heatmapChart) {
    heatmapChart.resize();
  }
};

onMounted(() => {
  geneInput.value = 'TP53, EGFR';
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (heatmapChart) {
    heatmapChart.dispose();
  }
});
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* DNA 双螺旋动画 */
.dna-helix {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto;
}

.dna-base {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.6);
  animation: dna-rotate 3s linear infinite;
  animation-delay: calc(var(--i) * -0.25s);
}

@keyframes dna-rotate {
  0% {
    transform: translateY(-100px) rotate(0rad) translateX(-40px);
    opacity: 1;
  }
  25% {
    transform: translateY(0px) rotate(1.57rad) translateX(0px);
    opacity: 0.5;
  }
  50% {
    transform: translateY(100px) rotate(3.14rad) translateX(40px);
    opacity: 1;
  }
  75% {
    transform: translateY(0px) rotate(4.71rad) translateX(0px);
    opacity: 0.5;
  }
  100% {
    transform: translateY(-100px) rotate(6.28rad) translateX(-40px);
    opacity: 1;
  }
}
</style>
