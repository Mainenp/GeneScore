<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Related Genes</h2>

    <div v-if="!relatedGenes.length" class="text-center py-8 text-gray-500">
      <p>No related genes found</p>
    </div>

    <div v-else>
      <!-- Filter Tabs -->
      <div class="flex gap-2 mb-6 border-b border-gray-200 pb-4">
        <button
          v-for="type in geneTypes"
          :key="type"
          @click="selectedType = type"
          class="px-4 py-2 rounded-lg font-medium transition-colors"
          :class="
            selectedType === type
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
        >
          {{ formatGeneType(type) }}
          <span class="ml-2 text-sm opacity-75">
            ({{ getGenesByType(type).length }})
          </span>
        </button>
      </div>

      <!-- Genes Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="gene in getGenesByType(selectedType)"
          :key="gene.symbol"
          class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
        >
          <!-- Gene Header -->
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3 class="text-lg font-bold text-gray-900 font-mono">{{ gene.symbol }}</h3>
              <p class="text-sm text-gray-600 mt-1">{{ gene.name }}</p>
            </div>
            <span
              class="px-3 py-1 rounded-full text-xs font-semibold"
              :class="getGeneTypeClass(gene.type)"
            >
              {{ formatGeneType(gene.type) }}
            </span>
          </div>

          <!-- Gene Details -->
          <div class="space-y-2 text-sm">
            <div v-if="gene.organism" class="flex justify-between">
              <span class="text-gray-600">Organism:</span>
              <span class="font-semibold text-gray-900">{{ gene.organism }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Similarity:</span>
              <div class="flex items-center gap-2">
                <div class="w-20 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    class="h-full bg-gradient-to-r from-blue-400 to-blue-600"
                    :style="{ width: `${gene.similarity * 100}%` }"
                  ></div>
                </div>
                <span class="font-semibold text-gray-900">{{ (gene.similarity * 100).toFixed(0) }}%</span>
              </div>
            </div>
          </div>

          <!-- External Links -->
          <div class="flex gap-2 mt-4 pt-3 border-t border-gray-200">
            <a
              v-if="gene.ensemblId"
              :href="`https://www.ensembl.org/Homo_sapiens/Gene/Summary?g=${gene.ensemblId}`"
              target="_blank"
              rel="noopener noreferrer"
              class="text-xs bg-teal-50 hover:bg-teal-100 text-teal-700 px-3 py-1 rounded transition-colors border border-teal-200"
            >
              Ensembl
            </a>
            <a
              v-if="gene.uniprotId"
              :href="`https://www.uniprot.org/uniprotkb/${gene.uniprotId}`"
              target="_blank"
              rel="noopener noreferrer"
              class="text-xs bg-purple-50 hover:bg-purple-100 text-purple-700 px-3 py-1 rounded transition-colors border border-purple-200"
            >
              UniProt
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { RelatedGene } from '@/types/gene';

interface Props {
  relatedGenes: RelatedGene[];
}

const props = withDefaults(defineProps<Props>(), {
  relatedGenes: () => []
});

const selectedType = ref<RelatedGene['type']>('ortholog');

const geneTypes = computed(() => {
  const types = new Set<RelatedGene['type']>();
  props.relatedGenes.forEach((gene) => {
    types.add(gene.type);
  });
  return Array.from(types).sort();
});

function getGenesByType(type: RelatedGene['type']): RelatedGene[] {
  return props.relatedGenes.filter((gene) => gene.type === type);
}

function getGeneTypeClass(type: RelatedGene['type']): string {
  switch (type) {
    case 'ortholog':
      return 'bg-blue-100 text-blue-700';
    case 'paralog':
      return 'bg-purple-100 text-purple-700';
    case 'pathway':
      return 'bg-green-100 text-green-700';
    case 'coexpression':
      return 'bg-orange-100 text-orange-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function formatGeneType(type: string): string {
  return type.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}
</script>
