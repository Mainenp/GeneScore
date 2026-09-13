<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Expression Profile</h2>

    <!-- Expression Heatmap -->
    <div class="overflow-x-auto mb-6">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b-2 border-gray-300">
            <th class="text-left py-3 px-4 font-semibold text-gray-700">Tissue/Organ</th>
            <th class="text-center py-3 px-4 font-semibold text-gray-700">Expression Level</th>
            <th class="text-center py-3 px-4 font-semibold text-gray-700">Cancer Score</th>
            <th class="text-center py-3 px-4 font-semibold text-gray-700">Normal vs Cancer</th>
            <th class="text-center py-3 px-4 font-semibold text-gray-700">Specificity</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="expr in expression"
            :key="expr.tissue"
            class="border-b border-gray-200 hover:bg-gray-50 transition-colors"
          >
            <td class="py-3 px-4 font-medium text-gray-900">{{ expr.tissue }}</td>
            <td class="py-3 px-4 text-center">
              <div class="flex items-center justify-center gap-2">
                <div class="w-24 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    class="h-full bg-gradient-to-r from-blue-400 to-blue-600 rounded-full"
                    :style="{ width: `${(expr.level / 10) * 100}%` }"
                  ></div>
                </div>
                <span class="font-mono text-xs font-semibold">{{ expr.level.toFixed(1) }}</span>
              </div>
            </td>
            <td class="py-3 px-4 text-center">
              <span
                class="inline-block px-3 py-1 rounded-full text-xs font-semibold"
                :class="getCancerScoreClass(expr.cancerScore)"
              >
                {{ expr.cancerScore.toFixed(2) }}
              </span>
            </td>
            <td class="py-3 px-4 text-center">
              <div class="flex items-center justify-center gap-1">
                <div class="w-8 h-6 bg-gray-100 rounded flex items-center justify-center text-xs font-semibold text-gray-600">
                  {{ expr.normalExpression.toFixed(1) }}
                </div>
                <span class="text-gray-400">→</span>
                <div class="w-8 h-6 bg-red-100 rounded flex items-center justify-center text-xs font-semibold text-red-600">
                  {{ expr.cancerExpression.toFixed(1) }}
                </div>
              </div>
            </td>
            <td class="py-3 px-4 text-center">
              <div class="flex items-center justify-center gap-2">
                <div class="w-16 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    class="h-full bg-gradient-to-r from-yellow-400 to-yellow-600 rounded-full"
                    :style="{ width: `${expr.specificity * 100}%` }"
                  ></div>
                </div>
                <span class="font-mono text-xs font-semibold">{{ (expr.specificity * 100).toFixed(0) }}%</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Legend -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <h3 class="font-semibold text-blue-900 mb-2">Legend</h3>
      <ul class="text-sm text-blue-800 space-y-1">
        <li><strong>Expression Level:</strong> Gene expression in tissue (0-10 scale, TPM normalized)</li>
        <li><strong>Cancer Score:</strong> Dependency score (-1 to 0, more negative = higher lethality)</li>
        <li><strong>Normal vs Cancer:</strong> Expression comparison between normal and cancer tissue</li>
        <li><strong>Specificity:</strong> Tissue-specific expression index (0-1)</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ExpressionData } from '@/types/gene';

interface Props {
  expression: ExpressionData[];
}

withDefaults(defineProps<Props>(), {
  expression: () => []
});

function getCancerScoreClass(score: number): string {
  if (score < -0.7) return 'bg-red-100 text-red-700';
  if (score < -0.4) return 'bg-yellow-100 text-yellow-700';
  return 'bg-green-100 text-green-700';
}
</script>
