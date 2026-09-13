<template>
  <div class="bg-white rounded-xl shadow-md p-6 border border-gray-200">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Protein 3D Structure</h2>
    
    <div v-if="!pdbId" class="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
      <p class="text-blue-700 font-semibold mb-2">No PDB Structure Available</p>
      <p class="text-blue-600 text-sm">This gene does not have a known PDB structure ID in the database.</p>
    </div>

    <div v-else>
      <!-- 使用 iframe 直接嵌入官方 3Dmol viewer - 绝对可靠！ -->
      <iframe 
        :src="iframeUrl" 
        width="100%" 
        height="450" 
        style="border: 3px solid #3b82f6; border-radius: 8px;"
        frameborder="0"
        allowfullscreen
      ></iframe>
      
      <div class="bg-gray-50 rounded-lg p-4 border border-gray-200 mt-4">
        <p class="text-sm text-gray-700"><span class="font-semibold">PDB ID:</span> {{ pdbId }}</p>
        <p class="text-xs text-gray-500 mt-1">
          <a :href="`https://www.rcsb.org/structure/${pdbId}`" target="_blank" class="text-blue-600 hover:text-blue-800 underline">
            View on RCSB →
          </a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  pdbId?: string;
  uniprotId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  pdbId: '',
  uniprotId: ''
});

// 使用 3Dmol 官方的查看器 URL
const iframeUrl = computed(() => {
  const pdb = props.pdbId?.toUpperCase() || '1M17';
  return `https://3dmol.org/viewer.html?pdb=${pdb}&style=cartoon:color=spectrum`;
});
</script>
