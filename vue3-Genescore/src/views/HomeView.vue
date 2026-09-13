<template>
  <div class="relative overflow-hidden min-h-screen bg-gradient-to-br from-white via-blue-50 to-teal-50">
    <!-- 3D Force Graph Background -->
    <div ref="forceGraphContainer" class="absolute inset-0 z-0 opacity-40"></div>

    <!-- Overlay gradient -->
    <div class="absolute inset-0 z-5 bg-gradient-to-b from-white/60 via-white/40 to-white/60"></div>

    <!-- Navigation Bar -->
    <nav class="relative z-20 bg-white/80 backdrop-blur-md border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center">
              <img src="/logo.png" alt="GeneScoreDB Logo" class="h-10 w-auto mr-3">
              <span class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent">
                DSF GeneScoreDB
              </span>
            </router-link>
          </div>
          <div class="hidden md:flex space-x-8">
            <router-link to="/" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Home
            </router-link>
            <router-link to="/analysis" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Analysis
            </router-link>
            <router-link to="/dko" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              Double Knockout
            </router-link>
            <router-link to="/api" class="text-gray-700 hover:text-blue-600 transition-colors font-medium">
              API & Downloads
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center">
      <h1 class="text-5xl md:text-7xl font-bold text-gray-900 mb-6 leading-tight">
        Cancer Gene<br />Dependency Database
      </h1>
      <p class="text-xl text-gray-600 mb-12 max-w-3xl mx-auto leading-relaxed">
        Explore CRISPR gene dependency scores, protein structures, and therapeutic targets across 1,000+ cancer cell lines
      </p>

      <!-- Global Search Bar -->
      <div class="max-w-2xl mx-auto mb-16">
        <div class="relative">
          <div class="bg-white/95 backdrop-blur-md rounded-full shadow-2xl border border-gray-200 flex items-center overflow-hidden hover:shadow-3xl transition-shadow">
            <svg class="w-5 h-5 text-gray-400 ml-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <input
              v-model="searchQuery"
              @input="handleSearchInput"
              @keydown.enter="handleSearchSubmit"
              @focus="isSearchFocused = true"
              @blur="handleBlur"
              type="text"
              placeholder="Search genes (e.g., EGFR, KRAS, TP53)"
              class="flex-1 px-4 py-4 focus:outline-none text-gray-900 placeholder-gray-500 bg-transparent"
            />
            <button
              @click="handleSearchSubmit"
              class="bg-gradient-to-r from-blue-600 to-teal-600 hover:from-blue-700 hover:to-teal-700 text-white px-8 py-4 font-medium transition-all"
            >
              Search
            </button>
          </div>

          <!-- Search Results Dropdown -->
          <transition name="fade">
            <div
              v-if="isSearchFocused && (searchResults.length > 0 || (searchQuery.length > 0 && !isLoading))"
              class="absolute top-full left-0 right-0 mt-3 bg-white rounded-xl shadow-2xl z-50 max-h-72 overflow-y-auto border border-gray-200"
            >
              <div v-if="searchResults.length === 0" class="px-6 py-4 text-gray-500 text-center">
                No genes found for "{{ searchQuery }}"
              </div>
              <div
                v-for="(gene, index) in searchResults"
                :key="gene.symbol"
                @click="selectGene(gene)"
                class="px-6 py-4 hover:bg-blue-50 cursor-pointer text-left border-b border-gray-100 last:border-b-0 transition-colors"
                :class="{ 'bg-blue-50': index === 0 }"
              >
                <div class="font-semibold text-gray-900">{{ gene.symbol }}</div>
                <div class="text-sm text-gray-600">{{ gene.name }}</div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- Quick Stats Section -->
    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8 text-center hover:shadow-xl transition-shadow">
          <div class="text-5xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent mb-3">
            16,000+
          </div>
          <div class="text-gray-600 font-medium">Genes Analyzed</div>
          <div class="text-sm text-gray-500 mt-2">From DepMap & CRISPR screens</div>
        </div>
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8 text-center hover:shadow-xl transition-shadow">
          <div class="text-5xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent mb-3">
            1,000+
          </div>
          <div class="text-gray-600 font-medium">Cancer Cell Lines</div>
          <div class="text-sm text-gray-500 mt-2">Across multiple lineages</div>
        </div>
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8 text-center hover:shadow-xl transition-shadow">
          <div class="text-5xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent mb-3">
            AI-Powered
          </div>
          <div class="text-gray-600 font-medium">Deep Learning</div>
          <div class="text-sm text-gray-500 mt-2">DKO predictions & analysis</div>
        </div>
      </div>
    </div>

    <!-- Models and Power Section -->
    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8 text-center hover:shadow-xl transition-shadow">
          <h3 class="text-2xl font-bold text-gray-900 mb-6">Model Architecture</h3>
          <img src="/model.png" alt="Model Architecture" class="w-full h-auto rounded-xl shadow-md">
        </div>
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8 text-center hover:shadow-xl transition-shadow">
          <h3 class="text-2xl font-bold text-gray-900 mb-6">Predictive Performance</h3>
          <img src="/power.png" alt="Power Analysis" class="w-full h-auto rounded-xl shadow-md">
        </div>
      </div>
    </div>

    <!-- Loading indicator -->
    <div v-if="isLoading" class="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-sm">
      <div class="bg-white rounded-lg p-8 shadow-2xl">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p class="text-gray-600 mt-4 text-center">Loading...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useGeneStore } from '../stores/gene';

const router = useRouter();
const geneStore = useGeneStore();

const searchQuery = ref('');
const searchResults = ref<Array<{ symbol: string; name: string; id: string }>>([]);
const isSearchFocused = ref(false);
const isLoading = ref(false);
const forceGraphContainer = ref<HTMLElement | null>(null);
let forceGraph: any = null;
let isGraphInitialized = false;

// 输入时的搜索 - 只获取结果不跳转
const handleSearchInput = async () => {
  console.log('handleSearchInput called with query:', searchQuery.value);
  if (searchQuery.value.length < 1) {
    searchResults.value = [];
    return;
  }

  try {
    isLoading.value = true;
    console.log('Calling geneStore.searchGenes...');
    const results = await geneStore.searchGenes(searchQuery.value);
    console.log('Search results:', results);
    searchResults.value = results;
  } catch (error) {
    console.error('Search error:', error);
  } finally {
    isLoading.value = false;
  }
};

// 点击搜索按钮或按回车时的搜索 - 获取结果并自动跳转
const handleSearchSubmit = async () => {
  console.log('handleSearchSubmit called with query:', searchQuery.value);
  if (searchQuery.value.length < 1) {
    searchResults.value = [];
    return;
  }

  try {
    isLoading.value = true;
    console.log('Calling geneStore.searchGenes...');
    const results = await geneStore.searchGenes(searchQuery.value);
    console.log('Search results:', results);
    searchResults.value = results;
    
    // 如果有搜索结果，自动选择第一个并跳转
    if (results.length > 0) {
      selectGene(results[0]);
    }
  } catch (error) {
    console.error('Search error:', error);
  } finally {
    isLoading.value = false;
  }
};

const selectGene = (gene: { symbol: string; name: string; id: string } | undefined) => {
  if (gene) {
    isSearchFocused.value = false;
    searchQuery.value = '';
    searchResults.value = [];
    router.push(`/gene/${gene.symbol}`);
  }
};

const handleBlur = () => {
  setTimeout(() => {
    isSearchFocused.value = false;
  }, 200);
};

const initForceGraph = async () => {
  if (!forceGraphContainer.value || isGraphInitialized) return;

  try {
    const ForceGraph3D = (await import('3d-force-graph')).default;

    const mockData = {
      nodes: [
        { id: 'EGFR', name: 'EGFR', val: 10 },
        { id: 'KRAS', name: 'KRAS', val: 8 },
        { id: 'TP53', name: 'TP53', val: 9 },
        { id: 'BRCA1', name: 'BRCA1', val: 7 },
        { id: 'PTEN', name: 'PTEN', val: 6 },
        { id: 'PIK3CA', name: 'PIK3CA', val: 7 },
        { id: 'AKT1', name: 'AKT1', val: 5 },
        { id: 'MTOR', name: 'MTOR', val: 6 },
        { id: 'MYC', name: 'MYC', val: 8 },
        { id: 'RB1', name: 'RB1', val: 7 },
        { id: 'CDK4', name: 'CDK4', val: 5 },
        { id: 'CDK6', name: 'CDK6', val: 5 },
        { id: 'CCND1', name: 'CCND1', val: 6 },
        { id: 'BRAF', name: 'BRAF', val: 7 },
        { id: 'NRAS', name: 'NRAS', val: 6 }
      ],
      links: [
        { source: 'EGFR', target: 'KRAS', value: 0.8 },
        { source: 'KRAS', target: 'PIK3CA', value: 0.7 },
        { source: 'PIK3CA', target: 'AKT1', value: 0.9 },
        { source: 'AKT1', target: 'MTOR', value: 0.85 },
        { source: 'TP53', target: 'MYC', value: 0.6 },
        { source: 'TP53', target: 'RB1', value: 0.7 },
        { source: 'RB1', target: 'CDK4', value: 0.8 },
        { source: 'RB1', target: 'CDK6', value: 0.75 },
        { source: 'CDK4', target: 'CCND1', value: 0.9 },
        { source: 'CDK6', target: 'CCND1', value: 0.85 },
        { source: 'BRAF', target: 'KRAS', value: 0.7 },
        { source: 'NRAS', target: 'KRAS', value: 0.65 },
        { source: 'PTEN', target: 'PIK3CA', value: 0.8 },
        { source: 'BRCA1', target: 'TP53', value: 0.6 }
      ]
    };

    forceGraph = new ForceGraph3D(forceGraphContainer.value)
    forceGraph
      .graphData(mockData)
      .nodeLabel('name')
      .nodeColor(() => '#0066FF')
      .linkColor(() => '#00BFA5')
      .linkOpacity(0.6)
      .linkWidth(2)
      .nodeResolution(8)
      .linkResolution(8)
      .backgroundColor('rgba(255, 255, 255, 0)')
      .cameraPosition({ z: 500 });

    forceGraph.onNodeClick((node: any) => {
      router.push(`/gene/${node.id}`);
    });

    const rotateGraph = () => {
      if (forceGraph) {
        forceGraph.cameraPosition({
          x: Math.sin(Date.now() / 3000) * 300,
          y: Math.cos(Date.now() / 4000) * 300,
          z: 500
        });
        requestAnimationFrame(rotateGraph);
      }
    };

    rotateGraph();
    isGraphInitialized = true;
  } catch (error) {
    console.error('Failed to initialize 3D force graph:', error);
  }
};

onMounted(() => {
  setTimeout(() => {
    initForceGraph();
  }, 100);
});

onUnmounted(() => {
  if (forceGraph) {
    try {
      forceGraph._destructor();
    } catch (error) {
      console.error('Error cleaning up force graph:', error);
    }
  }
});
</script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
