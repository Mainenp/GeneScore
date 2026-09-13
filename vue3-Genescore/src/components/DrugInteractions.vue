<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Drug Interactions</h2>

    <div v-if="!drugs.length" class="text-center py-8 text-gray-500">
      <p>No drug interactions found</p>
    </div>

    <div v-else class="space-y-6">
      <div
        v-for="drug in drugs"
        :key="drug.drugId"
        class="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow"
      >
        <!-- Drug Header -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex-1">
            <h3 class="text-xl font-bold text-gray-900">{{ drug.drugName }}</h3>
            <p class="text-sm text-gray-600 mt-1">{{ drug.mechanism }}</p>
            <p v-if="drug.description" class="text-sm text-gray-500 mt-2">{{ drug.description }}</p>
          </div>
          <div class="flex flex-col gap-2 ml-4">
            <span
              class="px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap"
              :class="getApprovalStatusClass(drug.approvalStatus)"
            >
              {{ formatApprovalStatus(drug.approvalStatus) }}
            </span>
            <span v-if="drug.source" class="text-xs text-gray-500">Source: {{ drug.source }}</span>
          </div>
        </div>

        <!-- Drug Details Grid -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
          <!-- Interaction Type -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Interaction Type</p>
            <span
              class="inline-block px-2 py-1 rounded text-xs font-semibold"
              :class="getInteractionTypeClass(drug.interactionType)"
            >
              {{ formatInteractionType(drug.interactionType) }}
            </span>
          </div>

          <!-- Clinical Trial Status -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Clinical Trial</p>
            <span
              class="inline-block px-2 py-1 rounded text-xs font-semibold"
              :class="drug.clinicalTrial ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'"
            >
              {{ drug.clinicalTrial ? 'Yes' : 'No' }}
            </span>
          </div>

          <!-- Drug ID -->
          <div class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Drug ID</p>
            <p class="text-sm font-mono text-gray-900">{{ drug.drugId || 'N/A' }}</p>
          </div>

          <!-- Indication -->
          <div v-if="drug.indication" class="bg-gray-50 rounded p-3">
            <p class="text-xs text-gray-600 font-semibold uppercase mb-1">Indication</p>
            <p class="text-sm text-gray-900">{{ drug.indication }}</p>
          </div>
        </div>

        <!-- Targeted Mutations -->
        <div v-if="drug.targetedMutations && drug.targetedMutations.length > 0" class="mt-4 pt-4 border-t border-gray-200">
          <h4 class="text-sm font-semibold text-gray-700 mb-3">Targeted Mutations</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div
              v-for="(mutation, index) in drug.targetedMutations"
              :key="index"
              class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-4 border border-blue-100"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="font-bold text-gray-800">{{ mutation.mutationName }}</span>
                <span
                  v-if="mutation.evidenceLevel"
                  class="px-2 py-1 rounded text-xs font-semibold"
                  :class="getEvidenceLevelClass(mutation.evidenceLevel)"
                >
                  {{ mutation.evidenceLevel }}
                </span>
              </div>
              <div class="space-y-1 text-sm">
                <div class="flex items-center gap-2">
                  <span class="text-gray-500">Type:</span>
                  <span class="text-gray-700">{{ mutation.mutationType }}</span>
                </div>
                <div v-if="mutation.aminoAcidChange" class="flex items-center gap-2">
                  <span class="text-gray-500">Change:</span>
                  <span class="font-mono text-gray-700">{{ mutation.aminoAcidChange }}</span>
                </div>
                <div v-if="mutation.codonPosition" class="flex items-center gap-2">
                  <span class="text-gray-500">Position:</span>
                  <span class="text-gray-700">Codon {{ mutation.codonPosition }}</span>
                </div>
                <div v-if="mutation.clinicalSignificance" class="flex items-center gap-2">
                  <span class="text-gray-500">Significance:</span>
                  <span class="text-gray-700">{{ mutation.clinicalSignificance }}</span>
                </div>
                <div v-if="mutation.sources && mutation.sources.length > 0" class="flex items-center gap-2">
                  <span class="text-gray-500">Sources:</span>
                  <div class="flex gap-1">
                    <span
                      v-for="(source, sourceIndex) in mutation.sources"
                      :key="sourceIndex"
                      class="px-1.5 py-0.5 bg-white rounded text-xs text-gray-600 border border-gray-200"
                    >
                      {{ source }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- External Links -->
        <div class="flex gap-2 pt-4 border-t border-gray-200 mt-4">
          <a
            v-if="drug.pubmedId"
            :href="`https://pubmed.ncbi.nlm.nih.gov/${drug.pubmedId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="text-xs bg-blue-50 hover:bg-blue-100 text-blue-700 px-3 py-2 rounded transition-colors border border-blue-200 font-medium"
          >
            PubMed
          </a>
          <a
            v-if="drug.drugId && drug.drugId.startsWith('DB')"
            :href="`https://go.drugbank.com/drugs/${drug.drugId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="text-xs bg-green-50 hover:bg-green-100 text-green-700 px-3 py-2 rounded transition-colors border border-green-200 font-medium"
          >
            DrugBank
          </a>
          <a
            href="https://www.dgidb.org"
            target="_blank"
            rel="noopener noreferrer"
            class="text-xs bg-purple-50 hover:bg-purple-100 text-purple-700 px-3 py-2 rounded transition-colors border border-purple-200 font-medium"
          >
            DGIdb
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DrugInteraction } from '@/types/gene';

interface Props {
  drugs: DrugInteraction[];
}

withDefaults(defineProps<Props>(), {
  drugs: () => []
});

function getApprovalStatusClass(status: string): string {
  switch (status) {
    case 'approved':
      return 'bg-green-100 text-green-700';
    case 'clinical_trial':
      return 'bg-blue-100 text-blue-700';
    case 'preclinical':
      return 'bg-yellow-100 text-yellow-700';
    case 'withdrawn':
      return 'bg-red-100 text-red-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function getInteractionTypeClass(type: string): string {
  switch (type) {
    case 'inhibitor':
      return 'bg-red-100 text-red-700';
    case 'activator':
      return 'bg-green-100 text-green-700';
    case 'antagonist':
      return 'bg-orange-100 text-orange-700';
    case 'agonist':
      return 'bg-blue-100 text-blue-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function getEvidenceLevelClass(level: string): string {
  switch (level) {
    case 'Strong':
      return 'bg-green-100 text-green-700';
    case 'Moderate':
      return 'bg-yellow-100 text-yellow-700';
    case 'Weak':
      return 'bg-gray-100 text-gray-700';
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function formatApprovalStatus(status: string): string {
  return status.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}

function formatInteractionType(type: string): string {
  return type.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
}
</script>
