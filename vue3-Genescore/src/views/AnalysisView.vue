<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
    <div class="bg-white/80 backdrop-blur-sm border-b border-gray-200 shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
              <span class="text-white font-bold text-lg">G</span>
            </div>
            <h1 class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              GeneScore Analysis
            </h1>
          </div>
          <router-link to="/" class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-all duration-200 text-gray-700 font-medium">
            Back to Home
          </router-link>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-6 py-8">
      <div class="bg-white rounded-2xl shadow-xl border border-gray-100 p-8 mb-8 overflow-hidden relative">
        <div class="absolute -top-24 -right-24 w-64 h-64 bg-indigo-100 rounded-full opacity-30 blur-3xl"></div>
        <div class="absolute -bottom-24 -left-24 w-64 h-64 bg-purple-100 rounded-full opacity-30 blur-3xl"></div>
        
        <div class="relative z-10">
          <h2 class="text-2xl font-bold text-gray-800 mb-3">Gene Analysis Platform</h2>
          <p class="text-gray-600 mb-6">Enter a gene symbol to perform comprehensive multi-omics analysis</p>
          
          <div class="flex flex-col lg:flex-row gap-4 items-start lg:items-end">
            <div class="flex-1 w-full">
              <label class="block text-sm font-semibold text-gray-700 mb-2">Gene Symbol</label>
              <input 
                v-model="searchQuery" 
                @keyup.enter="performSearch"
                type="text" 
                placeholder="e.g., TP53, BRCA1, EGFR..."
                class="w-full px-5 py-4 border-2 border-gray-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-200 text-lg shadow-sm"
              />
            </div>
            
            <div class="w-full lg:w-64">
              <label class="block text-sm font-semibold text-gray-700 mb-2">Cancer Type</label>
              <select 
                v-model="selectedCancer"
                class="w-full px-5 py-4 border-2 border-gray-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all duration-200 text-lg shadow-sm bg-white"
              >
                <option value="LUAD">LUAD (Lung Adenocarcinoma)</option>
                <option value="BRCA">BRCA (Breast Cancer)</option>
                <option value="COAD">COAD (Colon Cancer)</option>
                <option value="LIHC">LIHC (Liver Cancer)</option>
                <option value="PRAD">PRAD (Prostate Cancer)</option>
                <option value="STAD">STAD (Stomach Cancer)</option>
                <option value="BLCA">BLCA (Bladder Cancer)</option>
                <option value="HNSC">HNSC (Head & Neck Cancer)</option>
                <option value="KIRC">KIRC (Kidney Cancer)</option>
                <option value="LGG">LGG (Brain Cancer)</option>
              </select>
            </div>
            
            <button 
              @click="performSearch" 
              :disabled="loading"
              class="px-8 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl hover:from-indigo-700 hover:to-purple-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed font-semibold text-lg shadow-lg hover:shadow-xl"
            >
              <span v-if="loading" class="flex items-center gap-2">
                <span class="animate-spin">
                  <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" opacity="0.25" />
                    <path d="M12 2a10 10 0 0110 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                  </svg>
                </span>
                Analyzing...
              </span>
              <span v-else class="flex items-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                Analyze Gene
              </span>
            </button>
          </div>
          
          <div v-if="searchError" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
            {{ searchError }}
          </div>
        </div>
      </div>

      <div v-if="selectedGene">
        <div class="bg-gradient-to-r from-white to-blue-50 rounded-2xl shadow-xl border border-blue-100 p-6 mb-8">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h3 class="text-2xl font-bold text-gray-800 mb-1">{{ selectedGene.symbol || selectedGene.geneSymbol }}</h3>
              <p class="text-gray-600">{{ selectedGene.name || selectedGene.geneName }}</p>
              <div class="flex gap-2 mt-2">
                <span v-if="selectedCancer" class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
                  {{ selectedCancer }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
          <div class="border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
            <nav class="flex">
              <button 
                v-for="tab in tabs" 
                :key="tab.id"
                @click="switchTab(tab.id)"
                :class="[
                  'px-6 py-4 font-medium text-sm transition-all duration-200 relative',
                  activeTab === tab.id
                    ? 'text-indigo-600 bg-white'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                ]"
              >
                <span class="flex items-center gap-2">
                  {{ tab.name }}
                </span>
                <div 
                  v-if="activeTab === tab.id"
                  class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-600 to-purple-600"
                ></div>
              </button>
            </nav>
          </div>
          
          <div class="p-6">
            <div v-show="activeTab === 'expression'">
              <DifferentialExpressionAnalysis ref="expressionRef" :gene="selectedGene" :data="analysisData.expression" />
            </div>
            <div v-show="activeTab === 'prognosis'">
              <PrognosisAnalysis ref="prognosisRef" :gene="selectedGene" :data="analysisData.prognosis" />
            </div>
            <div v-show="activeTab === 'drug'">
              <DrugSensitivityAnalysis ref="drugRef" :gene="selectedGene" :data="analysisData.drug" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { searchGenes } from '../api/gene'
import { getGeneAnalysis } from '../api/gene'
import DifferentialExpressionAnalysis from '../components/analysis/DifferentialExpressionAnalysis.vue'
import PrognosisAnalysis from '../components/analysis/PrognosisAnalysis.vue'
import DrugSensitivityAnalysis from '../components/analysis/DrugSensitivityAnalysis.vue'

const searchQuery = ref('')
const selectedCancer = ref('LUAD')
const loading = ref(false)
const searchError = ref('')
const selectedGene = ref<any>(null)
const activeTab = ref('expression')

const expressionRef = ref<any>(null)
const prognosisRef = ref<any>(null)
const drugRef = ref<any>(null)

const tabs = [
  { id: 'expression', name: 'Differential Expression' },
  { id: 'prognosis', name: 'Prognosis Analysis' },
  { id: 'drug', name: 'Drug Sensitivity' }
]

const analysisData = reactive({
  geneSymbol: '',
  cancerType: '',
  expression: null,
  prognosis: null,
  enrichment: [],
  drug: []
})

const performSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchError.value = 'Please enter a gene symbol'
    return
  }
  
  loading.value = true
  searchError.value = ''
  
  try {
    const genesData = await searchGenes(searchQuery.value)
    console.log('Search result:', genesData)
    
    let genes = []
    if (genesData && genesData.data) {
      genes = Array.isArray(genesData.data) ? genesData.data : [genesData.data]
    } else if (Array.isArray(genesData)) {
      genes = genesData
    } else if (genesData) {
      genes = [genesData]
    }
    
    console.log('Processed genes:', genes)
    
    if (genes && genes.length > 0) {
      const gene = genes[0]
      selectedGene.value = {
        id: gene.id || 1,
        symbol: gene.symbol || gene.geneSymbol || searchQuery.value,
        name: gene.name || gene.geneName || 'Unknown Gene'
      }
      console.log('Loading analysis data for:', selectedGene.value.symbol, 'Cancer:', selectedCancer.value)
      await loadAnalysisData(selectedGene.value.symbol)
    } else {
      searchError.value = 'No gene found. Please try another symbol.'
    }
  } catch (error) {
    console.error('Search failed:', error)
    searchError.value = 'Search failed. Please try again.'
  } finally {
    loading.value = false
  }
}

const loadAnalysisData = async (geneSymbol: string) => {
  try {
    console.log('Calling getGeneAnalysis API for:', geneSymbol, 'with cancer:', selectedCancer.value)
    const data = await getGeneAnalysis(geneSymbol, selectedCancer.value)
    console.log('Analysis API returned:', data)
    
    if (data) {
      analysisData.geneSymbol = data.geneSymbol || data.gene_symbol || geneSymbol
      analysisData.cancerType = data.cancerType || data.cancer_type || selectedCancer.value
      analysisData.expression = data.expression || null
      analysisData.prognosis = data.prognosis || null
      analysisData.drug = data.drug || data.drug_sensitivity || []
      analysisData.enrichment = data.enrichment || []
      console.log('Analysis data loaded:', analysisData)
      
      // 给一点时间让数据更新，然后调整当前标签页的图表
      await nextTick()
      resizeActiveChart()
    }
  } catch (error) {
    console.error('Error loading analysis data:', error)
    searchError.value = 'Failed to load analysis data. Please try again.'
  }
}

const switchTab = async (newTab: string) => {
  activeTab.value = newTab
  console.log('Tab switched to:', newTab)
  
  // 等待 DOM 更新后调整图表大小
  await nextTick()
  
  // 稍微延迟一下，确保 v-show 生效
  setTimeout(() => {
    resizeActiveChart()
  }, 100)
}

const resizeActiveChart = () => {
  console.log('Resizing active chart:', activeTab.value)
  
  switch (activeTab.value) {
    case 'expression':
      if (expressionRef.value && expressionRef.value.resizeCharts) {
        expressionRef.value.resizeCharts()
      }
      break
    case 'prognosis':
      if (prognosisRef.value && prognosisRef.value.resizeCharts) {
        prognosisRef.value.resizeCharts()
      }
      break
    case 'drug':
      if (drugRef.value && drugRef.value.resizeCharts) {
        drugRef.value.resizeCharts()
      }
      break
  }
}

// 监听标签页变化
watch(activeTab, (newTab) => {
  console.log('Tab changed to:', newTab)
})
</script>

<style scoped>
</style>
