<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Gene Information</h2>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Official Name -->
      <div class="border-l-4 border-blue-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Official Name</h3>
        <p class="text-lg font-bold text-gray-900 mt-1">{{ annotation.name }}</p>
      </div>

      <!-- Gene Symbol -->
      <div class="border-l-4 border-teal-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Gene Symbol</h3>
        <p class="text-lg font-bold text-gray-900 mt-1 font-mono">{{ annotation.symbol }}</p>
      </div>

      <!-- Chromosomal Location -->
      <div class="border-l-4 border-purple-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Chromosomal Location</h3>
        <p class="text-lg font-bold text-gray-900 mt-1 font-mono">{{ annotation.chromosomalLocation }}</p>
      </div>

      <!-- Strand -->
      <div class="border-l-4 border-orange-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Strand</h3>
        <p class="text-lg font-bold text-gray-900 mt-1">{{ annotation.strand }}</p>
      </div>

      <!-- Exon Count -->
      <div class="border-l-4 border-green-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Exon Count</h3>
        <p class="text-lg font-bold text-gray-900 mt-1">{{ annotation.exonCount }}</p>
      </div>

      <!-- Protein Coding -->
      <div class="border-l-4 border-red-500 pl-4">
        <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide">Protein Coding</h3>
        <p class="text-lg font-bold text-gray-900 mt-1">
          <span v-if="annotation.proteinCoding" class="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm">
            Yes
          </span>
          <span v-else class="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm">
            No
          </span>
        </p>
      </div>
    </div>

    <!-- Description -->
    <div class="mt-6 pt-6 border-t border-gray-200">
      <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide mb-2">Description</h3>
      <p class="text-gray-700 leading-relaxed">{{ annotation.description }}</p>
    </div>

    <!-- Aliases -->
    <div v-if="annotation.aliases && annotation.aliases.length" class="mt-6 pt-6 border-t border-gray-200">
      <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide mb-3">Gene Aliases</h3>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="alias in annotation.aliases"
          :key="alias"
          class="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-sm font-mono border border-blue-200"
        >
          {{ alias }}
        </span>
      </div>
    </div>

    <!-- External Links -->
    <div class="mt-6 pt-6 border-t border-gray-200">
      <h3 class="text-sm font-semibold text-gray-600 uppercase tracking-wide mb-3">External Resources</h3>
      <div class="flex flex-wrap gap-3">
        <a
          v-if="annotation.ncbiUrl"
          :href="annotation.ncbiUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center gap-2 bg-blue-50 hover:bg-blue-100 text-blue-700 px-4 py-2 rounded-lg transition-colors border border-blue-200"
        >
          <span>NCBI Gene</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
        </a>
        <a
          v-if="annotation.ensemblUrl"
          :href="annotation.ensemblUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center gap-2 bg-teal-50 hover:bg-teal-100 text-teal-700 px-4 py-2 rounded-lg transition-colors border border-teal-200"
        >
          <span>Ensembl</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
        </a>
        <a
          v-if="annotation.uniprotUrl"
          :href="annotation.uniprotUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center gap-2 bg-purple-50 hover:bg-purple-100 text-purple-700 px-4 py-2 rounded-lg transition-colors border border-purple-200"
        >
          <span>UniProt</span>
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GeneAnnotation } from '@/types/gene';

interface Props {
  annotation: GeneAnnotation;
}

withDefaults(defineProps<Props>(), {
  annotation: () => ({
    symbol: '',
    name: '',
    aliases: [],
    description: '',
    chromosomalLocation: '',
    strand: '+',
    exonCount: 0,
    proteinCoding: false,
    entrezId: '',
    ensemblId: '',
    uniprotId: '',
    ncbiUrl: '',
    ensemblUrl: '',
    uniprotUrl: ''
  })
});
</script>
