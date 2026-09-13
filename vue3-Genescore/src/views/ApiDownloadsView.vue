<template>
  <div class="relative overflow-hidden min-h-screen bg-gradient-to-br from-white via-blue-50 to-teal-50">
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
            <router-link to="/api" class="text-blue-600 font-medium border-b-2 border-blue-600">
              API & Downloads
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <!-- Page Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
          API & Downloads
        </h1>
        <p class="text-xl text-gray-600 max-w-3xl mx-auto">
          Access our comprehensive gene dependency database programmatically or download datasets for your research
        </p>
      </div>

      <!-- API Documentation Section -->
      <div class="mb-16">
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-6 flex items-center">
            <svg class="w-7 h-7 text-blue-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
            </svg>
            REST API Documentation
          </h2>
          
          <div class="space-y-6">
            <!-- Gene Search API -->
            <div class="border-l-4 border-blue-500 pl-4 py-3">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">
                <span class="inline-block bg-green-500 text-white px-2 py-1 rounded text-sm mr-2">GET</span>
                Gene Search
              </h3>
              <code class="block bg-gray-100 px-4 py-3 rounded-lg text-sm font-mono text-gray-800">
                GET /api/genes/search?query=EGFR&limit=10
              </code>
              <p class="text-gray-600 mt-2">Search for genes by symbol or name</p>
            </div>

            <!-- Gene Detail API -->
            <div class="border-l-4 border-blue-500 pl-4 py-3">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">
                <span class="inline-block bg-green-500 text-white px-2 py-1 rounded text-sm mr-2">GET</span>
                Gene Detail
              </h3>
              <code class="block bg-gray-100 px-4 py-3 rounded-lg text-sm font-mono text-gray-800">
                GET /api/genes/{symbol}
              </code>
              <p class="text-gray-600 mt-2">Get detailed information about a specific gene</p>
            </div>

            <!-- Gene Cancer Scores API -->
            <div class="border-l-4 border-blue-500 pl-4 py-3">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">
                <span class="inline-block bg-green-500 text-white px-2 py-1 rounded text-sm mr-2">GET</span>
                Cancer Dependency Scores
              </h3>
              <code class="block bg-gray-100 px-4 py-3 rounded-lg text-sm font-mono text-gray-800">
                GET /api/genes/{symbol}/cancer-scores
              </code>
              <p class="text-gray-600 mt-2">Retrieve gene dependency scores across different cancer types</p>
            </div>

            <!-- Gene Enrichment API -->
            <div class="border-l-4 border-blue-500 pl-4 py-3">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">
                <span class="inline-block bg-purple-500 text-white px-2 py-1 rounded text-sm mr-2">POST</span>
                Gene Set Enrichment
              </h3>
              <code class="block bg-gray-100 px-4 py-3 rounded-lg text-sm font-mono text-gray-800">
                POST /api/enrichment
              </code>
              <p class="text-gray-600 mt-2">Perform gene set enrichment analysis on a list of genes</p>
            </div>
          </div>

          <div class="mt-8 p-4 bg-blue-50 rounded-xl border border-blue-200">
            <h4 class="font-semibold text-blue-800 mb-2">API Rate Limiting</h4>
            <p class="text-blue-700 text-sm">
              Our API is rate-limited to 100 requests per minute per IP address to ensure fair usage for all users.
            </p>
          </div>
        </div>
      </div>

      <!-- Downloads Section -->
      <div class="mb-16">
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-6 flex items-center">
            <svg class="w-7 h-7 text-teal-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
            </svg>
            Dataset Downloads
          </h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Pan-cancer Driver Matrix -->
            <div class="bg-gradient-to-br from-blue-50 to-white rounded-xl p-6 border border-blue-200 hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mr-4">
                    <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900">Pan-cancer Driver Matrix</h3>
                    <p class="text-sm text-gray-500">CSV Format • 2.3 MB</p>
                  </div>
                </div>
              </div>
              <p class="text-gray-600 text-sm mb-4">
                Comprehensive gene dependency scores across 1,000+ cancer cell lines
              </p>
              <button 
                @click="handleDownload('pan-cancer-driver-matrix')"
                class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
                Download
              </button>
            </div>

            <!-- Lung-specific Genes -->
            <div class="bg-gradient-to-br from-teal-50 to-white rounded-xl p-6 border border-teal-200 hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-12 h-12 bg-teal-100 rounded-lg flex items-center justify-center mr-4">
                    <svg class="w-6 h-6 text-teal-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900">Lung-specific Genes</h3>
                    <p class="text-sm text-gray-500">CSV Format • 1.1 MB</p>
                  </div>
                </div>
              </div>
              <p class="text-gray-600 text-sm mb-4">
                Gene dependency data specifically curated for lung cancer research
              </p>
              <button 
                @click="handleDownload('lung-specific-genes')"
                class="w-full bg-teal-600 hover:bg-teal-700 text-white font-medium py-2 px-4 rounded-lg transition-colors flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
                Download
              </button>
            </div>

            <!-- Gene Annotations -->
            <div class="bg-gradient-to-br from-purple-50 to-white rounded-xl p-6 border border-purple-200 hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mr-4">
                    <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900">Gene Annotations</h3>
                    <p class="text-sm text-gray-500">CSV Format • 5.2 MB</p>
                  </div>
                </div>
              </div>
              <p class="text-gray-600 text-sm mb-4">
                Complete gene annotations from HGNC, Ensembl, and other sources
              </p>
              <button 
                @click="handleDownload('gene-annotations')"
                class="w-full bg-purple-600 hover:bg-purple-700 text-white font-medium py-2 px-4 rounded-lg transition-colors flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
                Download
              </button>
            </div>

            <!-- Drug Interactions -->
            <div class="bg-gradient-to-br from-orange-50 to-white rounded-xl p-6 border border-orange-200 hover:shadow-md transition-shadow">
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center">
                  <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center mr-4">
                    <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path>
                    </svg>
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900">Drug-Gene Interactions</h3>
                    <p class="text-sm text-gray-500">CSV Format • 3.8 MB</p>
                  </div>
                </div>
              </div>
              <p class="text-gray-600 text-sm mb-4">
                Curated drug-gene interaction data from DGIdb and other sources
              </p>
              <button 
                @click="handleDownload('drug-interactions')"
                class="w-full bg-orange-600 hover:bg-orange-700 text-white font-medium py-2 px-4 rounded-lg transition-colors flex items-center justify-center"
              >
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
                Download
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- SDK & Libraries Section -->
      <div class="mb-16">
        <div class="bg-white/90 backdrop-blur-md rounded-2xl shadow-lg border border-gray-200 p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-6 flex items-center">
            <svg class="w-7 h-7 text-indigo-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
            </svg>
            SDK & Libraries
          </h2>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Python SDK -->
            <div class="text-center p-6 bg-gradient-to-br from-yellow-50 to-white rounded-xl border border-yellow-200 hover:shadow-md transition-shadow">
              <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-yellow-600" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M14.25.18l.9.2.73.26.59.3.45.32.34.34.25.34.16.33.1.3.04.26.02.2-.01.13V8.5l-.05.63-.13.55-.21.46-.26.38-.3.31-.33.25-.35.19-.35.14-.33.1-.3.07-.26.04-.21.02H8.77l-.69.05-.59.14-.5.22-.41.27-.33.32-.27.35-.2.36-.15.37-.1.35-.07.32-.04.27-.02.21v3.06H3.17l-.21-.03-.28-.07-.32-.12-.35-.18-.36-.26-.36-.36-.35-.46-.32-.59-.28-.73-.21-.88-.14-1.05-.05-1.23.06-1.22.16-1.04.24-.87.32-.71.36-.57.4-.44.42-.33.42-.24.4-.16.36-.1.32-.05.24-.01h.16l.06.01h8.16v-.83H6.18l-.01-2.75-.02-.37.05-.34.11-.31.17-.28.25-.26.31-.23.38-.2.44-.18.51-.15.58-.12.64-.1.71-.06.77-.04.84-.02 1.27.05zm-6.3 1.98l-.23.33-.08.41.08.41.23.34.33.22.41.09.41-.09.33-.22.23-.34.08-.41-.08-.41-.23-.33-.33-.22-.41-.09-.41.09zm13.09 3.95l.28.06.32.12.35.18.36.27.36.35.35.47.32.59.28.73.21.88.14 1.04.05 1.23-.06 1.23-.16 1.04-.24.86-.32.71-.36.57-.4.45-.42.33-.42.24-.4.16-.36.09-.32.05-.24.02-.16-.01h-8.22v.82h5.84l.01 2.76.02.36-.05.34-.11.31-.17.29-.25.25-.31.24-.38.2-.44.17-.51.15-.58.13-.64.09-.71.07-.77.04-.84.01-1.27-.04-1.07-.14-.9-.2-.73-.25-.59-.3-.45-.33-.34-.34-.25-.34-.16-.33-.1-.3-.04-.25-.02-.2.01-.13v-5.34l.05-.64.13-.54.21-.46.26-.38.3-.32.33-.24.35-.2.35-.14.33-.1.3-.06.26-.04.21-.02.13-.01h5.84l.69-.05.59-.14.5-.21.41-.28.33-.32.27-.35.2-.36.15-.36.1-.35.07-.32.04-.28.02-.21v-3.06h2.09l.14.02zm-6.47 14.25l-.23.33-.08.41.08.41.23.33.33.23.41.08.41-.08.33-.23.23-.33.08-.41-.08-.41-.23-.33-.33-.23-.41-.08-.41.08z"/>
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">Python SDK</h3>
              <p class="text-gray-600 text-sm mb-4">Official Python client library</p>
              <button class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">
                View on PyPI →
              </button>
            </div>

            <!-- JavaScript/TypeScript -->
            <div class="text-center p-6 bg-gradient-to-br from-yellow-50 to-white rounded-xl border border-yellow-200 hover:shadow-md transition-shadow">
              <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-yellow-600" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M0 0h24v24H0V0zm22.034 18.276c-.175-1.095-.888-2.015-3.003-2.873-.736-.345-1.554-.585-1.797-1.14-.091-.33-.105-.51-.046-.705.15-.646.915-.84 1.515-.66.39.12.75.42.976.9 1.034-.676 1.034-.676 1.755-1.125-.27-.42-.404-.601-.586-.78-.63-.705-1.469-1.065-2.834-1.034l-.705.089c-.676.165-1.32.525-1.71 1.005-1.14 1.291-.811 3.541.569 4.471 1.365 1.02 3.361 1.244 3.616 2.205.24 1.17-.87 1.545-1.966 1.41-.811-.18-1.26-.586-1.755-1.336l-1.83 1.051c.21.48.45.689.81 1.109 1.74 1.756 6.09 1.666 6.871-1.004.029-.09.24-.705.074-1.65l.046.067zm-8.983-7.245h-2.248c0 1.938-.009 3.864-.009 5.805 0 1.232.063 2.363-.138 2.711-.33.689-1.18.601-1.566.48-.396-.196-.597-.466-.83-.855-.063-.105-.11-.196-.127-.196l-1.825 1.125c.305.63.75 1.172 1.324 1.517.855.51 2.004.675 3.207.405.783-.226 1.458-.691 1.811-1.411.51-.93.402-2.07.397-3.346.012-2.054 0-4.109 0-6.179l.004-.056z"/>
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">TypeScript SDK</h3>
              <p class="text-gray-600 text-sm mb-4">Type-safe JavaScript/TypeScript client</p>
              <button class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">
                View on npm →
              </button>
            </div>

            <!-- R Package -->
            <div class="text-center p-6 bg-gradient-to-br from-blue-50 to-white rounded-xl border border-blue-200 hover:shadow-md transition-shadow">
              <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-blue-600" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm4.441 16.892c-2.102.144-6.784-.144-6.784-.144l.004-.964s1.788.48 3.864.48c1.428 0 2.46-.48 2.46-1.464 0-1.236-1.176-1.704-3.18-2.28-2.556-.756-4.332-1.896-4.332-4.152 0-2.436 1.896-4.284 5.256-4.284 2.376 0 5.112.408 5.112.408l-.072.984s-1.584-.408-3.528-.408c-1.536 0-2.184.624-2.184 1.368 0 1.056.912 1.512 3.048 2.088 2.7.708 4.464 1.968 4.464 4.296-.012 2.616-2.268 4.356-5.856 4.356z"/>
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">R Package</h3>
              <p class="text-gray-600 text-sm mb-4">Bioconductor-compatible R package</p>
              <button class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">
                View on Bioconductor →
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const handleDownload = (dataset: string) => {
  console.log(`Downloading dataset: ${dataset}`);
  // TODO: Implement actual download functionality
  alert(`Download for ${dataset} will be available soon!`);
};
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
</style>
