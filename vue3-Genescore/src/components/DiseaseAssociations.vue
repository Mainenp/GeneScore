<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Disease Associations</h2>

    <div v-if="!diseases.length" class="text-center py-8 text-gray-500">
      <p>No disease associations found</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="disease in diseases"
        :key="disease.diseaseId"
        class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <!-- Disease Header -->
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="text-lg font-bold text-gray-900">{{ disease.diseaseName }}</h3>
            <p class="text-sm text-gray-600 mt-1">Cancer Type: <span class="font-semibold">{{ disease.cancerType }}</span></p>
          </div>
          <span
            class="px-3 py-1 rounded-full text-xs font-semibold"
            :class="getAssociationTypeClass(disease.associationType)"
          >
            {{ formatAssociationType(disease.associationType) }}
          </span>
        </div>

        <!-- Disease Details Grid -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <!-- Mutation Frequency -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Mutation Frequency</p>
            <div class="flex items-center gap-2">
              <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-orange-400 to-red-600"
                  :style="{ width: `${disease.mutationFrequency}%` }"
                ></div>
              </div>
              <span class="font-bold text-sm text-gray-900">{{ disease.mutationFrequency }}%</span>
            </div>
          </div>

          <!-- Clinical Significance -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Clinical Significance</p>
            <span
              class="inline-block px-2 py-1 rounded text-xs font-semibold"
              :class="getClinicalSignificanceClass(disease.clinicalSignificance)"
            >
              {{ formatClinicalSignificance(disease.clinicalSignificance) }}
            </span>
          </div>

          <!-- Association Type -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Gene Role</p>
            <p class="text-sm font-semibold text-gray-900">{{ formatAssociationType(disease.associationType) }}</p>
          </div>

          <!-- Sources -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Data Sources</p>
            <div class="flex flex-wrap gap-1">
              <span
                v-for="source in disease.sources"
                :key="source"
                class="bg-blue-100 text-blue-700 px-2 py-0.5 rounded text-xs font-semibold"
              >
                {{ source }}
              </span>
            </div>
          </div>
        </div>

        <!-- External Links -->
        <div class="flex gap-2 pt-3 border-t border-gray-200">
          <a
            v-if="disease.clinvarId"
            :href="`https://www.ncbi.nlm.nih.gov/clinvar/${disease.clinvarId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="text-xs bg-blue-50 hover:bg-blue-100 text-blue-700 px-3 py-1 rounded transition-colors border border-blue-200"
          >
            ClinVar
          </a>
          <a
            v-if="disease.cosmicId"
            :href="`https://cancer.sanger.ac.uk/cosmic/search?q=${disease.cosmicId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="text-xs bg-purple-50 hover:bg-purple-100 text-purple-700 px-3 py-1 rounded transition-colors border border-purple-200"
          >
            COSMIC
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DiseaseAssociation } from '@/types/gene';

interface Props {
  diseases: DiseaseAssociation[];
}

withDefaults(defineProps<Props>(), {
  diseases: () => []
});

function getAssociationTypeClass(type: string): string {
  switch (type) {
    case 'oncogene':
      return 'bg-red-100 text-red-700';
    case 'tumor_suppressor':
      return 'bg-blue-100 text-blue-700';
    case 'fusion':
      return 'bg-purple-100 text-purple-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function getClinicalSignificanceClass(significance: string): string {
  switch (significance) {
    case 'pathogenic':
      return 'bg-red-100 text-red-700';
    case 'likely_pathogenic':
      return 'bg-orange-100 text-orange-700';
    case 'uncertain':
      return 'bg-yellow-100 text-yellow-700';
    case 'likely_benign':
      return 'bg-blue-100 text-blue-700';
    case 'benign':
      return 'bg-green-100 text-green-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function formatAssociationType(type: string): string {
  return type.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}

function formatClinicalSignificance(significance: string): string {
  return significance.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}
</script>
