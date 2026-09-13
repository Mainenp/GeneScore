<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-50 py-12">
    <div class="max-w-4xl mx-auto px-4">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">Gene Search</h1>
        <p class="text-lg text-gray-600">Search for genes in the database</p>
      </div>

      <!-- Search Box -->
      <div class="bg-white rounded-xl shadow-lg p-8 mb-8 border border-gray-200">
        <div class="flex gap-3">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Enter gene symbol (e.g., ASCL1, EGFR, TP53)..."
            @keyup.enter="performSearch"
            class="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button
            @click="performSearch"
            :disabled="!searchQuery || isSearching"
            class="px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold rounded-lg transition-colors"
          >
            <span v-if="isSearching" class="inline-block animate-spin mr-2">⟳</span>
            {{ isSearching ? 'Searching...' : 'Search' }}
          </button>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="searchError" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-8 text-red-700">
        {{ searchError }}
      </div>

      <!-- Search Results -->
      <div v-if="searchResults.length > 0" class="space-y-4">
        <h2 class="text-2xl font-bold text-gray-900 mb-6">
          Found {{ searchResults.length }} gene(s)
        </h2>
        
        <div
          v-for="gene in searchResults"
          :key="gene.id"
          class="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition-shadow cursor-pointer"
          @click="goToGeneDetail(gene.symbol)"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="text-xl font-bold text-blue-600 mb-2">{{ gene.symbol }}</h3>
              <p class="text-gray-600">{{ gene.name }}</p>
              <p class="text-sm text-gray-500 mt-2">ID: {{ gene.id }}</p>
            </div>
            <button
              class="px-4 py-2 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-lg font-medium transition-colors"
              @click.stop="goToGeneDetail(gene.symbol)"
            >
              View Details →
            </button>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div v-else-if="hasSearched && !isSearching" class="text-center py-12">
        <p class="text-gray-600 text-lg">No genes found matching "{{ searchQuery }}"</p>
        <p class="text-gray-500 mt-2">Try searching with a different gene symbol</p>
      </div>

      <!-- Initial State -->
      <div v-else-if="!hasSearched" class="text-center py-12">
        <p class="text-gray-600 text-lg">Enter a gene symbol to search</p>
        <p class="text-gray-500 mt-2">Examples: ASCL1, EGFR, TP53, KRAS, HNF1B</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useGeneStore } from '../stores/gene';

const router = useRouter();
const geneStore = useGeneStore();

const searchQuery = ref('');
const searchResults = ref<any[]>([]);
const isSearching = ref(false);
const searchError = ref('');
const hasSearched = ref(false);

const performSearch = async () => {
  if (!searchQuery.value.trim()) return;

  isSearching.value = true;
  searchError.value = '';
  hasSearched.value = true;

  try {
    const results = await geneStore.searchGenes(searchQuery.value);
    searchResults.value = results;
    
    if (results.length === 0) {
      searchError.value = `No genes found matching "${searchQuery.value}"`;
    }
  } catch (error) {
    console.error('Search error:', error);
    searchError.value = 'Failed to search genes. Please try again.';
  } finally {
    isSearching.value = false;
  }
};

const goToGeneDetail = (symbol: string) => {
  router.push(`/gene/${symbol}`);
};
</script>

<style scoped>
/* Smooth transitions */
button {
  transition: all 0.3s ease;
}

input {
  transition: all 0.3s ease;
}
</style>
